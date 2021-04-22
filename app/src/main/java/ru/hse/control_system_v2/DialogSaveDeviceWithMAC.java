package ru.hse.control_system_v2;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;


public class DialogSaveDeviceWithMAC extends DialogFragment {
    Context c;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            c = context;
        }
    }

    Spinner spinnerProtocol;
    ArrayList<String> data;
    ProtocolDBHelper protocolDBHelper;
    String name;
    DeviceDBHelper deviceDBHelper;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        androidx.appcompat.app.AlertDialog.Builder setMacAlertDialogSettings =
                new androidx.appcompat.app.AlertDialog.Builder(c);
        setMacAlertDialogSettings.setTitle(getResources().getString(R.string.dialog_mac));
        EditText editTextMac = new EditText(c);
        editTextMac.addTextChangedListener(new AddDeviceDBActivity.MaskWatcher("##:##:##:##:##:##"));
        editTextMac.setHint(R.string.hint_dialog_mac);
        setMacAlertDialogSettings.setView(editTextMac);
        setMacAlertDialogSettings.setPositiveButton(getResources().getString(R.string.add_bd_label), (dialogInterface, i) -> {
            String macAddr = editTextMac.getText().toString();
            if (BluetoothAdapter.checkBluetoothAddress(macAddr))
                alertDeviceSelected(macAddr);
            else
                Toast.makeText(c, "Введите корректный MAC", Toast.LENGTH_SHORT).show();
        });
        setMacAlertDialogSettings.setNegativeButton(getResources().getString(R.string.cancel_add_bd_label), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        androidx.appcompat.app.AlertDialog dialogMacAlert;
        dialogMacAlert = setMacAlertDialogSettings.show();
        dialogMacAlert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
        editTextMac.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //On user changes the text
                dialogMacAlert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //After user is done entering the text
            }
        });

        return dialogMacAlert;
    }

    void saveDevice(String MacAddress, String name){
        String protocol = data.get((int) spinnerProtocol.getSelectedItemId());
        if (BluetoothAdapter.checkBluetoothAddress(MacAddress)) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(DeviceDBHelper.KEY_MAC, MacAddress);
            contentValues.put(DeviceDBHelper.KEY_NAME, name);
            contentValues.put(DeviceDBHelper.KEY_PROTO, protocol);

            int res = deviceDBHelper.insert(contentValues);
            if (res == 1) {
                AddDeviceDBActivity.dataChanged = 1;
                Toast.makeText(c, "Accepted", Toast.LENGTH_LONG).show();
                Log.d("Add device", "Device accepted");
            }
            else {
                Toast.makeText(c, "MAC has already been registered", Toast.LENGTH_LONG).show();
                Log.d("Add device", "MAC is in database");
            }
            deviceDBHelper.viewData();
        }
        else {
            Toast.makeText(c, "Wrong MAC address", Toast.LENGTH_LONG).show();
            Log.d("Add device", "Device denied");
        }
    }

    void alertDeviceSelected(String MacAddress){
        deviceDBHelper = new DeviceDBHelper(c);
        protocolDBHelper = new ProtocolDBHelper(c);
        data = protocolDBHelper.getProtocolNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol = new Spinner(c);
        spinnerProtocol.setAdapter(adapter);

        androidx.appcompat.app.AlertDialog dialogSaveDevice;
        androidx.appcompat.app.AlertDialog.Builder setSettingsToDeviceAlertDialog = new androidx.appcompat.app.AlertDialog.Builder(c);
        setSettingsToDeviceAlertDialog.setTitle(getResources().getString(R.string.alert_device_saving));

        EditText editTextNameAlert = new EditText(c);

        editTextNameAlert.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextNameAlert.setHint(getResources().getString(R.string.label_name));

        LinearLayout layout = new LinearLayout(c);
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
            ((MainActivity) c).onRefresh();
            AddDeviceDBActivity.stateOfAlert = true;
        });
        setSettingsToDeviceAlertDialog.setNegativeButton(getResources()
                .getString(R.string.cancel_add_bd_label), (dialogInterface, i) -> {
            dialogInterface.cancel();
            ((MainActivity) c).onRefresh();
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
}
