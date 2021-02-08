package ru.hse.control_system_v2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;

public class DialogDevice extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int id = getArguments().getInt("id");
        String name = getArguments().getString("name");
        String MAC = getArguments().getString("MAC");
        String classDevice = getArguments().getString("protocol");
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        return builder.setTitle("Информация")
                .setMessage("Name: " + name + "\nMAC address: " + MAC)
                .setPositiveButton("Подключиться", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Bundle b = new Bundle();
                        b.putInt("id", id);
                        b.putString("protocol", classDevice);
                        b.putString("name", name);
                        Intent intent = new Intent().setClass(getActivity(), Manual_mode.class);
                        intent.putExtras(b);
                        startActivityForResult(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 3);
                    }
                })
                .setNegativeButton("Отмена", null)
                .setNeutralButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MyActivity.activity.setBdUpdated(id);
                    }
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
}
