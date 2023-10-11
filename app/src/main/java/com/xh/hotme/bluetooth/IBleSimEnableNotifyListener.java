package com.xh.hotme.bluetooth;

import com.xh.hotme.bean.CameraVideoListBean;

import java.util.List;

public interface IBleSimEnableNotifyListener {
    void onSimOpen() ;
    void onSimOpenFail(String msg) ;
    void onSimClose() ;
    void onSimCloseFail(String msg) ;
}
