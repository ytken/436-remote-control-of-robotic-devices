package ru.hse.control_system_v2;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbdevices.SetDeviceAlertDialog;
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

    ProtocolDBHelper protocolDBHelper;
    String name;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        protocolDBHelper = ProtocolDBHelper.getInstance(context);
        androidx.appcompat.app.AlertDialog.Builder setMacAlertDialogSettings =
                new androidx.appcompat.app.AlertDialog.Builder(c);
        setMacAlertDialogSettings.setTitle(getResources().getString(R.string.dialog_mac));
        EditText editTextMac = new EditText(c);
        editTextMac.addTextChangedListener(new AddDeviceDBActivity.MaskWatcher("##:##:##:##:##:##"));
        editTextMac.setHint(R.string.hint_dialog_mac);
        setMacAlertDialogSettings.setView(editTextMac);
        AddDeviceDBActivity addDeviceDBActivity = new AddDeviceDBActivity();
        setMacAlertDialogSettings.setPositiveButton(getResources().getString(R.string.add_bd_label), (dialogInterface, i) -> {
            String macAddr = editTextMac.getText().toString();
            if (BluetoothAdapter.checkBluetoothAddress(macAddr)) {
                SetDeviceAlertDialog alertDialog = new SetDeviceAlertDialog(context, macAddr);
                alertDialog.show();
                //Add textWatcher to notify the user
            }
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
}
