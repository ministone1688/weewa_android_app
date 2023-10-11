package com.xh.hotme.bean;

import java.io.Serializable;

public class DeviceUsageInfo implements Serializable {
    public int temperature;
    public int energy;

    public StorageBean storage;

    public String toString() {
        return "energy: " + this.energy + "\ntemperature=" + temperature + "\n" +
                "{total=" + storage.total + ", use=" + storage.used + ", free=" + storage.free + " }"
                ;
    }
}
