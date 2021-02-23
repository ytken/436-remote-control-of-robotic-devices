package ru.hse.control_system_v2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;




public class DialogDevice extends DialogFragment {
    DeviceDBHelper dbHelper;
    Spinner spinnerProtocol;
    String newName;
    String name;
    int id;
    String protocol;
    AlertDialog.Builder builder;
    ArrayList<String> data = new ArrayList<String>() {{
        add("main_protocol");
        add("wheel_platform");
    }};

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        id = getArguments().getInt("id");
        name = getArguments().getString("name");
        String MAC = getArguments().getString("MAC");
        String classDevice = getArguments().getString("protocol");
        builder=new AlertDialog.Builder(getActivity());
        dbHelper = new DeviceDBHelper(requireActivity());

        return builder.setTitle("Информация")
                .setMessage("Name: " + name + "\nMAC address: " + MAC)
                .setPositiveButton("Подключиться", (dialog, whichButton) -> {
                    Bundle b = new Bundle();
                    b.putString("protocol", classDevice);
                    b.putString("MAC", MAC);
                    b.putString("name", name);
                    Intent intent = new Intent().setClass(getActivity(), Manual_mode.class);
                    intent.putExtras(b);
                    NewDevice arduino = new NewDevice(requireActivity(),MAC, classDevice);
                    arduino.startBluetoothConnectionService();
                })
                .setNegativeButton("Удалить", (dialog, whichButton) -> MainActivity.activity.setBdUpdated(id))
                .setNeutralButton("Редактировать", (dialog, which) -> {
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
        String classDevice = getArguments().getString("protocol");
        for(int i = 0; i<data.size(); i++){
            if (data.get(i).equals(classDevice)){
                spinnerProtocol.setSelection(i);
                break;
            }
        }
        AlertDialog.Builder setSettingsToDeviceAlertDialog = new AlertDialog.Builder(requireActivity());
        setSettingsToDeviceAlertDialog.setTitle("Change Device settings");

        LinearLayout layout = new LinearLayout(requireActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        if(spinnerProtocol.getParent() != null) {
            ((ViewGroup)spinnerProtocol.getParent()).removeView(spinnerProtocol); // <- fix crash
        }
        layout.addView(editTextNameAlert);
        layout.addView(spinnerProtocol);
        setSettingsToDeviceAlertDialog.setView(layout);
        setSettingsToDeviceAlertDialog.setPositiveButton("OK", (dialogInterface, i) -> {
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

        });
        setSettingsToDeviceAlertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        setSettingsToDeviceAlertDialog.create();
        setSettingsToDeviceAlertDialog.show();
    }
}
