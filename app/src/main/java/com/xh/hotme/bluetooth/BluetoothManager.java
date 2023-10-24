package com.xh.hotme.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xh.hotme.bean.BindDeviceBean;
import com.xh.hotme.bean.BindDeviceResultBean;
import com.xh.hotme.camera.CameraManager;
import com.xh.hotme.camera.RecordInfoBean;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClsUtils;
import com.xh.hotme.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by GJK on 2018/11/9.
 */

public class BluetoothManager {
    private static final String TAG = BluetoothManager.class.getSimpleName();

    private final Context mContext;

    public static BluetoothManager mInstance;

    private int mMtu = 20;

    private BluetoothGatt mBluetoothGatt;
    private final BluetoothGattManager mBluetoothGattManager;
    private final BluetoothGattCallback mBluetoothGattCallback;

    private BluetoothGattService mBluetoothGattReadService;
    private BluetoothGattService mBluetoothGattWriteService;

    private BluetoothGattCharacteristic mReadCharacteristic;  //读特征
    private BluetoothGattCharacteristic mWriteCharacteristic; //写特征

    IBleScanListener _scanListener;

    private static List<Device> _scanDeviceList = new ArrayList<>();
    private static List<BindDeviceBean> _bindDeviceList = new ArrayList<>();

    public static BindDeviceResultBean _bindDevice;

    public static final String weewa_prefix = "weewa";

    private static final Handler mHandler = new Handler();
    private BluetoothDevice curConnDevice;  //当前连接的设备
    private boolean isConnectIng = false;  //是否正在连接中
    private boolean isScanning = false;  //是否正在扫描

    private static Device _connectDevice;

    public final static long scanTime = 20000;
    public final static long RESP_TIMEOUT = 20000;

    public final static long RESP_TIMEOUT_SHORT = 10000;
    private static final long MAX_CONNECT_TIME = 10000;  //连接超时时间10s

    //连接超时
    private final Runnable connectOutTimeRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            if (mBluetoothGatt == null) {
                Log.e(TAG, "connectOuttimeRunnable-->mBluetoothGatt == null");
                return;
            }
            //连接超时当作连接失败回调
            String deviceAddress = "";
            BluetoothDevice device = mBluetoothGatt.getDevice();
            if (device != null) {
                deviceAddress = device.getAddress();
            }

            isConnectIng = false;

            mBluetoothGatt.disconnect();

            BluetoothHandle.onConnectTimeoutNotify(deviceAddress);
        }
    };

    //发现服务超时
    private final Runnable serviceDiscoverOutTimeRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            if (mBluetoothGatt == null) {
                Log.e(TAG, "connectOuttimeRunnable-->mBluetoothGatt == null");
                return;
            }
            //连接超时当作连接失败回调
            String deviceAddress = "";
            BluetoothDevice device = mBluetoothGatt.getDevice();
            if (device != null) {
                deviceAddress = device.getAddress();
            }

            isConnectIng = false;
            mBluetoothGatt.disconnect();

            //发现服务超时当作连接失败回调
            BluetoothHandle.onConnectTimeoutNotify(deviceAddress);
        }
    };

    private final Runnable stopScanRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            isScanning = false;
            BluetoothHandle.onScanTimeout();

            //scanTime之后还没有扫描到设备，就停止扫描。
            stopDiscoveryDevice();
        }
    };

    //////////////////////////////////////  停止扫描  /////////////////////////////////////////////

    /**
     * 停止扫描
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopDiscoveryDevice() {
        mHandler.removeCallbacks(stopScanRunnable);

        if (mBluetoothGattManager == null) {
            Log.e(TAG, "stopDiscoveryDevice-->bluetooth4Adapter == null");
            return;
        }

        Log.d(TAG, "停止扫描设备");
        mBluetoothGattManager.stopLeScan();
    }

    public static BluetoothManager newInstance(Context context) {
        synchronized (BluetoothManager.class) {
            if (null == mInstance) {
                mInstance = new BluetoothManager(context);
            }
        }
        return mInstance;
    }

    public BluetoothManager(Context context) {
        this.mContext = context;

        mBluetoothGattManager = new BluetoothGattManager(context);
        mBluetoothGattManager.setBluetoothDeviceAdapterCallback(new BluetoothDeviceAdapterCallback() {
            @Override
            public void onLeScanStart() {

                if (_scanListener != null) {
                    _scanListener.onScanStart(false);
                }
            }

            @Override
            public void onLeScan(Device device, int position) {
                Log.d(TAG, "onLeScan position:" + position);

                if (_scanListener != null) {
                    _scanListener.onScan(device, position);
                }
            }

            @Override
            public void onLeScanStop(final int size) {
                Log.d(TAG, "Scaned devices " + size);

                if (_scanListener != null) {
                    _scanListener.onScanStop(size);
                }
            }

            @Override
            public void onBluetoothDeviceClick(Device device, int position) {
                Log.d(TAG, "onBluetoothDeviceClick name:" + device.getName() + " - " + position);

                if (_scanListener != null) {
                    _scanListener.onBluetoothDeviceClick(device, position);
                }

            }

            @Override
            public void onRemoveDevice(Device device, int position) {

            }

            @Override
            public void openDevice(Device device) {

            }
        });
        mBluetoothGattCallback = new BluetoothGattCallback() {
            private StringBuilder builder = new StringBuilder();

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                BluetoothDevice device = gatt.getDevice();
                String devAddress = "";
                if (device != null) {
                    devAddress = device.getAddress();
                }
                switch (status) {
                    case BluetoothGatt.GATT_SUCCESS:
                        Log.w(TAG, "BluetoothGatt.GATT_SUCCESS");
                        break;
                    case BluetoothGatt.GATT_FAILURE:
                        Log.w(TAG, "BluetoothGatt.GATT_FAILURE");
                        break;
                    case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                        Log.w(TAG, "BluetoothGatt.GATT_CONNECTION_CONGESTED");
                        break;
                    case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                        Log.w(TAG, "BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION");
                        break;
                    case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                        Log.w(TAG, "BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION");
                        break;
                    case BluetoothGatt.GATT_INVALID_OFFSET:
                        Log.w(TAG, "BluetoothGatt.GATT_INVALID_OFFSET");
                        break;
                    case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                        Log.w(TAG, "BluetoothGatt.GATT_READ_NOT_PERMITTED");
                        break;
                    case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                        Log.w(TAG, "BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED");
                        break;
                }

                isConnectIng = false;
                //移除连接超时
                mHandler.removeCallbacks(connectOutTimeRunnable);

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.w(TAG, "连接成功");
                    //连接成功去发现服务
                    gatt.discoverServices();
                    //设置发现服务超时时间
                    mHandler.postDelayed(serviceDiscoverOutTimeRunnable, MAX_CONNECT_TIME);

                    CameraManager.newCamera(devAddress);

                    BluetoothHandle.onConnectSuccess(status);   //连接成功回调

                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    //清空系统缓存
                    ClsUtils.refreshDeviceCache(gatt);
                    Log.e(TAG, "断开连接status:" + status);
                    gatt.close();  //断开连接释放连接
                    _connectDevice = null;
                    if (status == 133) {
                        //无法连接
//                        gatt.close();
                        BluetoothHandle.onConnectFailure(devAddress, "连接异常！");

                    } else if (status == 62) {
                        //成功连接没有发现服务断开
//                        gatt.close();
                        BluetoothHandle.onConnectFailure(devAddress, "连接成功服务未发现断开！");
                        Log.e(TAG, "连接成功服务未发现断开status:" + status);

                    } else if (status == 0) {
                        AppTrace.d("断开成功： status=" + status);
                        BluetoothHandle.onDisConnectSuccess(devAddress, status); //0正常断开 回调
                    } else if (status == 8) {
                        AppTrace.d("距离远或者电池无法供电断开连接：status=" + status);
                        //因为距离远或者电池无法供电断开连接
                        // 已经成功发现服务
                        BluetoothHandle.onDisConnectSuccess(devAddress, status);
                    } else if (status == 34) {
                        AppTrace.d("断开成功： status=" + status);
                        BluetoothHandle.onDisConnectSuccess(devAddress, status);
                    } else {
                        AppTrace.d("断开连接：status=" + status);
                        //其它断开连接
                        BluetoothHandle.onDisConnectSuccess(devAddress, status);
                    }
                    //释放
                    CameraManager.release();
                } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                    AppTrace.d(TAG, "正在连接...");
                    BluetoothHandle.onConnecting(new Device(devAddress, 0, ""));

                } else if (newState == BluetoothGatt.STATE_DISCONNECTING) {
                    AppTrace.d(TAG, "正在断开...");
                    BluetoothHandle.onDisConnecting(devAddress); //正在断开回调
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d(TAG, "onServicesDiscovered status:" + status);

                //移除发现服务超时
                mHandler.removeCallbacks(serviceDiscoverOutTimeRunnable);
                Log.d(TAG, "移除发现服务超时");

                if (status != 0) {
                    gatt.close();
                } else {
                    if (setupService(gatt, BleConstants.BLE_WRITE_SERVICE_UUID, BleConstants.BLE_READ_SERVICE_UUID, BleConstants.BLE_READ_CHARACTERISTIC_UUID, BleConstants.BLE_WRITE_CHARACTERISTIC_UUID)) {
                        mBluetoothGattManager.setNotify(true);
                        //这一步必须要有，否则接收不到通知
                        //gatt.setCharacteristicNotification(characteristic,enable);
                        gatt.requestMtu(134 + 3);

                        BluetoothHandle.onServiceDiscoverySucceed(status);

                    } else {
                        BluetoothHandle.onServiceDiscoveryFailed("获取服务特征异常");  //发现服务失败回调
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if (status == 0) {
                    try {
                        handleCharacteristicRead(gatt, characteristic);
                    } catch (Exception e) {
                        Log.d(TAG, Log.getStackTraceString(e));
                    }
                }
                mBluetoothGattManager.next();
            }

            private void handleCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) throws JSONException, UnsupportedEncodingException {

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                AppTrace.i(TAG, "onCharacteristicWrite status = " + status);
                if (status == 0) {
                    try {
                        handleCharacteristicWrite(gatt, characteristic);
                    } catch (Exception e) {

                    }
                }
                mBluetoothGattManager.next();
            }

            private void handleCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.d(TAG, "handleCharacteristicWrite ");

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                AppTrace.d(TAG, "onCharacteristicChanged ");

                try {
                    handleCharacteristicChanged(gatt, characteristic);
                } catch (JSONException | UnsupportedEncodingException e) {

                }
            }

            private void handleCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) throws JSONException, UnsupportedEncodingException {
                try {
                    String value = characteristic.getStringValue(0);
                    byte[] b = value.getBytes(StandardCharsets.UTF_8);
                    // 忽略APK 发送出去的数据
                    if (builder.toString().length() == 0 && b[0] != 0x01) {
                        return;
                    } else {
                        builder.append(value);
                    }

                    Log.d(TAG, "handleCharacteristicChanged mtu:" + mMtu + "; bytes:" + value.getBytes(StandardCharsets.UTF_8).length);
                    if (isDataVerify(builder.toString().getBytes())) {
                        if (builder.length() > 2) {
                            String data = builder.substring(1, builder.toString().length() - 1);
                            if (isJSONValid(data)) {
                                AppTrace.i(TAG, data);
                                JSONObject jsonRoot = (JSONObject) JSON.parse(data);
                                BluetoothHandle.handle(jsonRoot);
                            }
                        }
                        builder = new StringBuilder();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    builder = new StringBuilder();
                }

            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                Log.d(TAG, "onDescriptorRead ");
                if (status == 0) {
                    try {
                        handleDescriptorRead(gatt, descriptor);
                    } catch (Exception e) {

                    }
                }
                mBluetoothGattManager.next();
            }

            private void handleDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {

            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Log.d(TAG, "onDescriptorWrite status =" + status);
                if (status == 0) {
                    try {
                        handleDescriptorWrite(gatt, descriptor);
                    } catch (Exception e) {

                    }
                }
                mBluetoothGattManager.next();
            }

            private void handleDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {

            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                Log.d(TAG, "onMtuChanged mtu:" + mtu + "; status:" + status);
//                mBluetoothGattManager.notify(gatt);
                mMtu = mtu - 3;
            }
        };

//        loadLocalData();
        loadBindDeviceData();
    }


    private boolean isJSONValid(String str) {
        try {
            JSONObject.parseObject(str);
        } catch (JSONException ex) {
            try {
                JSONObject.parseArray(str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private boolean isDataVerify(byte[] data) {
        int len = data.length;
        return data[0] == BleConstants.BLE_CMD_CHAR_START && data[len - 1] == BleConstants.BLE_CMD_CHAR_END;
    }

    /**
     * 获取特定服务及特征
     * 1个serviceUUID -- 1个readUUID -- 1个writeUUID
     *
     * @param bluetoothGatt
     * @param writeServiceUUID
     * @param readServiceUUID
     * @param readUUID
     * @param writeUUID
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean setupService(BluetoothGatt bluetoothGatt, UUID writeServiceUUID, UUID readServiceUUID, UUID readUUID, UUID writeUUID) {
        if (bluetoothGatt == null) {
            Log.e(TAG, "setupService()-->bluetoothGatt == null");
            return false;
        }

        if (writeServiceUUID == null || readServiceUUID == null) {
            Log.e(TAG, "setupService()-->serviceUUID == null");
            return false;
        }

        for (BluetoothGattService service : bluetoothGatt.getServices()) {
//            Log.d(TAG, "service = " + service.getUuid().toString());
            if (service.getUuid().equals(readServiceUUID)) {
                mBluetoothGattReadService = service;
            } else if (service.getUuid().equals(writeServiceUUID)) {
                mBluetoothGattWriteService = service;
            }
        }
        //通过上面方法获取bluetoothGattService
//        bluetoothGattService = bleManager.getBluetoothGattService(bluetoothGatt,ConsData.MY_BLUETOOTH4_UUID);
        if (mBluetoothGattReadService == null) {
            //找不到该服务就立即断开连接
            Log.e(TAG, "setupService()-->bluetoothGattReadService == null");
            return false;
        }
        if (mBluetoothGattWriteService == null) {
            //找不到该服务就立即断开连接
            Log.e(TAG, "setupService()-->bluetoothGattWriteService == null");
            return false;
        }
        Log.d(TAG, "setupService()-->bluetoothGattReadService = " + mBluetoothGattReadService);
        Log.d(TAG, "setupService()-->bluetoothGattWriteService = " + mBluetoothGattWriteService.toString());

        if (readUUID == null || writeUUID == null) {
            Log.e(TAG, "setupService()-->readUUID == null || writeUUID == null");
            return false;
        }

        for (BluetoothGattCharacteristic characteristic : mBluetoothGattReadService.getCharacteristics()) {
            if (characteristic.getUuid().equals(readUUID)) {  //读特征
                mReadCharacteristic = characteristic;
            }
        }
        for (BluetoothGattCharacteristic characteristic : mBluetoothGattWriteService.getCharacteristics()) {
            if (characteristic.getUuid().equals(writeUUID)) {  //写特征
                mWriteCharacteristic = characteristic;
            }
        }
        if (mReadCharacteristic == null) {
            Log.e(TAG, "setupService()-->readCharacteristic == null");
            return false;
        }
        if (mWriteCharacteristic == null) {
            Log.e(TAG, "setupService()-->writeCharacteristic == null");
            return false;
        }
        //打开读通知
        enableNotification(true, bluetoothGatt, mReadCharacteristic);

        //重点中重点，需要重新设置
        List<BluetoothGattDescriptor> descriptors = mReadCharacteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }

        //延迟2s，保证所有通知都能及时打开
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 2000);

        return true;

    }

    /////////////////////////////////////////  打开通知  //////////////////////////////////////////

    /**
     * 设置读特征接收通知
     *
     * @param enable         为true打开通知
     * @param gatt           连接
     * @param characteristic 特征
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null) {
            Log.e(TAG, "enableNotification-->gatt == null");
            return;
        }
        if (characteristic == null) {
            Log.e(TAG, "enableNotification-->characteristic == null");
            return;
        }
        //这一步必须要有，否则接收不到通知
        gatt.setCharacteristicNotification(characteristic, enable);
    }


    public void setScanListener(IBleScanListener listener) {
        this._scanListener = listener;
    }

    public IBleScanListener getScanListener() {
        return _scanListener;
    }

    public boolean isScanning() {
        if (mBluetoothGattManager != null) {
            return mBluetoothGattManager.isLeScanning();
        }

        return false;
    }

    public void stopScan() {
        isScanning = false;
        mHandler.removeCallbacks(stopScanRunnable);
        if (mBluetoothGattManager != null) {
            mBluetoothGattManager.stopLeScan();
        }
    }

    public void startScan(boolean isBackground) {
        AppTrace.d(TAG, "startScan....");

        if (isScanning) {
            AppTrace.d(TAG, "skip, scanning....");
            return;
        }

        boolean start = false;
        if (mBluetoothGattManager != null) {
            start = mBluetoothGattManager.startLeScan();
        }

        BluetoothHandle.onScanning(isBackground);

        if (start) {
            if (_scanDeviceList != null) {
                _scanDeviceList.clear();
            }

            //设定最长扫描时间
            mHandler.postDelayed(stopScanRunnable, scanTime);

            isScanning = true;
        } else {
            isScanning = false;
        }

    }


    public boolean disconnectBle(Device device) {
        Log.d(TAG, "disconnectBle ...");
        try {
            if (mBluetoothGatt == null) {
                return false;
            }
            isConnectIng = false;
            mBluetoothGatt.disconnect();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public void connectToBle(Device device) {

        if (mBluetoothGattManager.isLeScanning())
            mBluetoothGattManager.stopLeScan();

        if (mBluetoothGatt != null && mBluetoothGatt.getDevice() != null) {
            String address = mBluetoothGatt.getDevice().getAddress();
//            if (!TextUtils.isEmpty(address) && device.address.equalsIgnoreCase(address)) {
            try {
                mBluetoothGatt.disconnect();
            } catch (Throwable e) {
                e.printStackTrace();
            }
//            }
        }

        mBluetoothGatt = mBluetoothGattManager.connectGatt(device.getAddress(), mBluetoothGattCallback);

        _connectDevice = device;

        //连接开始回调
        BluetoothHandle.onConnecting(device);

    }

    public boolean enableBlueTooth() {
        if (mBluetoothGattManager != null) {
            return mBluetoothGattManager.isEnable();
        }
        return false;
    }

    /**
     * 打开蓝牙
     *
     * @param isFast true 直接打开蓝牙  false 提示用户打开
     */
    public void openBluetooth(Context context, boolean isFast) {
        if (mBluetoothGattManager != null) {
            mBluetoothGattManager.openBluetooth(context, isFast);
        }

    }

    /**
     * 直接关闭蓝牙连接
     */
    public void closeBluetooth() {
        if (mBluetoothGattManager != null) {
            mBluetoothGattManager.closeBluetooth();
        }
    }


    public void onScanEnd() {
        //
        mHandler.removeCallbacks(stopScanRunnable);

        BluetoothHandle.onScanEnd();
        isScanning = false;
    }

    public static String getBluetoothName(String macAddress) {

        //更新本地记录的在线状态
        if (_scanDeviceList != null && _scanDeviceList.size() > 0) {
            for (Device tmpDevice : _scanDeviceList) {
                if (tmpDevice.getAddress().equalsIgnoreCase(macAddress)) {
                    return tmpDevice.name;
                }
            }
        }

        return "";
    }

    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecor) {

        if (device != null && device.getName() != null) {
            String deviceName = device.getName().toLowerCase(Locale.ROOT);
            if (deviceName.startsWith(weewa_prefix)) {
                Device blueDevice = new Device(device, rssi, deviceName);
                blueDevice.setStatus(Constants.DEVICE_STATUS_ONLINE);

                //更新本地记录的在线状态
                if (_scanDeviceList != null && _scanDeviceList.size() > 0) {
                    boolean exist = false;
                    for (Device tmpDevice : _scanDeviceList) {
                        if (tmpDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
                            if (tmpDevice.status == Constants.DEVICE_STATUS_OFFLINE) {
                                tmpDevice.status = Constants.DEVICE_STATUS_ONLINE;
                            }
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        _scanDeviceList.add(blueDevice);
                    }
                } else {
                    _scanDeviceList.add(blueDevice);
                }

                BluetoothHandle.onLeScan(blueDevice);
            }
        }


    }


    public static List<BindDeviceBean> getBindDeviceList() {
        return _bindDeviceList;
    }

    private void loadBindDeviceData() {
        try {
            String deviceString = AppFileUtil.loadStringFromFile(mContext, AppFileUtil.CACHE_DEVICE_BIND_FILE);

            _bindDevice = new Gson().fromJson(deviceString, new TypeToken<BindDeviceResultBean>() {
            }.getType());


            if (_bindDevice != null) {
                List<BindDeviceBean> bleDeviceList = _bindDevice.rows;

                if (bleDeviceList != null && bleDeviceList.size() > 0) {
                    for (BindDeviceBean device : bleDeviceList) {
                        device.setStatus(Constants.DEVICE_STATUS_OFFLINE);
                    }
                }

                if (_bindDeviceList == null) {
                    _bindDeviceList = new ArrayList<>();
                }
                _bindDeviceList.addAll(bleDeviceList);
            }
        } catch (Throwable e) {

        }
    }

    private void saveBindDeviceData() {
        if (_bindDevice != null) {
            String deviceString = new Gson().toJson(_bindDevice);
            AppFileUtil.saveString(mContext, deviceString, AppFileUtil.CACHE_DEVICE_BIND_FILE);
        }
    }

    public void updateBindDeviceStatus(Device connectDevice) {

        if (_bindDeviceList != null && _bindDeviceList.size() > 0) {
            BindDeviceBean cloneDevice = null;
            int curIndex = 0;
            for (int i = 0; i < _bindDeviceList.size(); i++) {
                BindDeviceBean device = _bindDeviceList.get(i);
                if (device.bluetoothUuid.equalsIgnoreCase(connectDevice.getAddress())) {
                    device.status = connectDevice.status;
                    cloneDevice = device.clone();
                    curIndex = i;
                    break;
                }
            }
            if (cloneDevice != null) {
                _bindDeviceList.remove(curIndex);
                _bindDeviceList.add(0, cloneDevice);
                saveBindDeviceData();
            }
        }
    }

    public void removeBindDeviceData(Device device) {
        if (_bindDeviceList != null && _bindDeviceList.size() > 0) {
            int curIndex = 0;
            for (int i = 0; i < _bindDeviceList.size(); i++) {
                BindDeviceBean bindDevice = _bindDeviceList.get(i);
                if (bindDevice.bluetoothUuid.equalsIgnoreCase(device.getAddress())) {
                    _bindDeviceList.remove(bindDevice);
                    break;
                }
            }
        }
    }


    public void addConnectDevice(Device connectDevice) {
        if (_scanDeviceList != null) {
            for (Device device : _scanDeviceList) {
                if (device.getAddress().equalsIgnoreCase(connectDevice.getAddress())) {
                    return;
                }
            }
        } else {
            _scanDeviceList = new ArrayList<>();
        }

        _scanDeviceList.add(connectDevice);

        saveLocalData();
    }

    public void updateConnectDeviceStatus(Device connectDevice) {

        if (_scanDeviceList == null) {
            _scanDeviceList = new ArrayList<>();
        }

        if (_scanDeviceList.size() > 0) {
            Device cloneDevice = null;
            int curIndex = 0;
            for (int i = 0; i < _scanDeviceList.size(); i++) {
                Device device = _scanDeviceList.get(i);
                if (device.getAddress().equalsIgnoreCase(connectDevice.getAddress())) {
                    device.status = connectDevice.status;

                    cloneDevice = device.clone();
                    curIndex = i;
                    break;
                }
            }
            if (cloneDevice != null) {
                _scanDeviceList.remove(curIndex);

                _scanDeviceList.add(0, cloneDevice);

                saveLocalData();
            }
        } else {
            Device cloneDevice = connectDevice.clone();
            _scanDeviceList.add(cloneDevice);
        }
    }

    private void saveLocalData() {
        if (_scanDeviceList != null && _scanDeviceList.size() > 0) {
            String deviceString = new Gson().toJson(_scanDeviceList);
            AppFileUtil.saveString(mContext, deviceString, AppFileUtil.CACHE_DEVICE_FILE);
        }
    }

    private void loadLocalData() {
        String deviceString = AppFileUtil.loadStringFromFile(mContext, AppFileUtil.CACHE_DEVICE_FILE);
        List<Device> bleDeviceList = new Gson().fromJson(deviceString, new TypeToken<List<Device>>() {
        }.getType());

        if (bleDeviceList != null && bleDeviceList.size() > 0) {
            for (Device device : bleDeviceList) {
                device.setStatus(Constants.DEVICE_STATUS_OFFLINE);
            }

            if (_scanDeviceList == null) {
                _scanDeviceList = new ArrayList<>();
            }
            _scanDeviceList.addAll(bleDeviceList);
        }
    }


    public void removeLocalData(Device device) {
        String deviceString = AppFileUtil.loadStringFromFile(mContext, AppFileUtil.CACHE_DEVICE_FILE);
        List<Device> bleDeviceList = new Gson().fromJson(deviceString, new TypeToken<List<Device>>() {
        }.getType());

        if (bleDeviceList != null && bleDeviceList.size() > 0) {
            for (Device tmpDevice : bleDeviceList) {
                if (tmpDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
                    bleDeviceList.remove(device);
                    return;
                }

            }
        }

    }

    public List<Device> getBleDeviceList() {
        return _scanDeviceList;
    }


    public boolean sendCmdWifiLists() {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdWifiLists(mBluetoothGatt);

        return result;
    }

    public boolean sendCmdSetup(String ssid, String password) {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdWifiSetup(mBluetoothGatt, ssid, password);

        return result;
    }

    public boolean sendCmdPowerOn() {
        Log.d(TAG, "sendCmdPowerOn ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdPowerOn(mBluetoothGatt);
    }

    public boolean sendCmdPowerOff() {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdPowerOff(mBluetoothGatt);
    }

    /**
     * 发送重启命令
     *
     * @return
     */
    public boolean sendCmdRestart() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdRestart(mBluetoothGatt);
    }

    public boolean sendCmdSoftapOpen(String ssid, String password) {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdSoftapOpen(mBluetoothGatt, ssid, password);
    }

    public boolean sendCmdSoftapClose() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdSoftapClose(mBluetoothGatt);
    }

    public boolean sendCmdSoftapStatus() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdSoftapStatus(mBluetoothGatt);
    }

    public boolean sendCmdEnergyStatus() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdEnergyStatus(mBluetoothGatt);
    }

    public boolean sendCmdStorageStatus() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdStorageStatus(mBluetoothGatt);
    }

    public boolean sendCmdNetStatus() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdNetStatus(mBluetoothGatt);
    }

    public boolean sendCmdStartRkipc() {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdRkipcStart(mBluetoothGatt);

        return result;
    }

    public boolean sendCmdStartRecord(RecordInfoBean info) {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdRecordStart(mBluetoothGatt, info);
        return result;
    }

    public boolean sendCmdStopRecord() {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdRecordStop(mBluetoothGatt);

        return result;
    }

    public boolean sendCmdRecordStatus() {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdRecordStatus(mBluetoothGatt);

        return result;
    }

    public boolean sendCmdUsageInfo() {
        if (mBluetoothGatt == null) {
            return false;
        }
        boolean result = mBluetoothGattManager.sendCmdUsageInfo(mBluetoothGatt);

        return result;
    }


    //打开sim卡
    public boolean sendCmdSimOpen() {
        Log.d(TAG, "sendCmdSetToken ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdSimOpen(mBluetoothGatt);
    }


    //打开sim卡
    public boolean sendCmdSimClose() {
        Log.d(TAG, "sendCmdSetToken ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdSimOpen(mBluetoothGatt);
    }

    public boolean sendCmdDeviceInfo() {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdDeviceInfo(mBluetoothGatt);
    }


    public boolean sendCmdUpdateName(String name) {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdUpdateName(mBluetoothGatt, name);
    }

    //发送激活码
    public boolean sendCmdDeviceActiveSms(String mobile, String bleAddress) {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdDeviceActiveSms(mBluetoothGatt, mobile, bleAddress);
    }


    //发送绑定
    public boolean sendCmdDeviceActiveBind(String mobile, String mac) {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdDeviceActiveBind(mBluetoothGatt, mobile, mac);
    }


    //发送解绑短信
    public boolean sendCmdDeviceUnBindSms(String mobile, String mac) {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdDeviceUnBindSms(mBluetoothGatt, mobile, mac);
    }

    //发送解绑
    public boolean sendCmdDeviceUnBind(String mobile, String mac, String code) {
        Log.d(TAG, "sendCmdPowerOff ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdDeviceUnBind(mBluetoothGatt, mobile, mac, code);
    }


    //发送登录token
    public boolean sendCmdSetToken(String token) {
        Log.d(TAG, "sendCmdSetToken ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdSetToken(mBluetoothGatt, token);
    }


    public static String getDisplayDeviceName(String deviceName) {
        if (deviceName.startsWith("weewa_")) {
            return deviceName.substring(6);
        }
        return deviceName;

    }

    public static String getDisplayDeviceName(Device device) {
        if (device == null) {
            return "";
        }
        return getDisplayDeviceName(device.getName());
    }

    public static void updateDeviceName(String address, String deviceName) {

        for (Device device : _scanDeviceList) {
            if (device != null && !TextUtils.isEmpty(device.getAddress()) && device.getAddress().equalsIgnoreCase(address)) {
                device.setName(deviceName);
                break;
            }
        }
        for (BindDeviceBean device : _bindDeviceList) {
            if (device != null && !TextUtils.isEmpty(device.bluetoothUuid) && device.bluetoothUuid.equalsIgnoreCase(address)) {
                device.deviceName = deviceName;
                if (device.blueDevice != null) {
                    device.blueDevice.setName(deviceName);
                }
                break;
            }
        }
    }


    //发送登录token
    public boolean sendCmdVideoList() {
        Log.d(TAG, "sendCmdVideoList ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdVideoList(mBluetoothGatt);
    }


    //发送登录token
    public boolean sendCmdVideoDetailList(String path) {
        Log.d(TAG, "sendCmdVideoDetailList ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdVideoDetailList(mBluetoothGatt, path);
    }

    /**
     * 上传比赛视频目录下的所有（包括其精彩视频，及）列表
     *
     * @param path 比赛视频根目录 20230708/1030
     * @return
     */
    public boolean sendCmdVideoDirUploadList(List<String> path) {
        Log.d(TAG, "sendCmdVideoUploadList ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdVideoDirUploadList(mBluetoothGatt, path);
    }

    /**
     * 上传单个视频列表
     *
     * @param path
     * @return
     */
    public boolean sendCmdVideoUploadList(List<String> path) {
        Log.d(TAG, "sendCmdVideoUploadList ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdVideoDirUploadList(mBluetoothGatt, path);
    }


    public boolean sendCmdVideoFilterList(int type) {
        Log.d(TAG, "sendCmdVideoFilterList ...");
        if (mBluetoothGatt == null) {
            return false;
        }
        return mBluetoothGattManager.sendCmdVideoFilterList(mBluetoothGatt, type);
    }

    public static String getRealFileImageUrl(String path) {
        return ConnectLogic.getInstance().getConnectBaseurl() + "/file?p=" + path;
    }


    public static String getConnectDeviceName() {
        if (_connectDevice != null) {
            String address = _connectDevice.getAddress();
            return address.replace(":", "");
        }
        return "";
    }

    public static String getConnectDeviceAddress() {
        if (_connectDevice != null) {
            return _connectDevice.getAddress();
        }
        return "";
    }


    public static Device getConnectDevice() {
        return _connectDevice;
    }

}
