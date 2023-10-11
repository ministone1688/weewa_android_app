package com.xh.hotme.event;

import androidx.annotation.Keep;

@Keep
public class RenameEvent {
    public String name;
    public String address;

    public RenameEvent(String address,String n){
        this.name = n;
        this.address = address;
    }
}
