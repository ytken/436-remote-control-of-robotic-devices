package ru.hse.control_system_v2.dbdevices;

import java.io.Serializable;

public class DeviceModel implements Serializable {

    private String deviceName;


    public DeviceModel(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}