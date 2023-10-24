package com.xh.hotme.active;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.event.DeviceWeewaStartEvent;
import com.xh.hotme.event.PoweroffEvent;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DialogUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastCustom;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class DeviceOnOffActivity extends BaseActivity implements IBleDeviceInfoNotifyListener {

    // views
    private ImageView _backBtn;
    private TextView _titleLabel, _onLabel, _offLabel;

    Handler _handle;

    Device _device;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            try {
                DialogUtil.dismissDialog();
                _offLabel.setVisibility(View.VISIBLE);
                _onLabel.setVisibility(View.GONE);
            } catch (Throwable e) {

            }
        }
    };

    public static void start(Context context, Device device) {
        if (null != context) {
            Intent intent = new Intent(context, DeviceOnOffActivity.class);
            intent.putExtra(Constants.REQUEST_DEVICE, device);
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
        setContentView(R.layout.activity_device_on_off);

        // find views
        _backBtn = findViewById(R.id.iv_back);
        _titleLabel = findViewById(R.id.tv_title);
        _onLabel = findViewById(R.id.tv_device_on);
        _offLabel = findViewById(R.id.tv_device_off);

        if (getIntent() != null) {
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
        }

        if (_device != null) {
            String deviceName = BluetoothManager.getDisplayDeviceName(_device.getName());
            if (!TextUtils.isEmpty(deviceName)) {
                _titleLabel.setText(deviceName);
            }
        }

        _handle = new Handler();

        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                finish();
                return true;
            }
        });

        _onLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdPowerOn();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                DialogUtil.showDialog(DeviceOnOffActivity.this, "正在开机...");
                return true;
            }
        });

        _offLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdPowerOff();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                DialogUtil.showDialog(DeviceOnOffActivity.this, "正在关机...");
                return true;
            }
        });

        _titleLabel.setText(getString(R.string.active_device_list_title));


        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        BluetoothManager.mInstance.sendCmdDeviceInfo();
        BluetoothHandle.addDeviceInfoNotifyListener(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        BluetoothHandle.removeDeviceInfoNotifyListener(this);
        if (_handle != null) {
            _handle.removeCallbacksAndMessages(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoweroff(PoweroffEvent event) {
        if (_handle != null) {
            _handle.removeCallbacks(_timeOutRunnable);
        }
        if (!BaseAppUtil.isDestroy(DeviceOnOffActivity.this)) {
            if (_handle != null) {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.dismissDialog();
                        ToastCustom.showCustomToast(DeviceOnOffActivity.this, DeviceOnOffActivity.this.getString(R.string.device_power_off_success));
                    }
                });
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeewaStart(DeviceWeewaStartEvent event) {
        if (_handle != null) {
            _handle.removeCallbacks(_timeOutRunnable);
        }
        if (!BaseAppUtil.isDestroy(DeviceOnOffActivity.this)) {
            if (_handle != null) {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.dismissDialog();
                        ToastCustom.showCustomToast(DeviceOnOffActivity.this, DeviceOnOffActivity.this.getString(R.string.device_launch_success));
                    }
                });
            }
        }
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
        if (_handle != null) {
            _handle.removeCallbacks(_timeOutRunnable);
        }
        if (!BaseAppUtil.isDestroy(DeviceOnOffActivity.this)) {
            if (_handle != null) {

                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        _offLabel.setVisibility(View.VISIBLE);
                        _onLabel.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    @Override
    public void onDeviceInfoFail() {

    }
}
