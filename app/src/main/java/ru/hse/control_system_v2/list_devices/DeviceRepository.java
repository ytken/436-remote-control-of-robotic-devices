package ru.hse.control_system_v2.list_devices;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.hse.control_system_v2.MyActivity;
import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;
import ru.hse.control_system_v2.dbprotocol.AddProtocolDBActivity;
import ru.hse.control_system_v2.dbprotocol.ProtocolDBHelper;

public class DeviceRepository implements Serializable {
    final Set<DeviceItem> mData;

    private static volatile DeviceRepository mInstance;

    public static DeviceRepository getInstance(Context context) {
        synchronized (DeviceRepository.class) {
            mInstance = new DeviceRepository(context);
        }
        return mInstance;
    }

    public DeviceRepository(Context context) {
        mData = initializeData(context);
    }

    Set<DeviceItem> list() {
        return mData;
    }

    public DeviceItem item(int id) {
        for (DeviceItem device : mData)
            if (device.id == id) {
                return device;
            }
        return new DeviceItem(0, "0", "0", "main_protocol");
    }

    int size() {
        return mData.size();
    }

    private Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    protected Set<DeviceItem> initializeData(Context context) {
        Set<DeviceItem> data = new HashSet<>();

        SQLiteDatabase deviceDB = DeviceDBHelper.getInstance(getActivity(context)).getReadableDatabase();
        Cursor cursor = deviceDB.query(DeviceDBHelper.TABLE_DEVICES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_NAME);
            int MacIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_MAC);
            int rateIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_CONNECTION);
            int protocolIndex = cursor.getColumnIndex(DeviceDBHelper.KEY_PROTO);
            do {
                DeviceItem item = new DeviceItem(cursor.getInt(idIndex), cursor.getString(nameIndex), cursor.getString(MacIndex), cursor.getString(protocolIndex));
                data.add(item);
            } while (cursor.moveToNext());
        }
        return data;
    }
}
