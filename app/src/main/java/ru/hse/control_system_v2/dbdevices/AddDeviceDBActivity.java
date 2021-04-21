package ru.hse.control_system_v2.dbdevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ru.hse.control_system_v2.MainActivity;
import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;

public class AddDeviceDBActivity extends AppCompatActivity implements DevicesAdapter.SelectedDevice, SwipeRefreshLayout.OnRefreshListener{


    ExtendedFloatingActionButton fabToOpenSettings;
    RecyclerView pairedList;
    BluetoothAdapter btAdapter;
    String selectedDevice;
    DevicesAdapter devicesAdapter;
    String deviceHardwareAddress;
    ExtendedFloatingActionButton manualMacButton;

    TextView pairedDevicesTitleTextView;
    LayoutInflater inflater;
    String rateString;

    //инициализация swipe refresh
    SwipeRefreshLayout swipeToRefreshLayout;
    Bundle b;
    public static boolean stateOfAlert;
    public static int dataChanged = 0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bd_device);
        inflater = AddDeviceDBActivity.this.getLayoutInflater();

        swipeToRefreshLayout = findViewById(R.id.swipeRefreshLayout_add_device);
        swipeToRefreshLayout.setOnRefreshListener(this);

        fabToOpenSettings = findViewById(R.id.floating_action_button_open_settings);
        fabToOpenSettings.setOnClickListener(this::openSettings);




        stateOfAlert = false;

        pairedDevicesTitleTextView = findViewById(R.id.paired_devices_title_add_activity);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedList = findViewById(R.id.paired_list);
        pairedList.setLayoutManager(new LinearLayoutManager(this));
        pairedList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


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

    // Добавляем сопряжённые устройства в List View
    public void searchForDevice(){
        // Обновление List View - удаление старых данных
        pairedList.setAdapter(null);
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // Если список спаренных устройств не пуст
        if(pairedDevices.size()>0) {
            List<DeviceModel> devicesList = new ArrayList<>();
            List<String> a = new ArrayList<>();
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
        alertDeviceSelected(selectedDevice);

    }

    Spinner spinnerProtocol;
    ArrayList<String> data;
    String name;

    void alertDeviceSelected(String MacAddress){
        ProtocolDBHelper protocolDBHelper = new ProtocolDBHelper(this);
        data = protocolDBHelper.getProtocolNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol = new Spinner(this);
        spinnerProtocol.setAdapter(adapter);

        androidx.appcompat.app.AlertDialog dialogSaveDevice;
        androidx.appcompat.app.AlertDialog.Builder setSettingsToDeviceAlertDialog = new androidx.appcompat.app.AlertDialog.Builder(this);
        setSettingsToDeviceAlertDialog.setTitle(getResources().getString(R.string.alert_device_saving));

        EditText editTextNameAlert = new EditText(this);

        editTextNameAlert.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextNameAlert.setHint(getResources().getString(R.string.label_name));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        if(spinnerProtocol.getParent() != null) {
            ((ViewGroup)spinnerProtocol.getParent()).removeView(spinnerProtocol); // <- fix crash
        }


        layout.addView(editTextNameAlert);

        layout.addView(spinnerProtocol);

        setSettingsToDeviceAlertDialog.setView(layout);
        setSettingsToDeviceAlertDialog.setPositiveButton(getResources()
                .getString(R.string.add_bd_label), (dialogInterface, i) -> {
            name = editTextNameAlert.getText().toString();
            saveDevice(MacAddress, name);
            AddDeviceDBActivity.stateOfAlert = true;
        });
        setSettingsToDeviceAlertDialog.setNegativeButton(getResources()
                .getString(R.string.cancel_add_bd_label), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        dialogSaveDevice = setSettingsToDeviceAlertDialog.show();
        dialogSaveDevice.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        //Add textWatcher to notify the user
        editTextNameAlert.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //On user changes the text
                dialogSaveDevice.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //After user is done entering the text
            }
        });
    }

    void saveDevice(String MacAddress, String name){
        DeviceDBHelper deviceDBHelper = new DeviceDBHelper(this);
        String protocol = data.get((int) spinnerProtocol.getSelectedItemId());
        if (BluetoothAdapter.checkBluetoothAddress(MacAddress)) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(DeviceDBHelper.KEY_MAC, MacAddress);
            contentValues.put(DeviceDBHelper.KEY_NAME, name);
            contentValues.put(DeviceDBHelper.KEY_PROTO, protocol);

            int res = deviceDBHelper.insert(contentValues);
            if (res == 1) {
                AddDeviceDBActivity.dataChanged = 1;
                Toast.makeText(this, "Accepted", Toast.LENGTH_LONG).show();
                Log.d("Add device", "Device accepted");
            }
            else {
                Toast.makeText(this, "MAC has already been registered", Toast.LENGTH_LONG).show();
                Log.d("Add device", "MAC is in database");
            }
            deviceDBHelper.viewData();
        }
        else {
            Toast.makeText(this, "Wrong MAC address", Toast.LENGTH_LONG).show();
            Log.d("Add device", "Device denied");
        }
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
            swipeToRefreshLayout.setRefreshing(false);
            finish();
            showToast("Please, enable bluetooth");
        }
    }
    //Обновляем внешний вид приложения, скрываем и добавляем нужные элементы интерфейса
}