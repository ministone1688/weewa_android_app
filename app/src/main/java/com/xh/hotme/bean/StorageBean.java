package com.xh.hotme.bean;

import java.io.Serializable;

public class StorageBean implements Serializable {
    public long total;
    public long used;
    public long free;

    public StorageBean(long total, long used, long free){
        this.total = total;
        this.used = used;
        this.free = free;
    }

    public String getString(){
        return "total: "+ this.total+"\nused: " + this.used+ "\nfree: " + this.free;
    }
}
