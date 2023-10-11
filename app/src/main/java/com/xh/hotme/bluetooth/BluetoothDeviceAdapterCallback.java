package com.xh.hotme.bluetooth;

public interface BluetoothDeviceAdapterCallback {
    void onLeScanStart();

    void onLeScan(Device device, int position);

    void onLeScanStop(int size);

    void onBluetoothDeviceClick(Device device, int position);

    void onRemoveDevice(Device device, int position);


    void openDevice(Device device);
}