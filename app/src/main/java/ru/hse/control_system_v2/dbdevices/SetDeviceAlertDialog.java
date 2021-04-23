package ru.hse.control_system_v2.dbdevices;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;

public class SetDeviceAlertDialog extends AlertDialog.Builder{

    ProtocolDBHelper protocolDBHelper;
    Spinner spinnerProtocol, spinnerClass, spinnerType;
    ArrayList<String> data;
    List<String> listClasses, listTypes;
    Context context;
    Resources res;
    String Mac, name;



    public SetDeviceAlertDialog(@NonNull Context context, String address) {
        super(context);
        this.context = context;
        res = context.getResources();
        protocolDBHelper = new ProtocolDBHelper(context);
        data = protocolDBHelper.getProtocolNames();
        listClasses = List.of("class_android", "class_computer", "class_arduino", "no_class");
        listTypes = List.of("type_sphere", "type_anthropomorphic", "type_cubbi", "type_computer", "no_type");
        this.Mac = address;
    }

    void saveDevice(String MacAddress, String name){
        DeviceDBHelper deviceDBHelper = new DeviceDBHelper(context);
        String protocol = data.get((int) spinnerProtocol.getSelectedItemId());
        String classDevice = listClasses.get((int) spinnerClass.getSelectedItemId());
        String typeDevice;
        if (classDevice.equals("class_arduino"))
            typeDevice = listTypes.get((int) spinnerType.getSelectedItemId());
        else
            typeDevice = "no_type";

        if (BluetoothAdapter.checkBluetoothAddress(MacAddress)) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(DeviceDBHelper.KEY_MAC, MacAddress);
            contentValues.put(DeviceDBHelper.KEY_NAME, name);
            contentValues.put(DeviceDBHelper.KEY_PROTO, protocol);
            contentValues.put(DeviceDBHelper.KEY_CLASS, classDevice);
            contentValues.put(DeviceDBHelper.KEY_TYPE, typeDevice);

            int res = deviceDBHelper.insert(contentValues);
            if (res == 1) {
                AddDeviceDBActivity.dataChanged = 1;
                Toast.makeText(context, "Accepted", Toast.LENGTH_LONG).show();
                Log.d("Add device", "Device accepted");
            }
            else {
                Toast.makeText(context, "MAC has already been registered", Toast.LENGTH_LONG).show();
                Log.d("Add device", "MAC is in database");
            }
            deviceDBHelper.viewData();
        }
        else {
            Toast.makeText(context, "Wrong MAC address", Toast.LENGTH_LONG).show();
            Log.d("Add device", "Device denied");
        }
    }


    @Override
    public AlertDialog show() {
        ArrayAdapter<String> adapterProto = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, data);
        adapterProto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol = new Spinner(context);
        spinnerProtocol.setAdapter(adapterProto);
        spinnerProtocol.setPadding(2,20,2,20);

        ArrayAdapter<String> adapterClass = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listClasses);
        adapterClass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass = new Spinner(context);
        spinnerClass.setAdapter(adapterClass);
        spinnerClass.setPadding(2,20,2,20);
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(listClasses.get(position)) {
                    case "class_arduino":
                        spinnerType.setEnabled(true);
                        break;
                    default:
                        spinnerType.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerType.setEnabled(false);
            }
        });

        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listTypes);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType = new Spinner(context);
        spinnerType.setAdapter(adapterType);
        spinnerType.setPadding(2,20,2,20);

        if(spinnerProtocol.getParent() != null) {
            ((ViewGroup)spinnerProtocol.getParent()).removeView(spinnerProtocol); // <- fix crash
        }

        AlertDialog.Builder setSettingsToDeviceAlertDialog = new AlertDialog.Builder(context);
        setSettingsToDeviceAlertDialog.setTitle(res.getString(R.string.alert_device_saving));

        EditText editTextNameAlert = new EditText(context);

        editTextNameAlert.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextNameAlert.setHint(res.getString(R.string.label_name));

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editTextNameAlert);
        layout.addView(spinnerProtocol);
        layout.addView(spinnerClass);
        layout.addView(spinnerType);
        setSettingsToDeviceAlertDialog.setView(layout);

        setSettingsToDeviceAlertDialog.setPositiveButton(res.getString(R.string.add_bd_label), (dialogInterface, i) -> {
            name = editTextNameAlert.getText().toString();
            saveDevice(Mac, name);
            AddDeviceDBActivity.stateOfAlert = true;
        });
        setSettingsToDeviceAlertDialog.setNegativeButton(res.getString(R.string.cancel_add_bd_label), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        return setSettingsToDeviceAlertDialog.show();
    }
}
