package ru.hse.control_system_v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbprotocol.AddProtocolDBActivity;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;
import ru.hse.control_system_v2.list_devices.DeviceItem;
import ru.hse.control_system_v2.list_devices.ListDevicesFragment;

public class MyActivity extends FragmentActivity implements View.OnClickListener
{
    DeviceDBHelper db;
    int bdUpdated = 0;
    public static MyActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity = this;

        FrameLayout frame = findViewById(R.id.frame_recycler);
        findViewById(R.id.button_exit).setOnClickListener(this);
        findViewById(R.id.button_new_device).setOnClickListener(this);
        findViewById(R.id.button_new_protocol).setOnClickListener(this);
        findViewById(R.id.button_delete_bd).setOnClickListener(this);
        findViewById(R.id.button_update_bd).setOnClickListener(this);

        db = new DeviceDBHelper(this);

        ListDevicesFragment newFragment = new ListDevicesFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_recycler, newFragment).addToBackStack(null).commit();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_exit:
                finish();
                break;
            case R.id.button_new_device:
                Intent intent = new Intent();
                intent.putExtra("mode",0);
                startActivityForResult(intent.setClass(MyActivity.this, AddDeviceDBActivity.class), 10);
                break;
            case R.id.button_new_protocol:
                startActivity(new Intent().setClass(MyActivity.this, AddProtocolDBActivity.class));
                break;
            case R.id.button_delete_bd:
                Log.d("button", "button delete");
                AlertDialog dialog =new AlertDialog.Builder(this)
                        .setTitle("Подтверждение")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Вы действительно хотите удалить все имеющиеся устройства?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DeviceDBHelper helper = new DeviceDBHelper(getApplicationContext());
                                db.onUpgrade(helper.getReadableDatabase(), db.DATABASE_VERSION, db.DATABASE_VERSION + 1);
                                db = helper;
                                bdUpdated = 1;
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create();
                dialog.show();
                break;
            case R.id.button_update_bd:
                if (bdUpdated == 1) {
                    ListDevicesFragment newFragment = new ListDevicesFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_recycler, newFragment).addToBackStack(null).commit();
                }
                bdUpdated = 0;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        int resultBundle = data.getIntExtra("result", 0);
        Log.d("Add device", "In onActivityResult " + resultBundle);
        bdUpdated = resultBundle;
    }

    public void setBdUpdated(int id) {
        if (id > 0) db.deleteDevice(id);
        DeviceDBHelper helper = new DeviceDBHelper(getApplicationContext());
        db = helper;
        db.viewData();
        bdUpdated = 1;
    }

}

