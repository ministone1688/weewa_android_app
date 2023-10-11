package com.xh.hotme.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public interface IBleConnectListener {

    void onConnecting(Device device);   //正在连接
    void onConnectSuccess(Device device, int status) ;
    void onConnectFailure(String address, String error) ;

    void onDisConnecting(String address); //正在断开
    void onDisConnectSuccess(String address, int status); // 断开连接

    void onConnectTimeOut(String address) ;

    void onServiceDiscoverySucceed(int status);  //发现服务成功
    void onServiceDiscoveryFailed(String message);  //发现服务失败

}
