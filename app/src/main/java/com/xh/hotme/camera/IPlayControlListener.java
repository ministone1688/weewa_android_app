package com.xh.hotme.camera;

public interface IPlayControlListener {
    void onRecordVideoStart();
    void getRecordVideoStatus(int status, int videoTime) ;
    void onRecordVideoStop();
    void onRecordException(String msg);
}
