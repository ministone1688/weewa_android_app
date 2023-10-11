package com.xh.hotme.wifi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;


public class WifiPasswordActivity extends BaseActivity implements IWifiNotifyListener {

    // views
    private ImageView _backBtn;
    private TextView _titleLabel, _joinLabel;

    EditText _passwordEt;

    String ssid;

    Handler _handler;

    public int type = 0; //0：配网，1:激活， 2:


    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

        }
    };


    public static void start(Context context, String ssid) {
        if (null != context) {
            Intent intent = new Intent(context, WifiPasswordActivity.class);
            intent.putExtra(Constants.SSID, ssid);
            context.startActivity(intent);
        }
    }

    public static void start(Activity context, String ssid) {
        if (null != context) {
            Intent intent = new Intent(context, WifiPasswordActivity.class);
            intent.putExtra(Constants.SSID, ssid);
            context.startActivityForResult(intent, Constants.REQUEST_CODE_WIFI_SETUP);
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

        if (getIntent() != null) {
            ssid = getIntent().getStringExtra(Constants.SSID);
        }

        _handler = new Handler();

        // find views
        _backBtn = findViewById(R.id.iv_back);
        _titleLabel = findViewById(R.id.tv_title);
        _passwordEt = findViewById(R.id.et_password);
        _joinLabel = findViewById(R.id.tv_right);

        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                finish();
                return true;
            }
        });

        _titleLabel.setText(ssid);

        _joinLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                String password = _passwordEt.getText().toString();
                if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
                    ToastUtil.s(WifiPasswordActivity.this, "请输入ssid和密码");
                    return true;
                }

                BluetoothManager.mInstance.sendCmdSetup(ssid, password);
                _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });


        BluetoothHandle.addWifiNotifyListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BluetoothHandle.removeWifiNotifyListener(this);

        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
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
                requestPermissions(permissionsList.toArray(strings), 0);
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

    @Override
    public void onWifiList(List<WifiInfo> device) {

    }

    @Override
    public void onSetupStatus(int status, String message) {
        setResult(1);
        finish();
    }
}
