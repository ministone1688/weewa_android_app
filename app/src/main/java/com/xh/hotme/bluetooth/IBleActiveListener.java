package com.xh.hotme.bluetooth;


public interface IBleActiveListener {
    void onSendActiveSms() ;
    void onSendActiveSmsFail(String message) ;
    void onActive() ;
    void onActiveFail(String message) ;
}
