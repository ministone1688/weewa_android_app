package com.xh.hotme.bluetooth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.obs.services.exception.ObsException;
import com.umeng.socialize.UMShareAPI;
import com.xh.hotme.R;
import com.xh.hotme.account.DeleteInteract;
import com.xh.hotme.account.LoginInteract;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bean.VideoUploadTaskBean;
import com.xh.hotme.bean.VideoUploadTaskResultBean;
import com.xh.hotme.camera.IPlayControlListener;
import com.xh.hotme.camera.PlayerActivity;
import com.xh.hotme.camera.RecordInfoBean;
import com.xh.hotme.camera.SetDeviceNameDialog;
import com.xh.hotme.databinding.ActivityMainBinding;

import com.xh.hotme.device.ConnectActivity;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.listener.IProgressListener;
import com.xh.hotme.listener.IUpdateDevNameListener;
import com.xh.hotme.obs.ObsManager;
import com.xh.hotme.softap.Constants;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.upload.VideoUploadInteract;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.PermissionsUtil;
import com.xh.hotme.utils.PlayUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.wifi.WifiListDialog;
import com.xh.hotme.wifi.IWifiNotifyListener;
import com.xh.hotme.wifi.WifiInfo;
import com.xh.hotme.wifi.WifiPasswordDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BlueTestActivity extends BaseActivity implements IWifiNotifyListener, IBleDeviceUsageInfoNotifyListener, IBleSoftApNotifyListener, IBleNetworkInfoNotifyListener, IPlayControlListener, IBleDeviceInfoNotifyListener {

    private final static String TAG = BlueTestActivity.class.getSimpleName();


    // request code
    private static final int REQUEST_CHECK_PERMISSION = 100;

    // views
    private Button btnPowerOn;
    private Button btnPowerOff;
    private Button btnRestart;
    private Button btnWifiList;
    private Button btnWifiSetup;
    private Button btnEnergyInfo;
    private Button btnStorageInfo;
    private Button btnDeviceInfo;

    private Button btnSoftapStart, btnSoftapStop;

    private ImageView backView;

    EditText etSsid, etPassword;

    TextView tvStatus;

    ActivityMainBinding mBinding;
    private Button _startRkipc, _stopRkipc;
    private Button _netstatus;

    WifiListDialog _wifiListDialog;
    WifiPasswordDialog _setupDialog;
    SetDeviceNameDialog _deviceNameDialog;

    private WifiManager mWifiManager;

    Handler _handle;


    String uploadId = "1671090275338526721";
    String videoName;

    DeviceInfo _deviceInfo;
    boolean getDeviceInfoAndPreview = false;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, BlueTestActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // set content view
        setContentView(R.layout.activity_blue_test);

        _handle = new Handler();

        backView = findViewById(R.id.iv_back);
        tvStatus = findViewById(R.id.tv_status);

        backView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });


        btnPowerOn = findViewById(R.id.btn_poweron);
        btnPowerOff = findViewById(R.id.btn_poweroff);
        btnRestart = findViewById(R.id.btn_restart);
        btnPowerOn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                BluetoothManager.mInstance.sendCmdPowerOn();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        btnPowerOff.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                BluetoothManager.mInstance.sendCmdPowerOff();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);

                return true;
            }
        });
        btnRestart.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                BluetoothManager.mInstance.sendCmdRestart();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        btnSoftapStart = findViewById(R.id.btn_softap_start);
        btnSoftapStop = findViewById(R.id.btn_softap_stop);

        btnSoftapStart.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdSoftapOpen(WifiManager.WEEWA_SOFTAP_NAME, "");
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });
        btnSoftapStop.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdSoftapClose();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        btnWifiList = findViewById(R.id.btn_wifilist);
        btnWifiSetup = findViewById(R.id.btn_wifi_setup);
        etSsid = findViewById(R.id.et_ssid);
        etPassword = findViewById(R.id.et_password);

        btnWifiList.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdWifiLists();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);

                return true;
            }
        });

        btnWifiSetup.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                String ssid = etSsid.getText().toString();
                String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
                    ToastUtil.s(BlueTestActivity.this, "请输入ssid和密码");
                    return true;
                }

                BluetoothManager.mInstance.sendCmdSetup(ssid, password);
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });


        btnEnergyInfo = findViewById(R.id.btn_energe_info);
        btnEnergyInfo.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdEnergyStatus();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        btnStorageInfo = findViewById(R.id.btn_storage_info);
        btnStorageInfo.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
//                BluetoothManager.mInstance.sendCmdEnergyStatus();
                BluetoothManager.mInstance.sendCmdStorageStatus();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });
        _startRkipc = findViewById(R.id.btn_start_rkipc);
        _startRkipc.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                RecordInfoBean recordInfoBean = new RecordInfoBean("weewa2", 15000);
                recordInfoBean.author = "zzh";
                recordInfoBean.width = 1920;
                recordInfoBean.height = 1080;
                recordInfoBean.place = "桃源球场";
                recordInfoBean.name = "桃源球场8v8";
                recordInfoBean.time = 12345;
                BluetoothManager.mInstance.sendCmdStartRecord(recordInfoBean);
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        _stopRkipc = findViewById(R.id.btn_stop_rkipc);
        _stopRkipc.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdStopRecord();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        findViewById(R.id.btn_rkipc_status).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdRecordStatus();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });
        findViewById(R.id.btn_usage_info).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdUsageInfo();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });


        _netstatus = findViewById(R.id.btn_netstatus);
        _netstatus.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                BluetoothManager.mInstance.sendCmdNetStatus();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });

        btnDeviceInfo = findViewById(R.id.btn_device_info);
        btnDeviceInfo.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                getDeviceInfoAndPreview = false;
                BluetoothManager.mInstance.sendCmdDeviceInfo();
                _handle.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                return true;
            }
        });
        findViewById(R.id.btn_update_name).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (_deviceNameDialog != null && _deviceNameDialog.isShowing()) {
                    _deviceNameDialog.dismiss();
                }
                _deviceNameDialog = null;
                _deviceNameDialog = new SetDeviceNameDialog(BlueTestActivity.this, "weewa_demo");
                _deviceNameDialog.setOnClickListener(new IUpdateDevNameListener() {
                    @Override
                    public void onUpdate(String name) {
                        _handle.post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("修改设备名称：" + new Gson().toJson(name));
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        _handle.post(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("取消修改设备名称");
                            }
                        });
                    }
                });
                _deviceNameDialog.show();
                return false;
            }
        });

        findViewById(R.id.btn_upload).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (!PermissionsUtil.checkWriteStoragePermission(BlueTestActivity.this)) {
                    return true;
                }
                if (!PermissionsUtil.checkReadStoragePermission(BlueTestActivity.this)) {
                    return true;
                }

                if (!Environment.isExternalStorageManager()) {
                    //跳转到打开权限页面
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);

                    intent.setData(Uri.parse("package:" + BlueTestActivity.this.getPackageName()));

                    startActivityForResult(intent, 100);

                    return true;
                }

                videoName = "桃源球场8vs8";
                if (TextUtils.isEmpty(uploadId)) {
                    VideoUploadTaskBean taskBean = new VideoUploadTaskBean();
                    taskBean.videoName = videoName;
                    taskBean.classify = 0;
                    taskBean.duration = 1800;
                    taskBean.size = 7168;
                    taskBean.deviceMac = "";
                    taskBean.court = "A";
                    taskBean.site = "A";
                    taskBean.competitionName = "恒大vs村霸";
                    taskBean.shootTime = "2023-06-18 20:30:00";
//                    taskBean.shootTime = "2023-06-18";
                    videoName = taskBean.videoName;
                    VideoUploadInteract.preUpload(BlueTestActivity.this, taskBean, new VideoUploadInteract.IVideoPreUploadListener() {
                        @Override
                        public void onSuccess(VideoUploadTaskResultBean data) {
                            uploadId = data.videoId;

                            AsyncTask<String, Void, String> task = new SimpleMultipartUploadTask();
                            task.execute(uploadId);

                        }

                        @Override
                        public void onFail(String code, String message) {

                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                } else {
                    AsyncTask<String, Void, String> task = new SimpleMultipartUploadTask();
                    task.execute(uploadId);

//                    uploadFile(uploadId);
                }

                return false;
            }
        });
        findViewById(R.id.btn_login).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                MobileLoginActivity.start(BlueTestActivity.this);

                return false;
            }
        });
        findViewById(R.id.btn_logout).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                LoginInteract.logout(BlueTestActivity.this, new ICommonListener() {
                    @Override
                    public void onSuccess() {
                        MainHandler.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.s(BlueTestActivity.this, "已退出");
                            }
                        });
                    }

                    @Override
                    public void onFail(String code, String message) {
                        MainHandler.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.s(BlueTestActivity.this, message);
                            }
                        });
                    }

                    @Override
                    public void onFinish() {

                    }
                });

                return true;
            }
        });

        findViewById(R.id.btn_delete).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                DeleteInteract.delete(BlueTestActivity.this, new ICommonListener() {
                    @Override
                    public void onSuccess() {
                        MainHandler.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.s(BlueTestActivity.this, "注销成功");
                            }
                        });
                    }

                    @Override
                    public void onFail(String code, String message) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });

                return false;
            }
        });

        findViewById(R.id.btn_rkipc_preview).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                getDeviceInfoAndPreview = true;
                if (_deviceInfo == null) {
                    BluetoothManager.mInstance.sendCmdDeviceInfo();
                } else {
                    PlayUtil.startPlay(BlueTestActivity.this, _deviceInfo, null);
                }

                return true;
            }
        });


        BluetoothHandle.addWifiNotifyListener(this);
        BluetoothHandle.addDeviceUsageInfoNotifyListener(this);
        BluetoothHandle.addSoftApNotifyListener(this);
        BluetoothHandle.addNetworkNotifyListener(this);
        BluetoothHandle.addDeviceInfoNotifyListener(this);
    }

    private void uploadFile(String videoId) {

        File externalStorage = Environment.getExternalStorageDirectory();
        String rootPath = externalStorage.getAbsolutePath();

        String filePath = rootPath + "/Download/taoyuan_fb8_04161743_7600_2160_380_150_10minute.mp4";
        File file = new File(filePath);
//
//
//
//        File filesPath =  AppFileUtil.getAppDir(BlueTestActivity.this);
//        File videoPath = new File(filesPath, "video");
//        if(!videoPath.exists()){
//            videoPath.mkdir();
//        }
//        String fileName = "taoyuan_fb8_04161743_7600_2160_380_150_10minute.mp4";
//        File file = new File(videoPath, fileName);
//        String filePath = file.getPath();
        if (!file.exists()) {
            tvStatus.setText("文件上传失败： 上传文件不存在=" + filePath);
            return;
        }

        ObsManager.uploadFile(BlueTestActivity.this, filePath, videoName, videoId, new IProgressListener() {
            @Override
            public void onProgressUpdate(long progress, long totalBytesWritten, long totalBytesExpectedToWrite) {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("上传中： progress=" + progress);
                    }
                });

            }

            @Override
            public void onComplete() {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("上传完成： ");
                    }
                });
                uploadId = "";
            }

            @Override
            public void abort(String message) {
                _handle.post(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("上传异常： message=" + message);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BluetoothHandle.removeWifiNotifyListener(this);
        BluetoothHandle.removeDeviceUsageInfoNotifyListener(this);
        BluetoothHandle.removeSoftApNotifyListener(this);
        BluetoothHandle.removeNetworkNotifyListener(this);

        BluetoothHandle.removeDeviceInfoNotifyListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);

        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            findViewById(R.id.btn_upload).callOnClick();
        }
    }

    @Override
    public void onWifiList(List<WifiInfo> device) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                String data = device.toString();
                tvStatus.setText(data);

                _wifiListDialog = new WifiListDialog(BlueTestActivity.this, "网络配置", device, new WifiListAdapterCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onWifiClick(WifiInfo device, int position) {
                        if (_setupDialog != null && _setupDialog.isShowing()) {
                            _setupDialog.dismiss();
                        }
                        _setupDialog = null;

                        _setupDialog = new WifiPasswordDialog(BlueTestActivity.this, device.getSsid());
                        _setupDialog.setOnClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {


                                        }
                                    });
                                }
                            }
                        });
                        _setupDialog.show();
                    }
                });
                _wifiListDialog.show();

            }
        });
    }

    @Override
    public void onSetupStatus(int status, String message) {
        _handle.removeCallbacks(_timeOutRunnable);
    }

    @Override
    public void onStorageInfo(StorageBean storageBean) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(storageBean.getString());
            }
        });
    }

    @Override
    public void onEnergy(int energy) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("电量：" + energy);
            }
        });
    }

    @Override
    public void onDeviceInfo(DeviceUsageInfo info) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("使用情况：" + info.toString());
            }
        });
    }

    @Override
    public void onTemperature(float temperature) {
        _handle.removeCallbacks(_timeOutRunnable);
    }

    @Override
    public void onSoftApStart(String ssid, String password) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("热点启动成功: ip = " + BleConstants.SOFT_AP_IP);

                if (mWifiManager == null) {
                    mWifiManager = WifiManager.getInstance(BlueTestActivity.this);
                }

                mWifiManager.search(Constants.SOFTAP_REGEX, new WifiManager.SearchWifiListener() {
                    @Override
                    public void onScanStart() {
                        Log.d(TAG, "onScanStart...");
//                        mLoadingView.show("正在扫描附近设备");
                    }

                    @Override
                    public void onScanFailed(WifiManager.ErrorType errorType) {
                        Log.d(TAG, "onScanFailed..." + errorType);
//                        mLoadingView.dismiss();
                        switch (errorType) {
                            case NO_WIFI_FOUND:
                                break;
                            case SEARCH_WIFI_TIMEOUT:
                                break;
                        }
                    }

                    @Override
                    public void onScanSuccess(List<WifiInfo> results) {
                        Log.d(TAG, "onScanSuccess..." + results.size());
                        for (WifiInfo info : results) {
                            Log.d(TAG, info.toString());
                        }
                        if (results.isEmpty()) {
                            tvStatus.setText("未找到热点");
//                            showState(STATE.Layout_NoDevice);
                        } else {
                            showSoftApDialog(results.get(0));
                        }
                    }
                });
            }
        });
    }

    private void showSoftApDialog(WifiInfo wifiInfo) {
        ModalDialog dialog = new ModalDialog(BlueTestActivity.this);
        dialog.setMessage("是否加入热我相机的无线网络");
        dialog.setRightButton(getString(R.string.wifi_join), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                tvStatus.setText("正在连接热点：" + wifiInfo.getSsid());
                if (mWifiManager != null) {
                    mWifiManager.connect(wifiInfo, "", new WifiManager.WifiConnectListener() {
                        @Override
                        public void onConnectStart(WifiInfo info) {
                            AppTrace.d(TAG, "connect ....");
                        }

                        @Override
                        public void onConnectFailed(WifiInfo info, int error) {
                            AppTrace.d(TAG, "connect fail....");
                        }

                        @Override
                        public void onConnectSuccess(WifiInfo info, int ip) {
                            AppTrace.d(TAG, "connect success....");
                            _handle.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.s(BlueTestActivity.this, "连接成功");
                                    tvStatus.setText("热点连接成功！");
                                }
                            });
                        }
                    });
                }
                return true;
            }
        });

        dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                dialog.dismiss();
                return true;
            }
        });

        dialog.show();
    }

    @Override
    public void onSoftApStartFail(String message) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("热点启动失败：" + message);
            }
        });
    }

    @Override
    public void onSoftApStop() {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("热点关闭");
            }
        });
    }

    @Override
    public void onSoftApStopFail(String message) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("热点关闭失败：" + message);
            }
        });
    }

    @Override
    public void onSoftApStatus(int status) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("热点状态：" + status);
            }
        });
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

    @Override
    public void onNetworkStatus(NetworkBean networkBean) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("网络状态：" + networkBean.toString());
            }
        });
    }

    @Override
    public void onRecordVideoStart() {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("视频开始录制...");
            }
        });
    }

    @Override
    public void getRecordVideoStatus(int status, int time) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("录制状态： status=" + status);
            }
        });
    }

    @Override
    public void onRecordVideoStop() {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("视频已停止录制");
            }
        });
    }

    @Override
    public void onRecordException(String msg) {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("录制状态： error=" + msg);
            }
        });
    }

    @Override
    public void onDeviceInfo(DeviceInfo data) {
        _deviceInfo = data;
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("设备信息：" + new Gson().toJson(data));

                if (!BaseAppUtil.isDestroy(BlueTestActivity.this) && BaseAppUtil.isForegroundActivity(BlueTestActivity.this, BlueTestActivity.class.getName())) {
                    if (getDeviceInfoAndPreview) {
                        PlayUtil.startPlay(BlueTestActivity.this, data, null);
                        getDeviceInfoAndPreview = false;
                    }
                }
            }
        });
    }

    @Override
    public void onDeviceInfoFail() {
        _handle.removeCallbacks(_timeOutRunnable);
        _handle.post(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText("获取设备信息异常");
            }
        });
    }


    class SimpleMultipartUploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String uploadId = "";
            try {
                uploadId = params[0];
                uploadFile(uploadId);

            } catch (ObsException e) {
            }
            return uploadId;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
