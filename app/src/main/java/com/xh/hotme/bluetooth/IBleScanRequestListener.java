package com.xh.hotme.bluetooth;

public interface IBleScanRequestListener {
    void onScanStart(boolean isBackground);

    void onScan(Device device, int position) ;

    void onScanStop(final int size) ;

    void onBluetoothDeviceClick(Device device, int position) ;

    void onRequestWifiList();

    void onSetupWifi(String ssid, String password);

    void onRemove(Device device, int position);
}
