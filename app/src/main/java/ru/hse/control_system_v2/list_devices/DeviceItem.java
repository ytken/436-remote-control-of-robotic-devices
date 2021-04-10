package ru.hse.control_system_v2.list_devices;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import ru.hse.control_system_v2.BluetoothConnectionService;
import ru.hse.control_system_v2.MainActivity;

public class DeviceItem {
    public String type, name, deviceMAC;
    int id;
    public DeviceItem(int id, String name, String deviceMAC, String type) {
        this.name = name;
        this.deviceMAC = deviceMAC;
        this.id = id;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceItem item = (DeviceItem) o;
        return name.equals(item.name) &&
                deviceMAC.equals(item.deviceMAC);
    }

    public String getMAC() {
        return deviceMAC;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() { return type; }

    public void startBluetoothConnectionService(Context ma){
        Intent intent = new Intent(ma, BluetoothConnectionService.class);
        intent.putExtra("protocol", type);
        ma.startService(intent);
    }
}
