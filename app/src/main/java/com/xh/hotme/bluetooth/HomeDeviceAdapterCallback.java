package com.xh.hotme.bluetooth;

import com.xh.hotme.bean.BindDeviceBean;

public interface HomeDeviceAdapterCallback {
    void onLeScanStart();

    void onLeScan(Device device, int position);

    void onLeScanStop(int size);

    void onBluetoothDeviceClick(BindDeviceBean device, int position);

    void onRemoveDevice(BindDeviceBean device, int position);


    void openDevice(Device device);
}