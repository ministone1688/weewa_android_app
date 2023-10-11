package com.xh.hotme.active;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.BindDeviceBean;
import com.xh.hotme.bean.BindDeviceResultBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleConnectListener;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleLoginTokenListener;
import com.xh.hotme.bluetooth.IBleNetworkInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleScanNotifyListener;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.broadcast.ScanBroadcastReceiver;
import com.xh.hotme.camera.PlayerActivity;
import com.xh.hotme.databinding.DeviceActivityListBinding;
import com.xh.hotme.device.BindDeviceInteract;
import com.xh.hotme.event.RemoveDeviceEvent;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.PermissionsUtil;
import com.xh.hotme.utils.PlayUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.GridSpacingItemDecoration;
import com.xh.hotme.widget.ModalDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class MyDeviceActivity extends BaseViewActivity<DeviceActivityListBinding> implements IBleScanRequestListener, IBleNetworkInfoNotifyListener, IBleDeviceInfoNotifyListener, IBleLoginTokenListener {
    private static final String TAG = MyDeviceActivity.class.getSimpleName();

    private SmartRefreshLayout _refreshLayout;
    RecyclerView recyclerView;

    MyDeviceAdapter _adapter;

    List<BindDeviceBean> _dataList = new ArrayList<>();
    IBleScanNotifyListener _scanNotifyListener;
    IBleConnectListener _connectNotifyListener;

    Handler _handler;

    boolean isScan = false;
    boolean isScanning = false;

    BluetoothLoadingDialog _bluetoothLoadingDialog, _connectingDialog;

    ModalDialog _notFoundDialog;

    Device _readyDevice;

    boolean isBackgroundScan = false;

    boolean _isRequest = false;

    int _total = -1;

    int _pageIndex = 0;

    Device _connectDevice;
    MobileUnbindDialog _unbindDialog;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, MyDeviceActivity.class);
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

        _handler = new Handler();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void initView() {

        viewBinding.titleBar.tvTitle.setText(getString(R.string.my_devices));
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        viewBinding.titleBar.ivRight.setVisibility(View.GONE);

        viewBinding.titleBar.tvRight.setText(R.string.loading_dialog_refresh);
        viewBinding.titleBar.tvRight.setVisibility(View.VISIBLE);
        viewBinding.titleBar.tvRight.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (!isScanning) {
                    if (BluetoothManager.mInstance != null) {
                        BluetoothManager.mInstance.startScan(false);
                    }
                }
                return true;
            }
        });


        _refreshLayout = viewBinding.refreshLayout;

        recyclerView = viewBinding.recyclerView;

        recyclerView.setLayoutManager(new GridLayoutManager(MyDeviceActivity.this, 1));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, DensityUtil.dip2px(MyDeviceActivity.this, 10), false));

        _adapter = new MyDeviceAdapter(MyDeviceActivity.this, _dataList);

        View emptyView = LayoutInflater.from(MyDeviceActivity.this).inflate(R.layout.my_device_empty_view, null);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        //添加空视图
        _adapter.setEmptyView(emptyView);

        _adapter.setCallback(new BluetoothDeviceAdapterCallback() {
            @Override
            public void onLeScanStart() {

            }

            @Override
            public void onLeScan(Device device, int position) {
                //隐藏加载框
                dismissLoadingDialog();
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

            }
        });

        recyclerView.setAdapter(_adapter);

        _refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                _pageIndex = 0;
                getDeviceList();
            }
        });

        _refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore(500);
                getDeviceList();
            }
        });

        _scanNotifyListener = new IBleScanNotifyListener() {
            @Override
            public void onScanning(boolean isBackground) {
                AppTrace.d(TAG, "scanning ...");
                if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
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
                AppTrace.d(TAG, "scan ...");
                //隐藏加载框
                dismissLoadingDialog();
                for (BindDeviceBean blue : _dataList) {
                    if (!TextUtils.isEmpty(blue.bluetoothUuid) && blue.bluetoothUuid.equals(device.getAddress())) {
                        if (blue.blueDevice == null) {
                            blue.blueDevice = device;
                        }
                        if (blue.status == Constants.DEVICE_STATUS_OFFLINE) {
                            blue.status = Constants.DEVICE_STATUS_ONLINE;
                        }
                        _adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onTimeout() {
                AppTrace.d(TAG, "scan timeout....");
                try {
                    dismissLoadingDialog();
                    List<Device> deviceList = BluetoothManager.mInstance.getBleDeviceList();
                    if (deviceList != null || deviceList.size() > 0) {
                        for (BindDeviceBean bindDevice : _dataList) {
                            for (Device device : deviceList) {
                                if (!TextUtils.isEmpty(bindDevice.bluetoothUuid) && bindDevice.bluetoothUuid.equals(device.getAddress())) {
                                    if (bindDevice.blueDevice == null) {
                                        bindDevice.blueDevice = device;
                                    }
                                    if (bindDevice.status == Constants.DEVICE_STATUS_OFFLINE) {
                                        bindDevice.status = Constants.DEVICE_STATUS_ONLINE;
                                    }
                                    break;
                                }
                            }
                        }
                        _adapter.notifyDataSetChanged();
                    }

                } catch (Throwable e) {

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
                if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
//                            if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
//                                showConnectingDialog(getString(R.string.loading_dialog_connect_device));
//                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectFailure(String address, String error) {
                _connectDevice = null;
                if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            dismissConnectingDialog();
                            if (MyDeviceActivity.this != null) {
                                ToastUtil.s(MyDeviceActivity.this, MyDeviceActivity.this.getString(R.string.info_ble_connect_fail));
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
                if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (_connectDevice != null && _connectDevice.getAddress().equalsIgnoreCase(address)) {
                                for (BindDeviceBean blue : _dataList) {
                                    if (blue.bluetoothUuid.equals(_connectDevice.getAddress())) {
                                        blue.status = Constants.DEVICE_STATUS_OFFLINE;
                                        break;
                                    }
                                }

                            }
                            _adapter.notifyDataSetChanged();
                            _connectDevice = null;
                            ToastUtil.s(MyDeviceActivity.this, getString(R.string.info_ble_disconnect));

                        }
                    });
                }
            }

            @Override
            public void onConnectTimeOut(String address) {
                if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            dismissConnectingDialog();
                            if (MyDeviceActivity.this != null) {
                                ToastUtil.s(MyDeviceActivity.this, MyDeviceActivity.this.getString(R.string.info_ble_connect_timeout));
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
                if (!BaseAppUtil.isDestroy(MyDeviceActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            dismissConnectingDialog();
                            if (MyDeviceActivity.this != null) {
                                ToastUtil.s(MyDeviceActivity.this, message);
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectSuccess(Device device, int status) {  //
                AppTrace.d(TAG, "onConnect status=" + status);
                try {
                    BindDeviceBean bindDeviceBean = null;
                    if (_dataList != null && _dataList.size() > 0) {
                        boolean isValid = false;
                        for (int i = 0; i < _dataList.size(); i++) {
                            BindDeviceBean blue = _dataList.get(i);
                            if (blue.bluetoothUuid.equals(_connectDevice.getAddress())) {
                                blue.status = Constants.DEVICE_STATUS_CONNECT;
                                isValid = true;
                                bindDeviceBean = blue;
                                break;
                            }
                        }
                        if (isValid) {
                            //更新设备的蓝牙连接状态
                            //_connectDevice.status = Constants.DEVICE_STATUS_CONNECT;
                            //添加本地记录
                            //BluetoothManager.mInstance.updateBindDeviceStatus(_connectDevice);
                            final String mac = bindDeviceBean.mac;
                            _handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    _adapter.notifyDataSetChanged();
                                    String loginToken = LoginManager.getUserToken(MyDeviceActivity.this);
                                    BluetoothManager.mInstance.sendCmdSetToken(loginToken);
                                }
                            }, 1000);
                        }
                    }
                }catch (Throwable e){

                }
            }
        };

    }

    @Override
    protected void initData() {
        BluetoothHandle.addConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.addScanNotifyListener(_scanNotifyListener);
        BluetoothHandle.addDeviceInfoNotifyListener(this);
        BluetoothHandle.addLoginTokenNotifyListener(this);

        String[] permissions = PermissionsUtil.checkPermission(MyDeviceActivity.this);
        if (permissions.length != 0) {
            requestPermission();
        }

        getDeviceList();

    }

    private String getDeviceMac(String address) {
        if (_dataList != null && _dataList.size() > 0) {
            for (BindDeviceBean blue : _dataList) {
                if (address.equalsIgnoreCase(blue.bluetoothUuid)) {
                    return blue.mac;
                }
            }
        }

        return "";

    }

    private void showUnbindDialog(String mac) {
        if (_unbindDialog != null && _unbindDialog.isShowing()) {
            _unbindDialog.dismiss();
        }
        _unbindDialog = null;

        _unbindDialog = new MobileUnbindDialog(MyDeviceActivity.this, mac);
        _unbindDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        _unbindDialog.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (_adapter != null) {
            _adapter.onDestroy();
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        unregisterScanBroadcast();

        BluetoothHandle.removeScanNotifyListener(_scanNotifyListener);

        BluetoothHandle.removeConnectNotifyListener(_connectNotifyListener);

        BluetoothHandle.removeDeviceInfoNotifyListener(this);
        BluetoothHandle.removeLoginTokenNotifyListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoveDevice(RemoveDeviceEvent event) {
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (_dataList != null && _dataList.size() > 0) {
                        for (BindDeviceBean deviceBean : _dataList) {
                            if (deviceBean.mac.equalsIgnoreCase(event.mac)) {
                                _dataList.remove(deviceBean);

                                break;
                            }
                        }
                        _adapter.notifyDataSetChanged();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
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


    private void showLoadingDialog(String message) {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
            _bluetoothLoadingDialog = null;
        }
        _bluetoothLoadingDialog = new BluetoothLoadingDialog(MyDeviceActivity.this);
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
            _connectingDialog = null;
        }
        _connectingDialog = new BluetoothLoadingDialog(MyDeviceActivity.this);
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
        _notFoundDialog = new ModalDialog(MyDeviceActivity.this);
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

                showLoadingDialog(MyDeviceActivity.this.getString(R.string.loading_dialog_search_device));
            }
        });

        _notFoundDialog.show();
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
                BluetoothManager.mInstance.openBluetooth(MyDeviceActivity.this, false);
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

    private void getDeviceList() {
        if (_dataList != null && _dataList.size() > 0 && _dataList.size() >= _total) {
            _refreshLayout.setEnableLoadMore(false);
            return;
        }

        if (_isRequest) {
            return;
        }
        _pageIndex++;
        _isRequest = true;

        BindDeviceInteract.getDeviceList(MyDeviceActivity.this, _pageIndex, Constants.BIND_DEVICE_PAGE_NUMBER, new BindDeviceInteract.IBindDeviceListener() {
            @Override
            public void onDeviceList(BindDeviceResultBean data) {
                _isRequest = false;
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (_pageIndex == 1) {
                                if (_dataList != null) {
                                    _dataList.clear();
                                }
                            }
                            if (data != null && data.rows != null && data.rows.size() > 0) {
                                _dataList.addAll(data.rows);
                                _adapter.notifyDataSetChanged();
                            }
                            if (!isScan) {
                                if (BluetoothManager.mInstance != null) {
                                    BluetoothManager.mInstance.startScan(true);
                                }
                            }
                            _total = data.total;

                            _refreshLayout.setEnableLoadMore(_dataList == null || _dataList.size() <= 0 || _dataList.size() < _total);

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFail(String code, String msg) {

            }

            @Override
            public void onFinish() {
                _isRequest = false;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
//                        _refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
        if (!BaseAppUtil.isDestroy(MyDeviceActivity.this) && BaseAppUtil.isForegroundActivity(MyDeviceActivity.this, MyDeviceActivity.class.getName())) {
            if (_connectDevice != null) {
                data.device_name = _connectDevice.name;
            }
            PlayUtil.startPlay(MyDeviceActivity.this, data, _connectDevice);
        }
    }

    @Override
    public void onDeviceInfoFail() {

    }

    @Override
    public void onSyncToken() {
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String mac = getDeviceMac(_connectDevice.address);
                    if (!BaseAppUtil.isDestroy(MyDeviceActivity.this) && BaseAppUtil.isForegroundActivity(MyDeviceActivity.this, MyDeviceActivity.class.getName())) {
                        showUnbindDialog(mac);
                    }
                } catch (Throwable e) {

                }
            }
        });

    }

    @Override
    public void onSyncTokenFail(String msg) {

    }
}
