package ru.hse.control_system_v2.list_devices;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import ru.hse.control_system_v2.BluetoothConnectionService;
import ru.hse.control_system_v2.MainActivity;

public class DeviceItem {
    private String name, deviceMAC, devClass, devType, protocol;
    int id;
    public DeviceItem(int id, String name, String deviceMAC, String protocol, String devClass, String devType) {
        this.name = name;
        this.deviceMAC = deviceMAC;
        this.id = id;
        this.protocol = protocol;
        this.devClass = devClass;
        this.devType = devType;
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

    public String getDevClass() { return devClass; }

    public String getDevType() { return devType; }

    public String getProtocol() { return protocol; }

    public void startBluetoothConnectionService(Context ma){
        Intent intent = new Intent(ma, BluetoothConnectionService.class);
        intent.putExtra("protocol", protocol);
        ma.startService(intent);
    }
}
