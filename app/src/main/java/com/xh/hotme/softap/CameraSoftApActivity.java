package com.xh.hotme.softap;


import static com.xh.hotme.utils.Constants.REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleSoftApNotifyListener;
import com.xh.hotme.camera.CameraManager;
import com.xh.hotme.databinding.ActivitySoftApBinding;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.DialogUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.ModalDialog;


public class CameraSoftApActivity extends BaseViewActivity<ActivitySoftApBinding> implements IBleSoftApNotifyListener {
    private static final String TAG = CameraSoftApActivity.class.getSimpleName();

    private int reqestCode = -1;

    private String info_soft_ap_ssid_is_empty = "";
    private String info_soft_ap_password_is_empty = "";
    public final static int REQUEST_SOFT_AP_TRANSFER = 0;
    public final static int REQUEST_SOFT_AP_PREVIEW = 1;

    public final static int REQUEST_SOFT_AP_CAMERA_INFO = 2;

    Handler _handle;

    String _ssid;
    String _password;
    int _status;
    int _requestCode;

    ModalDialog _dialog;

    boolean _isOpenSoftAp = false;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            DialogUtil.dismissDialog();
        }
    };

    public static void start(Context context, String ssid, String password, int status, int requestCode) {
        if (null != context) {
            Intent intent = new Intent(context, CameraSoftApActivity.class);
            intent.putExtra(BleConstants.BLE_KEY_SOFTAP_SSID, ssid);
            intent.putExtra(BleConstants.BLE_KEY_SOFTAP_PASSWORD, password);
            intent.putExtra(BleConstants.BLE_KEY_ST, status);
            intent.putExtra(REQUEST_CODE, requestCode);
            context.startActivity(intent);
        }
    }

    public static void start(Context context, int requestCode) {
        if (null != context) {
            Intent intent = new Intent(context, CameraSoftApActivity.class);
            intent.putExtra(REQUEST_CODE, requestCode);
            context.startActivity(intent);
        }
    }

    public static void startActivityForResult(Activity context, String ssid, String password, int status, int requestCode) {
        if (null != context) {
            Intent intent = new Intent(context, CameraSoftApActivity.class);
            intent.putExtra(BleConstants.BLE_KEY_SOFTAP_SSID, ssid);
            intent.putExtra(BleConstants.BLE_KEY_SOFTAP_PASSWORD, password);
            intent.putExtra(BleConstants.BLE_KEY_ST, status);
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

        BluetoothHandle.addSoftApNotifyListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BluetoothHandle.removeSoftApNotifyListener(this);

        if (_handle != null) {
            _handle.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void initView() {

        _handle = new Handler();
        viewBinding.titleBar.tvTitle.setText(getString(R.string.camera_video_transfer));
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                finish();
                return true;
            }
        });
        viewBinding.btnSetup.requestFocus();
        viewBinding.btnSetup.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                String ssid = viewBinding.etSsid.getText().toString();
                String password = viewBinding.etPassword.getText().toString();
                if (TextUtils.isEmpty(ssid)) {
                    ToastUtil.s(CameraSoftApActivity.this, info_soft_ap_ssid_is_empty);
                    return true;
                }

                _ssid = ssid;
                _password = password;

                doSoftApSetup();

                return true;
            }
        });
        viewBinding.softApSwitch.setChecked(true);
    }

    @Override
    protected void initData() {

        if (getIntent() != null) {
            _ssid = getIntent().getStringExtra(BleConstants.BLE_KEY_SOFTAP_SSID);
            _password = getIntent().getStringExtra(BleConstants.BLE_KEY_SOFTAP_PASSWORD);
            _status = getIntent().getIntExtra(BleConstants.BLE_KEY_ST, 0);
            _requestCode = getIntent().getIntExtra(REQUEST_CODE, REQUEST_SOFT_AP_PREVIEW);
        }

        NetworkBean networkBean = CameraManager.geNetworkInfo(null);
        if (networkBean != null && networkBean.getAp() != null) {
            _ssid = networkBean.getAp().ssid;
            _password = networkBean.getAp().password;
            _status = networkBean.getAp().st;
        }

        if (_status != 0) {
            viewBinding.softApSwitch.setChecked(false);
            _isOpenSoftAp = true;
        } else {
            _isOpenSoftAp = false;
            viewBinding.softApSwitch.setChecked(true);
        }

        viewBinding.etPassword.setText(_password);
        viewBinding.etSsid.setText(_ssid);

        if (_requestCode == REQUEST_SOFT_AP_TRANSFER) {
            viewBinding.titleBar.tvTitle.setText(getString(R.string.camera_video_transfer));
        } else {
            viewBinding.titleBar.tvTitle.setText(getString(R.string.camera_wifi_soft_ap_setting));
        }

        viewBinding.softApSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    viewBinding.layoutSetup.setVisibility(View.GONE);
                    if (_status != 0) {
                        doSoftApClose();
                    }
                } else {
                    viewBinding.layoutSetup.setVisibility(View.VISIBLE);
                }
            }
        });

        info_soft_ap_ssid_is_empty = getString(R.string.info_soft_ap_ssid_is_empty);
        info_soft_ap_password_is_empty = getString(R.string.info_soft_ap_password_is_empty);
    }

    private void doSoftApSetup() {
        BluetoothManager.mInstance.sendCmdSoftapOpen(_ssid, _password);
        _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);

        DialogUtil.showDialog(CameraSoftApActivity.this, "正在创建热点");
    }


    private void doSoftApClose() {
        BluetoothManager.mInstance.sendCmdSoftapClose();
        _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);

        DialogUtil.showDialog(CameraSoftApActivity.this, "正在关闭热点");
    }

    @Override
    public void onSoftApStart(String ssid, String password) {
        DialogUtil.dismissDialog();
        _handle.removeCallbacks(_timeOutRunnable);
        _isOpenSoftAp = true;
        if (!BaseAppUtil.isDestroy(CameraSoftApActivity.this)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (_requestCode == REQUEST_SOFT_AP_TRANSFER) {
                        CameraSoftApInfoActivity.startActivity(CameraSoftApActivity.this, _ssid, _password);
                    }
                    finish();
                }
            });
        }
    }

    @Override
    public void onSoftApStartFail(String message) {
        DialogUtil.dismissDialog();
        _handle.removeCallbacks(_timeOutRunnable);
        _isOpenSoftAp = false;
        if (!BaseAppUtil.isDestroy(CameraSoftApActivity.this)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (_dialog != null && _dialog.isShowing()) {
                        _dialog.dismiss();
                    }

                    _dialog = new ModalDialog(CameraSoftApActivity.this);
                    _dialog.setMessage(getString(R.string.info_soft_ap_setup_fail));
                    _dialog.setRightButton(getString(R.string.retry), new ClickGuard.GuardedOnClickListener() {
                        @Override
                        public boolean onClicked() {
                            return true;
                        }
                    });
                    _dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
                        @Override
                        public boolean onClicked() {
                            return true;
                        }
                    });
                    _dialog.show();
                }
            });
        }
    }

    @Override
    public void onSoftApStop() {
        _isOpenSoftAp = false;
        _handle.removeCallbacks(_timeOutRunnable);

        if (!BaseAppUtil.isDestroy(CameraSoftApActivity.this)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    DialogUtil.dismissDialog();
                    ToastUtil.s(CameraSoftApActivity.this, "已关闭热点");
                }
            });
        }
    }

    @Override
    public void onSoftApStopFail(String message) {
        _handle.removeCallbacks(_timeOutRunnable);
        if (!BaseAppUtil.isDestroy(CameraSoftApActivity.this)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtil.dismissDialog();

                    ToastUtil.s(CameraSoftApActivity.this, "已关闭热点");
                }
            });
        }
    }

    @Override
    public void onSoftApStatus(int status) {

    }

    @Override
    public void onSoftApConnectStart() {

    }

    @Override
    public void onSoftApConnectSuccess() {

    }

    @Override
    public void onSoftApConnectFail(String message) {

    }
}
