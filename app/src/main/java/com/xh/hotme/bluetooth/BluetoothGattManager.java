package com.xh.hotme.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.xh.hotme.camera.RecordInfoBean;
import com.xh.hotme.utils.AppTrace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class BluetoothGattManager {
    private static final String TAG = BluetoothGattManager.class.getSimpleName();

    private final Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private final BluetoothGattCallback mBluetoothGattCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
//    private final BluetoothDeviceAdapter mBluetoothDeviceAdapter;
    private BluetoothDeviceAdapterCallback mBluetoothDeviceAdapterCallback;
    private final GattUtils.RequestQueue mRequestQueue = GattUtils.createRequestQueue();
    private boolean mIsLeScanning = false;

    ScanCallback mScanCallback;

    public BluetoothGattManager(Context context) {
        this.mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
//        mBluetoothDeviceAdapter = new BluetoothDeviceAdapter(mContext);

        mBluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                boolean error = (status != 0);
                boolean isConnected = (newState == BluetoothAdapter.STATE_CONNECTED);

                Log.d(TAG, "onConnectionStateChange error:" + error + "; isConnected:" + isConnected);
                if (!error && isConnected) {
                    error = gatt.discoverServices();
                }

                if (error) {
                    gatt.close();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d(TAG, "onServicesDiscovered status:" + status);
                if (status != 0) {
                    gatt.close();
                } else {

                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }
        };
    }

    public void setBluetoothDeviceAdapterCallback(BluetoothDeviceAdapterCallback bluetoothDeviceAdapterCallback) {
        mBluetoothDeviceAdapterCallback = bluetoothDeviceAdapterCallback;
//        mBluetoothDeviceAdapter.setCallback(bluetoothDeviceAdapterCallback);
    }

//    public BluetoothDeviceAdapter getBluetoothDeviceAdapter() {
//        return mBluetoothDeviceAdapter;
//    }

    public boolean startLeScan() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
            return false;

        // 如果用户的设备没有开启蓝牙，则弹出开启蓝牙设备的对话框，让用户开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "请求用户打开蓝牙");
        }

        if (Build.VERSION.SDK_INT < 21) {

            if (mLeScanCallback == null) {
                mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        if (device != null && device.getName() != null) {
                            System.out.println("find bluetooth: " + device.getName());
                        }
//                        mBluetoothDeviceAdapter.onLeScan(device, rssi, scanRecord);
                    }
                };
            }

//            mBluetoothDeviceAdapter.getDevices().clear();
            boolean res = mBluetoothAdapter.startLeScan(/*new UUID[]{Constants.WIFI_SERVICE_UUID}, */mLeScanCallback);
            if (res) {
                mIsLeScanning = true;
//                if (mBluetoothDeviceAdapterCallback != null)
//                    mBluetoothDeviceAdapterCallback.onLeScanStart();
            }
            return res;
        } else {
            if (mScanCallback == null) {
                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        System.out.println(TAG + "\t" + "onScanResult, result = " + result);
//                        Log.d(TAG, "onScanResult: " + result.getDevice().getAddress() + ", " + result.getDevice().getName());
//                        mBluetoothDeviceAdapter.onLeScan(result.getDevice(), result.getRssi(), null);
                    }

                    @Override
                    public void onBatchScanResults(List results) {
                        super.onBatchScanResults(results);
                        Log.d(TAG, "onBatchScanResults");
                    }

                    //当扫描不能开启时回调
                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        //扫描太频繁会返回ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED，表示app无法注册，无法开始扫描。
                        Log.d(TAG, "onScanFailed. errorCode: " + errorCode);
                    }
                };
            }
//            mBluetoothDeviceAdapter.getDevices().clear();
            //开始扫描
            final BluetoothLeScanner mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mIsLeScanning = true;
            ScanSettings.Builder builder = new ScanSettings.Builder()
                    //设置高功耗模式
//                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
            //android 6.0添加设置回调类型、匹配模式等
            if (Build.VERSION.SDK_INT >= 23) {
                //定义回调类型
                builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
                builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
//                builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
            }

            // 若设备支持批处理扫描，可以选择使用批处理，但此时扫描结果仅触发onBatchScanResults()
            if (mBluetoothAdapter.isOffloadedScanBatchingSupported()) {
                //设置蓝牙LE扫描的报告延迟的时间(以毫秒为单位)
                //设置为0以立即通知结果
                builder.setReportDelay(0L);
            }

            ScanSettings scanSettings = builder.build();
            if (mBLEScanner != null) {
                mBluetoothAdapter.startDiscovery();
                if (mBluetoothDeviceAdapterCallback != null)
                    mBluetoothDeviceAdapterCallback.onLeScanStart();

                return true;
            } else {
                mIsLeScanning = false;
                return false;
            }
        }
    }

    public boolean stopLeScan() {
        if (mBluetoothAdapter == null)
            return false;
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            if (mScanCallback != null) {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }

            mBluetoothAdapter.cancelDiscovery();
        }
        mIsLeScanning = false;
        if (mBluetoothDeviceAdapterCallback != null)
            mBluetoothDeviceAdapterCallback.onLeScanStop(0);
        return true;
    }

    public boolean isLeScanning() {
        return mIsLeScanning;
    }

    public BluetoothGatt connectGatt(String address, BluetoothGattCallback callback) {
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice == null)
            return null;

        return bluetoothDevice.connectGatt(mContext, false, callback);
    }

    private static boolean mNotified = false;

    public void setNotify(boolean notify) {
        mNotified = notify;
    }

    public void notify(BluetoothGatt gatt) {
        if (mNotified)
            return;

        mNotified = true;
        BluetoothGattCharacteristic characteristic = null;
        characteristic = GattUtils.getCharacteristic(gatt, BleConstants.BLE_READ_SERVICE_UUID, BleConstants.BLE_READ_CHARACTERISTIC_UUID);
        if (characteristic == null) {
            Log.d(TAG, "BluetoothGattManager notify get characteristic failed.");
            return;
        }

        BluetoothGattDescriptor descriptor = null;
        descriptor = GattUtils.getDescriptor(gatt,
                BleConstants.BLE_READ_SERVICE_UUID,
                characteristic.getUuid(),
                BleConstants.CLIENT_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor1 = new BluetoothGattDescriptor(descriptor.getUuid(), descriptor.getPermissions());
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mRequestQueue.addWriteDescriptor(gatt, descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            //具有NOTIFY|INDICATE 属性，根据属性设置相应的值，这里先默认设置为ENABLE_NOTIFICATION_VALUE, tiantian
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mRequestQueue.addWriteDescriptor(gatt, descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            mRequestQueue.execute();
        }
    }

    public boolean sendCmdNetStatus(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdNetStatus ...");

        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_NETWORK_STATUS);
    }

    public boolean sendCmdWifiLists(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdWifiLists ...");

        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_WIFILIST);
    }

    public boolean sendCmdWifiSetup(BluetoothGatt gatt, String ssid, String pwd) {
       return sendCmdWifiSetup(gatt, ssid, pwd, null);
    }

    private boolean sendCmdWifiSetup(BluetoothGatt gatt, String ssid, String pwd, String userdata) {
        AppTrace.d(TAG, "sendCmdWifiSetup ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_WIFI_SETUP);
            obj.put(BleConstants.BLE_KEY_SSID, ssid);
            obj.put(BleConstants.BLE_KEY_PASSWORD, pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendCmdSimple(gatt, obj);
    }

    private boolean sendCmdSimple(BluetoothGatt gatt, String cmd) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, cmd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendCmdSimple(gatt, obj);
    }


    private boolean sendCmdSimple(BluetoothGatt gatt, JSONObject obj) {
        AppTrace.i(TAG, "sendCmdSimple:\"" + obj.toString() + "\"");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(BleConstants.BLE_CMD_CHAR_START);
        try {
            baos.write(obj.toString().getBytes());
        } catch(IOException e) {
        }
        baos.write(BleConstants.BLE_CMD_CHAR_END);

        return sendCmd(gatt, baos.toByteArray());
    }



    private boolean sendCmd(BluetoothGatt gatt, byte[] data) {
        BluetoothGattCharacteristic characteristic = null;

        characteristic = GattUtils.getCharacteristic(gatt, BleConstants.BLE_WRITE_SERVICE_UUID, BleConstants.BLE_WRITE_CHARACTERISTIC_UUID);
        if(characteristic ==null){
            return false;
        }

        characteristic.setValue(data);
        mRequestQueue.addWriteCharacteristic(gatt, characteristic);
        mRequestQueue.execute();
        return true;
    }

    private final Object lock = new Object();

    public void next() {
        synchronized (lock) {
            mRequestQueue.next();
        }
    }

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecor) {
//        if (mBluetoothDeviceAdapter != null) {
//            mBluetoothDeviceAdapter.onLeScan(device, rssi, null);
//        }
    }

    public boolean isEnable(){
        if(mBluetoothAdapter!=null) {
            return mBluetoothAdapter.isEnabled();
        }
        return  false;
    }


    /**
     * 打开蓝牙
     * @param isFast  true 直接打开蓝牙  false 提示用户打开
     */
    @SuppressLint("MissingPermission")
    public void openBluetooth(Context context, boolean isFast){
        if(!isEnable()){
            if(isFast){
                Log.d(TAG,"直接打开手机蓝牙");
                mBluetoothAdapter.enable();  //BLUETOOTH_ADMIN权限
            }else{
                Log.d(TAG,"提示用户去打开手机蓝牙");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
            }
        }else{
            Log.d(TAG,"手机蓝牙状态已开");
        }
    }

    /**
     * 直接关闭蓝牙
     */
    @SuppressLint("MissingPermission")
    public void closeBluetooth(){
        if(mBluetoothAdapter == null)
            return;

        mBluetoothAdapter.disable();
    }
    /**
     * 发送关机命令
     * @param gatt
     * @return
     */
    public boolean sendCmdPowerOn(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdPowerOn ...");
        //return  sendCmdSimple(gatt, BleConstants.BLE_CMD_POWER_OFF);

        //开机命令
        byte[] powneron = new byte[] { (byte) 0xFF, (byte) 0xFE, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x05 };

        return sendCmd(gatt, powneron);
    }

    /**
     * 发送关机命令
     * @param gatt
     * @return
     */
    public boolean sendCmdPowerOff(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdPowerOff ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_POWER_OFF);
    }

    /**
     * 发送重启命令
     * @param gatt
     * @return
     */
    public boolean sendCmdRestart(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdRestart ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_RESTART);
    }

    /**
     * 打开热点
     * @param gatt
     * @return
     */
    public boolean sendCmdSoftapOpen(BluetoothGatt gatt, String ssid, String password) {
        AppTrace.d(TAG, "sendCmdSoftapOpen ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_SOFTAP_OPEN);
            obj.put("s", ssid);
            obj.put("p", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendCmdSimple(gatt, obj);
    }

    /**
     * 关闭热点
     * @param gatt
     * @return
     */
    public boolean sendCmdSoftapClose(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdSoftapClose ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_SOFTAP_CLOSE);
    }

    /**
     * 热点状态
     * @param gatt
     * @return
     */
    public boolean sendCmdSoftapStatus(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdSoftapStatus ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_SOFTAP_STATUS);
    }

    /**
     * 电量
     * @param gatt
     * @return
     */
    public boolean sendCmdEnergyStatus(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdEnergeStatus ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_ENERGY);
    }

    /**
     * 存储空间
     * @param gatt
     * @return
     */
    public boolean sendCmdStorageStatus(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdStorageStatus ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_STORAGE);
    }

    /**
     * 设备信息
     * @param gatt
     * @return
     */
    public boolean sendCmdDeviceInfo(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdDeviceInfo ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_DEVICE_INFO);
    }

    /**
     * 打开sim卡
     * @param gatt
     * @return
     */
    public boolean sendCmdSimOpen(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdSimOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_SIM_OPEN);
    }

    /**
     * 关闭sim卡
     * @param gatt
     * @return
     */
    public boolean sendCmdSimClose(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdSimClose ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_SIM_CLOSE);
    }

    /**
     * 打开相机程序， 开始预览
     * @param gatt
     * @return
     */
    public boolean sendCmdCameraOpen(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdCameraOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_CAMERA_START);
    }

    /**
     * 关闭相机程序。关闭后，录制，直播均会退出
     * @param gatt
     * @return
     */
    public boolean sendCmdCameraClose(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdCameraOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_CAMERA_STOP);
    }


    /**
     * 相机程序状态
     * @param gatt
     * @return
     */
    public boolean sendCmdCameraStatus(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdCameraOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_CAMERA_STATUS);
    }

    /**
     * 相机程序 设置
     * @param gatt
     * @return
     */
    public boolean sendCmdCameraSetup(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdCameraOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_CAMERA_SETUP);
    }

    /**
     * 相机程序 设置
     * @param gatt
     * @return
     */
    public boolean sendCmdLiveStart(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdCameraOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_LIVE_START);
    }
    /**
     * 相机程序 设置
     * @param gatt
     * @return
     */
    public boolean sendCmdLiveEnd(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdCameraOpen ...");
        return  sendCmdSimple(gatt, BleConstants.BLE_CMD_CAMERA_STOP);
    }


    public boolean sendCmdRkipcStart(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdRkipcStart ...");
        return sendCmdSimple(gatt, BleConstants.BLE_CMD_RKIPC_START);
    }


    public boolean sendCmdRecordStart(BluetoothGatt gatt, RecordInfoBean recordInfoBean) {
        AppTrace.d(TAG, "sendCmdRecordStart ...");
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
            obj.put(BleConstants.BLE_KEY_VIDEO_CITY, recordInfoBean.city);
            obj.put(BleConstants.BLE_KEY_VIDEO_PLACE_TYPE, recordInfoBean.placeType);
            obj.put(BleConstants.BLE_KEY_MOTION_TYPE, recordInfoBean.motionType);
            obj.put(BleConstants.BLE_KEY_HOST_TEAM, recordInfoBean.hostTeam);
            obj.put(BleConstants.BLE_KEY_GUSET_TEAM, recordInfoBean.guestTeam);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdRecordStop(BluetoothGatt gatt) {
        AppTrace.i(TAG, "sendCmdRecordStop");
        return sendCmdSimple(gatt, BleConstants.BLE_CMD_RECORDER_STOP);
    }

    public boolean sendCmdRecordStatus(BluetoothGatt gatt) {
        AppTrace.i(TAG, "sendCmdRecordStatus");
        return sendCmdSimple(gatt, BleConstants.BLE_CMD_RECORDER_STATUS);
    }

    public boolean sendCmdUsageInfo(BluetoothGatt gatt) {
        AppTrace.d(TAG, "sendCmdUsageInfo ...");
        return sendCmdSimple(gatt, BleConstants.BLE_CMD_USAGE_INFO);
    }
    /**
     * 设备信息
     * @param gatt
     * @return
     */
    public boolean sendCmdUpdateName(BluetoothGatt gatt, String name) {
        AppTrace.d(TAG, "sendCmdUpdateName ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_UPDATE_NAME);
            obj.put(BleConstants.BLE_KEY_UPDATE_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdDeviceActiveSms(BluetoothGatt gatt, String mobile, String bleAddress) {
        AppTrace.d(TAG, "sendCmdDeviceActiveSms ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_ACTIVE_SMS);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_BLE_MAC, bleAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdDeviceActiveBind(BluetoothGatt gatt, String mobile,String mac) {
        AppTrace.d(TAG, "sendCmdDeviceActiveBind ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_ACTIVE_BIND);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_MAC, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdDeviceUnBindSms(BluetoothGatt gatt, String mobile, String mac) {
        AppTrace.d(TAG, "sendCmdDeviceInfo ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_UNBIND_SMS);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_MAC, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdDeviceUnBind(BluetoothGatt gatt, String mobile, String mac, String code) {
        Log.d(TAG, "sendCmdDeviceInfo ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_DEVICE_UNBIND);
            obj.put(BleConstants.BLE_KEY_MOBILE, mobile);
            obj.put(BleConstants.BLE_KEY_MAC, mac);
            obj.put(BleConstants.BLE_KEY_CODE, mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdSetToken(BluetoothGatt gatt, String token) {
        Log.d(TAG, "sendCmdSetToken ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_SET_TOKEN);
            obj.put(BleConstants.BLE_KEY_TOKEN, token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdVideoList(BluetoothGatt gatt) {
        Log.d(TAG, "sendCmdSetToken ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_LIST);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdVideoDetailList(BluetoothGatt gatt, String path) {
        Log.d(TAG, "sendCmdSetToken ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_DETAIL_LIST);
            obj.put(BleConstants.BLE_KEY_PATH, path);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdVideoDirUploadList(BluetoothGatt gatt, List<String> path) {
        Log.d(TAG, "sendCmdSetToken ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_DIR_UPLOAD);
            obj.put(BleConstants.BLE_KEY_VIDEO, path);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdVideoUploadList(BluetoothGatt gatt, List<String> path) {
        Log.d(TAG, "sendCmdSetToken ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_DIR_UPLOAD);
            obj.put(BleConstants.BLE_KEY_VIDEO, path);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }

    public boolean sendCmdVideoFilterList(BluetoothGatt gatt, int type) {
        Log.d(TAG, "sendCmdSetToken ...");
        JSONObject obj = new JSONObject();
        try {
            obj.put(BleConstants.BLE_CMD, BleConstants.BLE_CMD_VIDEO_FILTER);
            obj.put(BleConstants.BLE_KEY_FILTER, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendCmdSimple(gatt, obj);
    }
}
