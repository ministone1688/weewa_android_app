package com.xh.hotme.camera;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.event.DeviceEnergyInfoEvent;
import com.xh.hotme.event.DeviceStorageInfoEvent;
import com.xh.hotme.event.DeviceTemperatureInfoEvent;
import com.xh.hotme.event.DeviceUsageInfoEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 定时获取相机的运行数据
 */
public class CameraMonitor {

    private static CameraMonitor _instance;
    private Handler _handler;

    IBleDeviceUsageInfoNotifyListener _usageInfoListener;

    private final int DEVICE_USAGE_INFO_DELAY = 60000;  //定时1分钟

    DeviceUsageInfo _usageInfo;

    boolean isRunning = false;

    boolean isPolling = false;

    StorageBean _storage;

    float _temperature;
    int _energy;

    private final Runnable _deviceUsageInfoRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            if (isPolling) {
                BluetoothManager.mInstance.sendCmdUsageInfo();
                if (_handler != null) {
                    _handler.postDelayed(_deviceUsageInfoRunnable, DEVICE_USAGE_INFO_DELAY);
                }
            }
        }
    };


    public static CameraMonitor getInstance() {
        if (_instance == null) {
            synchronized (CameraMonitor.class) {
                if (_instance == null) {
                    _instance = new CameraMonitor();
                }
            }
        }
        return _instance;
    }

    private CameraMonitor() {
        _handler = new Handler();

        _usageInfoListener = new IBleDeviceUsageInfoNotifyListener() {
            @Override
            public void onStorageInfo(StorageBean storageBean) {
                _storage = storageBean;
                EventBus.getDefault().post(new DeviceStorageInfoEvent(storageBean));
            }

            @Override
            public void onEnergy(int energy) {
                _energy = energy;
                EventBus.getDefault().post(new DeviceEnergyInfoEvent(energy));
            }

            @Override
            public void onDeviceInfo(DeviceUsageInfo data) {
                _usageInfo = data;
                CameraManager.setUsageInfo(data);
                EventBus.getDefault().post(new DeviceUsageInfoEvent(data));
            }

            @Override
            public void onTemperature(float temperature) {
                _temperature = temperature;
                EventBus.getDefault().post(new DeviceTemperatureInfoEvent(temperature));
            }
        };
    }

    public void release() {
        isPolling = false;
        if (_usageInfoListener != null) {
            BluetoothHandle.removeDeviceUsageInfoNotifyListener(_usageInfoListener);
        }
        isRunning = false;
        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }

    public void start() {
        isPolling = true;
        BluetoothHandle.addDeviceUsageInfoNotifyListener(_usageInfoListener);

        BluetoothManager.mInstance.sendCmdUsageInfo();
        isRunning = true;
        if (_handler != null) {
            _handler.postDelayed(_deviceUsageInfoRunnable, DEVICE_USAGE_INFO_DELAY);
        }
    }

    public void stop() {
        isPolling = false;
        _usageInfo = null;
        _energy = 0;
        _temperature = 0;
        _storage = null;
        BluetoothHandle.removeDeviceUsageInfoNotifyListener(_usageInfoListener);
        isRunning = false;
        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }

    public DeviceUsageInfo getDeviceUsageInfo() {
        return _usageInfo;
    }

    public int getDeviceEnergy() {
        return _energy;
    }

    public float getStorageInfo() {
        return _temperature;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void requestEnergyInfo() {
        BluetoothManager.mInstance.sendCmdEnergyStatus();
    }

    public void requestStorageInfo() {
        BluetoothManager.mInstance.sendCmdStorageStatus();
    }
}
