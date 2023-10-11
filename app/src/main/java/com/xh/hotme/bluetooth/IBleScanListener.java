package com.xh.hotme.bluetooth;

import android.util.Log;

public interface IBleScanListener {
    void onScanStart(boolean isBackground);

    void onScan(Device device, int position) ;

    void onScanStop(final int size) ;

    void onBluetoothDeviceClick(Device device, int position) ;
}
