package com.xh.hotme.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.MainActivity;
import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.active.ActiveListActivity;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.bean.BindDeviceBean;
import com.xh.hotme.bean.BindDeviceResultBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.HomeDataBean;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleConnectListener;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleScanNotifyListener;
import com.xh.hotme.camera.IjkPlayerActivity;
import com.xh.hotme.camera.PlayerActivity;
import com.xh.hotme.device.BindDeviceInteract;
import com.xh.hotme.device.HomeBaseAdapter;
import com.xh.hotme.event.DeviceEvent;
import com.xh.hotme.event.LoginEvent;
import com.xh.hotme.event.RemoveDeviceEvent;
import com.xh.hotme.event.RenameEvent;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.PlayUtil;
import com.xh.hotme.widget.BluetoothListDialog;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.ModalDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends BaseFragment implements View.OnClickListener, IBleDeviceInfoNotifyListener {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private ImageView mAddButton, refreshButton;
    private TextView mTipView;
    RecyclerView recyclerView;

    HomeBaseAdapter mAdapter;

    List<HomeDataBean> mDataList = new ArrayList<>();

    HomeDataBean deviceBean;

    List<BindDeviceBean> _dataList = new ArrayList<>();
    IBleScanNotifyListener _scanNotifyListener;
    IBleConnectListener _connectNotifyListener;

    Device _connectDevice;
    DeviceInfo _connectDeviceInfo;

    Handler _handler;

    boolean isScan = false;

    boolean isBackgroundScan = true;

    BluetoothListDialog _listDialog;

    BluetoothLoadingDialog _bluetoothLoadingDialog;

    ModalDialog _notFoundDialog;


    private String str_search_device;
    private String str_connect_device;


    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            dismissLoadingDialog();
        }
    };

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        str_search_device = getActivity().getString(R.string.loading_dialog_search_device);
        str_connect_device = getActivity().getString(R.string.loading_dialog_connect_device);


        _handler = new Handler();
        _scanNotifyListener = new IBleScanNotifyListener() {
            @Override
            public void onScanning(boolean isBackground) {

                if (!isBackground) {
                    if (!BaseAppUtil.isDestroy(getActivity()) && BaseAppUtil.isForegroundActivity(getActivity(), getActivity().getClass().getName())) {
                        _bluetoothLoadingDialog = new BluetoothLoadingDialog(getActivity());
                        _bluetoothLoadingDialog.setMessage(getString(R.string.loading_dialog_search_device));
                        _bluetoothLoadingDialog.show();
                    }
                }
                isBackgroundScan = isBackground;
            }

            @Override
            public void onLeScan(Device device) {
                //隐藏加载框
                dismissLoadingDialog();
                for (BindDeviceBean blue : _dataList) {
                    if (!TextUtils.isEmpty(blue.bluetoothUuid) && blue.bluetoothUuid.equalsIgnoreCase(device.getAddress())) {
                        if (blue.blueDevice == null) {
                            blue.blueDevice = device;
                        }
                        if (blue.status == Constants.DEVICE_STATUS_OFFLINE) {
                            blue.status = Constants.DEVICE_STATUS_ONLINE;
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onTimeout() {
                AppTrace.d("扫描超时");

                dismissLoadingDialog();
            }

            @Override
            public void onScanEnd() {
                AppTrace.d("扫描结束");

                dismissLoadingDialog();
            }
        };

        _connectNotifyListener = new IBleConnectListener() {
            @Override
            public void onConnecting(Device device) {
                AppTrace.d(TAG, "onConnectStart....");
                _connectDevice = device;
            }

            @Override
            public void onConnectSuccess(Device device, int status) {  //
                AppTrace.d(TAG, "onConnect status=" + status);
                // ToastUtil.s(getContext(), "连接" + (status == 0 ? "成功" : "失败"));
                if (_connectDevice == null) {
                    return;
                }

                if (_dataList != null && _dataList.size() > 0) {
                    boolean isValid = false;
                    for (int i = 0; i < _dataList.size(); i++) {
                        BindDeviceBean blue = _dataList.get(i);
                        if (_connectDevice != null && blue.bluetoothUuid.equals(_connectDevice.getAddress())) {
                            blue.status = Constants.DEVICE_STATUS_CONNECT;
                            isValid = true;
                            break;
                        }
                    }
                    if (isValid) {
                        //更新设备的蓝牙连接状态
                        _connectDevice.status = Constants.DEVICE_STATUS_CONNECT;
                        //添加本地记录
                        BluetoothManager.mInstance.updateBindDeviceStatus(_connectDevice);

                        List<BindDeviceBean> deviceList = BluetoothManager.getBindDeviceList();
                        if (deviceList != null) {
                            deviceBean.setDataList(deviceList);
                        }

                        _handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (_bleScanRequestListener != null) {
                                    _bleScanRequestListener.onScanStop(status == Constants.DEVICE_STATUS_CONNECT ? 1 : 0);
                                }

                                mAdapter.notifyDataSetChanged();

                                if (!BaseAppUtil.isDestroy(getActivity()) && BaseAppUtil.isForegroundActivity(getActivity(), MainActivity.class.getName())) {
                                    showLoadingDialog(str_connect_device);

                                    BluetoothManager.mInstance.sendCmdDeviceInfo();
                                    _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                                }

                            }
                        }, BleConstants.BLE_CONNECTED_DELAY);
                    }
                }
            }

            @Override
            public void onConnectFailure(String address, String error) {

            }

            @Override
            public void onDisConnecting(String address) {

            }

            @Override
            public void onDisConnectSuccess(String address, int status) {

            }

            @Override
            public void onConnectTimeOut(String address) {

            }

            @Override
            public void onServiceDiscoverySucceed(int status) {

            }

            @Override
            public void onServiceDiscoveryFailed(String message) {

            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
        AppTrace.d(TAG, "onStart");
        BluetoothHandle.addConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.addScanNotifyListener(_scanNotifyListener);
        BluetoothHandle.addDeviceInfoNotifyListener(this);

        if (!isScan) {
            if (BluetoothManager.mInstance != null) {
                BluetoothManager.mInstance.startScan(true);
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        AppTrace.d(TAG, "onStop");

        BluetoothHandle.removeConnectNotifyListener(_connectNotifyListener);

        BluetoothHandle.removeScanNotifyListener(_scanNotifyListener);
        BluetoothHandle.removeDeviceInfoNotifyListener(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
                                _total--;
                                break;
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshDevice(final DeviceEvent event) {

        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BindDeviceBean> deviceList = BluetoothManager.getBindDeviceList();
                    if (deviceList != null) {
                        deviceBean.setDataList(deviceList);
                    }

                    mAdapter.notifyDataSetChanged();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRenameDevice(final RenameEvent event) {

        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<BindDeviceBean> deviceList = BluetoothManager.getBindDeviceList();
                    if (deviceList != null) {
                        deviceBean.setDataList(deviceList);
                    }

                    mAdapter.notifyDataSetChanged();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment_default, container, false);
        mAddButton = rootView.findViewById(R.id.iv_add_device);
        refreshButton = rootView.findViewById(R.id.iv_refresh);
        mTipView = rootView.findViewById(R.id.tv_camera_tip);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mAddButton.setOnClickListener(this);
        mTipView.setOnClickListener(this);
        refreshButton.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new HomeBaseAdapter(getActivity(), mDataList);

        mAdapter.setScanRequestListener(_bleScanRequestListener);

        recyclerView.setAdapter(mAdapter);

        loadData();

        mAdapter.notifyDataSetChanged();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        dismissLoadingDialog();

        dismissNotFoundDialog();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        _handler.removeCallbacksAndMessages(null);
        _handler = null;
    }

    private void loadData() {
        if (mDataList == null) {
            mDataList = new ArrayList<>();
        }
        deviceBean = new HomeDataBean();
        deviceBean.setType(0);

        List<BindDeviceBean> bleDeviceList = BluetoothManager.getBindDeviceList();
        if (bleDeviceList != null && bleDeviceList.size() > 0) {
            for (BindDeviceBean device : bleDeviceList) {
                device.setStatus(Constants.DEVICE_STATUS_OFFLINE);
            }
            if (_dataList == null) {
                _dataList = new ArrayList<>();
            }
            _dataList.addAll(bleDeviceList);
        }

        if (_dataList != null) {
            deviceBean.setDataList(_dataList);
        }

        mDataList.add(deviceBean);

        getDeviceList();
    }

    int _total = -1;

    int _pageIndex = 0;

    boolean _isRequest = false;


    private void getDeviceList() {
        UserInfoBean userInfoBean = LoginManager.getUserLoginInfo(getActivity());
        if (userInfoBean == null) {
            return;
        }


        if (_dataList != null && _dataList.size() > 0 && _dataList.size() >= _total && _total != -1) {
            // _refreshLayout.setEnableLoadMore(false);
            return;
        }

        if (_isRequest) {
            return;
        }
        _pageIndex++;
        _isRequest = true;

        BindDeviceInteract.getDeviceList(getActivity(), _pageIndex, Constants.BIND_DEVICE_PAGE_NUMBER, new BindDeviceInteract.IBindDeviceListener() {
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
                                mAdapter.notifyDataSetChanged();
                            }

                            loadConnectDevice();

                            if (!isScan) {
                                if (BluetoothManager.mInstance != null) {
                                    BluetoothManager.mInstance.startScan(true);
                                }
                            }
                            _total = data.total;

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


    public void loadConnectDevice() {
        Device connected = BluetoothManager.getConnectDevice();
        if (connected != null) {
            _connectDevice = connected;
            reloadConnectedDeviceStatus();
        }
    }

    private void reloadConnectedDeviceStatus() {
        if (_dataList != null && _dataList.size() > 0) {
            boolean isValid = false;
            for (int i = 0; i < _dataList.size(); i++) {
                BindDeviceBean blue = _dataList.get(i);
                if (_connectDevice != null && blue.bluetoothUuid.equals(_connectDevice.getAddress())) {
                    blue.status = Constants.DEVICE_STATUS_CONNECT;
                    isValid = true;
                    break;
                }
            }
            if (isValid) {
                //更新设备的蓝牙连接状态
                _connectDevice.status = Constants.DEVICE_STATUS_CONNECT;
                //添加本地记录
                BluetoothManager.mInstance.updateBindDeviceStatus(_connectDevice);

                List<BindDeviceBean> deviceList = BluetoothManager.getBindDeviceList();
                if (deviceList != null) {
                    deviceBean.setDataList(deviceList);
                }
                _handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mAdapter.notifyDataSetChanged();

                    }
                }, 1000);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_device:

                ActiveListActivity.start(getActivity());

                break;
            case R.id.tv_camera_tip:

                break;

            case R.id.iv_refresh:
                if (!isScan) {
                    if (BluetoothManager.mInstance != null) {
                        BluetoothManager.mInstance.startScan(false);
                    }
                }
                break;
        }
    }

    private void showLoadingDialog(String message) {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
            _bluetoothLoadingDialog = null;
        }
        _bluetoothLoadingDialog = new BluetoothLoadingDialog(getActivity());
        _bluetoothLoadingDialog.setMessage(message);
        _bluetoothLoadingDialog.show();
    }


    private void dismissLoadingDialog() {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
        }
        _bluetoothLoadingDialog = null;
    }

    private void showBluetoothListDialog(List<Device> deviceList) {
        if (_listDialog != null && _listDialog.isShowing()) {
            _listDialog.dismiss();
            _listDialog = null;
        }

        _listDialog = new BluetoothListDialog(getActivity(), deviceList);
        _listDialog.setScanListListener(_bleScanRequestListener);
        _listDialog.show();
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

                if (_bleScanRequestListener != null) {
                    _bleScanRequestListener.onScanStart(false);
                }
                showLoadingDialog(getActivity().getString(R.string.loading_dialog_search_device));
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
    public void onDeviceInfo(DeviceInfo data) {
        dismissLoadingDialog();
        _connectDeviceInfo = data;
        if (!BaseAppUtil.isDestroy(getActivity()) && BaseAppUtil.isForegroundActivity(getActivity(), getActivity().getClass().getName())) {
            if (_connectDevice != null) {
                data.device_name = _connectDevice.name;
            }
            PlayUtil.startPlay(getActivity(), data, _connectDevice);

//            BlueTestActivity.start(getActivity());
        }
    }

    @Override
    public void onDeviceInfoFail() {
        dismissLoadingDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshDevice(LoginEvent event) {
        _pageIndex = 0;
        getDeviceList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void renameDevice(RenameEvent event) {
        try {
            if (_dataList != null) {
                for (BindDeviceBean device : _dataList) {
                    if (event != null && device != null && device.bluetoothUuid != null && device.bluetoothUuid.equalsIgnoreCase(event.address)) {
                        device.deviceName = event.name;
                        break;
                    }
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        } catch (Throwable e) {

        }
    }
}
