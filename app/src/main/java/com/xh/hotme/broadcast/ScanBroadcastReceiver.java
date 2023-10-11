package com.xh.hotme.broadcast;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.xh.hotme.HotmeApplication;
import com.xh.hotme.bluetooth.BluetoothManager;

//监听扫描广播
public class ScanBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BluetoothScan", "intent=" + intent.toString());
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    Log.d("BluetoothScan", "扫描模式改变");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d("BluetoothScan", "扫描开始");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d("BluetoothScan", "扫描结束");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    Log.d("BluetoothScan", "发现设备");
                    //获取蓝牙设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ContextCompat.checkSelfPermission(HotmeApplication.getContext(), Manifest.permission.BLUETOOTH_SCAN)
                                != PackageManager.PERMISSION_GRANTED) {
                            System.out.println("BluetoothScan no bluetooth permission...");

                            return;
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(HotmeApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            System.out.println("BluetoothScan no bluetooth permission...");
                            return;
                        }
                    }

                    if (device != null && null != device.getName()) {
                        int rssi = -120;
                        Bundle extras = intent.getExtras();
                        String lv = "";
                        if (extras != null) {
                            //获取信号强度
                            rssi = extras.getShort(BluetoothDevice.EXTRA_RSSI);
                        }
//                            System.out.println("BluetoothScan "+"rssi=" + rssi + " name=" + device.getName() +
//                                    " address=" + device.getAddress() + " lv=" + lv);
                        if (BluetoothManager.mInstance != null) {
                            BluetoothManager.mInstance.onLeScan(device, rssi, null);
                        }
                    }
                    break;
            }
        }
    }
}