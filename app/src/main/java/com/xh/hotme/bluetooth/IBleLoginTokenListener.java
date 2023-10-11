package com.xh.hotme.bluetooth;


public interface IBleLoginTokenListener {
    void onSyncToken() ;
    void onSyncTokenFail(String msg) ;
}
