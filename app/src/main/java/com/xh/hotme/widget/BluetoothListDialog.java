package com.xh.hotme.widget;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.xh.hotme.R;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapter;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleConnectListener;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleScanNotifyListener;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.event.RenameEvent;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.PlayUtil;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Keep
public class BluetoothListDialog extends Dialog implements IBleDeviceInfoNotifyListener {
    private static final String TAG = "BluetoothListDialog";

    // views
    ImageView _refreshIv;
    private final LinearLayout _refreshButton;
    private final ImageView _cancelButton;

    // listener
    private OnClickListener _listener;


    private IBleScanRequestListener _scanRequestListener;

    BluetoothDeviceAdapter _adapter;
    RecyclerView _listView;

    List<Device> _dataList = new ArrayList<>();
    IBleScanNotifyListener _scanNotifyListener;
    IBleConnectListener _connectNotifyListener;

    Device _connectDevice;

    Handler _handler;

    boolean isScanning = false;

    Activity _context;


    ObjectAnimator _refreshAnimator;

    public BluetoothListDialog(@NonNull final Activity context, List<Device> dataList) {
        super(context, R.style.hotme_custom_dialog);

        _context = context;
        _handler = new Handler();

        if (dataList != null) {
            this._dataList.addAll(dataList);
        }

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_bluetooth_list, null);

        // views
        _refreshIv = view.findViewById(R.id.iv_refresh);
        _refreshButton = view.findViewById(R.id.refresh);
        _cancelButton = view.findViewById(R.id.iv_close);
        _listView = view.findViewById(R.id.recyclerView);


        _refreshAnimator = ObjectAnimator.ofFloat(_refreshIv, "rotation", 1080f);
        _refreshAnimator.setDuration(2000L);
        _refreshAnimator.setRepeatCount(-1);

        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onClick(BluetoothListDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
                dismiss();
                return true;
            }
        });

//		// ok button
        _refreshButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_scanRequestListener != null) {
                    _scanRequestListener.onScanStart(true);
                }
                return true;
            }
        });

        _listView.setLayoutManager(new GridLayoutManager(context, 1));
        _listView.addItemDecoration(new GridSpacingItemDecoration(1, DensityUtil.dip2px(context, 10), false));

        _adapter = new BluetoothDeviceAdapter(context, _dataList);

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
                if (_scanRequestListener != null) {
                    _scanRequestListener.onBluetoothDeviceClick(device, position);
                }
            }

            @Override
            public void onRemoveDevice(Device device, int position) {

            }

            @Override
            public void openDevice(Device device) {

            }
        });


        _listView.setAdapter(_adapter);

        _scanNotifyListener = new IBleScanNotifyListener() {
            @Override
            public void onScanning(boolean isBackground) {
                if (_refreshAnimator != null) {
                    _refreshAnimator.start();
                }
            }

            @Override
            public void onLeScan(Device device) {
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
                if (_refreshAnimator != null) {
                    _refreshAnimator.end();
                }
            }

            @Override
            public void onScanEnd() {
                AppTrace.d("扫描结束");
                if (_refreshAnimator != null) {
                    _refreshAnimator.end();
                }
            }
        };

        _connectNotifyListener = new IBleConnectListener() {

            @Override
            public void onConnecting(Device device) {
                AppTrace.d(TAG, "onConnectStart....");
                _connectDevice = device;
            }

            @Override
            public void onConnectFailure(String address, String error) {
                if (getContext() != null) {
                    ToastUtil.s(getContext(), getContext().getString(R.string.info_ble_connect_fail));
                }
            }

            @Override
            public void onDisConnecting(String address) {

            }

            @Override
            public void onDisConnectSuccess(String address, int status) {
                if (!BaseAppUtil.isDestroy(getOwnerActivity())) {
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
                            if (getContext() != null) {
                                ToastUtil.s(getContext(), getContext().getString(R.string.info_ble_disconnect));
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectTimeOut(String address) {
                if (getContext() != null) {
                    ToastUtil.s(getContext(), getContext().getString(R.string.info_ble_connect_timeout));
                }
            }

            @Override
            public void onServiceDiscoverySucceed(int status) {

            }

            @Override
            public void onServiceDiscoveryFailed(String message) {
                if (getContext() != null) {
                    ToastUtil.s(getContext(), message);
                }
            }

            @Override
            public void onConnectSuccess(Device device, int status) {  //
                AppTrace.d(TAG, "onConnect status=" + status);
                // ToastUtil.s(getContext(), "连接" + (status == 0 ? "成功" : "失败"));

                for (Device blue : _dataList) {
                    if (blue.getAddress().equals(_connectDevice.getAddress())) {
                        blue.status = Constants.DEVICE_STATUS_CONNECT;
                        break;
                    }
                }
                //更新设备的蓝牙连接状态
                _connectDevice.status = status;
                //添加本地记录
                BluetoothManager.mInstance.updateConnectDeviceStatus(_connectDevice);

                _handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _adapter.notifyDataSetChanged();

                        BluetoothManager.mInstance.sendCmdNetStatus();
//                        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);

                    }
                }, 1000);

            }
        };

        // set content view
        setContentView(view);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams windowparams = window.getAttributes();
        windowparams.width = BaseAppUtil.getDeviceWidth(context);
        float topHeight = getContext().getResources().getDimension(R.dimen.dialog_margin_top);
        windowparams.height = (int) (BaseAppUtil.getDeviceHeight(context) - DensityUtil.dip2px(context, topHeight));


    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        BluetoothHandle.addScanNotifyListener(_scanNotifyListener);
        BluetoothHandle.addConnectNotifyListener(_connectNotifyListener);
        BluetoothHandle.addDeviceInfoNotifyListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        BluetoothHandle.removeScanNotifyListener(_scanNotifyListener);

        BluetoothHandle.removeConnectNotifyListener(_connectNotifyListener);
    }

    public void setOnClickListener(OnClickListener listener) {
        _listener = listener;
    }


    public void setScanListListener(IBleScanRequestListener l) {
        _scanRequestListener = l;
    }

    public IBleScanRequestListener getScanListListener() {
        return _scanRequestListener;
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
        String deviceInfo = new Gson().toJson(data);
        AppTrace.d(TAG, "onDeviceInfo: " + deviceInfo);

        String mobile = LoginManager.getMobile(getContext());
        if (data.active_status == 3 && !TextUtils.isEmpty(data.account) && mobile.equalsIgnoreCase(data.account)) {
            if (!BaseAppUtil.isDestroy(_context) && BaseAppUtil.isForegroundActivity(_context, _context.getClass().getName())) {
                PlayUtil.startPlay(getContext(), data, _connectDevice);
            }
        } else if (data.active_status == 2) {

            BluetoothManager.mInstance.sendCmdNetStatus();
//            _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
//            dismissConnectingDialog();
        }

    }

    @Override
    public void onDeviceInfoFail() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void renameDevice(RenameEvent event) {
        try {
            if (_dataList != null) {
                for (Device device : _dataList) {
                    if (event != null && device != null && device.address != null && device.address.equalsIgnoreCase(event.address)) {
                        device.name = event.name;
                        break;
                    }
                }
                _adapter.notifyDataSetChanged();
            }

        } catch (Throwable e) {

        }

    }
}
