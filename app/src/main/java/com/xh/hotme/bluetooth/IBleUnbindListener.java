package com.xh.hotme.bluetooth;


public interface IBleUnbindListener {
    void onSendUnbindSms() ;
    void onSendUnbindSmsFail(String message) ;

    void onUnbind() ;
    void onUnbindFail(String message) ;
}
