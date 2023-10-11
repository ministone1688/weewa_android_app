package com.xh.hotme.bluetooth;

public interface IBleScanNotifyListener {
    void onScanning(boolean isBackground) ;

    void onLeScan(Device device)  ;
    void onTimeout()  ;

    void onScanEnd()  ;
}
