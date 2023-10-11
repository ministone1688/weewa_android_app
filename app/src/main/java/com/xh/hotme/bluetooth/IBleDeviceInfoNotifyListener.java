package com.xh.hotme.bluetooth;

import com.xh.hotme.bean.DeviceInfo;

public interface IBleDeviceInfoNotifyListener {
    void onDeviceInfo(DeviceInfo data) ;
    void onDeviceInfoFail() ;

}
