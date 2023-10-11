package com.xh.hotme;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.umeng.socialize.UMShareAPI;
import com.weewa.lib.WeewaLib;
import com.weewa.lib.WeewaMode;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.fragment.CouldFragment;
import com.xh.hotme.fragment.HomeFragment;
import com.xh.hotme.fragment.LiveFragment;
import com.xh.hotme.fragment.MeFragment;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.NetUtil;
import com.xh.hotme.utils.PermissionsUtil;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.widget.MyRadioGroup;
import com.xh.hotme.wifi.HotmeWiFiManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements MyRadioGroup.OnCheckedChangeListener, IBleScanRequestListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    RadioButton tabHomeBtn;
    RadioButton tabIcloudBtn;
    RadioButton tabLiveBtn;
    RadioButton tabMeBtn;
    MyRadioGroup tabGroup;

    private Map<Integer, BaseFragment> _fragments;
    private Map<Integer, Class> _fragmentClasses;
    BaseFragment curFragment;

    Device readyDevice = null;


    Handler mHandler = new Handler();

    public static void start(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeewaLib.Companion.setMode( WeewaMode.SLAVE1);
        WeewaLib.Companion.shared().launch(this);
        ConnectLogic.getInstance();

        BluetoothManager.newInstance(MainActivity.this);

        setContentView(R.layout.activity_main);

        tabHomeBtn = findViewById(R.id.tab_home);
        tabIcloudBtn = findViewById(R.id.tab_icloud);
        tabLiveBtn = findViewById(R.id.tab_live);
        tabMeBtn = findViewById(R.id.tab_me);
        tabGroup = findViewById(R.id.tab_group);

        _fragments = new HashMap<>();
        _fragmentClasses = new HashMap<>();
        _fragmentClasses.put(R.id.tab_home, HomeFragment.class);
        _fragmentClasses.put(R.id.tab_icloud, CouldFragment.class);
        _fragmentClasses.put(R.id.tab_live, LiveFragment.class);
        _fragmentClasses.put(R.id.tab_me, MeFragment.class);


        tabGroup.setOnCheckedChangeListener(this);
        tabGroup.check(R.id.tab_home);

        mHandler = new Handler();


        PermissionsUtil.requestBlePermission(MainActivity.this);

        registerScanBroadcast();
    }

    @Override
    public void onCheckedChanged(MyRadioGroup group, int checkedId) {

        if (checkedId == R.id.tab_me) {
            if (!LoginManager.isSignedIn(MainActivity.this)) {
               // MobileLoginActivity.startActivityByRequestCode(MainActivity.this, Constants.REQUEST_CODE_LOGIN_TAB_ME);
                //return;
            }
        } else if (checkedId == R.id.tab_live){
            if (!LoginManager.isSignedIn(MainActivity.this)) {
                MobileLoginActivity.startActivityByRequestCode(MainActivity.this, Constants.REQUEST_CODE_LOGIN_TAB_LIVE);
                return;
            }
        }

        // lazy create fragment
        BaseFragment fragment = _fragments.get(checkedId);
        if (fragment == null) {
            try {
                Class klass = _fragmentClasses.get(checkedId);
                Method m = klass.getDeclaredMethod("newInstance");
                fragment = (BaseFragment) m.invoke(klass);
                fragment.setBleScanRequestListener(this);
            } catch (Throwable e) {
            }
            if (fragment != null) {
                _fragments.put(checkedId, fragment);
            }
        }

        // add fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment != null && !fragment.isAdded()) {
            fragmentTransaction.add(R.id.container, fragment);
        }
        if (curFragment == fragment) {
            return;
        }

        // switch fragment
        if (null != curFragment) {
            fragmentTransaction.hide(curFragment).show(fragment).commitAllowingStateLoss();
        } else {
            fragmentTransaction.commitAllowingStateLoss();
        }
        getSupportFragmentManager().executePendingTransactions();

        curFragment = fragment;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_LOGIN_TAB_ME) {
            if (resultCode == 1) {
                tabGroup.check(R.id.tab_me);
            }
        } else if (requestCode == Constants.REQUEST_CODE_LOGIN_TAB_LIVE) {
            if (resultCode == 1) {
                tabGroup.check(R.id.tab_live);
            }
        } else if (requestCode == Constants.REQUEST_CODE_LOGIN_HOME_DEVICE) {
            if (resultCode == 1) {
                if (readyDevice != null) {
                    onBluetoothDeviceClick(readyDevice, 0);
                }
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScanBroadcast();
    }

    public void unregisterScanBroadcast() {
        if (scanBroadcastReceiver != null) {
            Application application = getApplication();
            application.unregisterReceiver(scanBroadcastReceiver);
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

    @Override
    public void onScanStart(boolean isBackground) {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.startScan(isBackground);
        }
    }

    @Override
    public void onScan(Device device, int position) {

    }

    @Override
    public void onScanStop(final int size) {
//
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (bluetoothLoadingDialog != null && bluetoothLoadingDialog.isShowing()) {
//                    bluetoothLoadingDialog.dismiss();
//                }
//
//                if (size == 0) {
//                    ModalDialog modalDialog = new ModalDialog(MainActivity.this);
//                    modalDialog.setTitle(getString(R.string.loading_dialog_device_not_found));
//                    modalDialog.setMessage(getString(R.string.loading_dialog_device_message));
//                    modalDialog.setLeftButton(getString(R.string.loading_dialog_cancel), new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            modalDialog.dismiss();
//                            if (bluetoothLoadingDialog != null && bluetoothLoadingDialog.isShowing()) {
//
//                                bluetoothLoadingDialog.dismiss();
//                            }
//                        }
//                    });
//                    modalDialog.setRightButton(getString(R.string.loading_dialog_refresh), new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            modalDialog.dismiss();
//                            if (bluetoothLoadingDialog != null && bluetoothLoadingDialog.isShowing()) {
//
//                                bluetoothLoadingDialog.dismiss();
//                            }
//                        }
//                    });
//
//                    modalDialog.show();
//                }
//
//            }
//        });


        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.stopScan();
        }
    }

    @Override
    public void onBluetoothDeviceClick(Device device, int position) {
        if(device==null){
            return;
        }
        Log.d(TAG, "connect to =" );

//        if (!NetUtil.isNetworkAvailable(MainActivity.this)) {
//            ModalDialog dialog = new ModalDialog(MainActivity.this, getString(R.string.info_network_error_not_open), getString(R.string.info_network_error_open_network), false);
//            dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
//                @Override
//                public boolean onClicked() {
//                    dialog.dismiss();
//                    return true;
//                }
//            });
//            dialog.setRightButton(getString(R.string.setting), new ClickGuard.GuardedOnClickListener() {
//                @Override
//                public boolean onClicked() {
//                    HotmeWiFiManager.startWifiSettingPage(MainActivity.this);
//                    dialog.dismiss();
//                    return true;
//                }
//            });
//            dialog.show();
//            return;
//        }

        if (!LoginManager.isSignedIn(MainActivity.this)) {
            readyDevice = device;
            MobileLoginActivity.startActivityByRequestCode(MainActivity.this, Constants.REQUEST_CODE_LOGIN_HOME_DEVICE);
            return;
        }
        readyDevice = null;
        if (BluetoothManager.mInstance != null) {
            if (!BluetoothManager.mInstance.enableBlueTooth()) {
                BluetoothManager.mInstance.openBluetooth(MainActivity.this, false);
                return;
            }
            BluetoothManager.mInstance.connectToBle(device);
        }
    }

    @Override
    public void onRequestWifiList() {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.sendCmdWifiLists();
        }
    }

    @Override
    public void onSetupWifi(String ssid, String password) {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.sendCmdSetup(ssid, password);
        }
    }

    @Override
    public void onRemove(Device device, int position) {
        if (BluetoothManager.mInstance != null) {
            BluetoothManager.mInstance.removeBindDeviceData(device);
        }
    }

    //监听扫描广播
    private class ScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BluetoothScan", "intent=" + intent.toString());
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                        AppTrace.d("BluetoothScan", "扫描模式改变");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        AppTrace.d("BluetoothScan", "扫描开始");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        AppTrace.d("BluetoothScan", "扫描结束");
                        if (BluetoothManager.mInstance != null) {
                            BluetoothManager.mInstance.onScanEnd();
                        }
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        AppTrace.d("BluetoothScan", "发现设备");
                        //获取蓝牙设备
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null && null != device.getName()) {
                            int rssi = -120;
                            Bundle extras = intent.getExtras();
                            String lv = "";
                            if (extras != null) {
                                //获取信号强度
                                rssi = extras.getShort(BluetoothDevice.EXTRA_RSSI);
                            }
                            if (BluetoothManager.mInstance != null) {
                                BluetoothManager.mInstance.onLeScan(device, rssi, null);
                            }
                        }
                        break;
                }
            }
        }
    }

}
