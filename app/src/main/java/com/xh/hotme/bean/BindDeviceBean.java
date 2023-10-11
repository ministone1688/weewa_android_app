package com.xh.hotme.bean;


import androidx.annotation.Keep;

import com.xh.hotme.bluetooth.Device;

@Keep
public class BindDeviceBean {
    public String deviceId;
    public String deviceName;
    public String mac;
    public String bluetoothUuid;
    public int status;

    public Device blueDevice;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getBluetoothUuid() {
        return bluetoothUuid;
    }

    public void setBluetoothUuid(String bluetoothUuid) {
        this.bluetoothUuid = bluetoothUuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Device getBlueDevice() {
        return blueDevice;
    }

    public void setBlueDevice(Device blueDevice) {
        this.blueDevice = blueDevice;
    }


    public BindDeviceBean clone() {
        BindDeviceBean cloneDevice = new BindDeviceBean();
        cloneDevice.bluetoothUuid = this.bluetoothUuid;
        cloneDevice.mac = this.mac;
        cloneDevice.deviceId = this.deviceId;
        cloneDevice.deviceName = this.deviceName;
        cloneDevice.setStatus(this.status);

        if (this.blueDevice != null) {
            Device device = this.blueDevice.clone();
            cloneDevice.blueDevice = device;
        }

        return cloneDevice;
    }
}
