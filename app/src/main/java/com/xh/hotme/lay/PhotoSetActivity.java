package com.xh.hotme.lay;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xh.hotme.R;
import com.xh.hotme.active.DeviceOnOffActivity;
import com.xh.hotme.active.MobileUnbindDialog;
import com.xh.hotme.active.MyDeviceActivity;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceInfoNotifyListener;
import com.xh.hotme.databinding.ActivityMobileViewBinding;
import com.xh.hotme.databinding.ActivityPhotoSetBinding;
import com.xh.hotme.event.DeviceWeewaStartEvent;
import com.xh.hotme.event.PoweroffEvent;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.softap.CameraNetworkSettingActivity;
import com.xh.hotme.softap.CameraSoftApActivity;
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

import java.util.ArrayList;
import java.util.List;

public class PhotoSetActivity extends BaseActivity implements View.OnClickListener, IBleDeviceInfoNotifyListener {

    private RelativeLayout photo_select;
    private LinearLayout btn_photo_open;
    private LinearLayout btn_photo_close;
    private LinearLayout btn_photo_fastup;
    private LinearLayout btn_photo_unbind;
    private ImageView _backBtn;
    private TextView _photo_name;

    MobileUnbindDialog _unbindDialog;

    List<Device> _dataList = new ArrayList<>();
    Handler _handle;
    Device _device;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }
        // set content view
        setContentView(R.layout.activity_photo_set);

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText("相机功能管理");
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        photo_select = findViewById(R.id.photo_select);
        photo_select.setOnClickListener(this);
        btn_photo_open = findViewById(R.id.btn_photo_open);
        btn_photo_open.setOnClickListener(this);
        btn_photo_close = findViewById(R.id.btn_photo_close);
        btn_photo_close.setOnClickListener(this);
        btn_photo_fastup = findViewById(R.id.btn_photo_fastup);
        btn_photo_fastup.setOnClickListener(this);
        btn_photo_unbind = findViewById(R.id.btn_photo_unbind);
        btn_photo_unbind.setOnClickListener(this);
        _photo_name = findViewById(R.id.photo_name);

        getCurrentCamera();
    }

    private void getCurrentCamera() {
        List<Device> deviceList = BluetoothManager.mInstance.getBleDeviceList();
        if (deviceList != null && deviceList.size() > 0) {
            _dataList.addAll(deviceList);
            _device = _dataList.get(0);
            String deviceName = BluetoothManager.getDisplayDeviceName(_dataList.get(0).getName());
            _photo_name.setText(deviceName);
        }else{
            _photo_name.setText("请选择相机");
        }

        _handle = new Handler();

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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.photo_select:
                Intent intent = new Intent(PhotoSetActivity.this,CameraListActivity.class);
                startActivityForResult(intent, 0xFF);
            break;
            //开机
            case R.id.btn_photo_open:
                BluetoothManager.mInstance.sendCmdPowerOn();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                DialogUtil.showDialog(PhotoSetActivity.this, "正在开机...");
                break;
            //关机
            case R.id.btn_photo_close:
                BluetoothManager.mInstance.sendCmdPowerOff();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                DialogUtil.showDialog(PhotoSetActivity.this, "正在关机...");
                break;
            //快传
            case R.id.btn_photo_fastup:
                CameraSoftApActivity.start(getApplicationContext(), CameraSoftApActivity.REQUEST_SOFT_AP_CAMERA_INFO);
                break;
            //解绑
            case R.id.btn_photo_unbind:
                if(_device!=null){
                    String mac = _device.getAddress();
                    showUnbindDialog(mac);
                }else{
                    MyToolUtils.myToast(PhotoSetActivity.this,"请先绑定设备",3000);
                    return;
                }

                break;
        }
    }


    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            try {
                DialogUtil.dismissDialog();

            } catch (Throwable e) {

            }
        }
    };

    private void showUnbindDialog(String mac) {
        if (_unbindDialog != null && _unbindDialog.isShowing()) {
            _unbindDialog.dismiss();
        }
        _unbindDialog = null;

        _unbindDialog = new MobileUnbindDialog(PhotoSetActivity.this, mac);
        _unbindDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        _unbindDialog.show();
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
        if (_handle != null) {
            _handle.removeCallbacks(_timeOutRunnable);
        }
        if (!BaseAppUtil.isDestroy(PhotoSetActivity.this)) {
            if (_handle != null) {

                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        //_offLabel.setVisibility(View.VISIBLE);
                       // _onLabel.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    @Override
    public void onDeviceInfoFail() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPoweroff(PoweroffEvent event) {
        if (_handle != null) {
            _handle.removeCallbacks(_timeOutRunnable);
        }
        if (!BaseAppUtil.isDestroy(PhotoSetActivity.this)) {
            if (_handle != null) {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.dismissDialog();
                        ToastCustom.showCustomToast(PhotoSetActivity.this, PhotoSetActivity.this.getString(R.string.device_power_off_success));
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
        if (!BaseAppUtil.isDestroy(PhotoSetActivity.this)) {
            if (_handle != null) {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.dismissDialog();
                        ToastCustom.showCustomToast(PhotoSetActivity.this, PhotoSetActivity.this.getString(R.string.device_launch_success));
                    }
                });
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0xFF) {
           // Bundle bundle = data.getExtras();
           // int imageID = bundle.getInt("id");
            System.out.println("000000000000000000000000000000000");
        }
    }




}
