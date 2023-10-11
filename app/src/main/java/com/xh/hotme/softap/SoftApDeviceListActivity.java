package com.xh.hotme.softap;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xh.hotme.R;
import com.xh.hotme.active.DeviceListFragment;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleScanNotifyListener;
import com.xh.hotme.broadcast.ScanBroadcastReceiver;
import com.xh.hotme.databinding.ActiveActivityListBinding;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.StatusBarUtil;

import java.util.ArrayList;


public class SoftApDeviceListActivity extends BaseViewActivity<ActiveActivityListBinding> implements IBleScanNotifyListener {

    // views
    private ImageView _refreshIv;
    private TextView _refreshLabel;

    ObjectAnimator _refreshAnimator;
    DeviceListFragment _fragment;


    private static final int REQUEST_READ_PHONE_STATE = 0x00000001;

    private static final int REQUEST_LOCATION = 0x00000002;
    private static final int REQUEST_PERMISSION_STORAGE = 0x00000003;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, SoftApDeviceListActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }


        super.onCreate(savedInstanceState);

        BluetoothHandle.addScanNotifyListener(this);
    }

    @Override
    protected void initView() {
        // find views
        viewBinding.titleBar.tvTitle.setText(getString(R.string.camera_video_transfer));
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });
        viewBinding.titleBar.ivRight.setImageResource(R.mipmap.home_refresh);

        _refreshLabel = viewBinding.titleBar.tvRight;
        _refreshIv = viewBinding.titleBar.ivRight;
        _refreshIv.setVisibility(View.VISIBLE);

        _refreshAnimator = ObjectAnimator.ofFloat(_refreshIv, "rotation", 1080f);
        _refreshAnimator.setDuration(2000L);
        _refreshAnimator.setRepeatCount(-1);

//        _refreshLabel.setVisibility(View.GONE);
        _refreshLabel.setText(getString(R.string.loading_dialog_refresh));

        _refreshLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                _fragment.onRefresh();

                return true;
            }
        });

        _fragment = DeviceListFragment.newInstance(DeviceListFragment.type_soft_ap);

        getSupportFragmentManager().beginTransaction()
                .add(viewBinding.content.getId(), _fragment)
                .commit();

        requestPermission();

        registerScanBroadcast();


    }

    @Override
    protected void initData() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (_refreshAnimator != null) {
            _refreshAnimator = null;
        }

        unregisterScanBroadcast();

        BluetoothHandle.removeScanNotifyListener(this);
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
                requestPermissions(permissionsList.toArray(strings), REQUEST_LOCATION);
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

    @Override
    public void onScanning(boolean isBackground) {
        if (_refreshIv != null)
            _refreshIv.setVisibility(View.VISIBLE);

        _refreshAnimator.start();

    }

    @Override
    public void onLeScan(Device device) {

    }

    @Override
    public void onTimeout() {
        if (_refreshIv != null) {
            _refreshIv.setVisibility(View.GONE);
        }
        _refreshAnimator.end();
    }

    @Override
    public void onScanEnd() {
        if (_refreshIv != null) {
            _refreshIv.setVisibility(View.GONE);
        }
        _refreshAnimator.end();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_STORAGE);
                    } else {

                        //TODO
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (_fragment != null) {
                                    _fragment.onRefresh();
                                }
                            }
                        }, 2000);
                    }
                }
                break;
            case REQUEST_PERMISSION_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            if (_fragment != null) {
                                _fragment.onRefresh();
                            }
                        }
                    });
                }
                break;
            case REQUEST_LOCATION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            if (_fragment != null) {
                                _fragment.onRefresh();
                            }
                        }
                    });
                }
                break;
        }
    }
}
