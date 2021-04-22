package ru.hse.control_system_v2.dbprotocol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;

public class AddProtocolDBActivity extends Activity implements View.OnClickListener {
    ProtocolDBHelper dbHelper;
    EditText editTextName, editTextLen, editTextCode;
    Button buttonAdd, buttonRead, buttonCancel, buttonFile;
    TextView textListProtocols;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bd_protocol);

        dbHelper = new ProtocolDBHelper(this);

        editTextName = findViewById(R.id.editTextProtocolName);
        editTextLen = findViewById(R.id.editTextLength);
        editTextCode = findViewById(R.id.editTextCode);

        textListProtocols = findViewById(R.id.text_protocols);

        buttonAdd = findViewById(R.id.button_add_protocol);
        buttonAdd.setOnClickListener(this);

        buttonRead = findViewById(R.id.button_read_protocol);
        buttonRead.setOnClickListener(this);

        buttonCancel = findViewById(R.id.button_cancel_protocol);
        buttonCancel.setOnClickListener(this);

        buttonFile = findViewById(R.id.button_choose_file);
        buttonFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        switch (v.getId()){
            case R.id.button_add_protocol:
                String name = editTextName.getText().toString();
                Integer length = Integer.parseInt(editTextLen.getText().toString());
                String code = editTextCode.getText().toString();
                ProtocolRepo protocolRepo = new ProtocolRepo(getApplicationContext(), "");
                int result = protocolRepo.stringXMLparser(code);
                if (result > 0) {
                    Toast.makeText(getApplicationContext(), "Invalid XML code", Toast.LENGTH_LONG).show();
                    break;
                }

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

                break;

            case R.id.button_read_protocol:
                Cursor cursor = database.query(ProtocolDBHelper.TABLE_PROTOCOLS, null, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    textListProtocols.setText("");
                    int idIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_ID);
                    int nameIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_NAME);
                    int lenIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_LEN);
                    int codeIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_CODE);
                    textListProtocols.append("Доступные протоколы:");
                    do {
                        textListProtocols.append("\n" + "ID = " + cursor.getInt(idIndex) +
                                ", name = " + cursor.getString(nameIndex) +
                                ", length = " + cursor.getString(lenIndex) +
                                ", code = " + cursor.getString(codeIndex));
                    } while (cursor.moveToNext());
                } else
                    textListProtocols.append("Нет доступных протоколов");

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

            case R.id.button_choose_file:
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("text/*");
                startActivityForResult(fileIntent,20);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 20:
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String fileContent = readTextFile(uri);
                    editTextCode.setText(fileContent);
                }

                break;
        }
    }

    private String readTextFile(Uri uri)
    {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try
        {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                builder.append(line + "\n");
            }
            reader.close();
        }
        catch (IOException e) {e.printStackTrace();}
        return builder.toString();
    }

    private String saveToFile(String name, String code) throws IOException {
        String fileName = name + ".xml";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new
                File(getFilesDir() + File.separator + name + ".xml")));
        bufferedWriter.write(code);
        bufferedWriter.close();
        return fileName;
    }

    public ProtocolDBHelper getDbHelper(){
        return dbHelper;
    }

}
