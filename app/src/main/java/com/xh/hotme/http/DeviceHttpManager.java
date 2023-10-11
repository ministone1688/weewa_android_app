package com.xh.hotme.http;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.xh.hotme.bean.CameraVideoDetailListBean;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.IBleVideoDetailListNotifyListener;
import com.xh.hotme.bluetooth.IBleVideoListNotifyListener;
import com.xh.hotme.camera.RecordInfoBean;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.GsonUtils;
import com.xh.hotme.utils.OkHttpUtil;

import okhttp3.HttpUrl;


public class DeviceHttpManager {
    private static final String TAG = "DeviceHttpManager";

    private static String mHostIP = "10.201.126.1";
    private static String mHost;
    private static final int mHostPost = 38888;

    public static void setHttpHost(String ip) {
        mHostIP = ip;
        mHost = "http://:" + ip + ":" + mHostPost;
    }

    public static HttpUrl getHttpUrl(String endPoint) {

        HttpUrl build = new HttpUrl.Builder().scheme("http").host(mHostIP).port(mHostPost).addPathSegment(endPoint).build();

        return build;
    }

    public static boolean sendCmdWifiLists() {
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_WIFILIST);

        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_WIFILIST);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {

            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postWifiList(data);
            }
        });
        return true;
    }

    public static boolean sendCmdSetup(String ssid, String pwd) {
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_WIFI_SETUP);

        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_WIFI_SETUP);
            obj.put(BleConstants.BLE_KEY_SSID, ssid);
            obj.put(BleConstants.BLE_KEY_PASSWORD, pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postWifiSetup(data);
            }
        });
        return true;
    }

    public static boolean sendCmdNetStatus() {
        AppTrace.d(TAG, "sendCmdNetStatus ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_NETWORK_STATUS);

        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_NETWORK_STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AppTrace.d(TAG, "sendCmdNetStatus status:" + obj.toString());
        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                AppTrace.d(TAG, "sendCmdNetStatus success: " + GsonUtils.GsonString(data));
//                BluetoothHandle.postNetworkStatus(data);
            }
            @Override
            public void onFailure(String code, String message) {
                AppTrace.d(TAG, "sendCmdNetStatus fail: " + message);
            }
        });
        return true;
    }

    /**
     * 发送关机命令
     * @return
     */
    public static boolean sendCmdPowerOff() {
        Log.d(TAG, "sendCmdPowerOff ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_POWER_OFF);

        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_POWER_OFF);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postPowerOff(data);
            }
        });

        return true;
    }

    /**
     * 发送重启命令
     * @return
     */
    public static boolean sendCmdRestart() {
        AppTrace.d(TAG, "sendCmdRestart ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_RESTART);

        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_RESTART);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
//                BluetoothHandle.postPowerOff(data);
            }
        });
        return true;
    }

    /**
     * 打开热点
     * @return
     */
    public boolean sendCmdSoftapOpen() {
        Log.d(TAG, "sendCmdSoftapOpen ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_SOFTAP_OPEN);

        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_SOFTAP_OPEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postSoftApOpen(data);
            }
        });
        return true;
    }

    /**
     * 关闭热点
     * @return
     */
    public boolean sendCmdSoftapClose() {
        Log.d(TAG, "sendCmdSoftapClose ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_SOFTAP_CLOSE);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_SOFTAP_CLOSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postSoftApClose(data);
            }
        });
        return true;
    }


    /**
     * 热点状态
     * @return
     */
    public static boolean sendCmdSoftapStatus() {
        Log.d(TAG, "sendCmdSoftapStatus ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_SOFTAP_STATUS);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_SOFTAP_STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
//                BluetoothHandle.postSoftApOpen(data);
            }
        });
        return true;
    }


    /**
     * 电量
     * @return
     */
    public static boolean sendCmdEnergyStatus() {
        Log.d(TAG, "sendCmdEnergeStatus ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_ENERGY);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_ENERGY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postEnergyInfo(data);
            }
        });
        return true;
    }

    /**
     * 存储空间
     * @return
     */
    public static boolean sendCmdStorageStatus() {
        Log.d(TAG, "sendCmdStorageStatus ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_STORAGE);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_STORAGE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postStorageInfo(data);
            }
        });
        return true;
    }


    /**
     * 设备信息
     * @return
     */
    public static boolean sendCmdDeviceInfo() {
        Log.d(TAG, "sendCmdDeviceInfo ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_DEVICE_INFO);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_INFO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postDeviceInfo(data);
            }
        });
        return true;
    }



    public static boolean sendCmdRecordStart(RecordInfoBean recordInfoBean) {
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_RECORDER_START);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_RECORDER_START);
            obj.put(BleConstants.BLE_KEY_VIDEO_NAME, recordInfoBean.name);
            obj.put(BleConstants.BLE_KEY_MAX_RATE, recordInfoBean.maxRate);
            obj.put(BleConstants.BLE_KEY_AUTHOR, recordInfoBean.author);
            obj.put(BleConstants.BLE_KEY_VIDEO_WIDTH, recordInfoBean.width);
            obj.put(BleConstants.BLE_KEY_VIDEO_HEIGHT, recordInfoBean.height);
            obj.put(BleConstants.BLE_KEY_VIDEO_ADDRESS, recordInfoBean.place);
            obj.put(BleConstants.BLE_KEY_VIDEO_TIME, recordInfoBean.time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postRecordStart(data);
            }
        });
        return true;

    }


    public static boolean sendCmdRecordStop() {
        Log.d(TAG, "sendCmdRecordStop ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_RECORDER_STOP);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_RECORDER_STOP);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postRecordStart(data);
            }
        });
        return true;
    }

    public static boolean sendCmdRecordStatus() {
        Log.d(TAG, "sendCmdRecordStatus ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_RECORDER_STATUS);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_RECORDER_STATUS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postRecordStatus(data);
            }
        });


        return true;
    }

    public static boolean sendCmdUsageInfo() {
        Log.d(TAG, "sendCmdUsageInfo ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_USAGE_INFO);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_USAGE_INFO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postUsageInfo(data);
            }
        });

        return true;
    }
    /**
     * 设备信息
     * @return
     */
    public static boolean sendCmdUpdateName(String name) {
        Log.d(TAG, "sendCmdUpdateName ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_UPDATE_NAME);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_UPDATE_NAME);
            obj.put(BleConstants.BLE_KEY_UPDATE_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postDeviceUpdateName(data);
            }
        });

        return true;
    }

    public static boolean sendCmdDeviceActiveSms(String mobile, String bleAddress) {
        Log.d(TAG, "sendCmdDeviceActiveSms ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_DEVICE_ACTIVE_SMS);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_ACTIVE_SMS);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_BLE_MAC, bleAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postActiveSms(data);
            }
        });

        return true;
    }

    public static boolean sendCmdDeviceActiveBind(String mobile,String mac) {
        AppTrace.d(TAG, "sendCmdDeviceActiveBind ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_DEVICE_ACTIVE_BIND);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_ACTIVE_BIND);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_MAC, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postActiveBind(data);
            }
        });

        return true;
    }

    public static boolean sendCmdDeviceUnBindSms(String mobile, String mac) {
        AppTrace.d(TAG, "sendCmdDeviceUnBindSms ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_DEVICE_UNBIND_SMS);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_UNBIND_SMS);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_MAC, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postDeviceUnBindSms(data);
            }
        });
        return  true;
    }

    public static boolean sendCmdDeviceUnBind(String mobile,String mac) {
        AppTrace.d(TAG, "sendCmdDeviceUnBind ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_DEVICE_UNBIND);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_UNBIND);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_MAC, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                BluetoothHandle.postDeviceUnBind(data);
            }
        });
        return  true;
    }


    public static boolean sendCmdVideoList(IBleVideoListNotifyListener listener) {
        AppTrace.d(TAG, "sendCmdVideoList ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_VIDEO_LIST_URL);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_LIST);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                CameraVideoListBean bean = new Gson().fromJson(data.toJSONString(), CameraVideoListBean.class);
                if (bean != null && bean.videos != null) {
                    if(listener!=null){
                        listener.onVideoList(bean.videos );
                    }

                } else {
                    if(listener!=null){
                        listener.onVideoList(null);
                    }
                }
            }
            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                if(listener!=null){
                    listener.onVideoListFail(msg);
                }
            }

        });
        return  true;
    }


    public static boolean sendCmdVideoDetailList(String path, IBleVideoDetailListNotifyListener listener) {
        AppTrace.d(TAG, "sendCmdVideoDetailList ...");
        HttpUrl url = getHttpUrl(BleConstants.BLE_CMD_VIDEO_DETAIL_LIST);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_DETAIL_LIST);
            obj.put(BleConstants.BLE_KEY_PATH, path);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpUtil.postData(url, obj.toString(), null, new DeviceHttpCallbackDecode<JSONObject>() {
            @Override
            public void onDataSuccess(JSONObject data) {
                CameraVideoDetailListBean bean = new Gson().fromJson(data.toJSONString(), CameraVideoDetailListBean.class);
                if (bean != null && bean.videos != null) {
                    if(listener!=null){
                        listener.onVideoDetailList(bean.videos );
                    }

                } else {
                    if(listener!=null){
                        listener.onVideoDetailList(null);
                    }
                }
            }
            @Override
            public void onFailure(String code, String msg) {
                super.onFailure(code, msg);
                if(listener!=null){
                    listener.onVideoDetailListFail(msg);
                }
            }

        });
        return  true;
    }
}
