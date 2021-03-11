package ru.hse.control_system_v2.dbdevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ru.hse.control_system_v2.MainActivity;
import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;

public class AddDeviceDBActivity extends AppCompatActivity implements DevicesAdapter.SelectedDevice, SwipeRefreshLayout.OnRefreshListener{
    DeviceDBHelper deviceDBHelper;
    ProtocolDBHelper protocolDBHelper;
    public ExtendedFloatingActionButton fabToOpenSettings;
    Spinner spinnerProtocol;
    RecyclerView pairedList;
    public BluetoothAdapter btAdapter;
    String selectedDevice;
    DevicesAdapter devicesAdapter;
    String deviceHardwareAddress;

    public TextView pairedDevicesTitleTextView;
    LayoutInflater inflater;
    String rateString;
    String name;
    //инициализация swipe refresh
    SwipeRefreshLayout swipeToRefreshLayout;
    Bundle b;
    boolean stateOfAlert;

    int dataChanged = 0, mode, id;

    ArrayList<String> data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bd_device);
        inflater = AddDeviceDBActivity.this.getLayoutInflater();

        swipeToRefreshLayout = findViewById(R.id.swipeRefreshLayout_add_device);
        swipeToRefreshLayout.setOnRefreshListener(this);

        fabToOpenSettings = findViewById(R.id.floating_action_button_open_settings);
        fabToOpenSettings.setOnClickListener(this::openSettings);

        deviceDBHelper = new DeviceDBHelper(this);
        protocolDBHelper = new ProtocolDBHelper(this);
        data = protocolDBHelper.getProtocolNames();

        stateOfAlert = false;

        pairedDevicesTitleTextView = findViewById(R.id.paired_devices_title_add_activity);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedList = findViewById(R.id.paired_list);
        pairedList.setLayoutManager(new LinearLayoutManager(this));
        pairedList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol = new Spinner(AddDeviceDBActivity.this);
        spinnerProtocol.setAdapter(adapter);


        b = getIntent().getExtras();
        mode = b.getInt("mode");
        if (mode == 1) {
            alertDeviceSelected(b.getString("MAC"));
        }

        Toolbar toolbar = findViewById(R.id.toolbar_add_device);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> exitActivity());

    }

    private void openSettings(View view) {
        Intent intent_add_device = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent_add_device);
    }


    void alertDeviceSelected(String MacAddress){
        AlertDialog.Builder setSettingsToDeviceAlertDialog = new AlertDialog.Builder(AddDeviceDBActivity.this);
        setSettingsToDeviceAlertDialog.setTitle("Set connection settings");

        EditText editTextNameAlert = new EditText(AddDeviceDBActivity.this);

        editTextNameAlert.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextNameAlert.setHint("Device Name");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        if(spinnerProtocol.getParent() != null) {
            ((ViewGroup)spinnerProtocol.getParent()).removeView(spinnerProtocol); // <- fix crash
        }


        layout.addView(editTextNameAlert);

        layout.addView(spinnerProtocol);
        if(mode == 1){
            editTextNameAlert.setText(b.getString("name"));
            spinnerProtocol.setSelection(data.indexOf(b.getString("protocol")));
            id = b.getInt("id");
        }

        setSettingsToDeviceAlertDialog.setView(layout);
        setSettingsToDeviceAlertDialog.setPositiveButton("OK", (dialogInterface, i) -> {
            name = editTextNameAlert.getText().toString();
            saveDevice(MacAddress, name);
            stateOfAlert = true;
        });
        setSettingsToDeviceAlertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
            if(mode == 1){
                stateOfAlert = true;
                finish();
            }
        });
        setSettingsToDeviceAlertDialog.show();

    }

    void saveDevice(String MacAddress, String name){
        String protocol = protocolDBHelper.getFileName(data.get((int) spinnerProtocol.getSelectedItemId()));
        if (BluetoothAdapter.checkBluetoothAddress(MacAddress)) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(DeviceDBHelper.KEY_MAC, MacAddress);
            contentValues.put(DeviceDBHelper.KEY_NAME, name);
            contentValues.put(DeviceDBHelper.KEY_PROTO, protocol);

            if (mode == 0) {
                int res = deviceDBHelper.insert(contentValues);
                if (res == 1) {
                    dataChanged = 1;
                    Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();
                    Log.d("Add device", "Device accepted");
                }
                else {
                    Toast.makeText(getApplicationContext(), "MAC has already been registered", Toast.LENGTH_LONG).show();
                    Log.d("Add device", "MAC is in database");
                }
            }
            else {
                deviceDBHelper.update(contentValues, id);
                Toast.makeText(getApplicationContext(), "Device has been edited", Toast.LENGTH_LONG).show();
                MainActivity.activity.setBdUpdated(-1);
                finish();
            }
            deviceDBHelper.viewData();
        }
        else {
            Toast.makeText(this, "Wrong MAC address", Toast.LENGTH_LONG).show();
            Log.d("Add device", "Device denied");
        }
    }

    // Добавляем сопряжённые устройства в List View
    public void searchForDevice(){
        // Обновление List View - удаление старых данных
        pairedList.setAdapter(null);
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // Если список спаренных устройств не пуст
        if(pairedDevices.size()>0) {
            List<DeviceModel> devicesList = new ArrayList<>();
            List<String> a = new ArrayList<String>();
            // устанавливаем связь между данными
            // проходимся в цикле по этому списку
            for (BluetoothDevice device : pairedDevices) {
                // Обновление List View - добавляем в него сопряжённые устройства
                deviceHardwareAddress = device.getName() + "\n" + device.getAddress(); // Name + MAC address в виде String переменной
                a.add(deviceHardwareAddress);
            }
            String[] array = a.toArray(new String[0]);

            for (String s : array) {
                DeviceModel deviceModel = new DeviceModel(s);

                devicesList.add(deviceModel);
            }
            devicesAdapter = new DevicesAdapter(devicesList, this);

            pairedList.setAdapter(devicesAdapter);

            pairedDevicesTitleTextView.setText(R.string.paired_devices);
        } else {
            //no_devices_added
            pairedDevicesTitleTextView.setText(R.string.no_devices_added);
            pairedList.setAdapter(null);
        }
        swipeToRefreshLayout.setRefreshing(false);
    }

    //Получаем адрес устройства из List View
    public void checkDeviceAddress(DeviceModel deviceModel) {

        selectedDevice = deviceModel.getDeviceName();
        //Get information from List View in String
        showToast(selectedDevice);
        int i = selectedDevice.indexOf(':');
        i = i - 2;
        //В текущем пункте List View находим первый символ ":", всё после него, а также два символа до него - адрес выбранного устройства
        selectedDevice = selectedDevice.substring(i);
        // запускаем длительную операцию подключения в Service
        alertDeviceSelected(selectedDevice);

    }

    // Метод для вывода всплывающих данных на экран
    public void showToast(String outputInfoString) {
        Toast outputInfoToast = Toast.makeText(this, outputInfoString, Toast.LENGTH_SHORT);
        outputInfoToast.show();
    }


    @Override
    public void selectedDevice(DeviceModel deviceModel) {
        checkDeviceAddress(deviceModel);
    }

    //True, если Bluetooth включён
    public boolean btIsEnabledFlagVoid(){
        return btAdapter.isEnabled();
    }

    void exitActivity(){
        Intent intent = new Intent();
        intent.putExtra("result", dataChanged);
        Log.d("Add device", "Exit " + dataChanged);
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        exitActivity();
    }

    @Override
    protected void onStart(){
        super.onStart();
        searchForDevice();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(stateOfAlert){
            exitActivity();
        } else{
            onRefresh();
        }
    }

    @Override
    public void onRefresh() {
        swipeToRefreshLayout.setRefreshing(true);
        if (btIsEnabledFlagVoid()) {
            // Bluetooth включён. Предложим пользователю добавить устройства и начать передачу данных.
            searchForDevice();

        } else {
            finish();
            showToast("Please, enable bluetooth");
        }
    }
    //Обновляем внешний вид приложения, скрываем и добавляем нужные элементы интерфейса
}
