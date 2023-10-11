package com.xh.hotme.event;

import androidx.annotation.Keep;

@Keep
public class DeviceEnergyInfoEvent {
    public int percent;

    public DeviceEnergyInfoEvent(int data){
        percent = data;
    }
}
