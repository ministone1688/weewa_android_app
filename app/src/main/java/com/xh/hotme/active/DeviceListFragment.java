package com.xh.hotme.active;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapter;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleConnectListener;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleNetworkInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleScanNotifyListener;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.device.ConnectActivity;
import com.xh.hotme.event.DeviceEvent;
import com.xh.hotme.event.RenameEvent;
import com.xh.hotme.softap.CameraSoftApActivity;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.PermissionsUtil;
import com.xh.hotme.utils.PlayUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.GridSpacingItemDecoration;
import com.xh.hotme.widget.ModalDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by GJK on 2018/11/9.
 */

public class DeviceListFragment extends BaseFragment implements IBleScanRequestListener, IBleNetworkInfoNotifyListener, IBleDeviceInfoNotifyListener {
    private static final String TAG = DeviceListFragment.class.getSimpleName();
    RecyclerView recyclerView;

    BluetoothDeviceAdapter _adapter;

    List<Device> _dataList = new ArrayList<>();
    IBleScanNotifyListener _scanNotifyListener;
    IBleConnectListener _connectNotifyListener;

    Device _connectDevice;

    Handler _handler;

    boolean isScanning = false;

    BluetoothLoadingDialog _connectingDialog;

    ModalDialog _notFoundDialog;

    Device _readyDevice;

    boolean isBackgroundScan = false;

    private String str_search_device;
    private String str_connect_device;

    private String str_connect_camera;
    private String str_connect_load_device_info;
    private String str_connect_load_device_network;


    private int type = 0;
    public static int type_active = 0;
    public static int type_soft_ap = 11;
    public static int type_test = 99;

    NetworkBean _networkBean;
    DeviceInfo _deviceInfo;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            MainHandler.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.s(getActivity(), "获取相机信息失败！");
                }
            });
            dismissConnectingDialog();
        }
    };

    public static DeviceListFragment newInstance() {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VIDEO_CATEGORY, type_active);
        fragment.setArguments(bundle);
        return fragment;
    }


    public static DeviceListFragment newInstance(int type) {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VIDEO_CATEGORY, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getInt(Constants.VIDEO_CATEGORY, type_active);
        }


        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        str_search_device = getActivity().getString(R.string.loading_dialog_search_device);
        str_connect_device = getActivity().getString(R.string.loading_dialog_connect_device);
        str_connect_camera = getActivity().getString(R.string.loading_dialog_connect_camera_image);
        str_connect_load_device_info = getActivity().getString(R.string.loading_dialog_load_device_info);
        str_connect_load_device_network = getActivity().getString(R.string.loading_dialog_load_device_network);

        _handler = new Handler();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BluetoothHandle.removeConnectNotifyListener(_connectNotifyListener);

        BluetoothHandle.removeScanNotifyListener(_scanNotifyListener);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshDevice(final DeviceEvent event) {
        List<Device> deviceList = BluetoothManager.mInstance.getBleDeviceList();
        if (deviceList != null && deviceList.size() > 0) {
            _dataList.addAll(deviceList);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.active_fragment_list, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, DensityUtil.dip2px(getActivity(), 10), false));

        loadData();

        _adapter = new BluetoothDeviceAdapter(getActivity(), _dataList);

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
                if (type == type_active) {
                    if (_networkBean != null) {
                        if (_networkBean != null && _networkBean.getWifi() != null && (_networkBean.getWifi().ip.isEmpty() || _networkBean.getWifi().ip.startsWith("169.254"))) {
                            ConnectActivity.start(getActivity(), _connectDevice, Constants.ACTIVE_STEP_SETUP_WIFI);
                            getActivity().finish();
                        } else {
                            ConnectActivity.start(getActivity(), _connectDevice, Constants.ACTIVE_STEP_ACTIVE);
                            getActivity().finish();
                        }
                    } else {
                        BluetoothManager.mInstance.sendCmdNetStatus();
                        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                        showConnectingDialog(getString(R.string.loading_dialog_connect_device));
                    }
                } else {
                    getDeviceInfo();
                }
            }
        });

        recyclerView.setAdapter(_adapter);

        _scanNotifyListener = new IBleScanNotifyListener() {
            @Override
            public void onScanning(boolean isBackground) {
                if (!isBackground) {
                    showConnectingDialog(getString(R.string.loading_dialog_search_device));
                }
                isBackgroundScan = isBackground;
            }

            @Override
            public void onLeScan(Device device) {
                dismissConnectingDialog();
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
                dismissConnectingDialog();

                if (_connectDevice == null) {
                    return;
                }
                if (!isBackgroundScan) {
                    if (_dataList == null || _dataList.size() == 0) {
                        showNotFoundDialog();
                    }
                }
            }

            @Override
            public void onScanEnd() {
                dismissConnectingDialog();
            }
        };

        _connectNotifyListener = new IBleConnectListener() {

            @Override
            public void onConnecting(Device device) {
                AppTrace.d(TAG, "onConnectStart....");
                _connectDevice = device;
                showConnectingDialog(getString(R.string.loading_dialog_connect_device));
            }

            @Override
            public void onConnectFailure(String address, String error) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {

                        dismissConnectingDialog();
                        if (getContext() != null) {
                            ToastUtil.s(getContext(), getContext().getString(R.string.info_ble_connect_fail));
                        }
                    }
                });
            }

            @Override
            public void onDisConnecting(String address) {

            }

            @Override
            public void onDisConnectSuccess(String address, int status) {
                if (!BaseAppUtil.isDestroy(getActivity())) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (_connectDevice != null && _connectDevice.getAddress().equalsIgnoreCase(address)) {
                                for (Device blue : _dataList) {
                                    if (blue.getAddress().equals(_connectDevice.getAddress())) {
                                        blue.status = Constants.DEVICE_STATUS_ONLINE;
                                        break;
                                    }
                                }

                            }
                            _adapter.notifyDataSetChanged();
                            if (getContext() != null) {
                                ToastUtil.s(getContext(), getContext().getString(R.string.info_ble_disconnect));
                            }
                            _connectDevice = null;
                            _networkBean = null;
                        }
                    });
                }
            }

            @Override
            public void onConnectTimeOut(String address) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {

                        dismissConnectingDialog();
                        if (getContext() != null) {
                            ToastUtil.s(getContext(), getContext().getString(R.string.info_ble_connect_timeout));
                        }
                    }
                });
            }

            @Override
            public void onServiceDiscoverySucceed(int status) {

            }

            @Override
            public void onServiceDiscoveryFailed(String message) {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {

                        dismissConnectingDialog();
                        if (getContext() != null) {
                            ToastUtil.s(getContext(), message);
                        }
                    }
                });
            }

            @Override
            public void onConnectSuccess(Device device, int status) {  //
                AppTrace.d(TAG, "onConnect status=" + status);
                // ToastUtil.s(getContext(), "连接" + (status == 0 ? "成功" : "失败"));

                for (Device blue : _dataList) {
                    if (_connectDevice != null && blue.getAddress().equals(_connectDevice.getAddress())) {
                        blue.status = Constants.DEVICE_STATUS_CONNECT;
                        break;
                    }
                }
                //更新设备的蓝牙连接状态
                if (_connectDevice != null) {
                    _connectDevice.status = Constants.DEVICE_STATUS_CONNECT;
                    //添加本地记录
                    BluetoothManager.mInstance.updateConnectDeviceStatus(_connectDevice);
                }

                _networkBean = null;
                _deviceInfo = null;
                isBackgroundScan = true;
                _handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _adapter.notifyDataSetChanged();
                        getDeviceInfo();

                    }
                }, BleConstants.BLE_CONNECTED_DELAY);
            }
        };

        BluetoothHandle.addConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.addNetworkNotifyListener(this);
        BluetoothHandle.addDeviceInfoNotifyListener(this);

        BluetoothHandle.addScanNotifyListener(_scanNotifyListener);
        if (!BluetoothManager.mInstance.enableBlueTooth()) {
            BluetoothManager.mInstance.openBluetooth(getActivity(), false);
        } else {
            if (PermissionsUtil.hasBlePermission(getActivity())) {
                if (!isScanning) {
                    if (BluetoothManager.mInstance != null) {
                        BluetoothManager.mInstance.startScan(false);
                    }
                }
            }
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        BluetoothHandle.removeNetworkNotifyListener(this);
        BluetoothHandle.removeConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.removeScanNotifyListener(_scanNotifyListener);
        BluetoothHandle.removeDeviceInfoNotifyListener(this);

        dismissConnectingDialog();

        dismissNotFoundDialog();

        _handler = null;

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    public void onRefresh() {
        if (isScanning) {
            ToastUtil.s(getContext(), getString(R.string.loading_dialog_search_device));
            return;
        }

        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.startScan(false);
        }
    }

    private void loadData() {
        AppTrace.d(TAG, "load data");
        List<Device> deviceList = BluetoothManager.mInstance.getBleDeviceList();
        if (deviceList != null && deviceList.size() > 0) {
            _dataList.addAll(deviceList);
        }
    }

    private void showConnectingDialog(String message) {

        if (!BaseAppUtil.isDestroy(getActivity())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (_connectingDialog != null && _connectingDialog.isShowing()) {
                        _connectingDialog.dismiss();
                        _connectingDialog = null;
                    }
                    _connectingDialog = new BluetoothLoadingDialog(getActivity());
                    _connectingDialog.setMessage(message);
                    _connectingDialog.show();
                }
            });
        }
    }

    private void updateConnectMessage(String message) {
        if (!BaseAppUtil.isDestroy(getActivity())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (_connectingDialog != null && _connectingDialog.isShowing()) {
                        _connectingDialog.setMessage(message);
                    }
                }
            });
        }
    }

    private void dismissConnectingDialog() {
        if (!BaseAppUtil.isDestroy(getActivity())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (_connectingDialog != null && _connectingDialog.isShowing()) {
                        _connectingDialog.dismiss();
                    }
                    _connectingDialog = null;
                }
            });
        }
    }


    private void showNotFoundDialog() {
        _notFoundDialog = new ModalDialog(getActivity());
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

                showConnectingDialog(getActivity().getString(R.string.loading_dialog_search_device));
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
    public void onScanStart(boolean isBackground) {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.startScan(isBackground);
        }
    }

    @Override
    public void onScan(Device device, int position) {

    }

    @Override
    public void onScanStop(int size) {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.stopScan();
        }
    }

    public void connectDevice(Device device, int position) {
        onBluetoothDeviceClick(device, position);
    }

    @Override
    public void onBluetoothDeviceClick(Device device, int position) {
        isBackgroundScan = true;
//        if (!NetUtil.isNetworkAvailable(getActivity())) {
//            ModalDialog dialog = new ModalDialog(getActivity(), getString(R.string.info_network_error_not_open), getString(R.string.info_network_error_open_network), false);
//            dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
//                @Override
//                public boolean onClicked() {
//                    dialog.dismiss();
//                    return true;
//                }
//            });
//            dialog.setRightButton(getString(R.string.setting), new ClickGuard.GuardedOnClickListener() {
//                @Override
//                public boolean onClicked() {
//                    HotmeWiFiManager.startWifiSettingPage(getActivity());
//                    dialog.dismiss();
//                    return true;
//                }
//            });
//            dialog.show();
//            return;
//        }

        _readyDevice = null;
        if (BluetoothManager.mInstance != null) {
            if (!BluetoothManager.mInstance.enableBlueTooth()) {
                BluetoothManager.mInstance.openBluetooth(getActivity(), false);
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
                BluetoothManager.mInstance.openBluetooth(getActivity(), false);
                return;
            }
        }
        updateConnectMessage(str_connect_load_device_info);

        BluetoothManager.mInstance.sendCmdDeviceInfo();
        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
    }

    @Override
    public void onNetworkStatus(NetworkBean networkBean) {
        dismissConnectingDialog();
        _handler.removeCallbacks(_timeOutRunnable);
        _networkBean = networkBean;
        if (!BaseAppUtil.isDestroy(getActivity()) && BaseAppUtil.isForegroundActivity(getActivity(), getActivity().getClass().getName())) {
            if (type == type_active) {
                if (networkBean != null && networkBean.getWifi() != null && (networkBean.getWifi().ip.isEmpty() || networkBean.getWifi().ip.startsWith("169.254"))) {
                    ConnectActivity.start(getActivity(), _connectDevice, Constants.ACTIVE_STEP_SETUP_WIFI);
                    getActivity().finish();
                } else {
                    ConnectActivity.start(getActivity(), _connectDevice, Constants.ACTIVE_STEP_ACTIVE);
                    getActivity().finish();
                }
            } else if (type == type_soft_ap) {
                String ssid = "";
                String password = "";
                int status = 0;
                if (networkBean.getAp() != null) {
                    ssid = networkBean.getAp().ssid;
                    password = networkBean.getAp().password;
                    status = networkBean.getAp().st;
                }
                CameraSoftApActivity.start(getActivity(), ssid, password, status, CameraSoftApActivity.REQUEST_SOFT_AP_TRANSFER);
            } else if (type == type_test) {
                PlayUtil.startPlay(getActivity(), _deviceInfo, _connectDevice, networkBean);
            }
        }
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
//        dismissConnectingDialog();
        _handler.removeCallbacks(_timeOutRunnable);
        _deviceInfo = data;
//        if (type == type_soft_ap) {
//        if (!BaseAppUtil.isDestroy(getActivity()) && BaseAppUtil.isForegroundActivity(getActivity(), getActivity().getClass().getName())) {
//            PlayUtil.startPlay(getActivity(), data, _connectDevice);
//            }
//        }

        showConnectingDialog(getString(R.string.loading_dialog_connect_device));

        updateConnectMessage(str_connect_load_device_network);
        BluetoothManager.mInstance.sendCmdNetStatus();
        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);

    }

    @Override
    public void onDeviceInfoFail() {
        dismissConnectingDialog();
        _handler.removeCallbacks(_timeOutRunnable);

        ToastUtil.s(getActivity(), "获取设备信息失败，请稍后重试");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void renameDevice(RenameEvent event) {
        if (_dataList != null) {
            for (Device device : _dataList) {
                if (event != null && device != null && device.address != null && device.address.equalsIgnoreCase(event.address)) {
                    device.name = event.name;
                    break;
                }
            }
            _adapter.notifyDataSetChanged();
        }
    }
}
