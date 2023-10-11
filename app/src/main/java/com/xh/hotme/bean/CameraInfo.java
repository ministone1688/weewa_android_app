package com.xh.hotme.bean;

public class CameraInfo {
    public DeviceUsageInfo deviceUsageInfo;
    public DeviceInfo deviceBaseInfo;
    public NetworkBean deviceNetworkInfo;
    public String bluetoothId;

    public CameraInfo(String uuid){
        this.bluetoothId = uuid;
    }
}
