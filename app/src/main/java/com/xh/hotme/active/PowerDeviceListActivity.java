package com.xh.hotme.active;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleConnectListener;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleNetworkInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleScanNotifyListener;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.broadcast.ScanBroadcastReceiver;
import com.xh.hotme.databinding.DeviceActivityListBinding;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.PermissionsUtil;
import com.xh.hotme.utils.PlayUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.GridSpacingItemDecoration;
import com.xh.hotme.widget.ModalDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PowerDeviceListActivity extends BaseViewActivity<DeviceActivityListBinding> implements IBleScanRequestListener, IBleNetworkInfoNotifyListener, IBleDeviceInfoNotifyListener {
    private static final String TAG = PowerDeviceListActivity.class.getSimpleName();

    PowerDeviceAdapter _adapter;

    List<Device> _dataList = new ArrayList<>();
    IBleScanNotifyListener _scanNotifyListener;
    IBleConnectListener _connectNotifyListener;

    Device _connectDevice;

    Handler _handler;

    boolean isScan = false;
    boolean isScanning = false;

    BluetoothLoadingDialog _bluetoothLoadingDialog, _connectingDialog;

    ModalDialog _notFoundDialog;

    Device _readyDevice;

    boolean isBackgroundScan = false;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, PowerDeviceListActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }

    }

    @Override
    protected void initView() {
        // find views
        viewBinding.titleBar.tvTitle.setText(getString(R.string.my_devices));
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        viewBinding.titleBar.tvRight.setVisibility(View.VISIBLE);
        viewBinding.titleBar.tvRight.setText(getString(R.string.loading_dialog_refresh));

        viewBinding.titleBar.tvRight.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                onRefresh();

                return true;
            }
        });

//        viewBinding.titleBar.ivRight.setImageResource(R.drawable.ic_loading_48);
        viewBinding.titleBar.ivRight.setVisibility(View.GONE);

        viewBinding.recyclerView.setLayoutManager(new GridLayoutManager(PowerDeviceListActivity.this, 1));
        viewBinding.recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, DensityUtil.dip2px(PowerDeviceListActivity.this, 10), false));

        _adapter = new PowerDeviceAdapter(PowerDeviceListActivity.this, _dataList);

        _adapter.setCallback(new BluetoothDeviceAdapterCallback() {
            @Override
            public void onLeScanStart() {

            }

            @Override
            public void onLeScan(Device device, int position) {

            }

            @Override
            public void onLeScanStop(int size) {
                isScanning = false;
            }

            @Override
            public void onBluetoothDeviceClick(Device device, int position) {
                isScanning = false;
                connectDevice(device, position);
            }

            @Override
            public void onRemoveDevice(Device device, int position) {

            }

            @Override
            public void openDevice(Device device) {
                getDeviceInfo();
            }
        });

        viewBinding.recyclerView.setAdapter(_adapter);

        _scanNotifyListener = new IBleScanNotifyListener() {
            @Override
            public void onScanning(boolean isBackground) {
                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isBackground) {
                                showLoadingDialog(getString(R.string.loading_dialog_search_device));
                            }
                            isBackgroundScan = isBackground;
                        }
                    });
                }
            }

            @Override
            public void onLeScan(Device device) {
                dismissLoadingDialog();
                for (Device blue : _dataList) {
                    if (blue.getAddress().equals(device.getAddress())) {
                        blue.setRssi(device.rssi);
                        return;
                    }
                }

                if (device != null && device.getName() != null) {
                    String deviceName = device.getName().toLowerCase(Locale.ROOT);
                    if (deviceName.startsWith(BluetoothManager.weewa_prefix)) {
                        Device blueDevice = new Device(device.address, device.rssi, deviceName);
                        blueDevice.setStatus(Constants.DEVICE_STATUS_ONLINE);
                        _dataList.add(blueDevice);
                        _adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onTimeout() {

                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();

                            if (_connectDevice == null) {
                                return;
                            }
                            if (!isBackgroundScan) {
                                if (_dataList == null || _dataList.size() == 0) {
                                    showNotFoundDialog();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onScanEnd() {

            }
        };

        _connectNotifyListener = new IBleConnectListener() {

            @Override
            public void onConnecting(Device device) {
                AppTrace.d(TAG, "onConnectStart....");
                _connectDevice = device;
                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            _connectDevice = device;
                            if (BaseAppUtil.isForegroundActivity(PowerDeviceListActivity.this, PowerDeviceListActivity.class.getName())) {
                                showConnectingDialog(getString(R.string.loading_dialog_connect_device));
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectFailure(String address, String error) {
                _connectDevice = null;
                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            dismissConnectingDialog();
                            if (PowerDeviceListActivity.this != null) {
                                ToastUtil.s(PowerDeviceListActivity.this, PowerDeviceListActivity.this.getString(R.string.info_ble_connect_fail));
                            }
                        }
                    });
                }
            }

            @Override
            public void onDisConnecting(String address) {

            }

            @Override
            public void onDisConnectSuccess(String address, int status) {

                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (_connectDevice != null && _connectDevice.getAddress().equalsIgnoreCase(address)) {
                                for (Device blue : _dataList) {
                                    if (blue.getAddress().equals(_connectDevice.getAddress())) {
                                        blue.status = Constants.DEVICE_STATUS_OFFLINE;
                                        break;
                                    }
                                }

                            }
                            _adapter.notifyDataSetChanged();
                            _connectDevice = null;
                            ToastUtil.s(PowerDeviceListActivity.this, getString(R.string.info_ble_disconnect));

                        }
                    });
                }
            }

            @Override
            public void onConnectTimeOut(String address) {
                _connectDevice = null;
                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            dismissConnectingDialog();
                            if (PowerDeviceListActivity.this != null) {
                                ToastUtil.s(PowerDeviceListActivity.this, PowerDeviceListActivity.this.getString(R.string.info_ble_connect_timeout));
                            }
                        }
                    });
                }
            }

            @Override
            public void onServiceDiscoverySucceed(int status) {

            }

            @Override
            public void onServiceDiscoveryFailed(String message) {
                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            dismissConnectingDialog();
                            if (PowerDeviceListActivity.this != null) {
                                ToastUtil.s(PowerDeviceListActivity.this, message);
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectSuccess(Device device, int status) {  //
                AppTrace.d(TAG, "onConnect status=" + status);

                // ToastUtil.s(MyDeviceActivity.this, "连接" + (status == 0 ? "成功" : "失败"));
                if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this)) {

                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (_connectDevice != null) {
                                for (Device blue : _dataList) {
                                    if (device != null) {
                                        if (blue.getAddress().equals(_connectDevice.getAddress())) {
                                            blue.status = Constants.DEVICE_STATUS_CONNECT;
                                            break;
                                        }
                                    } else {
                                        if (blue.getAddress().equals(_connectDevice.getAddress())) {
                                            blue.status = Constants.DEVICE_STATUS_CONNECT;
                                            break;
                                        }
                                    }

                                }
                                //更新设备的蓝牙连接状态
                                _connectDevice.status = Constants.DEVICE_STATUS_CONNECT;
                                //添加本地记录
                                BluetoothManager.mInstance.updateConnectDeviceStatus(_connectDevice);

                                isBackgroundScan = true;
                                _handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        _adapter.notifyDataSetChanged();
                                    }
                                });

                                DeviceOnOffActivity.start(PowerDeviceListActivity.this, _connectDevice);
                            }
                            dismissConnectingDialog();

                        }
                    });
                }
            }
        };
    }

    @Override
    protected void initData() {
        _handler = new Handler();

        BluetoothHandle.addConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.addDeviceInfoNotifyListener(this);

        BluetoothHandle.addScanNotifyListener(_scanNotifyListener);
        if (!BluetoothManager.mInstance.enableBlueTooth()) {
            BluetoothManager.mInstance.openBluetooth(PowerDeviceListActivity.this, false);
        } else {
            String[] permissions = PermissionsUtil.checkPermission(PowerDeviceListActivity.this);
            if (permissions.length == 0) {
                if (!isScan) {
                    if (BluetoothManager.mInstance != null) {
                        BluetoothManager.mInstance.startScan(false);
                    }
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterScanBroadcast();

        BluetoothHandle.removeScanNotifyListener(_scanNotifyListener);

        BluetoothHandle.removeConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.removeDeviceInfoNotifyListener(this);

    }


    /*动态申请权限操作*/
    private boolean isPermissionRequested = false;

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {//在这里加入你要使用的权限
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入这里代表没有权限.
                }
            }

            if (!permissionsList.isEmpty()) {
                String[] strings = new String[permissionsList.size()];
                requestPermissions(permissionsList.toArray(strings), 0);
            }
        }
    }

    // 蓝牙权限
    public void requestBlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_ADVERTISE
                        },
                        1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }

    }

    private ScanBroadcastReceiver scanBroadcastReceiver;

    /**
     * 使用新api扫描
     * 注册蓝牙扫描监听
     */
    public void registerScanBroadcast() {
        Application application = getApplication();
        //注册蓝牙扫描状态广播接收者
        if (scanBroadcastReceiver == null && application != null) {
            scanBroadcastReceiver = new ScanBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            //开始扫描
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            //扫描结束
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //扫描中，返回结果
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            //扫描模式改变
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            //注册广播接收监听，用完不要忘了解注册哦
            application.registerReceiver(scanBroadcastReceiver, filter);
        }
    }

    public void unregisterScanBroadcast() {
        if (scanBroadcastReceiver != null) {
            Application application = getApplication();
            application.unregisterReceiver(scanBroadcastReceiver);
        }
    }


    public void connectDevice(Device device, int position) {
        onBluetoothDeviceClick(device, position);
    }

    public void onRefresh() {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.startScan(false);
        }
    }


    private void showLoadingDialog(String message) {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
            _bluetoothLoadingDialog = null;
        }
        _bluetoothLoadingDialog = new BluetoothLoadingDialog(PowerDeviceListActivity.this);
        _bluetoothLoadingDialog.setMessage(message);
        _bluetoothLoadingDialog.show();
    }


    private void dismissLoadingDialog() {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
        }
        _bluetoothLoadingDialog = null;
    }


    private void showConnectingDialog(String message) {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
        }
        _connectingDialog = null;
        _connectingDialog = new BluetoothLoadingDialog(PowerDeviceListActivity.this);
        _connectingDialog.setMessage(message);
        _connectingDialog.show();
    }


    private void dismissConnectingDialog() {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
        }
        _connectingDialog = null;
    }


    private void showNotFoundDialog() {
        _notFoundDialog = new ModalDialog(PowerDeviceListActivity.this);
        _notFoundDialog.setTitle(getString(R.string.loading_dialog_device_not_found));
        _notFoundDialog.setMessage(getString(R.string.loading_dialog_device_message));
        _notFoundDialog.setLeftButton(getString(R.string.loading_dialog_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _notFoundDialog.dismiss();

            }
        });
        _notFoundDialog.setRightButton(getString(R.string.loading_dialog_refresh), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _notFoundDialog.dismiss();

                onScanStart(false);

                showLoadingDialog(PowerDeviceListActivity.this.getString(R.string.loading_dialog_search_device));
            }
        });

        _notFoundDialog.show();
    }

    private void dismissNotFoundDialog() {
        if (_notFoundDialog != null && _notFoundDialog.isShowing()) {
            _notFoundDialog.dismiss();
            _notFoundDialog = null;
        }
    }


    @Override
    public void onNetworkStatus(NetworkBean networkBean) {

    }


    @Override
    public void onScanStart(boolean isBackground) {

    }

    @Override
    public void onScan(Device device, int position) {

    }

    @Override
    public void onScanStop(int size) {

    }

    @Override
    public void onBluetoothDeviceClick(Device device, int position) {
        _readyDevice = null;
        if (BluetoothManager.mInstance != null) {
            if (!BluetoothManager.mInstance.enableBlueTooth()) {
                BluetoothManager.mInstance.openBluetooth(PowerDeviceListActivity.this, false);
                return;
            }

            BluetoothManager.mInstance.connectToBle(device);
        }
    }

    @Override
    public void onRequestWifiList() {

    }

    @Override
    public void onSetupWifi(String ssid, String password) {

    }

    @Override
    public void onRemove(Device device, int position) {

    }

    public void getDeviceInfo() {
        if (BluetoothManager.mInstance != null) {
            if (!BluetoothManager.mInstance.enableBlueTooth()) {
                BluetoothManager.mInstance.openBluetooth(PowerDeviceListActivity.this, false);
                return;
            }
        }

        BluetoothManager.mInstance.sendCmdDeviceInfo();
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
        if (!BaseAppUtil.isDestroy(PowerDeviceListActivity.this) && BaseAppUtil.isForegroundActivity(PowerDeviceListActivity.this, PowerDeviceListActivity.class.getName())) {
            PlayUtil.startPlay(PowerDeviceListActivity.this, data, null);
        }

    }

    @Override
    public void onDeviceInfoFail() {

    }
}
