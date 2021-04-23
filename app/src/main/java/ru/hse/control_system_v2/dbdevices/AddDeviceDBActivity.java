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
import android.widget.AdapterView;
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
    DeviceDBHelper deviceDBHelper;
    ProtocolDBHelper protocolDBHelper;
    ExtendedFloatingActionButton fabToOpenSettings;
    RecyclerView pairedList;
    BluetoothAdapter btAdapter;
    String selectedDevice;
    DevicesAdapter devicesAdapter;
    String deviceHardwareAddress;

    TextView pairedDevicesTitleTextView;
    LayoutInflater inflater;
    String name;
    //инициализация swipe refresh
    SwipeRefreshLayout swipeToRefreshLayout;
    Bundle b;
    public static boolean stateOfAlert;
    public static int dataChanged = 0;

    ArrayList<String> data;
    List<String> listClasses, listTypes;

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
        int i = selectedDevice.indexOf(':');
        i = i - 2;
        //В текущем пункте List View находим первый символ ":", всё после него, а также два символа до него - адрес выбранного устройства
        selectedDevice = selectedDevice.substring(i);
        SetDeviceAlertDialog alertDialog = new SetDeviceAlertDialog(this, selectedDevice);
        alertDialog.show();
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

    public static class MaskWatcher implements TextWatcher {
        private boolean isRunning = false;
        private boolean isDeleting = false;
        private final String mask;

        public MaskWatcher(String mask) {
            this.mask = mask;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            isDeleting = count > after;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (isRunning || isDeleting) {
                return;
            }
            isRunning = true;

            int editableLength = editable.length();
            if (editableLength < mask.length()) {
                if (mask.charAt(editableLength) != '#') {
                    editable.append(mask.charAt(editableLength));
                } else if (mask.charAt(editableLength-1) != '#') {
                    editable.insert(editableLength-1, mask, editableLength-1, editableLength);
                }
            }

            isRunning = false;
        }
    }

}