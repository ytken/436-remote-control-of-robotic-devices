package ru.hse.control_system_v2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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


public class DialogDevice extends DialogFragment {
    DeviceDBHelper dbHelper;
    Spinner spinnerProtocol;
    String newName;
    String name;
    String MAC;
    String classDevice;
    int id;
    String protocol;
    AlertDialog.Builder builder;
    ProtocolDBHelper protocolDBHelper;
    ArrayList<String> data;
    MainActivity ma;

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
        id = getArguments().getInt("id");
        name = getArguments().getString("name");
        MAC = getArguments().getString("MAC");
        classDevice = getArguments().getString("protocol");
        builder=new AlertDialog.Builder(getActivity());
        dbHelper = new DeviceDBHelper(requireActivity());
        protocolDBHelper = new ProtocolDBHelper(requireActivity());

        data = protocolDBHelper.getProtocolNames();

        return builder.setTitle(getResources().getString(R.string.alert_info))
                .setMessage(getResources().getString(R.string.alert_device_name) + name + "\n" + getResources().getString(R.string.alert_MAC) + MAC+ "\n"+getResources().getString(R.string.alert_protocol) + classDevice)
                .setPositiveButton(getResources().getString(R.string.loading_label), (dialog, whichButton) -> {
                    //запуск подключения происходит ниже
                    NewDevice arduino = new NewDevice(requireActivity(),MAC, classDevice, name);
                    arduino.startBluetoothConnectionService();
                })
                .setNegativeButton(getResources().getString(R.string.alert_delete), (dialog, whichButton) -> {
                    MainActivity.activity.setBdUpdated(id);}
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
        spinnerProtocol = new Spinner(requireActivity());
        EditText editTextNameAlert = new EditText(requireActivity());
        spinnerProtocol.setAdapter(spinnerAdapter);
        id = getArguments().getInt("id");
        String name = getArguments().getString("name");
        editTextNameAlert.setText(name);
        editTextNameAlert.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextNameAlert.setHint("Device Name");
        String MAC = getArguments().getString("MAC");
        final String[] classDevice = {getArguments().getString("protocol")};
        for(int i = 0; i<data.size(); i++){
            if (data.get(i).equals(classDevice[0])){
                spinnerProtocol.setSelection(i);
                break;
            }
        }
        AlertDialog.Builder setSettingsToDeviceAlertDialog = new AlertDialog.Builder(requireActivity());
        setSettingsToDeviceAlertDialog.setTitle(getResources().getString(R.string.alert_editing));
        //alert_editing

        LinearLayout layout = new LinearLayout(requireActivity());
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
            MainActivity.activity.setBdUpdated(-1);
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
