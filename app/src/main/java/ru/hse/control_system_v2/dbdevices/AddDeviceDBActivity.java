package ru.hse.control_system_v2.dbdevices;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import ru.hse.control_system_v2.MainActivity;
import ru.hse.control_system_v2.R;

public class AddDeviceDBActivity extends Activity implements View.OnClickListener{
    DeviceDBHelper dbHelper;
    EditText editTextMAC, editTextName, editTextRate;
    Spinner spinnerProtocol;
    Button buttonAdd, buttonOK, buttonCancel;

    int dataChanged = 0, mode, id;

    ArrayList<String> data = new ArrayList<String>() {{
        add("main_protocol");
        add("wheel_platform");
    }};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bd_device);

        dbHelper = new DeviceDBHelper(this);

        editTextMAC = findViewById(R.id.editTextMAC);
        editTextName = findViewById(R.id.editTextName);
        editTextRate = findViewById(R.id.editTextRate);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProtocol = findViewById(R.id.spinnerProtocol);
        spinnerProtocol.setAdapter(adapter);

        Bundle b = getIntent().getExtras();
        mode = b.getInt("mode");
        if (mode == 1) {
            editTextName.setText(b.getString("name"));
            editTextMAC.setText(b.getString("MAC"));
            editTextRate.setText(b.getString("rate"));
            spinnerProtocol.setSelection(data.indexOf(b.getString("protocol")));
            id = b.getInt("id");
        }

        buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(this);

        buttonOK = findViewById(R.id.button_ok);
        buttonOK.setOnClickListener(this);

        buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_add:
                String MacAddress = editTextMAC.getText().toString();
                String name = editTextName.getText().toString();
                String rateString = editTextRate.getText().toString();
                int rate = 0;
                if (!rateString.isEmpty())
                    rate = Integer.parseInt(rateString);
                String protocol = data.get((int) spinnerProtocol.getSelectedItemId());

                if (BluetoothAdapter.checkBluetoothAddress(MacAddress)) {
                    ContentValues contentValues = new ContentValues();

                    contentValues.put(DeviceDBHelper.KEY_MAC, MacAddress);
                    contentValues.put(DeviceDBHelper.KEY_NAME, name);
                    contentValues.put(DeviceDBHelper.KEY_CONNECTION, rate);
                    contentValues.put(DeviceDBHelper.KEY_PROTO, protocol);

                    if (mode == 0) {
                        int res = dbHelper.insert(contentValues);
                        if (res == 1) {
                            dataChanged = 1;
                            editTextMAC.setText("");
                            editTextName.setText("");
                            editTextRate.setText("");
                            Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();
                            Log.d("Add device", "Device accepted");
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "MAC has already been registered", Toast.LENGTH_LONG).show();
                            Log.d("Add device", "MAC is in database");
                        }
                    }
                    else {
                        dbHelper.update(contentValues, id);
                        Toast.makeText(getApplicationContext(), "Device has been edited", Toast.LENGTH_LONG).show();
                        MainActivity.activity.setBdUpdated(-1);
                    }
                    dbHelper.viewData();
                }
                else {
                    Toast.makeText(this, "Wrong MAC address", Toast.LENGTH_LONG).show();
                    Log.d("Add device", "Device denied");
                }

                break;

            case R.id.button_ok:
                Intent intent = new Intent();
                intent.putExtra("result", dataChanged);
                Log.d("Add device", "Exit " + dataChanged);
                setResult(RESULT_OK, intent);
                finish();
                break;

            case R.id.button_cancel:
                editTextMAC.setText("");
                editTextName.setText("");
                editTextRate.setText("");
                break;
        }
    }


}


/*Cursor cursor = database.query(DeviceDBHelper.TABLE_DEVICES, null, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_ID);
                    int nameIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_NAME);
                    int MacIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_MAC);
                    int rateIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_CONNECTION);
                    int protocolIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_PROTO);
                    do {
                        Log.d("mLog", "ID = " + cursor.getInt(idIndex) +
                                ", name = " + cursor.getString(nameIndex) +
                                ", MAC = " + cursor.getString(MacIndex) +
                                ", rate = " + cursor.getString(rateIndex) +
                                ", protocol = " + cursor.getString(protocolIndex));
                    } while (cursor.moveToNext());
                } else
                    Log.d("mLog","0 rows");

                cursor.close();
                break;*/