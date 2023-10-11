package com.xh.hotme.bluetooth;


public interface IBleActiveSmsListener {
    void onSendActiveSms(String code) ;
    void onSendActiveSmsFail(String message) ;
}
