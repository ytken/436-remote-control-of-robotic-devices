package ru.hse.control_system_v2.dbprotocol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.hse.control_system_v2.R;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.list_devices.DeviceItem;

public class ProtocolDBHelper extends SQLiteOpenHelper {

    public static ProtocolDBHelper instance = null;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "addedProtocols";
    public static final String TABLE_PROTOCOLS = "protocols";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_LEN = "length";
    public static final String KEY_CODE = "code";
    SQLiteDatabase db;
    DeviceDBHelper deviceDBHelper;

    Context context;

    public ProtocolDBHelper(Context context) {super(context, DATABASE_NAME, null, DATABASE_VERSION); this.context = context; deviceDBHelper = DeviceDBHelper.getInstance(context); }

    @Override
    public void onCreate(SQLiteDatabase dataBase) {
        db = dataBase;
        db.execSQL("create table " + TABLE_PROTOCOLS + "(" + KEY_ID + " integer primary key,"
                + KEY_NAME + " text," + KEY_LEN + " text,"
                + KEY_CODE + " text" + ")");
        ContentValues arduino = new ContentValues();
        arduino.put(KEY_NAME, context.getResources().getString(R.string.TAG_default_protocol));
        arduino.put(KEY_LEN, "32");
        String arduino_default_code = context.getResources().getString(R.string.TAG_default_protocol);
        Log.d("XMLFile", arduino_default_code);
        arduino.put(KEY_CODE, arduino_default_code);
        insert(arduino);
    }

    @Override
    public void onUpgrade(SQLiteDatabase dataBase, int oldVersion, int newVersion) {
        db = dataBase;
        String query = "select * from " + TABLE_PROTOCOLS + ";";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        File dir = context.getFilesDir();
        for (int i = 0; i < cursor.getCount(); i++) {
            String fileName = cursor.getString(3);
            Log.d("SQL",  fileName + " deleting");
            File file = new File(dir, fileName);
            deviceDBHelper.deleteProto(fileName);
            boolean result = file.delete();
            Log.d("SQL", cursor.getString(3) + " deleting " + (result ? "yes" : "no"));
            cursor.moveToNext();
        }
        db.execSQL("drop table if exists " + TABLE_PROTOCOLS);
        onCreate(db);
    }

    public ArrayList<String> getProtocolNames() {
        if(db == null || !db.isOpen()) {
            db = getReadableDatabase();
        }
        //db = getReadableDatabase();
        String query = "Select " + KEY_NAME + " from " + TABLE_PROTOCOLS;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            names.add(cursor.getString(cursor.getColumnIndex(KEY_NAME)).replace(".txt","").replace(".xml", ""));
            Log.d("SQL", cursor.getString(0));
            cursor.moveToNext();
        }
        return names;
    }

    public String getFileName(String name) {
        if(db == null || !db.isOpen()) {
            db = getWritableDatabase();
        }
        String query = "select " + KEY_CODE + " from " + TABLE_PROTOCOLS + " where " + KEY_NAME + " = '" + name + "';";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        if (cursor.isNull(0))
            return "";
        return cursor.getString(0);
    }

    public int insert(ContentValues contentValues) {
        int result = 0;
        if(db == null || !db.isOpen()) {
            db = getWritableDatabase();
        }
        String query ="select * from " + TABLE_PROTOCOLS + " where " + KEY_NAME + " = '" + contentValues.get(KEY_NAME) + "';";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) {
            db.insert(TABLE_PROTOCOLS, null, contentValues);
            result = 1;
        }
        return result;
    }

    public static synchronized ProtocolDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ProtocolDBHelper(context);
        }
        return instance;
    }

}
