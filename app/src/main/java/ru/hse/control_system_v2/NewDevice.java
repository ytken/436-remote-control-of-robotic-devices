package ru.hse.control_system_v2;

import android.content.Context;
import android.content.Intent;

public class NewDevice {
    Context c;
    String selectedDevice;
    String classDevice;
    String deviceName;
    public NewDevice(Context c, String selectedDevice, String classDevice, String deviceName){
        this.c = c;
        this.selectedDevice = selectedDevice;
        this.classDevice = classDevice;
        this.deviceName = deviceName;
    }
    public void startBluetoothConnectionService(){
        Intent intent = new Intent(c, BluetoothConnectionService.class);
        intent.putExtra("MAC", selectedDevice);
        intent.putExtra("protocol", classDevice);
        intent.putExtra("name", deviceName);
        c.startService(intent);
    }
}
