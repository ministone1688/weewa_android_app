package com.xh.hotme.bluetooth;

import com.xh.hotme.wifi.WifiInfo;

public interface WifiListAdapterCallback {

    void onCancel();

    void onWifiClick(WifiInfo device, int position);
}