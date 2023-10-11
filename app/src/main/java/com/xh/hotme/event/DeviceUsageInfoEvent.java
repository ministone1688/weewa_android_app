package com.xh.hotme.event;

import androidx.annotation.Keep;

import com.xh.hotme.bean.DeviceUsageInfo;

@Keep
public class DeviceUsageInfoEvent {
    public DeviceUsageInfo deviceUsageInfo;

    public  DeviceUsageInfoEvent(DeviceUsageInfo data){
        deviceUsageInfo = data;
    }
}
