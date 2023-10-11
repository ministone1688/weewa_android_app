package com.xh.hotme.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.event.RenameEvent;
import com.xh.hotme.listener.IUpdateDevNameListener;
import com.xh.hotme.me.bean.SettingBean;
import com.xh.hotme.me.holder.SettingAdapter;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.widget.RoundProgressBar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class CameraInfoActivity extends BaseActivity implements IBleDeviceUsageInfoNotifyListener {

    private final static String TAG = CameraInfoActivity.class.getSimpleName();

    // request code
    private static final int REQUEST_CHECK_PERMISSION = 100;

    // views
    private TextView _cameraTitleTv, _usageLeftTv;

    RoundProgressBar _usagePb;

    ImageView _modifyIv;
    ImageView _titleBack;
    TextView _titleTv;

    DeviceUsageInfo _usageInfo;

    DeviceInfo _deviceInfo;
    Device _device;

    SetDeviceNameDialog _deviceNameDialog;

    String _deviceName;

    Handler _handler;

    SettingAdapter _taskAdapter;
    List<SettingBean> _taskList = new ArrayList<>();

    RecyclerView _recyclerView;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, CameraInfoActivity.class);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, DeviceUsageInfo usageInfo, DeviceInfo deviceInfo, Device device) {
        Intent intent = new Intent(ctx, CameraInfoActivity.class);
        intent.putExtra(Constants.REQUEST_INTENT_USAGE_INFO, usageInfo);
        intent.putExtra(Constants.REQUEST_INTENT_DEVICE_INFO, deviceInfo);
        intent.putExtra(Constants.REQUEST_DEVICE, device);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            _usageInfo = (DeviceUsageInfo) getIntent().getSerializableExtra(Constants.REQUEST_INTENT_USAGE_INFO);
            _deviceInfo = (DeviceInfo) getIntent().getSerializableExtra(Constants.REQUEST_INTENT_DEVICE_INFO);
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
        }

        _handler = new Handler();

        // set content view
        setContentView(R.layout.activity_camera_info);
        _titleTv = findViewById(R.id.tv_title);

        _titleTv.setText(getString(R.string.active_camera_info_title));

        _titleBack = findViewById(R.id.iv_back);
        _titleBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        _modifyIv = findViewById(R.id.title_modify);
        _cameraTitleTv = findViewById(R.id.camera_title);
        _usagePb = findViewById(R.id.usage_progress);

        _usageLeftTv = findViewById(R.id.tv_usage_left);

        _modifyIv.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (_deviceNameDialog != null && _deviceNameDialog.isShowing()) {
                    _deviceNameDialog.dismiss();
                }
                _deviceNameDialog = null;

                _deviceNameDialog = new SetDeviceNameDialog(CameraInfoActivity.this, _deviceName);

                _deviceNameDialog.setOnClickListener(new IUpdateDevNameListener() {
                    @Override
                    public void onUpdate(String name) {
                        _cameraTitleTv.setText(name);

                        _deviceNameDialog.dismiss();

                        if (_deviceInfo != null) {
                            _deviceInfo.device_name = name;
                        }

                        BluetoothManager.updateDeviceName(_device.address, name);

                        EventBus.getDefault().post(new RenameEvent(_device.address, name));
                    }

                    @Override
                    public void onCancel() {

                        _deviceNameDialog.dismiss();
                    }
                });

                _deviceNameDialog.show();

                return true;
            }
        });

        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_CAMERA_RUNNING_INFO, 0, this.getString(R.string.camera_running_info_title)));
        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_CAMERA_SOFT_AP_SETTING, 0,this.getString(R.string.camera_soft_ap_setting)));
        _taskList.add(new SettingBean(SettingAdapter.SETTING_TYPE_CAMERA_ABOUT, 0, this.getString(R.string.camera_about)));

        this._recyclerView = findViewById(R.id.recyclerView);
        _taskAdapter = new SettingAdapter(CameraInfoActivity.this, _taskList);
        // setup views
        _recyclerView.setLayoutManager(new LinearLayoutManager(CameraInfoActivity.this));
        _recyclerView.setAdapter(_taskAdapter);

        _recyclerView.setNestedScrollingEnabled(false);

        if (_usageInfo != null) {
            initData();
        } else {
            BluetoothManager.mInstance.sendCmdUsageInfo();
            _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
        }

        BluetoothHandle.addDeviceUsageInfoNotifyListener(this);


    }

    public void initData() {
        if (_usageInfo != null) {
            _usagePb.setMax(100);
            _usagePb.setProgress(_usageInfo.energy);
        }

        _deviceName = "热我相机";
        if (_device != null) {
            _deviceName = BluetoothManager.getDisplayDeviceName(_device);
        } else if (_deviceInfo != null && !TextUtils.isEmpty(_deviceInfo.device_name)) {
            _deviceName = BluetoothManager.getDisplayDeviceName(_deviceInfo.device_name);
        }

        _cameraTitleTv.setText(_deviceName);
        _usageLeftTv.setText("4h");

    }

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        BluetoothHandle.removeDeviceUsageInfoNotifyListener(this);

        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onStorageInfo(StorageBean storageBean) {

    }

    @Override
    public void onEnergy(int energy) {

    }

    @Override
    public void onDeviceInfo(DeviceUsageInfo data) {
        _usageInfo = data;
    }

    @Override
    public void onTemperature(float temperature) {

    }
}
