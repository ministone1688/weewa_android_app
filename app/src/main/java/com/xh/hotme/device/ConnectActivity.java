package com.xh.hotme.device;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.MainActivity;
import com.xh.hotme.R;
import com.xh.hotme.active.MobileActiveLoginActivity;
import com.xh.hotme.active.MobileActiveSmsDialog;
import com.xh.hotme.base.ActivityStackManager;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.BindDeviceResultBean;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.WifiListAdapterCallback;
import com.xh.hotme.event.DeviceEvent;
import com.xh.hotme.softap.CameraNetworkSettingActivity;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.GsonUtils;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastCustom;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.wifi.IWifiNotifyListener;
import com.xh.hotme.wifi.WifiInfo;
import com.xh.hotme.wifi.WifiListDialog;
import com.xh.hotme.wifi.WifiPasswordDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


public class ConnectActivity extends BaseActivity implements IWifiNotifyListener {
    private final static String TAG = ConnectActivity.class.getSimpleName();
    // views
    private ImageView _backBtn, _statusIv;
    private TextView _titleLabel, _settingLabel, _statusLabel, _contentLabel;

    public int _type = 0; //0：配网，1:激活， 2:

    Device _device;


    String _mobile;

    String _networkInfo;

    NetworkBean _networkBean;

    Handler _handler;

    Runnable _timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            dismissLoadingDialog();
        }
    };

    MobileActiveSmsDialog _smsDialog;

    BluetoothLoadingDialog _bluetoothLoadingDialog;

    WifiListDialog _wifiListDialog;
    WifiPasswordDialog _setupDialog;

    public static void start(Context context, Device device, int step) {
        if (null != context) {
            Intent intent = new Intent(context, ConnectActivity.class);
            intent.putExtra(Constants.REQUEST_DEVICE, device);
            intent.putExtra(Constants.REQUEST_ACTIVE_STEP, step);
            context.startActivity(intent);
        }
    }


    public static void start(Context context, Device device, String netConfig) {
        if (null != context) {
            Intent intent = new Intent(context, ConnectActivity.class);
            intent.putExtra(Constants.REQUEST_DEVICE, device);
            intent.putExtra(Constants.REQUEST_ACTIVE_STEP, Constants.ACTIVE_STEP_SETUP_WIFI);
            intent.putExtra(Constants.REQUEST_NETWORK_INFO, netConfig);
            context.startActivity(intent);
        }
    }

    public static void start(Context context, Device device, int step, String mobile) {
        if (null != context) {
            Intent intent = new Intent(context, ConnectActivity.class);
            intent.putExtra(Constants.REQUEST_DEVICE, device);
            intent.putExtra(Constants.REQUEST_ACTIVE_STEP, step);
            intent.putExtra(Constants.REQUEST_MOBILE, mobile);
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

        // set content view
        setContentView(R.layout.activity_camera_connect);

        _handler = new Handler();

        if (getIntent() != null) {
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
            _type = (int) getIntent().getIntExtra(Constants.REQUEST_ACTIVE_STEP, Constants.ACTIVE_STEP_SETUP_WIFI);
            _mobile = (String) getIntent().getStringExtra(Constants.REQUEST_MOBILE);
            _networkInfo = (String) getIntent().getStringExtra(Constants.REQUEST_NETWORK_INFO);
        }

        if (!TextUtils.isEmpty(_networkInfo)) {
            _networkBean = GsonUtils.GsonToBean(_networkInfo, NetworkBean.class);
        }

        // find views
        _backBtn = findViewById(R.id.iv_back);
        _titleLabel = findViewById(R.id.tv_title);
        _settingLabel = findViewById(R.id.tv_setting);
        _statusLabel = findViewById(R.id.tv_status);
        _contentLabel = findViewById(R.id.tv_content);
        _statusIv = findViewById(R.id.iv_status);

        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                finish();
                return true;
            }
        });

        _titleLabel.setText(getString(R.string.player_title));

        _settingLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_type == Constants.ACTIVE_STEP_SETUP_WIFI) {
                    setupWifi();
                } else if (_type == Constants.ACTIVE_STEP_ACTIVE) {
                    showActiveSmsDialog();
                } else if (_type == Constants.ACTIVE_STEP_BIND_FAIL) {
                    finish();
                } else if (_type == Constants.ACTIVE_STEP_ACTIVE_FAIL) {
                    finish();
                }
                return true;
            }
        });

        setup(_type);

//        BluetoothHandle.addWifiNotifyListener(this);
    }

    private void showActiveSmsDialog() {
        if (_smsDialog != null && _smsDialog.isShowing()) {
            _smsDialog.dismiss();
        }
        _smsDialog = null;

        _smsDialog = new MobileActiveSmsDialog(ConnectActivity.this, _device);
        _smsDialog.setOnClickListener(new MobileActiveSmsDialog.IActiveSmsListener() {
            @Override
            public void onActiveSms(String mobile, String code) {
                _mobile = mobile;
                if (!TextUtils.isEmpty(code)) {
                    if (code.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_DEVICE_BOUND)) {
                        _type = Constants.ACTIVE_STEP_BOUND;
                        setup(_type);
                        _smsDialog.dismiss();
                    } else if (code.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_BIND)) {
                        _smsDialog.dismiss();
                        MobileActiveLoginActivity.start(ConnectActivity.this, mobile, Constants.ACTIVE_TYPE_ACTIVE);
                        finish();
                    } else if (code.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_LOGIN)) {
                        _smsDialog.dismiss();
                        MobileActiveLoginActivity.start(ConnectActivity.this, mobile, Constants.ACTIVE_TYPE_LOGIN);
                        finish();
                    }

                }
            }

            @Override
            public void onActiveSmsFail(String mobile, String message) {
                ToastCustom.showCustomToast(ConnectActivity.this, message);
            }
        });

        _smsDialog.show();
    }


    public void setup(int status) {
        if (status == Constants.ACTIVE_STEP_SETUP_WIFI) {
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_connect_success));
            _contentLabel.setText(getString(R.string.active_device_setup_wifi));
            _settingLabel.setText(getString(R.string.setup_wifi));
        } else if (status == Constants.ACTIVE_STEP_ACTIVE) {
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_network_setup_success));
            _contentLabel.setText(getString(R.string.device_active_nextstep));
            _settingLabel.setText(getString(R.string.device_active));
        } else if (status == Constants.ACTIVE_STEP_BIND_SUCCESS) {
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_bind_success));
            _contentLabel.setText(getString(R.string.right_now_to_home));
            _settingLabel.setText("");
            _settingLabel.setVisibility(View.GONE);

            //获取绑定列表
            getDeviceList();

        } else if (status == Constants.ACTIVE_STEP_ACTIVE_SUCCESS) {
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_active_success));
            _contentLabel.setText(getString(R.string.right_now_to_home));
            _settingLabel.setText("");
            _settingLabel.setVisibility(View.GONE);

            //获取绑定列表
            getDeviceList();

        } else if (status == Constants.ACTIVE_STEP_BOUND) {
            _statusIv.setImageResource(R.mipmap.device_status);
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_bound));
            _contentLabel.setText(getString(R.string.right_now_to_login));
            _settingLabel.setVisibility(View.GONE);

            _handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MobileActiveLoginActivity.start(ConnectActivity.this, _mobile, Constants.ACTIVE_TYPE_LOGIN);
                    finish();
                }
            }, 2000);
        } else if (status == Constants.ACTIVE_STEP_BIND_FAIL) {
            _statusIv.setImageResource(R.mipmap.device_status_error);
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_bind_fail));
            _contentLabel.setText(getString(R.string.device_try_another_device));
            _settingLabel.setText(getString(R.string.know));
            _settingLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    finish();
                    return true;
                }
            });

        } else if (status == Constants.ACTIVE_STEP_ACTIVE_FAIL) {
            _statusIv.setImageResource(R.mipmap.device_status_error);
            _statusIv.setVisibility(View.VISIBLE);
            _statusLabel.setText(getString(R.string.device_bind_fail));
            _contentLabel.setText(getString(R.string.right_now_to_home));
            _settingLabel.setText(getString(R.string.device_bind));

            _handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!ActivityStackManager.getInstance().hasActivity(MainActivity.class)) {
                        MainActivity.start(ConnectActivity.this);
                    }
                    finish();
                }
            }, 2000);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        AppTrace.d(TAG, "onDestroy");
//        BluetoothHandle.removeWifiNotifyListener(this);

        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }


    private void showLoadingDialog(String message) {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
            _bluetoothLoadingDialog = null;
        }
        _bluetoothLoadingDialog = new BluetoothLoadingDialog(ConnectActivity.this);
        _bluetoothLoadingDialog.setMessage(message);
        _bluetoothLoadingDialog.show();
    }


    private void dismissLoadingDialog() {
        if (_bluetoothLoadingDialog != null && _bluetoothLoadingDialog.isShowing()) {
            _bluetoothLoadingDialog.dismiss();
        }
        _bluetoothLoadingDialog = null;
    }


    private void setupWifi() {
//        BluetoothManager.mInstance.sendCmdWifiLists();
//        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
//
//        showLoadingDialog("");

        String ssid = "";

        boolean fourG = false;

        //待相机接口确认参数
        if (_networkBean != null && _networkBean.getWifi() != null && (_networkBean.getWifi().ip.isEmpty() || _networkBean.getWifi().ip.startsWith("169.254"))) {
            ssid= "";
        }

        CameraNetworkSettingActivity.start(ConnectActivity.this, _device, ssid, fourG);
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
                _wifiListDialog = new WifiListDialog(ConnectActivity.this, "网络配置", device, new WifiListAdapterCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onWifiClick(WifiInfo device, int position) {
                        if (_setupDialog != null && _setupDialog.isShowing()) {
                            _setupDialog.dismiss();
                        }
                        _setupDialog = null;

                        _setupDialog = new WifiPasswordDialog(ConnectActivity.this, device.getSsid());
                        _setupDialog.setOnClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    _type = Constants.ACTIVE_STEP_ACTIVE;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            setup(_type);
                                            if (_wifiListDialog != null && _wifiListDialog.isShowing()) {
                                                _wifiListDialog.dismiss();
                                            }

                                        }
                                    });
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

    private void getDeviceList() {
        BindDeviceInteract.getDeviceList(ConnectActivity.this, 1, Constants.BIND_DEVICE_PAGE_NUMBER, new BindDeviceInteract.IBindDeviceListener() {
            @Override
            public void onDeviceList(BindDeviceResultBean data) {

            }

            @Override
            public void onFail(String code, String msg) {

            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EventBus.getDefault().post(new DeviceEvent());
                            if (!ActivityStackManager.getInstance().hasActivity(MainActivity.class)) {
                                MainActivity.start(ConnectActivity.this);
                            }
                            finish();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
