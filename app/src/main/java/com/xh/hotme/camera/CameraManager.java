package com.xh.hotme.camera;

import android.text.TextUtils;

import com.xh.hotme.bean.CameraInfo;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.NetworkBean;

public class CameraManager {

    private static CameraInfo _connectCameraInfo;

    public static void release() {
        if (_connectCameraInfo != null) {
            _connectCameraInfo = null;
        }
    }

    public static CameraInfo getCameraInfo(String bluetoothId) {
        if (TextUtils.isEmpty(bluetoothId)) {
            return _connectCameraInfo;
        }

        if (_connectCameraInfo != null && bluetoothId.equalsIgnoreCase(_connectCameraInfo.bluetoothId)) {
            return _connectCameraInfo;
        }

        return null;
    }

    public static void newCamera(String bluetoothId) {
        if (TextUtils.isEmpty(bluetoothId)) {
            return;
        }

        if (_connectCameraInfo != null && bluetoothId.equalsIgnoreCase(_connectCameraInfo.bluetoothId)) {
            return;
        }

        _connectCameraInfo = null;
        _connectCameraInfo = new CameraInfo(bluetoothId);

        return;
    }

    public static DeviceUsageInfo getUsageInfo(String bluetoothId) {

        if (_connectCameraInfo != null) {
            return _connectCameraInfo.deviceUsageInfo;
        }

        if (_connectCameraInfo != null && bluetoothId.equalsIgnoreCase(_connectCameraInfo.bluetoothId)) {
            return _connectCameraInfo.deviceUsageInfo;
        }

        return null;
    }

    public static void setUsageInfo(DeviceUsageInfo userInfo) {
        if (_connectCameraInfo != null) {
            _connectCameraInfo.deviceUsageInfo = userInfo;
        }
    }

    public static DeviceInfo getDeviceBaseInfo(String bluetoothId) {
        if (_connectCameraInfo != null) {
            return _connectCameraInfo.deviceBaseInfo;
        }
        if (_connectCameraInfo != null && bluetoothId.equalsIgnoreCase(_connectCameraInfo.bluetoothId)) {
            return _connectCameraInfo.deviceBaseInfo;
        }

        return null;
    }

    public static void setDeviceBaseInfo(DeviceInfo deviceInfo) {
        if (_connectCameraInfo != null) {
            _connectCameraInfo.deviceBaseInfo = deviceInfo;
        }
    }

    public static NetworkBean geNetworkInfo(String bluetoothId) {
        if (_connectCameraInfo != null) {
            return _connectCameraInfo.deviceNetworkInfo;
        }
        if (_connectCameraInfo != null && bluetoothId.equalsIgnoreCase(_connectCameraInfo.bluetoothId)) {
            return _connectCameraInfo.deviceNetworkInfo;
        }

        return null;
    }

    public static void seNetworkInfo(NetworkBean networkBean) {
        if (_connectCameraInfo != null) {
            _connectCameraInfo.deviceNetworkInfo = networkBean;
        }
    }

    public static void updateSoftAp(NetworkBean.ApInfo softApInfo) {
        if (_connectCameraInfo != null && _connectCameraInfo.deviceNetworkInfo != null) {
            _connectCameraInfo.deviceNetworkInfo.setAp(softApInfo);
        }
    }


    public static String getDeviceBluetoothId() {
        if (_connectCameraInfo != null) {
            return _connectCameraInfo.bluetoothId;
        }
        return "";
    }


}
