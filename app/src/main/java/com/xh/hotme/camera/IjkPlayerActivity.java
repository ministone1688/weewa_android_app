package com.xh.hotme.camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.dou361.ijkplayer.widget.AndroidMediaController;
import com.dou361.ijkplayer.widget.IjkVideoView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.umeng.socialize.UMShareAPI;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleSoftApNotifyListener;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.widget.playerview.MyPlayerView;
import com.xh.hotme.wifi.HotmeWiFiManager;
import com.xh.hotme.wifi.WifiInfo;

import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class IjkPlayerActivity extends BaseActivity implements IPlayControlListener, IBleDeviceUsageInfoNotifyListener, IBleSoftApNotifyListener {

    private final static String TAG = IjkPlayerActivity.class.getSimpleName();

    // request code
    private static final int REQUEST_CHECK_PERMISSION = 100;
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    private final String STATE_LAUNCH_SOFT_AP = "launchSoftAp";

    //view
    LinearLayout _localLayout, _settingLayout;
    TextView _exitBtn;
    TextView _indicatorView;
    TextView _storageTv, _energyTv;

//    ImageView _fullView;

    Button _playerButton;

    IjkVideoView _playerView;

    IjkMediaPlayer ijkMediaPlayer;

    FrameLayout _countDownView;
    TextView _countDownTv;
    TextView _recordTv;

    LinearLayout _panelSetting;
    RelativeLayout _titleBar;

    FrameLayout _contentLayout, _contentVerticalLayout;

    LinearLayout _controlLayout, _storageLayout, _energyLayout;

    String _url = "rtmp://" + BleConstants.SOFT_AP_IP + ":1935/live/preview2";

    private static boolean useSoftAp = false;
    private static boolean _requestSoftAp = false;
    private static boolean isPortrait = true;

    private int mResumeWindow = C.INDEX_UNSET;
    private long mResumePosition;

    private boolean mExoPlayerFullscreen = false;

    private boolean isRecord = false;
    private boolean _deviceIsRecord = false;
    private boolean _deviceStatusChecked = false;
    private boolean _deviceStatusChecking = false;
    private boolean _deviceStatusBackgroundCheck = false;

    boolean _init = true;
    Handler _handler;
    private WifiManager mWifiManager;

    boolean _deviceSoftApLaunched = false;

    BluetoothLoadingDialog _connectingDialog;

    DeviceUsageInfo _deviceUsageInfo;

    DeviceInfo _deviceInfo;
    Device _device;

    WifiInfo _wifiInfo;

    ModalDialog _dialog;

    boolean requestEnergy = false;
    boolean requestStorage = false;

    boolean isPlay = false;

    int retryCount = 0;

    long _recordCountDown = 0;

    int connectSoftApAction = 0; //1 for video

    private final int DEVICE_USAGE_INFO_DELAY = 60000;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

        }
    };

    private final Runnable _recordStatusTimeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            _deviceStatusChecking = false;

        }
    };

    private final Runnable _energyTimeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            requestEnergy = false;
        }
    };

    private final Runnable _storageTimeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            //
            requestStorage = false;
        }
    };

    private final Runnable _recordCountDownRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            BluetoothManager.mInstance.sendCmdUsageInfo();
        }
    };


    private final Runnable _deviceUsageInfoRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

            _handler.postDelayed(_deviceUsageInfoRunnable, 1000);
        }
    };

    public static void start(Context context, DeviceInfo deviceInfo, Device device) {
        Intent intent = new Intent(context, IjkPlayerActivity.class);
        intent.putExtra(Constants.REQUEST_DEVICE_INFO, deviceInfo);
        intent.putExtra(Constants.REQUEST_DEVICE, device);
        context.startActivity(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        outState.putBoolean(STATE_LAUNCH_SOFT_AP, _deviceSoftApLaunched);
        outState.putSerializable(Constants.REQUEST_DEVICE_INFO, _deviceInfo);
        outState.putSerializable(Constants.REQUEST_DEVICE, _device);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
            _deviceInfo = (DeviceInfo) savedInstanceState.getSerializable(Constants.REQUEST_DEVICE_INFO);
            _device = (Device) savedInstanceState.getSerializable(Constants.REQUEST_DEVICE);
        }


        if (getIntent() != null) {
            _deviceInfo = (DeviceInfo) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE_INFO);
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
        }


        // set content view
        setContentView(R.layout.activity_player_ijk);

        _handler = new Handler();

        if (_playerView == null) {
            initView();
        }

        if (mExoPlayerFullscreen) {
            enterFullScreen();
        } else {

        }

        if (!_deviceSoftApLaunched) {

            if (mWifiManager == null) {
                mWifiManager = WifiManager.getInstance(IjkPlayerActivity.this);
            }

            String wifiSsid = mWifiManager.getConnectWifiSsid();
            if (TextUtils.isEmpty(wifiSsid) || !wifiSsid.startsWith(WifiManager.WEEWA_SOFTAP_NAME)) {
                _handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectSoftApAction = 0;
                        startSoftAp();
                    }
                }, 500);
            } else {
                _deviceSoftApLaunched = true;
            }
        }

        BluetoothHandle.addPlayNotifyListener(this);
        BluetoothHandle.addDeviceUsageInfoNotifyListener(this);
        BluetoothHandle.addSoftApNotifyListener(this);

        BluetoothManager.mInstance.sendCmdUsageInfo();

        _deviceStatusBackgroundCheck = true;
        BluetoothManager.mInstance.sendCmdRecordStatus();
        _deviceStatusChecking = true;
        _handler.postDelayed(_recordStatusTimeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
    }


    private boolean isConnectedSoftAp() {
        if (mWifiManager == null) {
            mWifiManager = WifiManager.getInstance(IjkPlayerActivity.this);
        }

        String wifiSsid = mWifiManager.getConnectWifiSsid();
        if (!TextUtils.isEmpty(wifiSsid) && wifiSsid.startsWith(WifiManager.WEEWA_SOFTAP_NAME)) {
            return true;
        }
        return false;
    }

    private void initView() {
        _contentVerticalLayout = findViewById(R.id.content_layout_vertical);
        _contentLayout = findViewById(R.id.content_layout);
        _controlLayout = findViewById(R.id.control_layout);

        _recordTv = findViewById(R.id.record_time);
        _energyLayout = findViewById(R.id.layout_energy);
        _storageLayout = findViewById(R.id.layout_storage);
        _exitBtn = findViewById(R.id.btn_exit);
        _localLayout = findViewById(R.id.ll_local);
        _settingLayout = findViewById(R.id.ll_setting);
        _playerButton = findViewById(R.id.btn_recorder);
        _indicatorView = findViewById(R.id.indicator);

        _countDownView = findViewById(R.id.lay_count_down);

        _countDownView.setVisibility(View.GONE);

        _playerView = findViewById(R.id.video_view);

        _titleBar = findViewById(R.id.title_bar);
        _panelSetting = findViewById(R.id.panel_setting);
        _storageTv = findViewById(R.id.tv_storage_info);
        _energyTv = findViewById(R.id.tv_energy);

//        _fullView = _playerView.findViewById(R.id.exo_max_btn);
//        _fullView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
//            @Override
//            public boolean onClicked() {
//
////                if (isPortrait) {
////                    isPortrait = false;
////                    setFullScreen(true);
////                } else {
////                    isPortrait = true;
////                    setFullScreen(false);
////                }
//
//                return true;
//            }
//        });


        _exitBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (isRecord) {
                    showExitRecordDialog();
                } else {
                    MyToolUtils.myToast(IjkPlayerActivity.this,"确认退出？");
                    if (_wifiInfo != null) {
                        mWifiManager.disconnectNetWork();
                    }
                    _wifiInfo = null;
                    finish();
                }
                return true;
            }
        });

        _localLayout.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (!_deviceSoftApLaunched) {
                    if (!isConnectedSoftAp()) {
                        connectSoftApAction = 1;
                        startSoftAp();
                    } else {
                        _deviceSoftApLaunched = true;
                        startCameraVideoFilterActivity();
                    }
                } else {
                    startCameraVideoFilterActivity();
                }

                return true;
            }
        });

        _settingLayout.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                CameraInfoActivity.start(IjkPlayerActivity.this, _deviceUsageInfo, _deviceInfo, _device);

                return true;
            }
        });

        _playerButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (isConnectedSoftAp()) {
                    if (!isRecord) {
                        if (!_deviceStatusChecked) {
                            _deviceStatusChecking = true;
                            _deviceStatusBackgroundCheck = false;
                            BluetoothManager.mInstance.sendCmdRecordStatus();
                            _handler.postDelayed(_timeOutRunnable, 5000);
                            showConnectingDialog(IjkPlayerActivity.this.getString(R.string.record_launching));
                        } else {
                            if (_deviceIsRecord) {
                                showRecordingDialog();
                            } else {
                                startRecord();
                                showConnectingDialog(IjkPlayerActivity.this.getString(R.string.record_launching));
                            }
                        }


                    } else {
                        BluetoothManager.mInstance.sendCmdStopRecord();
                        showConnectingDialog(IjkPlayerActivity.this.getString(R.string.record_ending));
                        _handler.postDelayed(_timeOutRunnable, 5000);
                    }
                } else {
                    if (!_deviceSoftApLaunched) {
                        startSoftAp();
                    } else {
                        searchSoftAp();
                    }
                }

                return true;
            }
        });

        _energyLayout.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (!requestEnergy) {
                    requestEnergy = true;
                    BluetoothManager.mInstance.sendCmdEnergyStatus();
                    _handler.postDelayed(_energyTimeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                } else {
                    ToastUtil.s(IjkPlayerActivity.this, "正在查询，请勿重复请求");
                }
                return true;
            }
        });

        _storageLayout.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (!requestStorage) {
                    requestStorage = true;
                    BluetoothManager.mInstance.sendCmdStorageStatus();
                    _handler.postDelayed(_storageTimeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                } else {
                    ToastUtil.s(IjkPlayerActivity.this, "正在查询，请勿重复请求");
                }
                return true;
            }
        });

        _playerView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (!isPlay) {
                    startPlay();
                }
                return false;
            }
        });
    }

    private void startRecord() {
        RecordInfoBean recordInfoBean = new RecordInfoBean("weewa2_test", 15000);
        recordInfoBean.author = "zzh";
        recordInfoBean.width = 1920;
        recordInfoBean.height = 1080;
        recordInfoBean.place = "桃源球场";
        recordInfoBean.name = "桃源球场8v8";
        recordInfoBean.time = 1234;
        BluetoothManager.mInstance.sendCmdStartRecord(recordInfoBean);
        _handler.postDelayed(_timeOutRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppTrace.d(TAG, "onPause...");

        if (_playerView.isPlaying()) {
            _playerView.stopPlayback();
            _playerView.release(true);
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTrace.d(TAG, "onResume...");

        if (_playerView == null) {
            initView();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        AppTrace.d(TAG, "onStop...");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppTrace.d(TAG, "onDestroy...");
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.release();
        }

        BluetoothHandle.removePlayNotifyListener(this);
        BluetoothHandle.removeDeviceUsageInfoNotifyListener(this);
        BluetoothHandle.removeSoftApNotifyListener(this);
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait) {
            // 如果当前是横屏，则切换为竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isPortrait = true;
        } else {
            isPortrait = false;
            super.onBackPressed();
        }
    }

    private void startCameraVideoFilterActivity() {
        CameraVideoFilterListActivity.start(IjkPlayerActivity.this, _deviceUsageInfo, _deviceInfo, _device);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);

        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    private void startPlay() {
        retryCount = 0;
        if (ijkMediaPlayer == null) {
            //初始化播放库
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");

            ijkMediaPlayer = new IjkMediaPlayer();


            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "rtsp,rtmp");
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "rtsp,rtmp,crypto,file,http,https,tcp,tls,udp");

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "timeout", 90000);

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

            // 黑屏
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg4", 1);
            // 降低 播放 rtmp 播放的延迟
            ijkMediaPlayer.setOption(1, "analyzemaxduration", 100L);
            ijkMediaPlayer.setOption(1, "probesize", 10240L);
            ijkMediaPlayer.setOption(1, "flush_packets", 1L);
            ijkMediaPlayer.setOption(4, "packet-buffering", 0L);
            //丢帧
            ijkMediaPlayer.setOption(4, "framedrop", 1L);

            //硬解码造成黑屏无声
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);


            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);

            // 清空DNS，因为DNS的问题报10000
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);

            ijkMediaPlayer.setLogEnabled(true);

            //使用AndroidMediaController类控制播放相关操作
            AndroidMediaController mMediaController = new AndroidMediaController(IjkPlayerActivity.this, false);
            //mMediaController.setSupportActionBar(actionBar);

            _playerView.setMediaController(mMediaController);

            // 测试可用地址
            // 香港财经  rtmp://202.69.69.180:443/webcast/bshdlive-pc
            // 湖南卫视   rtmp://58.200.131.2:1935/livetv/hunantv
            // 美国2, rtmp://media3.scctv.net/live/scctv_800

        } else {
            ijkMediaPlayer.stop();
        }

        //设置要播放的直播或者视频的地址：
        _playerView.setVideoPath(_url);
        //开始播放
        _playerView.start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 隐藏状态栏和导航栏
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            isPortrait = false;


        } else {
            // 显示状态栏和导航栏
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            // 竖屏
            isPortrait = true;
        }
    }


    @Override
    public void onRecordVideoStart() {
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissConnectingDialog();
                isRecord = true;
                _playerButton.setBackgroundResource(R.mipmap.player_recorder_stop);

                startPlay();

                _handler.postDelayed(_recordCountDownRunnable, 1000);
            }
        }, 3000);

    }


    private void showExitRecordDialog() {
        if (_dialog != null && _dialog.isShowing()) {
            _dialog.dismiss();
        }
        _dialog = null;
        _dialog = new ModalDialog(IjkPlayerActivity.this);
        _dialog.setMessage("正在录制，是否退出？");
        _dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                return true;
            }
        });

        _dialog.setRightButton(getString(R.string.exit), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (_wifiInfo != null) {
                    mWifiManager.disconnectNetWork();
                }
                _wifiInfo = null;
                BluetoothManager.mInstance.sendCmdStopRecord();
                finish();
                return true;
            }
        });

        _dialog.show();
    }

    private void showRecordingDialog() {
        if (_dialog != null && _dialog.isShowing()) {
            _dialog.dismiss();
        }
        _dialog = null;
        _dialog = new ModalDialog(IjkPlayerActivity.this);
        _dialog.setMessage("录像程序已启动，是否重新启动？");
        _dialog.setLeftButton(getString(R.string.play), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                dismissConnectingDialog();
                isRecord = true;
                startPlay();

                return true;
            }
        });

        _dialog.setRightButton(getString(R.string.record_restart), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                return true;
            }
        });

        _dialog.show();
    }

    @Override
    public void getRecordVideoStatus(int status, int time) {
        _deviceStatusChecking = false;
        _deviceStatusChecked = true;
        _deviceIsRecord = status == 1;

        if (_deviceStatusBackgroundCheck) {
            return;
        }

        _handler.post(new Runnable() {
            @Override
            public void run() {
                showConnectingDialog(IjkPlayerActivity.this.getString(R.string.record_launching));
                if (!_deviceIsRecord) {
                    startRecord();
                } else {
                    showRecordingDialog();
                }
            }
        });
    }

    @Override
    public void onRecordVideoStop() {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                dismissConnectingDialog();
                isRecord = false;
                _deviceIsRecord = false;
                _playerButton.setBackgroundResource(R.mipmap.player_recorder_btn);
                if(_playerView.isPlaying()) {
                    _playerView.stopPlayback();
                    _playerView.release(true);
                }
                IjkMediaPlayer.native_profileEnd();

            }
        });
    }

    @Override
    public void onRecordException(String msg) {

    }

    private void updateStorageInfo(StorageBean storageBean) {
        String totalStr = String.format("%dG", (int) (storageBean.total / 1024 / 1024 / 1024));
        String usedStr = "";
        if (storageBean.used / 1024 / 1024 > 1024) {
            if (storageBean.used * 1.0 / 1024 / 1024 / 1024 > 10) {
                usedStr = String.format("%dG", (int) (storageBean.used * 1.0 / 1024 / 1024 / 1024));
            } else {
                usedStr = String.format("%.1fG", storageBean.used * 1.0 / 1024 / 1024 / 1024);
            }
        } else {
            usedStr = String.format("%.2fM", storageBean.used * 1.0 / 1024 / 1024);
        }

        _storageTv.setText(String.format("%s/%s", usedStr, totalStr));
    }

    @Override
    public void onStorageInfo(StorageBean storageBean) {

        requestStorage = false;
        _handler.post(new Runnable() {
            @Override
            public void run() {
                updateStorageInfo(storageBean);
            }
        });
    }

    @Override
    public void onEnergy(int energy) {
        requestEnergy = false;
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _energyTv.setText("" + energy + "%");

            }
        });
    }

    @Override
    public void onDeviceInfo(DeviceUsageInfo data) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _deviceUsageInfo = data;
                if (data != null) {
                    _energyTv.setText("" + data.energy + "%");

                    updateStorageInfo(data.storage);
                    _handler.postDelayed(_deviceUsageInfoRunnable, DEVICE_USAGE_INFO_DELAY);
                }
            }
        });
    }

    @Override
    public void onTemperature(float temperature) {

    }

    /* 退出全屏 */
    public void exitFullScreen() {
        // 旋转屏幕
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 将JJPlayerView从Activity的R.id.content移除，加入RecyclerView的ItemView下
        if (IjkPlayerActivity.this != null) {
            ViewGroup controlView = findViewById(R.id.control_layout);
            controlView.setVisibility(View.VISIBLE);

            ViewGroup parentView = findViewById(R.id.content_layout);
            parentView.removeView(_playerView);

            ViewGroup parentVerticalView = findViewById(R.id.content_layout_vertical);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            parentVerticalView.addView(_playerView, params);

            ImageView fullScreen = _playerView.findViewById(R.id.exo_max_btn);
            fullScreen.setImageResource(R.mipmap.fullscreen_expand);
        }
    }

    /* 进入全屏 */
    public void enterFullScreen() {
        // 旋转屏幕
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 将JJPlayerView从Activity的R.id.content移除，加入RecyclerView的ItemView下
        if (IjkPlayerActivity.this != null) {
            ViewGroup controlView = findViewById(R.id.control_layout);
            controlView.setVisibility(View.GONE);

            ViewGroup parentVerticalView = findViewById(R.id.content_layout_vertical);
            parentVerticalView.removeView(_playerView);

            ViewGroup parentView = findViewById(R.id.content_layout);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            parentView.addView(_playerView, params);


            ImageView fullScreen = _playerView.findViewById(R.id.exo_max_btn);
            fullScreen.setImageResource(R.mipmap.fullscreen_skrink);
        }
    }


    private void startSoftAp() {
        BluetoothManager.mInstance.sendCmdSoftapOpen(WifiManager.WEEWA_SOFTAP_NAME, WifiManager.WEEWA_SOFTAP_PWD);
        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
        _requestSoftAp = true;
    }

    private void searchSoftAp() {
        if (mWifiManager == null) {
            mWifiManager = WifiManager.getInstance(IjkPlayerActivity.this);
        }

        mWifiManager.search(WifiManager.WEEWA_SOFTAP_NAME, new WifiManager.SearchWifiListener() {
            @Override
            public void onScanStart() {
                Log.d(TAG, "onScanStart...");
//                        mLoadingView.show("正在扫描附近设备");
                if (!BaseAppUtil.isDestroy(IjkPlayerActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showConnectingDialog("正在扫描附近设备");
                        }
                    });
                }

            }

            @Override
            public void onScanFailed(WifiManager.ErrorType errorType) {
                Log.d(TAG, "onScanFailed..." + errorType);
                if (!BaseAppUtil.isDestroy(IjkPlayerActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnectingDialog();
                            switch (errorType) {
                                case NO_WIFI_FOUND:
                                    break;
                                case SEARCH_WIFI_TIMEOUT:
                                    break;
                            }
                        }
                    });
                }

            }

            @Override
            public void onScanSuccess(List<WifiInfo> results) {
                Log.d(TAG, "onScanSuccess..." + results.size());
                if (!BaseAppUtil.isDestroy(IjkPlayerActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnectingDialog();
                            for (WifiInfo info : results) {
                                Log.d(TAG, info.toString());
                            }
                            if (results.isEmpty()) {
//                            tvStatus.setText("未找到热点");
//                            showState(STATE.Layout_NoDevice);
                            } else {
                                showSoftApDialog(results.get(0));
                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onSoftApStart(String ssid, String password) {
        _handler.removeCallbacks(_timeOutRunnable);
        _handler.post(new Runnable() {
            @Override
            public void run() {
                AppTrace.d(TAG, "热点启动成功, ip = " + BleConstants.SOFT_AP_IP);
                _deviceSoftApLaunched = true;
                searchSoftAp();
            }
        });
    }

    @Override
    public void onSoftApStartFail(String message) {
        _handler.removeCallbacks(_timeOutRunnable);
    }

    @Override
    public void onSoftApStop() {

    }

    @Override
    public void onSoftApStopFail(String message) {

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

    private void connectToWifiSoftAp(WifiInfo wifiInfo) {
        if (mWifiManager != null) {
            mWifiManager.connect(wifiInfo, WifiManager.WEEWA_SOFTAP_PWD, new WifiManager.WifiConnectListener() {
                @Override
                public void onConnectStart(WifiInfo info) {
                    AppTrace.d(TAG, "connect ....");
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showConnectingDialog("正在加入热我相机的无线网络");
                        }
                    });
                }

                @Override
                public void onConnectFailed(WifiInfo info, int error) {
                    AppTrace.d(TAG, "connect fail....");
                    useSoftAp = false;
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnectingDialog();

                            ModalDialog dialog = new ModalDialog(IjkPlayerActivity.this);
                            dialog.setMessage(getString(R.string.reconnect_on_connect_fail));
                            dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
                                @Override
                                public boolean onClicked() {
                                    dialog.dismiss();
                                    return true;
                                }
                            });
                            dialog.setRightButton(getString(R.string.retry), new ClickGuard.GuardedOnClickListener() {
                                @Override
                                public boolean onClicked() {
                                    AppTrace.d(TAG, "retry connect to soft ap");
                                    connectToWifiSoftAp(wifiInfo);
                                    return true;
                                }
                            });
                            dialog.show();
                        }
                    });
                }

                @Override
                public void onConnectSuccess(WifiInfo info, int ip) {
                    AppTrace.d(TAG, "connect success....");

                    _wifiInfo = wifiInfo;
                    useSoftAp = true;

                    _url = "rtmp://" + BleConstants.SOFT_AP_IP + ":1935/live/preview2";
                    _handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnectingDialog();
                            ToastUtil.s(IjkPlayerActivity.this, "连接成功");

                            if (connectSoftApAction == 1) {
                                startCameraVideoFilterActivity();
                                connectSoftApAction = 0;
                            }

                        }
                    }, 2000);
                }
            });
        }
    }


    private void disconnectWifi() {
        if (mWifiManager != null) {
            mWifiManager.disconnectNetWork();
        }
    }



    private void showSoftApDialog(WifiInfo wifiInfo) {

        ModalDialog dialog = new ModalDialog(IjkPlayerActivity.this);
        dialog.setTitle("是否加入热我相机的无线网络");
        dialog.setMessage(String.format("wifi： %s\n密码：%s", WifiManager.WEEWA_SOFTAP_NAME, WifiManager.WEEWA_SOFTAP_PWD));
        dialog.setRightButton(getString(R.string.wifi_join), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                HotmeWiFiManager.startWifiSettingPage(IjkPlayerActivity.this);
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


    private void showConnectingDialog(String message) {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
            _connectingDialog = null;
        }
        _connectingDialog = new BluetoothLoadingDialog(IjkPlayerActivity.this);
        _connectingDialog.setMessage(message);
        _connectingDialog.show();
    }


    private void dismissConnectingDialog() {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
        }
        _connectingDialog = null;
    }


}
