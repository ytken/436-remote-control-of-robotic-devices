package ru.hse.control_system_v2.dbdevices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DeviceDBHelper extends SQLiteOpenHelper {

    public static DeviceDBHelper instance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "addedDevices";
    public static final String TABLE_DEVICES = "device";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_MAC = "address";
    public static final String KEY_URL_PH = "photo";
    public static final String KEY_CLASS = "class";
    public static final String KEY_PROTO = "id_protocol";
    public static final String KEY_PANEL = "id_panel";
    public static final String KEY_CONNECTION = "connection_rate";

    public DeviceDBHelper(Context context) {super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_DEVICES + "(" + KEY_ID + " integer primary key AUTOINCREMENT,"
                + KEY_NAME + " text," + KEY_MAC + " text," + KEY_CLASS + " text,"
                + KEY_URL_PH + " text," + KEY_PROTO + " text,"
                + KEY_PANEL + " text," + KEY_CONNECTION + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_DEVICES);

        onCreate(db);
    }

    public int count() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * from " + TABLE_DEVICES;
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }

    public void viewData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * from " + TABLE_DEVICES;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Log.d("SQL", cursor.getInt(0) + " " + cursor.getString(1));
            cursor.moveToNext();
        }
    }

    public int insert(ContentValues contentValues) {
        int result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        String query ="select * from " + TABLE_DEVICES + " where " + KEY_MAC + " = '" + contentValues.get(KEY_MAC) + "';";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) {
            db.insert(TABLE_DEVICES, null, contentValues);
            result = 1;
        }
        return result;
    }

    public void update(ContentValues contentValues, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_DEVICES, contentValues, "_id=?", new String[]{String.valueOf(id)});
    }

    public void deleteDevice(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE FROM " + TABLE_DEVICES + " WHERE _id = " + id + ";";
        Log.d("SQL", query);
        db.execSQL(query);
    }

    public static synchronized DeviceDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceDBHelper(context);
        }
        return instance;
    }
}
