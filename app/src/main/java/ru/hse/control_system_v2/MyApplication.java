package ru.hse.control_system_v2;

import android.app.Application;

import ru.hse.control_system_v2.dbdevices.DeviceDBHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DeviceDBHelper.getInstance(getApplicationContext());
    }
}
