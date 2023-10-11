package com.xh.hotme.wifi;


import java.util.List;

public interface IWifiNotifyListener {
    void onWifiList(List<WifiInfo> device)  ;

    void onSetupStatus(int  status, String message)  ;
}
