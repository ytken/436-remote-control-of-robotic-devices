package ru.hse.control_system_v2.dbprotocol;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Set;

import ru.hse.control_system_v2.list_devices.DeviceItem;

public class ProtocolDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "addedProtocols";
    public static final String TABLE_PROTOCOLS = "protocols";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_LEN = "length";
    public static final String KEY_CODE = "code";

    public ProtocolDBHelper(Context context) {super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_PROTOCOLS + "(" + KEY_ID + " integer primary key,"
                + KEY_NAME + " text," + KEY_LEN + " text,"
                + KEY_CODE + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_PROTOCOLS);

        onCreate(db);
    }

}

