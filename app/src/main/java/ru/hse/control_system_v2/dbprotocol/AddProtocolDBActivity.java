package ru.hse.control_system_v2.dbprotocol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;

public class AddProtocolDBActivity extends Activity implements View.OnClickListener {
    ProtocolDBHelper dbHelper;
    EditText editTextName, editTextLen, editTextCode;
    Button buttonAdd, buttonRead, buttonCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bd_protocol);

        dbHelper = new ProtocolDBHelper(this);

        editTextName = findViewById(R.id.editTextProtocolName);
        editTextLen = findViewById(R.id.editTextLength);
        editTextCode = findViewById(R.id.editTextCode);

        buttonAdd = findViewById(R.id.button_add_protocol);
        buttonAdd.setOnClickListener(this);

        buttonRead = findViewById(R.id.button_read_protocol);
        buttonRead.setOnClickListener(this);

        buttonCancel = findViewById(R.id.button_cancel_protocol);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        switch (v.getId()){
            case R.id.button_add_protocol:
                String name = editTextName.getText().toString();
                Integer length = Integer.parseInt(editTextLen.getText().toString());
                String code = editTextCode.getText().toString();

                ContentValues contentValues = new ContentValues();
                contentValues.put(ProtocolDBHelper.KEY_NAME, name);
                contentValues.put(ProtocolDBHelper.KEY_LEN, length);
                try {
                    contentValues.put(ProtocolDBHelper.KEY_CODE, saveToFile(name,code));
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error saving: try again", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                if (dbHelper.insert(contentValues) == 0)
                    Toast.makeText(getApplicationContext(), "Protocol has already been registered", Toast.LENGTH_LONG).show();
                else {
                    editTextName.setText("");
                    editTextLen.setText("");
                    editTextCode.setText("");
                    Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();
                }

                if (dbHelper.insert(contentValues) == 0)
                    Toast.makeText(getApplicationContext(), "Protocol name has already been registered", Toast.LENGTH_LONG).show();
                else {
                    editTextName.setText("");
                    editTextLen.setText("");
                    editTextCode.setText("");
                    Toast.makeText(getApplicationContext(), "Accepted", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.button_read_protocol:
                Cursor cursor = database.query(ProtocolDBHelper.TABLE_PROTOCOLS, null, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_ID);
                    int nameIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_NAME);
                    int lenIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_LEN);
                    int codeIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_CODE);
                    do {
                        Log.d("mLog", "ID = " + cursor.getInt(idIndex) +
                                ", name = " + cursor.getString(nameIndex) +
                                ", length = " + cursor.getString(lenIndex) +
                                ", code = " + cursor.getString(codeIndex));
                    } while (cursor.moveToNext());
                } else
                    Log.d("mLog","0 rows");

                cursor.close();
                break;

            case R.id.button_cancel_protocol:
                AlertDialog dialog =new AlertDialog.Builder(this)
                        .setTitle("Подтверждение")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Вы действительно хотите удалить все кастомные протоколы?")
                        .setPositiveButton("OK", (dialog1, whichButton) -> {
                            ProtocolDBHelper dbHelper = new ProtocolDBHelper(getApplicationContext());
                            dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1,1);
                        })
                        .setNegativeButton("Отмена", null)
                        .create();
                dialog.show();
                break;
        }
    }

    private String saveToFile(String name, String code) throws IOException {
        String fileName = name + ".txt";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new
                File(getFilesDir() + File.separator + name + ".txt")));
        bufferedWriter.write(code);
        bufferedWriter.close();
        return fileName;
    }

    public ProtocolDBHelper getDbHelper(){
        return dbHelper;
    }

}
