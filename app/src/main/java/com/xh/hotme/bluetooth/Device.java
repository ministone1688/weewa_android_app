package com.xh.hotme.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.SystemClock;

import java.io.Serializable;

/**
 * Created by GJK on 2018/11/9.
 */

public class Device implements Serializable {
    public String address;
    public int rssi;
    public Long last_scanned;
    public String name;
    public int status;

    public Device(BluetoothDevice device, int rssi) {
        this.address = device.getAddress();
        this.rssi = rssi;
        this.last_scanned = SystemClock.currentThreadTimeMillis();
        this.name = device.getName();
    }

    public Device(BluetoothDevice device, int rssi, String name) {
        this.address = device.getAddress();
        this.rssi = rssi;
        this.last_scanned = SystemClock.currentThreadTimeMillis();
        this.name = name;
    }

    public Device(String address, int rssi, String name) {
        this.address = address;
        this.rssi = rssi;
        this.last_scanned = SystemClock.currentThreadTimeMillis();
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Long getLast_scanned() {
        return last_scanned;
    }

    public void setLast_scanned(Long last_scanned) {
        this.last_scanned = last_scanned;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public  Device clone(){
        Device cloneDevice = new Device(this.address, this.rssi, this.name);
        cloneDevice.setStatus(this.status);

        return cloneDevice;
    }
}
