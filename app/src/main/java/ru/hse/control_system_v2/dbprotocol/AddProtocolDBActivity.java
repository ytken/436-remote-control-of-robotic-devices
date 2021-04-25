package ru.hse.control_system_v2.dbprotocol;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

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

import ru.hse.control_system_v2.MainActivity;
import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.TextChangedListener;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class AddProtocolDBActivity extends AppCompatActivity implements View.OnClickListener {
    ProtocolDBHelper dbHelper;
    EditText editTextName, editTextLen, editTextCode;
    Button buttonAdd, buttonShowProtoMenu, buttonCancel, buttonFile;
    TextView textListProtocols;
    final int REQUEST_CODE_OPEN = 20, PERMISSION_REQUEST_CODE = 123;
    SQLiteDatabase database;
    boolean isEditTextNameChanged, isEditTextLenChanged, isEditTextCodeChanged;
    ScrollView menuProto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bd_protocol);

        dbHelper = new ProtocolDBHelper(this);

        buttonAdd = findViewById(R.id.button_add_protocol);
        buttonAdd.setOnClickListener(this);
        buttonAdd.setBackgroundColor(getResources().getColor(R.color.foregroundColor));
        buttonAdd.setEnabled(false);

        editTextName = findViewById(R.id.editTextProtocolName);
        editTextName.addTextChangedListener(new TextChangedListener<EditText>(editTextName) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                isEditTextNameChanged = s.toString().trim().length() != 0;
                canShowSaveButton();
            }
        });
        editTextLen = findViewById(R.id.editTextLength);
        editTextLen.addTextChangedListener(new TextChangedListener<EditText>(editTextLen) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                isEditTextLenChanged = s.toString().trim().length() != 0;
                canShowSaveButton();
            }
        });
        editTextCode = findViewById(R.id.editTextCode);
        editTextCode.addTextChangedListener(new TextChangedListener<EditText>(editTextCode) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                isEditTextCodeChanged = s.toString().trim().length() != 0;
                canShowSaveButton();
            }
        });

        editTextCode.setMovementMethod(new ScrollingMovementMethod());

        menuProto = findViewById(R.id.add_proto_scroll);
        menuProto.setVisibility(GONE);

        textListProtocols = findViewById(R.id.text_protocols);
        textListProtocols.setMovementMethod(new ScrollingMovementMethod());

        database = dbHelper.getWritableDatabase();

        buttonCancel = findViewById(R.id.button_cancel_protocol);
        buttonCancel.setOnClickListener(this);

        buttonShowProtoMenu = findViewById(R.id.button_show_add_proto);
        buttonShowProtoMenu.setOnClickListener(this);

        buttonFile = findViewById(R.id.button_choose_file);
        buttonFile.setOnClickListener(this);

    }

    @Override
    protected void onStart(){
        super.onStart();
        showProtocols();
    }

    void canShowSaveButton(){
        if (isEditTextNameChanged && isEditTextLenChanged && isEditTextCodeChanged){
            buttonAdd.setEnabled(true);
            buttonAdd.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            buttonAdd.setEnabled(false);
            buttonAdd.setBackgroundColor(getResources().getColor(R.color.foregroundColor));
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_add_protocol:
                String name = editTextName.getText().toString();
                String slength = editTextLen.getText().toString();
                int length;
                String code = editTextCode.getText().toString();
                ProtocolRepo protocolRepo = new ProtocolRepo(getApplicationContext(), "");
                int result = protocolRepo.stringXMLparser(code);
                if (result > 0) {
                    Toast.makeText(getApplicationContext(), "Invalid XML code", Toast.LENGTH_LONG).show();
                    break;
                }
                if (name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Invalid name", Toast.LENGTH_LONG).show();
                    break;
                }
                if (slength.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Invalid length", Toast.LENGTH_LONG).show();
                    break;
                }
                else
                    length = Integer.parseInt(slength);
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
                    showProtocols();
                }

                break;

            case R.id.button_cancel_protocol:
                AlertDialog dialog =new AlertDialog.Builder(this)
                        .setTitle("Подтверждение")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Вы действительно хотите удалить все кастомные протоколы?")
                        .setPositiveButton("OK", (dialog1, whichButton) -> {
                            ProtocolDBHelper dbHelper = new ProtocolDBHelper(getApplicationContext());
                            dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1,1);
                            showProtocols();
                        })
                        .setNegativeButton("Отмена", null)
                        .create();
                dialog.show();
                break;

            case R.id.button_choose_file:
                if (hasPermissions()){
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    String[] mimetypes = {"text/xml", "text/plain"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, REQUEST_CODE_OPEN);
                }
                else {
                    requestPermissionWithRationale();
                }

                break;

            case R.id.button_show_add_proto:
                if (menuProto.getVisibility() == VISIBLE) {
                    menuProto.setVisibility(GONE);
                    buttonShowProtoMenu
                            .setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.ic_baseline_keyboard_arrow_right_24, 0);
                } else if (menuProto.getVisibility() == GONE){
                    menuProto.setVisibility(VISIBLE);
                    buttonShowProtoMenu
                            .setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String fileContent = readTextFile(uri);
                editTextCode.setText(fileContent);
            }
        }
    }

    private boolean hasPermissions(){
        int res = 0;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        for (String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            final String message = "Storage permission is needed to show files count";
            Snackbar.make(AddProtocolDBActivity.this.findViewById(R.id.activity_explain_perms), message, Snackbar.LENGTH_LONG)
                    .setAction("GRANT", v -> requestPerms())
                    .show();
        } else {
            requestPerms();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode){
            case PERMISSION_REQUEST_CODE:

                for (int res : grantResults){
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed){

        }
        else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "Storage Permissions denied.", Toast.LENGTH_SHORT).show();

                } else {
                    showNoStoragePermissionSnackbar();
                }
            }
        }

    }



    public void showNoStoragePermissionSnackbar() {
        Snackbar.make(AddProtocolDBActivity.this.findViewById(R.id.activity_explain_perms), "Storage permission isn't granted" , Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openApplicationSettings();

                        Toast.makeText(getApplicationContext(),
                                "Open Permissions and grant the Storage permission",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .show();
    }

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, PERMISSION_REQUEST_CODE);
    }

    private void requestPerms(){
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions,PERMISSION_REQUEST_CODE);
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
                builder.append(line).append("\n");
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

    void showProtocols(){
        Cursor cursor = database.query(ProtocolDBHelper.TABLE_PROTOCOLS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            textListProtocols.setText("");
            textListProtocols.append("Список доступных протоколов");
            int idIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_NAME);
            int lenIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_LEN);
            int codeIndex = cursor.getColumnIndex(ProtocolDBHelper.KEY_CODE);
            do {
                textListProtocols.append("\n" + "ID = " + cursor.getInt(idIndex) +
                        ", name = " + cursor.getString(nameIndex) +
                        ", length = " + cursor.getString(lenIndex) +
                        ", code = " + cursor.getString(codeIndex));
            } while (cursor.moveToNext());
        } else
            textListProtocols.append("Нет доступных протоколов");

        cursor.close();
    }

}
