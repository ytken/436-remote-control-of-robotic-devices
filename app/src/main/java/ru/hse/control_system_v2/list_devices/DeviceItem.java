package ru.hse.control_system_v2.list_devices;

import java.util.Objects;

public class DeviceItem {
    String name, MAC;
    String type;
    int id;
    public DeviceItem(int id, String name, String MAC, String type) {
        this.name = name;
        this.MAC = MAC;
        this.id = id;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceItem item = (DeviceItem) o;
        return name.equals(item.name) &&
                MAC.equals(item.MAC);
    }

    public String getMAC() {
        return MAC;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() { return type; }
}
