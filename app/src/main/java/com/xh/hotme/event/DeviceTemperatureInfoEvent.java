package com.xh.hotme.event;

import androidx.annotation.Keep;

import com.xh.hotme.bean.DeviceUsageInfo;

@Keep
public class DeviceTemperatureInfoEvent {
    public float temperature;

    public DeviceTemperatureInfoEvent(float data){
        temperature = data;
    }
}
