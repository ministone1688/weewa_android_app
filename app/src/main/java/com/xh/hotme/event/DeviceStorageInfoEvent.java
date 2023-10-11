package com.xh.hotme.event;

import androidx.annotation.Keep;

import com.xh.hotme.bean.StorageBean;

@Keep
public class DeviceStorageInfoEvent {
    public StorageBean storageBean;

    public DeviceStorageInfoEvent(StorageBean data){
        storageBean = data;
    }
}
