package com.xh.hotme.bluetooth;


import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.bean.CameraVideoDetailListBean;
import com.xh.hotme.bean.CameraVideoFilterListBean;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.camera.CameraManager;
import com.xh.hotme.camera.IPlayControlListener;
import com.xh.hotme.event.DeviceLowerPoweroffEvent;
import com.xh.hotme.event.DeviceWeewaStartEvent;
import com.xh.hotme.event.HighTempEvent;
import com.xh.hotme.event.PoweroffEvent;
import com.xh.hotme.event.RkIpcEvent;
import com.xh.hotme.video.VideoManager;
import com.xh.hotme.wifi.IWifiNotifyListener;
import com.xh.hotme.wifi.WifiInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class BluetoothHandle {
    private static final String TAG = BluetoothHandle.class.getSimpleName();


    private static List<IBleScanNotifyListener> _scanNotifyListeners;
    private static List<IBleConnectListener> _scanConnectListeners;

    private static List<IWifiNotifyListener> _wifiNotifyListeners;

    private static List<IPlayControlListener> _playNotifyListeners;
    private static List<IBleDeviceUsageInfoNotifyListener> _deviceUsageInfoNotifyListeners;
    private static List<IBleDeviceInfoNotifyListener> _deviceInfoNotifyListeners;
    private static List<IBleSoftApNotifyListener> _softApNotifyListeners;

    private static List<IBleNetworkInfoNotifyListener> _networkNotifyListeners;

    public static List<IBleDeviceNameNotifyListener> _deviceNameListeners = new ArrayList<>();
    public static List<IBleActiveSmsListener> _activeSmsListeners = new ArrayList<>();
    public static List<IBleActiveLoginListener> _activeLoginListeners = new ArrayList<>();

    private static List<IBleVideoListNotifyListener> _videoListNotifyListeners;
    private static List<IBleVideoDetailListNotifyListener> _videoDetailListNotifyListeners;

    public static List<IBleVideoUploadNotifyListener> _videoUploadListeners = new ArrayList<>();
    public static List<IBleSimEnableNotifyListener> _simEnableListeners = new ArrayList<>();
    public static List<IBleRkipcListener> _rkipcListeners = new ArrayList<>();


    public static void handle(JSONObject jsonRoot) {
        if (jsonRoot == null || jsonRoot.getString(BleConstants.BLE_CMD) == null)
            return;

        String cmd = jsonRoot.getString(BleConstants.BLE_CMD);
        switch (cmd) {
            case BleConstants.BLE_CMD_WIFILIST:
                postWifiList(jsonRoot);
                break;
            case BleConstants.BLE_CMD_WIFI_SETUP:
                postWifiSetup(jsonRoot);
                break;
            case BleConstants.BLE_CMD_RECORDER_START:
                postRecordStart(jsonRoot);
                break;
            case BleConstants.BLE_CMD_RECORDER_STOP:
                postRecordStop(jsonRoot);
                break;
            case BleConstants.BLE_CMD_RECORDER_STATUS:
                postRecordStatus(jsonRoot);
                break;
            case BleConstants.BLE_CMD_STORAGE:
                postStorageInfo(jsonRoot);
                break;
            case BleConstants.BLE_CMD_ENERGY:
                postEnergyInfo(jsonRoot);
                break;
            case BleConstants.BLE_CMD_SOFTAP_OPEN:
                postSoftApOpen(jsonRoot);
                break;
            case BleConstants.BLE_CMD_SOFTAP_CLOSE:
                postSoftApClose(jsonRoot);
                break;
            case BleConstants.BLE_CMD_NETWORK_STATUS:
                postNetworkStatus(jsonRoot);
                break;
            case BleConstants.BLE_CMD_POWER_ON:
            case BleConstants.BLE_CMD_WEEWA_START:
                postWeewaStart(jsonRoot);
                break;
            case BleConstants.BLE_CMD_POWER_OFF:
                postPowerOff(jsonRoot);
                break;
            case BleConstants.BLE_CMD_USAGE_INFO:
                postUsageInfo(jsonRoot);
                break;
            case BleConstants.BLE_CMD_DEVICE_INFO:
                postDeviceInfo(jsonRoot);
                break;
            case BleConstants.BLE_CMD_UPDATE_NAME:
                postDeviceUpdateName(jsonRoot);
                break;
            case BleConstants.BLE_CMD_DEVICE_ACTIVE_SMS:
                postActiveSms(jsonRoot);
                break;
            case BleConstants.BLE_CMD_DEVICE_ACTIVE_BIND:
                postActiveBind(jsonRoot);
                break;
            case BleConstants.BLE_CMD_DEVICE_UNBIND_SMS:
                postDeviceUnBindSms(jsonRoot);
                break;
            case BleConstants.BLE_CMD_DEVICE_UNBIND:
                postDeviceUnBind(jsonRoot);
                break;
            case BleConstants.BLE_CMD_SET_TOKEN:
                postSetToken(jsonRoot);
                break;
            case BleConstants.BLE_CMD_VIDEO_LIST:
                postVideoList(jsonRoot);
                break;
            case BleConstants.BLE_CMD_VIDEO_DETAIL_LIST:
                postVideoDetailList(jsonRoot);
                break;
            case BleConstants.BLE_CMD_VIDEO_FILTER:
                postVideoFilter(jsonRoot);
                break;
            case BleConstants.BLE_CMD_VIDEO_DIR_UPLOAD:
                postVideoUpload(jsonRoot);
                break;
            case BleConstants.BLE_CMD_SIM_OPEN:
                postSimOpen(jsonRoot);
                break;
            case BleConstants.BLE_CMD_SIM_CLOSE:
                postSimClose(jsonRoot);
                break;
            case BleConstants.BLE_CMD_HIGH_TEMP:
                postHighTemp(jsonRoot);
                break;
            case BleConstants.BLE_CMD_RKIPC_START:
                postRkipc(jsonRoot);
                break;
            case BleConstants.BLE_CMD_LOWER_POWEROFF:
                postLowerPoweroff(jsonRoot);
                break;
            default:
                Log.w(TAG, "unknow cmd: " + cmd);
                break;
        }
    }

    public static void postWifiList(@NonNull JSONObject jsonRoot) {
        final List<WifiInfo> wifis = new ArrayList<>();

        //Utils.saveFile("ble_wifilist.txt", data);
        JSONArray content = jsonRoot.getJSONArray(BleConstants.BLE_KEY_WIFILIST);
        for (int i = 0, size = content.size(); i < size; i++) {
            JSONObject wifiJson = (JSONObject) content.get(i);
            String ssid = wifiJson.getString("s");
            String bs = wifiJson.getString("bs");
            int frequency = wifiJson.getIntValue("f");
            int rs = wifiJson.getIntValue("rs");
            if (ssid == null || ssid.isEmpty())
                continue;
            WifiInfo wifi = new WifiInfo();
            wifi.setBssid(bs);
            wifi.setSsid(ssid);
            wifi.setFrequency("" + frequency);
            wifi.setSignalLevel("" + rs);
            wifi.setFlags("");
            wifis.add(wifi);
        }

        onWifiListNotify(wifis);
    }

    public static void postWifiSetup(@NonNull JSONObject jsonRoot) {
        String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
        if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
            onWifiSetupNotify(1, "");
        } else {
            onWifiSetupNotify(0, status);
        }
    }


    public static void postRecordStart(@NonNull JSONObject jsonRoot) {
        String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
        if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
            onRecordStartNotify();
        } else {
            onRecordExceptionNotify(status);
        }
    }


    public static void postRecordStop(@NonNull JSONObject jsonRoot) {
        String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
        if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
            onRecordStopNotify();
        } else {
            onRecordExceptionNotify(status);
        }
    }


    public static void postRecordStatus(@NonNull JSONObject jsonRoot) {
        String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
        if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
            int st = jsonRoot.getIntValue(BleConstants.BLE_KEY_ST);
            int vt = jsonRoot.getIntValue(BleConstants.BLE_KEY_VIDEO_TIME);
            onRecordStatusNotify(st, vt);
        } else {
            onRecordStatusNotify(0, 0);
        }
    }

    public static void postStorageInfo(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_FREE);
        if (suc) {
            long total = jsonRoot.getLong(BleConstants.BLE_KEY_TOTAL);
            long used = jsonRoot.getLong(BleConstants.BLE_KEY_USED);
            long free = jsonRoot.getLong(BleConstants.BLE_KEY_FREE);
            StorageBean storageBean = new StorageBean(total, used, free);
            onStorageInfoNotify(storageBean);
        } else {

        }
    }

    public static void postEnergyInfo(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_FREE);
        if (suc) {
            int free = jsonRoot.getIntValue(BleConstants.BLE_KEY_FREE);
            onEnergyNotify(free);
        } else {
            onEnergyNotify(-1);
        }
    }

    public static void postDeviceInfo(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_DEVICE_NAME);
        if (suc) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.device_name = jsonRoot.getString(BleConstants.BLE_KEY_DEVICE_NAME);
            deviceInfo.device_id = jsonRoot.getString(BleConstants.BLE_KEY_DEVICE_ID);
            deviceInfo.mac = jsonRoot.getString(BleConstants.BLE_KEY_MAC);
            deviceInfo.active_status = jsonRoot.getIntValue(BleConstants.BLE_KEY_DEVICE_ACTIVE_STATUS);
            deviceInfo.account = jsonRoot.getString(BleConstants.BLE_KEY_DEVICE_ACCOUNT);
            deviceInfo.version = jsonRoot.getString(BleConstants.BLE_KEY_DEVICE_VERTION);
            onDeviceInfoNotify(deviceInfo);

            CameraManager.setDeviceBaseInfo(deviceInfo);
        } else {
            onDeviceInfoFail();
        }

    }


    public static void postSoftApOpen(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                String ssid = jsonRoot.getString(BleConstants.BLE_KEY_SOFTAP_SSID);
                String password = jsonRoot.getString(BleConstants.BLE_KEY_SOFTAP_PASSWORD);
                onSoftApOpenNotify(ssid, password);
            } else {
                onSoftApOpenFailNotify(status);
            }
        } else {
            onSoftApOpenFailNotify("");
        }
    }

    public static void postSoftApClose(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            onSoftApCloseNotify();
        } else {
            onSoftApCloseFailNotify();
        }
    }

    public static void postNetworkStatus(@NonNull JSONObject jsonRoot) {

        NetworkBean networkBean = new NetworkBean();
        boolean wifi = jsonRoot.containsKey(BleConstants.BLE_KEY_WIFI);
        if (wifi) {
            JSONObject wifiJson = jsonRoot.getJSONObject(BleConstants.BLE_KEY_WIFI);
            NetworkBean.NetworkStatus networkStatus = new NetworkBean.NetworkStatus();
            networkStatus.ip = wifiJson.getString(BleConstants.BLE_KEY_IP);
            networkStatus.st = wifiJson.getIntValue(BleConstants.BLE_KEY_ST);
            networkBean.setWifi(networkStatus);
        }

        boolean eth = jsonRoot.containsKey(BleConstants.BLE_KEY_ETH);
        if (eth) {
            JSONObject ethJson = jsonRoot.getJSONObject(BleConstants.BLE_KEY_ETH);
            NetworkBean.NetworkStatus networkStatus = new NetworkBean.NetworkStatus();
            networkStatus.ip = ethJson.getString(BleConstants.BLE_KEY_IP);
            networkStatus.st = ethJson.getIntValue(BleConstants.BLE_KEY_ST);
            networkBean.setEth(networkStatus);
        }

        boolean sim = jsonRoot.containsKey(BleConstants.BLE_KEY_SIM);
        if (sim) {
            JSONObject simJson = jsonRoot.getJSONObject(BleConstants.BLE_KEY_SIM);
            NetworkBean.NetworkStatus networkStatus = new NetworkBean.NetworkStatus();
            networkStatus.ip = simJson.getString(BleConstants.BLE_KEY_IP);
            networkStatus.st = simJson.getIntValue(BleConstants.BLE_KEY_ST);
            networkBean.setSim(networkStatus);
        }

        boolean ap = jsonRoot.containsKey(BleConstants.BLE_KEY_SOFTAP);
        if (ap) {
            JSONObject simJson = jsonRoot.getJSONObject(BleConstants.BLE_KEY_SOFTAP);
            NetworkBean.ApInfo apInfo = new NetworkBean.ApInfo();
            apInfo.ssid = simJson.getString(BleConstants.BLE_KEY_SOFTAP_SSID);
            apInfo.password = simJson.getString(BleConstants.BLE_KEY_SOFTAP_PASSWORD);
            apInfo.st = simJson.getIntValue(BleConstants.BLE_KEY_ST);
            networkBean.setAp(apInfo);
        }

        int port = jsonRoot.getIntValue(BleConstants.BLE_KEY_HTTP);
        networkBean.setHttp(port);

        CameraManager.seNetworkInfo(networkBean);

        onNetworkStatus(networkBean);
    }


    public static void onScanning(boolean isBackground) {
        if (_scanNotifyListeners != null) {
            for (IBleScanNotifyListener listener : _scanNotifyListeners) {
                listener.onScanning(isBackground);
            }
        }
    }


    public static void onScanTimeout() {
        if (_scanNotifyListeners != null) {
            for (IBleScanNotifyListener listener : _scanNotifyListeners) {
                listener.onTimeout();
            }
        }
    }

    public static void onScanEnd() {

        if (_scanNotifyListeners != null) {
            for (IBleScanNotifyListener listener : _scanNotifyListeners) {
                listener.onScanEnd();
            }
        }
    }

    public static void onLeScan(Device blueDevice) {
        if (_scanNotifyListeners != null) {
            for (IBleScanNotifyListener listener : _scanNotifyListeners) {
                listener.onLeScan(blueDevice);
            }
        }
    }

    public static void addScanNotifyListener(IBleScanNotifyListener listener) {
        if (_scanNotifyListeners == null) {
            _scanNotifyListeners = new ArrayList<>();
        }
        _scanNotifyListeners.add(listener);
    }

    public static void removeScanNotifyListener(IBleScanNotifyListener listener) {
        if (_scanNotifyListeners == null) {
            _scanNotifyListeners = new ArrayList<>();
        }
        _scanNotifyListeners.remove(listener);
    }

    public static void addConnectNotifyListener(IBleConnectListener listener) {
        if (_scanConnectListeners == null) {
            _scanConnectListeners = new ArrayList<>();
        }
        _scanConnectListeners.add(listener);
    }

    public static void removeConnectNotifyListener(IBleConnectListener listener) {
        if (_scanConnectListeners == null) {
            _scanConnectListeners = new ArrayList<>();
        }
        _scanConnectListeners.remove(listener);
    }

    public static void onConnecting(Device device) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onConnecting(device);
            }
        }
    }

    public static void onConnectSuccess(int status) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onConnectSuccess(null, status);
            }
        }
    }

    public static void onConnectFailure(String address, String msg) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onConnectFailure(address, msg);
            }
        }
    }

    public static void onConnectTimeoutNotify(String address) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onConnectTimeOut(address);
            }
        }
    }

    public static void onDisConnecting(String address) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onDisConnecting(address);
            }
        }
    }

    public static void onDisConnectSuccess(String address, int status) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onDisConnectSuccess(address, status);
            }
        }
    }

    public static void onServiceDiscoverySucceed(int status) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                listener.onServiceDiscoverySucceed(status);
            }
        }
    }

    public static void onServiceDiscoveryFailed(String message) {
        if (_scanConnectListeners != null) {
            for (IBleConnectListener listener : _scanConnectListeners) {
                if (listener != null) {
                    listener.onServiceDiscoveryFailed(message);
                }
            }
        }
    }

    public static void addWifiNotifyListener(IWifiNotifyListener listener) {
        if (_wifiNotifyListeners == null) {
            _wifiNotifyListeners = new ArrayList<>();
        }
        _wifiNotifyListeners.add(listener);
    }

    public static void removeWifiNotifyListener(IWifiNotifyListener listener) {
        if (_wifiNotifyListeners == null) {
            _wifiNotifyListeners = new ArrayList<>();
        }
        _wifiNotifyListeners.remove(listener);
    }

    public static void onWifiListNotify(List<WifiInfo> data) {
        if (_wifiNotifyListeners != null && _wifiNotifyListeners.size() > 0) {
            for (IWifiNotifyListener listener : _wifiNotifyListeners) {
                if (listener != null) {
                    listener.onWifiList(data);
                }
            }
        }
    }

    public static void onWifiSetupNotify(int status, String message) {
        if (_wifiNotifyListeners != null && _wifiNotifyListeners.size() > 0) {
            for (IWifiNotifyListener listener : _wifiNotifyListeners) {
                if (listener != null) {
                    listener.onSetupStatus(status, message);
                }
            }
        }
    }

    public static void addSoftApNotifyListener(IBleSoftApNotifyListener listener) {
        if (_softApNotifyListeners == null) {
            _softApNotifyListeners = new ArrayList<>();
        }
        _softApNotifyListeners.add(listener);
    }

    public static void removeSoftApNotifyListener(IBleSoftApNotifyListener listener) {
        if (_softApNotifyListeners == null) {
            _softApNotifyListeners = new ArrayList<>();
        }
        _softApNotifyListeners.remove(listener);
    }

    public static void onSoftApOpenNotify(String ssid, String password) {
        if (_softApNotifyListeners != null) {
            for (IBleSoftApNotifyListener listener : _softApNotifyListeners) {
                listener.onSoftApStart(ssid, password);
            }
        }
    }

    public static void onSoftApOpenFailNotify(String msg) {
        if (_softApNotifyListeners != null) {
            for (IBleSoftApNotifyListener listener : _softApNotifyListeners) {
                listener.onSoftApStartFail(msg);
            }
        }
    }

    public static void onSoftApCloseNotify() {
        if (_softApNotifyListeners != null) {
            for (IBleSoftApNotifyListener listener : _softApNotifyListeners) {
                listener.onSoftApStop();
            }
        }
    }

    public static void onSoftApCloseFailNotify() {
        if (_softApNotifyListeners != null) {
            for (IBleSoftApNotifyListener listener : _softApNotifyListeners) {
                listener.onSoftApStopFail("");
            }
        }
    }

    public static void onSoftApStatusNotify(int status) {
        if (_softApNotifyListeners != null) {
            for (IBleSoftApNotifyListener listener : _softApNotifyListeners) {
                listener.onSoftApStatus(status);
            }
        }
    }

    public static void addNetworkNotifyListener(IBleNetworkInfoNotifyListener listener) {
        if (_networkNotifyListeners == null) {
            _networkNotifyListeners = new ArrayList<>();
        }
        _networkNotifyListeners.add(listener);
    }

    public static void removeNetworkNotifyListener(IBleNetworkInfoNotifyListener listener) {
        if (_networkNotifyListeners == null) {
            _networkNotifyListeners = new ArrayList<>();
        }
        _networkNotifyListeners.remove(listener);
    }

    public static void onNetworkStatus(NetworkBean networkBean) {
        if (_networkNotifyListeners != null) {
            for (IBleNetworkInfoNotifyListener listener : _networkNotifyListeners) {
                listener.onNetworkStatus(networkBean);
            }
        }
    }

    public static void postPowerOff(@NonNull JSONObject jsonRoot) {

        EventBus.getDefault().post(new PoweroffEvent());
    }

    public static void postUsageInfo(@NonNull JSONObject jsonRoot) {
        DeviceUsageInfo usageInfo = new DeviceUsageInfo();

        JSONObject storage = jsonRoot.getJSONObject("s");
        long total = storage.getLong(BleConstants.BLE_KEY_TOTAL);
        long used = storage.getLong(BleConstants.BLE_KEY_USED);
        long free = storage.getLong(BleConstants.BLE_KEY_FREE);
        StorageBean storageBean = new StorageBean(total, used, free);
        usageInfo.storage = storageBean;
        usageInfo.energy = jsonRoot.getIntValue("e");
        usageInfo.temperature = jsonRoot.getIntValue("t");
        onUserageNotify(usageInfo);

        CameraManager.setUsageInfo(usageInfo);
    }

    public static void postActiveSms(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_BIND) || status.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_LOGIN) || status.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_DEVICE_BOUND)) {
                onActiveSms(status);
            } else {
                onActiveSmsFail(status);
            }
        } else {
            onActiveSmsFail("unknow");
        }
    }

    public static void postActiveBind(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                String token = jsonRoot.getString(BleConstants.BLE_KEY_TOKEN);
                LoginResultBean loginResultBean = new LoginResultBean(token);
                onActive(loginResultBean);
            } else {
                onActiveFail(status);
            }
        } else {
            onActiveFail("unknow");
        }
    }

    public static void postDeviceUnBindSms(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onUnbindSms();
            } else {
                onUnbindSmsFail(status);
            }
        } else {
            onUnbindSmsFail("unknow");
        }
    }

    public static void postDeviceUnBind(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onUnbind();
            } else {
                onUnbindFail(status);
            }
        } else {
            onUnbindFail("unknow");
        }
    }

    public static void postDeviceUpdateName(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onUpdateDeviceName();
            } else {
                onUpdateDeviceNameFail(status);
            }
        } else {
            onUpdateDeviceNameFail("unknow");
        }
    }

    public static void postSetToken(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onSyncToken();
            } else {
                onSyncTokenFail(status);
            }
        } else {
            onSyncTokenFail("unknow");
        }
    }

    public static void postVideoList(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_VIDEOS);
        if (suc) {
            CameraVideoListBean bean = new Gson().fromJson(jsonRoot.toJSONString(), CameraVideoListBean.class);
            if (bean != null && bean.videos != null) {
                onVideoList(bean.videos);
            } else {
                onVideoList(null);
            }
        } else {
            onVideoListFail("unknow");
        }
    }

    public static void postVideoDetailList(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_VIDEOS);
        if (suc) {
            CameraVideoDetailListBean bean = new Gson().fromJson(jsonRoot.toJSONString(), CameraVideoDetailListBean.class);
            if (bean != null && bean.videos != null) {
                onVideoDetailList(bean.videos);
            } else {
                onVideoDetailList(null);
            }
        } else {
            onVideoDetailListFail("unknow");
        }
    }

    public static void postVideoFilter(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_GROUP);
        if (suc) {
            CameraVideoFilterListBean bean = new Gson().fromJson(jsonRoot.toJSONString(), CameraVideoFilterListBean.class);
            if (bean != null && bean.groups != null && bean.groups.size() > 0) {

                List<CameraVideoDateListBean> data = VideoManager.convertVideoInfo(bean.groups);

                int type = data.get(0).groups.get(0).type;
//                String cacheVideoFileName = AppFileUtil.CACHE_DEVICE_VIDEO + "_" + type;
//                AppFileUtil.saveVideoJson(HotmeApplication.getContext(), GsonUtils.GsonString(bean.groups), cacheVideoFileName);
                VideoManager.saveVideoFilterInfo(type, bean.groups);

                onVideoFilterList(type, data);
            } else {
                onVideoFilterListFail("no data");
            }
        } else {
            onVideoFilterListFail("unknow");
        }
    }

    public static void postVideoUpload(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onVideoUpload();
            } else {
                onVideoUploadFail(status);
            }
        } else {
            onVideoUploadFail("unknow");
        }
    }


    public static void onRecordStartNotify() {
        if (_playNotifyListeners != null && _playNotifyListeners.size() > 0) {
            for (IPlayControlListener listener : _playNotifyListeners) {
                listener.onRecordVideoStart();
            }
        }
    }

    public static void onRecordStopNotify() {
        if (_playNotifyListeners != null) {
            for (IPlayControlListener listener : _playNotifyListeners) {
                if (listener != null) {
                    listener.onRecordVideoStop();
                }
            }
        }
    }

    public static void onRecordStatusNotify(int status, int videoTime) {
        if (_playNotifyListeners != null) {
            for (IPlayControlListener listener : _playNotifyListeners) {
                listener.getRecordVideoStatus(status, videoTime);
            }
        }
    }

    public static void onRecordExceptionNotify(String message) {
        if (_playNotifyListeners != null) {
            for (IPlayControlListener listener : _playNotifyListeners) {
                listener.onRecordException(message);
            }
        }
    }


    public static void onStorageInfoNotify(StorageBean storageBean) {
        if (_deviceUsageInfoNotifyListeners != null) {
            for (IBleDeviceUsageInfoNotifyListener listener : _deviceUsageInfoNotifyListeners) {
                listener.onStorageInfo(storageBean);
            }
        }
    }

    public static void onEnergyNotify(int energy) {
        if (_deviceUsageInfoNotifyListeners != null) {
            for (IBleDeviceUsageInfoNotifyListener listener : _deviceUsageInfoNotifyListeners) {
                listener.onEnergy(energy);
            }
        }
    }

    public static void onTemperatureNotify(int temperature) {
        if (_deviceUsageInfoNotifyListeners != null) {
            for (IBleDeviceUsageInfoNotifyListener listener : _deviceUsageInfoNotifyListeners) {
                listener.onTemperature(temperature);
            }
        }
    }

    public static void onDeviceInfoNotify(DeviceInfo deviceInfo) {
        if (_deviceInfoNotifyListeners != null) {
            for (IBleDeviceInfoNotifyListener listener : _deviceInfoNotifyListeners) {
                listener.onDeviceInfo(deviceInfo);
            }
        }
    }

    public static void onDeviceInfoFail() {
        if (_deviceInfoNotifyListeners != null) {
            for (IBleDeviceInfoNotifyListener listener : _deviceInfoNotifyListeners) {
                listener.onDeviceInfoFail();
            }
        }
    }


    public static void onUserageNotify(DeviceUsageInfo info) {
        if (_deviceUsageInfoNotifyListeners != null) {
            for (IBleDeviceUsageInfoNotifyListener listener : _deviceUsageInfoNotifyListeners) {
                listener.onDeviceInfo(info);
            }
        }
    }

    public static void addPlayNotifyListener(IPlayControlListener listener) {
        if (_playNotifyListeners == null) {
            _playNotifyListeners = new ArrayList<>();
        }
        _playNotifyListeners.add(listener);
    }

    public static void removePlayNotifyListener(IPlayControlListener listener) {
        if (_playNotifyListeners == null) {
            _playNotifyListeners = new ArrayList<>();
        }
        _playNotifyListeners.remove(listener);
    }


    public static void addDeviceUsageInfoNotifyListener(IBleDeviceUsageInfoNotifyListener listener) {
        if (_deviceUsageInfoNotifyListeners == null) {
            _deviceUsageInfoNotifyListeners = new ArrayList<>();
        }
        _deviceUsageInfoNotifyListeners.add(listener);
    }

    public static void removeDeviceUsageInfoNotifyListener(IBleDeviceUsageInfoNotifyListener listener) {
        if (_deviceUsageInfoNotifyListeners == null) {
            _deviceUsageInfoNotifyListeners = new ArrayList<>();
        }
        _deviceUsageInfoNotifyListeners.remove(listener);
    }

    public static void addDeviceInfoNotifyListener(IBleDeviceInfoNotifyListener listener) {
        if (_deviceInfoNotifyListeners == null) {
            _deviceInfoNotifyListeners = new ArrayList<>();
        }
        _deviceInfoNotifyListeners.add(listener);
    }

    public static void removeDeviceInfoNotifyListener(IBleDeviceInfoNotifyListener listener) {
        if (_deviceInfoNotifyListeners == null) {
            _deviceInfoNotifyListeners = new ArrayList<>();
        }
        _deviceInfoNotifyListeners.remove(listener);
    }

    public static void addVideoUploadNotifyListener(IBleVideoUploadNotifyListener listener) {
        if (_videoUploadListeners == null) {
            _videoUploadListeners = new ArrayList<>();
        }
        _videoUploadListeners.add(listener);
    }

    public static void removeVideoUploadNotifyListener(IBleVideoUploadNotifyListener listener) {
        if (_videoUploadListeners == null) {
            _videoUploadListeners = new ArrayList<>();
        }
        _videoUploadListeners.remove(listener);
    }


    public static void onVideoUpload() {
        if (_videoUploadListeners != null) {
            for (IBleVideoUploadNotifyListener listener : _videoUploadListeners) {
                listener.onVideoUpload();
            }
        }
    }

    public static void onVideoUploadFail(String msg) {
        if (_videoUploadListeners != null) {
            for (IBleVideoUploadNotifyListener listener : _videoUploadListeners) {
                listener.onVideoUploadFail(msg);
            }
        }
    }

    public static List<IBleVideoFilterNotifyListener> _videoFilterListeners = new ArrayList<>();

    public static void addVideoFilterNotifyListener(IBleVideoFilterNotifyListener listener) {
        if (_videoFilterListeners == null) {
            _videoFilterListeners = new ArrayList<>();
        }
        _videoFilterListeners.add(listener);
    }

    public static void removeVideoFilterNotifyListener(IBleVideoFilterNotifyListener listener) {
        if (_videoFilterListeners == null) {
            _videoFilterListeners = new ArrayList<>();
        }
        _videoFilterListeners.remove(listener);
    }

    public static void onVideoFilterList(int type, List<CameraVideoDateListBean> data) {
        if (_videoFilterListeners != null) {
            for (IBleVideoFilterNotifyListener listener : _videoFilterListeners) {
                listener.onVideoFilterList(type, data);
            }
        }
    }

    public static void onVideoFilterListFail(String msg) {
        if (_videoFilterListeners != null) {
            for (IBleVideoFilterNotifyListener listener : _videoFilterListeners) {
                listener.onVideoFilterListFail(msg);
            }
        }
    }


    public static void onActiveSms(String code) {
        if (_activeSmsListeners != null) {
            for (IBleActiveSmsListener listener : _activeSmsListeners) {
                listener.onSendActiveSms(code);
            }
        }
    }


    public static void onActiveSmsFail(String message) {
        if (_activeSmsListeners != null) {
            for (IBleActiveSmsListener listener : _activeSmsListeners) {
                listener.onSendActiveSmsFail(message);
            }
        }
    }

    public static void onActive(LoginResultBean loginBean) {
        if (_activeLoginListeners != null) {
            for (IBleActiveLoginListener listener : _activeLoginListeners) {
                listener.onActive(loginBean);
            }
        }
    }

    public static void onActiveFail(String msg) {
        if (_activeLoginListeners != null) {
            for (IBleActiveLoginListener listener : _activeLoginListeners) {
                listener.onActiveFail(msg);
            }
        }
    }


    public static void onSimOpen() {
        if (_simEnableListeners != null) {
            for (IBleSimEnableNotifyListener listener : _simEnableListeners) {
                listener.onSimOpen();
            }
        }
    }

    public static void onSimOpenFail(String msg) {
        if (_simEnableListeners != null) {
            for (IBleSimEnableNotifyListener listener : _simEnableListeners) {
                listener.onSimOpenFail(msg);
            }
        }
    }


    public static void onSimClose() {
        if (_simEnableListeners != null) {
            for (IBleSimEnableNotifyListener listener : _simEnableListeners) {
                listener.onSimClose();
            }
        }
    }

    public static void onSimCloseFail(String msg) {
        if (_simEnableListeners != null) {
            for (IBleSimEnableNotifyListener listener : _simEnableListeners) {
                listener.onSimCloseFail(msg);
            }
        }
    }


    public static List<IBleUnbindListener> _unbindListeners = new ArrayList<>();

    public static void addDeviceUnbindNotifyListener(IBleUnbindListener listener) {
        if (_unbindListeners == null) {
            _unbindListeners = new ArrayList<>();
        }
        _unbindListeners.add(listener);
    }

    public static void removeDeviceUnbindNotifyListener(IBleUnbindListener listener) {
        if (_unbindListeners == null) {
            _unbindListeners = new ArrayList<>();
        }
        _unbindListeners.remove(listener);
    }

    public static void onUnbindSms() {
        if (_unbindListeners != null) {
            for (IBleUnbindListener listener : _unbindListeners) {
                listener.onSendUnbindSms();
            }
        }
    }


    public static void onUnbindSmsFail(String message) {
        if (_unbindListeners != null) {
            for (IBleUnbindListener listener : _unbindListeners) {
                listener.onSendUnbindSmsFail(message);
            }
        }
    }

    public static void onUnbind() {
        if (_unbindListeners != null) {
            for (IBleUnbindListener listener : _unbindListeners) {
                listener.onUnbind();
            }
        }
    }

    public static void onUnbindFail(String msg) {
        if (_unbindListeners != null) {
            for (IBleUnbindListener listener : _unbindListeners) {
                listener.onUnbindFail(msg);
            }
        }
    }


    public static void addDeviceActiveSmsNotifyListener(IBleActiveSmsListener listener) {
        if (_activeSmsListeners == null) {
            _activeSmsListeners = new ArrayList<>();
        }
        _activeSmsListeners.add(listener);
    }

    public static void removeDeviceActiveSmsNotifyListener(IBleActiveSmsListener listener) {
        if (_activeSmsListeners == null) {
            _activeSmsListeners = new ArrayList<>();
        }
        _activeSmsListeners.remove(listener);
    }


    public static void addDeviceActiveLoginNotifyListener(IBleActiveLoginListener listener) {
        if (_activeLoginListeners == null) {
            _activeLoginListeners = new ArrayList<>();
        }
        _activeLoginListeners.add(listener);
    }

    public static void removeDeviceActiveLoginNotifyListener(IBleActiveLoginListener listener) {
        if (_activeLoginListeners == null) {
            _activeLoginListeners = new ArrayList<>();
        }
        _activeLoginListeners.remove(listener);
    }


    public static List<IBleLoginTokenListener> _loginTokenListeners = new ArrayList<>();


    public static void addLoginTokenNotifyListener(IBleLoginTokenListener listener) {
        if (_loginTokenListeners == null) {
            _loginTokenListeners = new ArrayList<>();
        }
        _loginTokenListeners.add(listener);
    }

    public static void removeLoginTokenNotifyListener(IBleLoginTokenListener listener) {
        if (_loginTokenListeners == null) {
            _loginTokenListeners = new ArrayList<>();
        }
        _loginTokenListeners.remove(listener);
    }


    public static void onSyncToken() {
        if (_loginTokenListeners != null) {
            for (IBleLoginTokenListener listener : _loginTokenListeners) {
                listener.onSyncToken();
            }
        }
    }


    public static void onSyncTokenFail(String msg) {
        if (_loginTokenListeners != null) {
            for (IBleLoginTokenListener listener : _loginTokenListeners) {
                listener.onSyncTokenFail(msg);
            }
        }
    }


    public static void addDeviceNameNotifyListener(IBleDeviceNameNotifyListener listener) {
        if (_deviceNameListeners == null) {
            _deviceNameListeners = new ArrayList<>();
        }
        _deviceNameListeners.add(listener);
    }

    public static void removeDeviceNameNotifyListener(IBleDeviceNameNotifyListener listener) {
        if (_deviceNameListeners == null) {
            _deviceNameListeners = new ArrayList<>();
        }
        _deviceNameListeners.remove(listener);
    }

    public static void onUpdateDeviceName() {
        if (_deviceNameListeners != null) {
            for (IBleDeviceNameNotifyListener listener : _deviceNameListeners) {
                listener.onDeviceName();
            }
        }
    }

    public static void onUpdateDeviceNameFail(String msg) {
        if (_deviceNameListeners != null) {
            for (IBleDeviceNameNotifyListener listener : _deviceNameListeners) {
                listener.onDeviceNameFail(msg);
            }
        }
    }


    public static void addVideoListNotifyListener(IBleVideoListNotifyListener listener) {
        if (_videoListNotifyListeners == null) {
            _videoListNotifyListeners = new ArrayList<>();
        }
        _videoListNotifyListeners.add(listener);
    }

    public static void removeVideoListNotifyListener(IBleVideoListNotifyListener listener) {
        if (_videoListNotifyListeners == null) {
            _videoListNotifyListeners = new ArrayList<>();
        }
        _videoListNotifyListeners.remove(listener);
    }

    public static void addVideoDetailListNotifyListener(IBleVideoDetailListNotifyListener listener) {
        if (_videoDetailListNotifyListeners == null) {
            _videoDetailListNotifyListeners = new ArrayList<>();
        }
        _videoDetailListNotifyListeners.add(listener);
    }

    public static void removeVideoDetailListNotifyListener(IBleVideoDetailListNotifyListener listener) {
        if (_videoDetailListNotifyListeners == null) {
            _videoDetailListNotifyListeners = new ArrayList<>();
        }
        _videoDetailListNotifyListeners.remove(listener);
    }


    public static void onVideoList(List<CameraVideoListBean.VideosBean> list) {
        if (_videoListNotifyListeners != null) {
            for (IBleVideoListNotifyListener listener : _videoListNotifyListeners) {
                listener.onVideoList(list);
            }
        }
    }

    public static void onVideoListFail(String msg) {
        if (_videoListNotifyListeners != null) {
            for (IBleVideoListNotifyListener listener : _videoListNotifyListeners) {
                listener.onVideoListFail(msg);
            }
        }
    }

    public static void onVideoDetailList(List<CameraVideoDetailListBean.VideosBean> list) {
        if (_videoDetailListNotifyListeners != null) {
            for (IBleVideoDetailListNotifyListener listener : _videoDetailListNotifyListeners) {
                listener.onVideoDetailList(list);
            }
        }
    }

    public static void onVideoDetailListFail(String msg) {
        if (_videoDetailListNotifyListeners != null) {
            for (IBleVideoDetailListNotifyListener listener : _videoDetailListNotifyListeners) {
                listener.onVideoDetailListFail(msg);
            }
        }
    }


    public static void addSimEnableNotifyListener(IBleSimEnableNotifyListener listener) {
        if (_simEnableListeners == null) {
            _simEnableListeners = new ArrayList<>();
        }
        _simEnableListeners.add(listener);
    }

    public static void removeSimEnableNotifyListener(IBleSimEnableNotifyListener listener) {
        if (_simEnableListeners == null) {
            _simEnableListeners = new ArrayList<>();
        }
        _simEnableListeners.remove(listener);
    }


    public static void postSimOpen(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onSimOpen();
            } else {
                onSimOpenFail(status);
            }
        } else {
            onSimOpenFail("unknow");
        }
    }


    public static void postSimClose(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                onSimClose();
            } else {
                onSimCloseFail(status);
            }
        } else {
            onSimCloseFail("unknow");
        }
    }


    public static void postHighTemp(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_TOKEN);
        if (suc) {
            float temp = jsonRoot.getFloat(BleConstants.BLE_KEY_TOKEN);
            EventBus.getDefault().post(new HighTempEvent(temp));
        }
    }

    public static void addRkipcNotifyListener(IBleRkipcListener listener) {
        if (_rkipcListeners == null) {
            _rkipcListeners = new ArrayList<>();
        }
        _rkipcListeners.add(listener);
    }

    public static void removeRkipcNotifyListener(IBleRkipcListener listener) {
        if (_rkipcListeners == null) {
            _rkipcListeners = new ArrayList<>();
        }
        _rkipcListeners.remove(listener);
    }

    public static void postRkipc(@NonNull JSONObject jsonRoot) {
        boolean suc = jsonRoot.containsKey(BleConstants.BLE_KEY_RESULT);
        if (suc) {
            String status = jsonRoot.getString(BleConstants.BLE_KEY_RESULT);
            if (status.equalsIgnoreCase(BleConstants.BLE_RESULT_SUCCESS)) {
                EventBus.getDefault().post(new RkIpcEvent(1));
            } else {
                EventBus.getDefault().post(new RkIpcEvent(0, status));
            }
        } else {
            EventBus.getDefault().post(new RkIpcEvent(0, "unknow"));
        }
    }

    public static void postWeewaStart(@NonNull JSONObject jsonRoot) {
        EventBus.getDefault().post(new DeviceWeewaStartEvent());
    }

    public static void postLowerPoweroff(@NonNull JSONObject jsonRoot) {
        EventBus.getDefault().post(new DeviceLowerPoweroffEvent());
    }

}
