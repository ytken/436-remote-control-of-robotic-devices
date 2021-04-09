package ru.hse.control_system_v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;
import ru.hse.control_system_v2.list_devices.DeviceItem;


public class DialogDevice extends DialogFragment {
    DeviceDBHelper dbHelper;
    Spinner spinnerProtocol;
    String newName;
    String name;
    String MAC;
    int id;
    String protocol;
    AlertDialog.Builder builder;
    ProtocolDBHelper protocolDBHelper;
    ArrayList<String> data;
    MainActivity ma;
    DeviceDBHelper dbdevice;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            ma = (MainActivity) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dbdevice = DeviceDBHelper.getInstance(ma);
        id = MainActivity.devicesList.get(0).getId();
        name = MainActivity.devicesList.get(0).getName();
        MAC = MainActivity.devicesList.get(0).getMAC();
        protocol = MainActivity.devicesList.get(0).getType();
        builder = new AlertDialog.Builder(getActivity());
        dbHelper = new DeviceDBHelper(requireActivity());
        protocolDBHelper = new ProtocolDBHelper(requireActivity());

        data = protocolDBHelper.getProtocolNames();

        return builder.setTitle(getResources().getString(R.string.alert_info))
                .setMessage(getResources().getString(R.string.alert_device_name) + name + "\n" + getResources().getString(R.string.alert_MAC) + MAC+ "\n"+getResources().getString(R.string.alert_protocol) + protocol)
                .setPositiveButton(getResources().getString(R.string.loading_label), (dialog, whichButton) -> {
                    //запуск подключения происходит ниже
                    MainActivity.devicesList.get(0).startBluetoothConnectionService(ma);
                })
                .setNegativeButton(getResources().getString(R.string.alert_delete), (dialog, whichButton) -> {
                            dbdevice.deleteDevice(id);
                            ma.onRefresh();}
                    )
                .setNeutralButton(getResources().getString(R.string.alert_change), (dialog, which) -> {
                    changeDeviceAlert();
                })
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {return;}
        String message = data.getStringExtra("message");
        Bundle args = new Bundle();
        args.putString("message", message);
        setArguments(args);
    }

    void changeDeviceAlert(){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, data);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol = new Spinner(ma);
        EditText editTextNameAlert = new EditText(ma);
        spinnerProtocol.setAdapter(spinnerAdapter);
        editTextNameAlert.setText(name);
        editTextNameAlert.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextNameAlert.setHint(getResources().getString(R.string.alert_device_name_tint));
        for(int i = 0; i<data.size(); i++){
            if (data.get(i).equals(protocol)){
                spinnerProtocol.setSelection(i);
                break;
            }
        }
        AlertDialog.Builder setSettingsToDeviceAlertDialog = new AlertDialog.Builder(ma);
        setSettingsToDeviceAlertDialog.setTitle(getResources().getString(R.string.alert_editing));
        //alert_editing

        LinearLayout layout = new LinearLayout(ma);
        layout.setOrientation(LinearLayout.VERTICAL);
        if(spinnerProtocol.getParent() != null) {
            ((ViewGroup)spinnerProtocol.getParent()).removeView(spinnerProtocol); // <- fix crash
        }
        layout.addView(editTextNameAlert);
        layout.addView(spinnerProtocol);
        setSettingsToDeviceAlertDialog.setView(layout);
        setSettingsToDeviceAlertDialog.setPositiveButton(getResources().getString(R.string.alert_save), (dialogInterface, i) -> {
            newName = editTextNameAlert.getText().toString();
            protocol = data.get((int) spinnerProtocol.getSelectedItemId());

            ContentValues contentValues = new ContentValues();
            contentValues.put(DeviceDBHelper.KEY_MAC, MAC);
            contentValues.put(DeviceDBHelper.KEY_NAME, newName);
            contentValues.put(DeviceDBHelper.KEY_PROTO, protocol);
            dbHelper.update(contentValues, id);
            //Toast.makeText(requireActivity(), "Device has been edited", Toast.LENGTH_LONG).show();
            dbHelper.viewData();
            //Обновление MainActivity
            ma.onRefresh();

        });
        setSettingsToDeviceAlertDialog.setNegativeButton(getResources().getString(R.string.cancel_add_bd_label), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        setSettingsToDeviceAlertDialog.create();
        setSettingsToDeviceAlertDialog.show();
    }
}
