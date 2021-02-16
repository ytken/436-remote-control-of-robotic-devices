package ru.hse.control_system_v2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.hse.control_system_v2.dbdevices.AddDeviceDBActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbprotocol.AddProtocolDBActivity;
import ru.hse.control_system_v2.list_devices.DeviceItem;
import ru.hse.control_system_v2.list_devices.DeviceRepository;
import ru.hse.control_system_v2.list_devices.ListDevicesAdapter;

import static android.view.View.INVISIBLE;

public class MainActivity extends FragmentActivity implements View.OnClickListener
{
    DeviceDBHelper db;
    int bdUpdated = 0;
    public static MainActivity activity;
    RecyclerView recycler;
    ListDevicesAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        activity = this;
        DeviceDBHelper.getInstance(getApplicationContext());

        FrameLayout frame = findViewById(R.id.frame_recycler);
        findViewById(R.id.button_exit).setOnClickListener(this);
        findViewById(R.id.button_new_device).setOnClickListener(this);
        findViewById(R.id.button_new_protocol).setOnClickListener(this);
        findViewById(R.id.button_delete_bd).setOnClickListener(this);

        db = new DeviceDBHelper(this);

        recycler = findViewById(R.id.recycler_main);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListDevicesAdapter(DeviceRepository.getInstance(getApplicationContext()).list(), new MyListener());
        recycler.setAdapter(adapter);

        registerReceiver(mMessageReceiverNotSuccess, new IntentFilter("not_success"));
        registerReceiver(mMessageReceiverSuccess, new IntentFilter("success"));
    }

    public class MyListener implements ListDevicesAdapter.DeviceClickedListener{
        @Override
        public void deviceClicked(DeviceItem item) {
            DialogDevice dialog = new DialogDevice();
            Bundle args = new Bundle();
            args.putInt("id", item.getId());
            args.putString("name", item.getName());
            args.putString("MAC", item.getMAC());
            args.putString("protocol", item.getType());
            dialog.setArguments(args);
            //dialog.setTargetFragment(this, MY_REQUEST_CODE);
            dialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    private final BroadcastReceiver mMessageReceiverNotSuccess = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            showToast("Not success");
            //progressBar.setVisibility(INVISIBLE);
        }
    };

    private final BroadcastReceiver mMessageReceiverSuccess = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Устройство подключено, Service выполнился успешно
            showToast("success");
            //progressBar.setVisibility(INVISIBLE);
        }
    };

    public void showToast(String outputInfoString) {
        Toast outputInfoToast = Toast.makeText(this, outputInfoString, Toast.LENGTH_SHORT);
        outputInfoToast.show();
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
                startActivityForResult(intent.setClass(MainActivity.this, AddDeviceDBActivity.class), 10);
                break;
            case R.id.button_new_protocol:
                startActivity(new Intent().setClass(MainActivity.this, AddProtocolDBActivity.class));
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
        }
    }

    protected void onUpdateList() {
        if (bdUpdated == 1){
            adapter = new ListDevicesAdapter(DeviceRepository.getInstance(getApplicationContext()).list(), new MyListener());
            recycler.setAdapter(adapter);
        }

        bdUpdated = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        onUpdateList();
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
        db = new DeviceDBHelper(getApplicationContext());
        db.viewData();
        bdUpdated = 1;
        onUpdateList();
    }

}

