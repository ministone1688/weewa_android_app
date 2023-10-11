package com.xh.hotme.event;

import androidx.annotation.Keep;

@Keep
public class RemoveDeviceEvent {
    public String mac;

    public RemoveDeviceEvent(String nick) {
        this.mac = nick;
    }
}
