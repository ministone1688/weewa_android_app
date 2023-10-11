package com.xh.hotme.softap;


import static com.xh.hotme.utils.Constants.REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleSimEnableNotifyListener;
import com.xh.hotme.bluetooth.WifiListAdapterCallback;
import com.xh.hotme.databinding.ActivityCameraNetworkSettingBinding;
import com.xh.hotme.device.ConnectActivity;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DialogUtil;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.wifi.IWifiNotifyListener;
import com.xh.hotme.wifi.WifiInfo;
import com.xh.hotme.wifi.WifiListDialog;
import com.xh.hotme.wifi.WifiPasswordDialog;

import java.util.List;


public class CameraNetworkSettingActivity extends BaseViewActivity<ActivityCameraNetworkSettingBinding> implements IWifiNotifyListener, IBleSimEnableNotifyListener {
    private static final String TAG = CameraNetworkSettingActivity.class.getSimpleName();

    private int reqestCode = -1;

    private String info_soft_ap_ssid_is_empty = "";
    private String info_soft_ap_password_is_empty = "";

    String _ssid;
    boolean _simCard;

    BluetoothLoadingDialog _bluetoothLoadingDialog;


    WifiListDialog _wifiListDialog;
    WifiPasswordDialog _setupDialog;

    boolean _wifiSetup = false;

    boolean _isOpenSim = false;


    Handler _handler = null;


    int _network_sim = -1;
    int _network_wifi = -1;

    String _wifiSsid;
    String _wifiPassword;


    Device _device;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            DialogUtil.dismissDialog();
        }
    };

    public static void start(Context context, Device device, String ssid, boolean fourG) {
        if (null != context) {
            Intent intent = new Intent(context, CameraNetworkSettingActivity.class);
            intent.putExtra(BleConstants.BLE_KEY_SOFTAP_SSID, ssid);
            intent.putExtra(BleConstants.BLE_KEY_SIM, fourG);
            intent.putExtra(Constants.REQUEST_DEVICE, device);

            context.startActivity(intent);
        }
    }

    public static void startActivityByRequestCode(Activity context, int requestCode) {
        if (null != context) {
            Intent intent = new Intent(context, CameraNetworkSettingActivity.class);
            intent.putExtra(REQUEST_CODE, requestCode);

            context.startActivityForResult(intent, requestCode);
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
    public void onDestroy() {
        super.onDestroy();

        BluetoothHandle.removeWifiNotifyListener(this);
        BluetoothHandle.removeSimEnableNotifyListener(this);

        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void initView() {

        _handler = new Handler();
        viewBinding.titleBar.tvTitle.setText(getString(R.string.setup_wifi));
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                finish();
                return true;
            }
        });

        viewBinding.wifiLayout.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                BluetoothManager.mInstance.sendCmdWifiLists();
                _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);

                showLoadingDialog("");

                return true;
            }
        });

        viewBinding.btnSubmit.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (_network_sim != 1 && _network_wifi != 1) {
                    ToastUtil.s(CameraNetworkSettingActivity.this, getString(R.string.select_network));
                    return true;
                }

                ConnectActivity.start(CameraNetworkSettingActivity.this, _device, Constants.ACTIVE_STEP_ACTIVE);
                finish();
                return true;
            }
        });
    }

    @Override
    protected void initData() {

        if (getIntent() != null) {
            _ssid = getIntent().getStringExtra(BleConstants.BLE_KEY_SOFTAP_SSID);
            _simCard = getIntent().getBooleanExtra(BleConstants.BLE_KEY_SIM, false);
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
        }

        viewBinding.sbFourG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _isOpenSim = isChecked;
                if (!isChecked) {
                    doSimCard(isChecked);
                }

            }
        });

        info_soft_ap_ssid_is_empty = getString(R.string.info_soft_ap_ssid_is_empty);
        info_soft_ap_password_is_empty = getString(R.string.info_soft_ap_password_is_empty);

        BluetoothHandle.addWifiNotifyListener(this);
        BluetoothHandle.addSimEnableNotifyListener(this);
    }

    private void doSimCard(boolean isOpen) {
        if (isOpen) {
            BluetoothManager.mInstance.sendCmdSimOpen();
            _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);

            DialogUtil.showDialog(CameraNetworkSettingActivity.this, "正在打开4G网络");

        } else {
            BluetoothManager.mInstance.sendCmdSimClose();
            _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);

            DialogUtil.showDialog(CameraNetworkSettingActivity.this, "正在关闭4G网络");
        }
    }


    private void showLoadingDialog(String message) {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
            _bluetoothLoadingDialog = null;
        }
        _bluetoothLoadingDialog = new BluetoothLoadingDialog(CameraNetworkSettingActivity.this);
        _bluetoothLoadingDialog.setMessage(message);
        _bluetoothLoadingDialog.show();
    }


    private void dismissLoadingDialog() {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
        }
        _bluetoothLoadingDialog = null;
    }


    @Override
    public void onWifiList(List<WifiInfo> device) {
        _handler.removeCallbacks(_timeOutRunnable);
        _handler.post(new Runnable() {
            @Override
            public void run() {
                dismissLoadingDialog();
                if (_wifiListDialog != null && _wifiListDialog.isShowing()) {
                    _wifiListDialog.dismiss();
                }
                _wifiListDialog = new WifiListDialog(CameraNetworkSettingActivity.this, "网络配置", device, new WifiListAdapterCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onWifiClick(WifiInfo device, int position) {
                        if (_setupDialog != null && _setupDialog.isShowing()) {
                            _setupDialog.dismiss();
                        }
                        _setupDialog = null;

                        _setupDialog = new WifiPasswordDialog(CameraNetworkSettingActivity.this, device.getSsid());
                        _setupDialog.setOnClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    _wifiSsid = device.getSsid();
                                    viewBinding.wifiSsid.setText(_wifiSsid);

                                    _network_wifi = 1;
                                }
                            }
                        });
                        _setupDialog.show();
                    }
                });
                //关闭热点
                BluetoothManager.mInstance.sendCmdSoftapClose();
                _wifiListDialog.show();

            }
        });
    }

    @Override
    public void onSetupStatus(int status, String message) {

    }

    @Override
    public void onSimOpen() {
        if (BaseAppUtil.isDestroy(CameraNetworkSettingActivity.this)) {
            return;
        }
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    DialogUtil.dismissDialog();
                    _network_sim = 1;
                    viewBinding.sbFourG.setChecked(true);
                } catch (Throwable e) {

                }
            }
        });
    }

    @Override
    public void onSimOpenFail(String msg) {
        try {
            if (BaseAppUtil.isDestroy(CameraNetworkSettingActivity.this)) {
                return;
            }
            MainHandler.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DialogUtil.dismissDialog();
                        ToastUtil.s(CameraNetworkSettingActivity.this, "打开网络失败");
                    } catch (Throwable e) {

                    }
                }
            });

        } catch (Throwable e) {

        }
    }

    @Override
    public void onSimClose() {
        try {
            if (BaseAppUtil.isDestroy(CameraNetworkSettingActivity.this)) {
                return;
            }
            MainHandler.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DialogUtil.dismissDialog();
                        _network_sim = 0;
                        viewBinding.sbFourG.setChecked(false);
                    } catch (Throwable e) {

                    }
                }
            });

        } catch (Throwable e) {

        }
    }

    @Override
    public void onSimCloseFail(String msg) {
        try {
            if (BaseAppUtil.isDestroy(CameraNetworkSettingActivity.this)) {
                return;
            }
            MainHandler.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DialogUtil.dismissDialog();
                        ToastUtil.s(CameraNetworkSettingActivity.this, "关闭网络失败");
                    } catch (Throwable e) {

                    }
                }
            });

        } catch (Throwable e) {

        }
    }
}
