package ru.hse.control_system_v2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;

public class DialogDevice extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int id = getArguments().getInt("id");
        String name = getArguments().getString("name");
        String MAC = getArguments().getString("MAC");
        String classDevice = getArguments().getString("protocol");
        int rate = getArguments().getInt("rate");
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        return builder.setTitle("Информация")
                .setMessage("Name: " + name + "\nMAC address: " + MAC)
                .setPositiveButton("Подключиться", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Bundle b = new Bundle();
                        b.putString("protocol", classDevice);
                        b.putString("MAC", MAC);
                        b.putString("name", name);
                        Intent intent = new Intent().setClass(getActivity(), Manual_mode.class);
                        intent.putExtras(b);
                        startActivityForResult(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), 3);
                    }
                })
                .setNegativeButton("Удалить", (dialog, whichButton) -> MainActivity.activity.setBdUpdated(id))
                .setNeutralButton("Редактировать", (dialog, which) -> {
                    Bundle b = new Bundle();
                    b.putString("name", name);
                    b.putString("protocol", classDevice);
                    b.putInt("rate", rate);
                    b.putString("MAC", MAC);
                    b.putInt("mode", 1);
                    b.putInt("id", id);
                    Intent intent = new Intent().setClass(getActivity(), AddDeviceDBActivity.class);
                    intent.putExtras(b);
                    startActivity(intent);

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
