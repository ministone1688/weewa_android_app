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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.EventLogger;
import com.umeng.socialize.UMShareAPI;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleSoftApNotifyListener;
import com.xh.hotme.event.DeviceEnergyInfoEvent;
import com.xh.hotme.event.DeviceLowerPoweroffEvent;
import com.xh.hotme.event.DeviceStorageInfoEvent;
import com.xh.hotme.event.DeviceUsageInfoEvent;
import com.xh.hotme.event.RenameEvent;
import com.xh.hotme.event.RkIpcEvent;
import com.xh.hotme.softap.CameraSoftApActivity;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.NetUtil;
import com.xh.hotme.utils.TimeUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.widget.playerview.MyPlayerView;
import com.xh.hotme.wifi.HotmeWiFiManager;
import com.xh.hotme.wifi.WifiInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class PlayerActivity extends BaseActivity implements IPlayControlListener, IBleSoftApNotifyListener {

    private final static String TAG = PlayerActivity.class.getSimpleName();

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

    ImageView _fullView;

    Button _playerButton;

    MyPlayerView _playerView;

    ExoPlayer _player;

    FrameLayout _countDownView;
    TextView _countDownTv;
    TextView _recordTv;

    LinearLayout _panelSetting;
    RelativeLayout _titleBar;

    FrameLayout _contentLayout, _contentVerticalLayout;

    LinearLayout _controlLayout, _storageLayout, _energyLayout;

    String _url = "rtmp://" + BleConstants.SOFT_AP_IP + ":1935/live/preview";

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

    String _softap_ssid = WifiManager.WEEWA_SOFTAP_NAME;
    String _softap_password = WifiManager.WEEWA_SOFTAP_PWD;
    int _softap_status;

    NetworkBean _networkInfo;

    ModalDialog _dialog;

    boolean requestEnergy = false;
    boolean requestStorage = false;

    boolean isPlay = false;
    boolean isPreviewInited = false;

    boolean isRkipcRunning = false;

    int retryCount = 0;

    long _recordCountDown = 0;

    int _recordTime = 0;

    int connectSoftApAction = 0; //1 for video

    private final int DEVICE_USAGE_INFO_DELAY = 60000;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            dismissConnectingDialog();

        }
    };


    private final Runnable _openSoftApRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            ToastUtil.s(PlayerActivity.this, "打开热点超时");
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

        }
    };


    private final Runnable _recordTimeRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            _recordTime += 1;

            _handler.postDelayed(_recordTimeRunnable, 1000);
        }
    };

    private final Runnable _retryPlayRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            startPlay();
        }
    };
    private final Runnable _startRkipcTimeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            dismissConnectingDialog();
            ToastUtil.s(PlayerActivity.this, "启动录像程序超时");
        }
    };

    public static void start(Context context, DeviceInfo deviceInfo, Device device) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(Constants.REQUEST_DEVICE_INFO, deviceInfo);
        intent.putExtra(Constants.REQUEST_DEVICE, device);
        context.startActivity(intent);
    }


    public static void start(Context context, DeviceInfo deviceInfo, Device device, NetworkBean network) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(Constants.REQUEST_DEVICE_INFO, deviceInfo);
        intent.putExtra(Constants.REQUEST_DEVICE, device);
        intent.putExtra(Constants.REQUEST_NETWORK_INFO, network);
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
        outState.putSerializable(Constants.REQUEST_NETWORK_INFO, _networkInfo);
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
            _networkInfo = (NetworkBean) savedInstanceState.getSerializable(Constants.REQUEST_NETWORK_INFO);
        }

        if (getIntent() != null) {
            _deviceInfo = (DeviceInfo) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE_INFO);
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
            _networkInfo = (NetworkBean) getIntent().getSerializableExtra(Constants.REQUEST_NETWORK_INFO);
            if (_networkInfo != null && _networkInfo.getAp() != null && !TextUtils.isEmpty(_networkInfo.getAp().ssid)) {
                _softap_ssid = _networkInfo.getAp().ssid;
                _softap_password = _networkInfo.getAp().password;
                _softap_status = _networkInfo.getAp().st;
                _deviceSoftApLaunched = _softap_status == 1 ? true : false;
            }
        }

        // set content view
        setContentView(R.layout.activity_player);

        _handler = new Handler();

        if (_playerView == null) {
            initView();
        }

        if (mExoPlayerFullscreen) {
            enterFullScreen();
        } else {

        }

        BluetoothManager.mInstance.sendCmdStartRkipc();
        _handler.postDelayed(_startRkipcTimeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
        showConnectingDialog("正在启动录像程序");

        BluetoothHandle.addPlayNotifyListener(this);
        BluetoothHandle.addSoftApNotifyListener(this);

        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraMonitor.getInstance().start();
            }
        }, 2000);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private boolean isConnectedSoftAp() {
        if (mWifiManager == null) {
            mWifiManager = WifiManager.getInstance(PlayerActivity.this);
        }

        String wifiSsid = mWifiManager.getConnectWifiSsid();
        if (!TextUtils.isEmpty(wifiSsid) && wifiSsid.startsWith(_softap_ssid)) {
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

        _fullView = _playerView.findViewById(R.id.exo_max_btn);
        _fullView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

//                if (isPortrait) {
//                    isPortrait = false;
//                    setFullScreen(true);
//                } else {
//                    isPortrait = true;
//                    setFullScreen(false);
//                }

                return true;
            }
        });

        _exitBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (isRecord) {
                    showExitRecordModeDialog();
                } else {
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
                CameraInfoActivity.start(PlayerActivity.this, _deviceUsageInfo, _deviceInfo, _device);
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
                            showConnectingDialog(PlayerActivity.this.getString(R.string.record_status));
                            _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                        } else {
                            if (_deviceIsRecord) {
                                showRecordingDialog();
                            } else {
                                startRecord();
                                showConnectingDialog(PlayerActivity.this.getString(R.string.record_launching));
                            }
                        }
                    } else {
                        BluetoothManager.mInstance.sendCmdStopRecord();
                        showConnectingDialog(PlayerActivity.this.getString(R.string.record_ending));
                        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
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
                    ToastUtil.s(PlayerActivity.this, "正在查询，请勿重复请求");
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
                    ToastUtil.s(PlayerActivity.this, "正在查询，请勿重复请求");
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
                return true;
            }
        });
    }

    private void startRecord() {
        RecordInfoBean recordInfoBean = new RecordInfoBean("weewa2_test", 8192);
        recordInfoBean.author = "zzh";
        recordInfoBean.width = 1920;
        recordInfoBean.height = 1080;
        recordInfoBean.place = "桃源球场";
        recordInfoBean.name = "桃源球场8v8";
        recordInfoBean.motionType = "足球";
        recordInfoBean.placeType = "8人场";
        recordInfoBean.city = "广东省深圳市";  //城市
        recordInfoBean.hostTeam = "";
        recordInfoBean.guestTeam = "";
        recordInfoBean.time = System.currentTimeMillis();
        BluetoothManager.mInstance.sendCmdStartRecord(recordInfoBean);
        _handler.postDelayed(_timeOutRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppTrace.d(TAG, "onPause...");

        if (_playerView != null && _playerView.getPlayer() != null) {
            mResumeWindow = _playerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, _playerView.getPlayer().getContentPosition());
            _playerView.getPlayer().pause();
            _playerView.getPlayer().release();
            _player = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTrace.d(TAG, "onResume...");

        if (_playerView == null) {
            initView();
        }
        if (isPreviewInited && isConnectedSoftAp()) {
            startPlay();
        }

//        if (isPlay) {
//            startPlay();
//        }
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

        CameraMonitor.getInstance().stop();

        if (_player != null) {
            _player.release();
        }

        _handler.removeCallbacks(_recordTimeRunnable);
        _handler.removeCallbacksAndMessages(null);

        BluetoothHandle.removePlayNotifyListener(this);
        BluetoothHandle.removeSoftApNotifyListener(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait) {
            // 如果当前是横屏，则切换为竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isPortrait = true;
        } else {
            showExitPlayerDialog();
        }
    }

    private void startCameraVideoFilterActivity() {
        CameraVideoFilterListActivity.start(PlayerActivity.this, _deviceUsageInfo, _deviceInfo, _device);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);

        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        AppTrace.d(TAG, "onActivityResult: request code=" + requestCode);
        if (!isPlay && isConnectedSoftAp()) {
            isPreviewInited = true;
            startPlay();
        }
    }

    private void startPlay() {

        String localIp = NetUtil.getLocalIpAddress(PlayerActivity.this);
        AppTrace.d(TAG, "local ip: " + localIp);

        if (_player == null) {
            LoadControl loadControl = new DefaultLoadControl.Builder()
                    .setAllocator(new DefaultAllocator(true, 16))
                    .setBufferDurationsMs(15000, 50000, 2000, 2000)
//                .setBufferDurationsMs(MIN_BUFFER_DURATION,
//                        MAX_BUFFER_DURATION,
//                        MIN_PLAYBACK_START_BUFFER,
//                        MIN_PLAYBACK_RESUME_BUFFER)
                    .setTargetBufferBytes(-1)
                    .setPrioritizeTimeOverSizeThresholds(true).createDefaultLoadControl();

            DefaultAllocator defaultAllocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);


            ExoTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(PlayerActivity.this, trackSelectionFactory);

            _player = new ExoPlayer.Builder(PlayerActivity.this)
                    .setRenderersFactory(new DefaultRenderersFactory(PlayerActivity.this)
                            .setEnableDecoderFallback(true))
                    .setBandwidthMeter(new DefaultBandwidthMeter())
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .build();

            _player.addAnalyticsListener(new EventLogger());
            _player.addListener(new Player.Listener() {
                @Override
                public void onEvents(Player player, Player.Events events) {
                    Player.Listener.super.onEvents(player, events);
                }

                public void onLoadingChanged(boolean isLoading) {
                    AppTrace.d(TAG, "onLoadingChanged: " + isLoading);
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int status) {
                    AppTrace.d(TAG, "onPlayerStateChanged: playWhenReady+" + playWhenReady + ", status=" + status);
                    switch (status) {
                        case Player.STATE_IDLE:
                            AppTrace.d(TAG, "onPlayerStateChanged: 空闲");
//                            if (!isPlay) {
//                                retryCount++;
//                                if (retryCount < 20) {
//                                    _handler.postDelayed(_retryPlayRunnable, 2000);
//                                } else {
//                                    retryCount = 0;
//                                }
//                            }
                            break;

                        case Player.STATE_BUFFERING:
                            AppTrace.d(TAG, "onPlayerStateChanged: 加载中");
                            ToastUtil.s(PlayerActivity.this, "加载中");
                            break;
                        case Player.STATE_READY:
                            AppTrace.d(TAG, "onPlayerStateChanged: 播放中");
//                            ToastUtil.s(PlayerActivity.this, "播放中");
                            isPlay = true;
                            retryCount = 0;
                            break;
                        case Player.STATE_ENDED:
                            AppTrace.d(TAG, "onPlayerStateChanged: 播放完成");
//                            ToastUtil.s(PlayerActivity.this, "播放完成");
                            break;
                        default:
                            AppTrace.d(TAG, "onPlayerStateChanged: " + status);
                            break;
                    }
                }

                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason) {
                    Log.e("ExoPlayer", "onPlayWhenReadyChanged:" + reason);
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e("ExoPlayer", "onPlayerError:" + error.getMessage());
                    switch (error.errorCode) {
                        case ExoPlaybackException.ERROR_CODE_IO_UNSPECIFIED:
                            Log.e(TAG, "TYPE_SOURCE: " + error.getMessage());
                            break;

                        case ExoPlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
                            Log.e(TAG, "connect fail: " + error.getMessage());
                            break;
                        case ExoPlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                            Log.e(TAG, "connect time cout: " + error.getMessage());
                            break;
                        case ExoPlaybackException.ERROR_CODE_REMOTE_ERROR:
                            Log.e(TAG, "remote error: " + error.getMessage());
                            break;
                        case ExoPlaybackException.ERROR_CODE_UNSPECIFIED:
                            Log.e(TAG, "unknow error: " + error.getMessage());
                            break;
                    }
                    isPlay = false;
                }

            });

            _playerView.requestFocus();
            _playerView.setPlayer(_player);
        } else {
            _player.stop();
        }

        Uri url = Uri.parse(_url);
        AppTrace.d(TAG, "play url: " + _url);


        TransferListener transferListener = new TransferListener() {
            @Override
            public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                Log.e(TAG, "onTransferInitializing: isNetwork = " + isNetwork);
            }

            @Override
            public void onTransferStart(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                Log.e(TAG, "onTransferStart: isNetwork = " + isNetwork);
            }

            @Override
            public void onBytesTransferred(DataSource source, DataSpec dataSpec, boolean isNetwork, int bytesTransferred) {
//                Log.e(TAG, "onBytesTransferred: isNetwork = " + isNetwork);
            }

            @Override
            public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                Log.e(TAG, "onTransferEnd: isNetwork = " + isNetwork);
            }
        };

//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(PlayerActivity.this, Util.getUserAgent(PlayerActivity.this, "HotmePlayer"), transferListener);
//        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
        dataSourceFactory.setTransferListener(transferListener);
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url));
        _player.setMediaSource(videoSource);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            _playerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
        }

        //auto start playing
        _player.setPlayWhenReady(true);

        // Prepare the player with the source.
        _player.prepare();


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
            if (_playerView != null)
                _playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        } else {
            // 显示状态栏和导航栏
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            // 竖屏
            isPortrait = true;
            if (_playerView != null)
                _playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
    }

    private void setFullScreen(boolean enableFullScreen) {
        if (enableFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            _playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            isPortrait = false;
            mExoPlayerFullscreen = true;
            enterFullScreen();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            _playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            isPortrait = true;
            mExoPlayerFullscreen = false;
            exitFullScreen();
        }
    }

    @Override
    public void onRecordVideoStart() {

        if (!BaseAppUtil.isDestroy(PlayerActivity.this)) {
            _handler.post(new Runnable() {
                @Override
                public void run() {
                    updateConnectMessage("正在连接相机画面");
                }
            });
        }


        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissConnectingDialog();
                isRecord = true;
                _playerButton.setBackgroundResource(R.mipmap.player_recorder_stop);

                if (!isPreviewInited) {
                    isPreviewInited = true;
                    startPlay();
                }

                _handler.postDelayed(_recordCountDownRunnable, 1000);
            }
        }, 10000);

    }


    private void showExitPlayerDialog() {
        showExitRecordDialog("是否退出相机？");
    }

    private void showExitRecordModeDialog() {
        showExitRecordDialog("正在录制，是否退出？");
    }


    private void showExitRecordDialog(String message) {
        if (_dialog != null && _dialog.isShowing()) {
            _dialog.dismiss();
        }
        _dialog = null;
        _dialog = new ModalDialog(PlayerActivity.this);
        _dialog.setMessage(message);
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
//                BluetoothManager.mInstance.sendCmdStopRecord();
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
        _dialog = new ModalDialog(PlayerActivity.this);
        _dialog.setMessage("录像程序已启动，是否重新录制？");
        _dialog.setLeftButton(getString(R.string.play), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                dismissConnectingDialog();
                isRecord = true;
                isPreviewInited = true;
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
        _recordTime = TimeUtil.getStartDuration(time);

        AppTrace.d(TAG, String.format("record status: %d, time: %d", status, time));


        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (_deviceStatusBackgroundCheck) {

                    _handler.postDelayed(_recordTimeRunnable, 1000);
                    _recordTv.setText(TimeUtil.intToTimeHour(_recordTime));
                    _playerButton.setBackgroundResource(R.mipmap.player_recorder_stop);
                    if (isPreviewInited && !isPlay) {
                        startPlay();
                    }

                    return;
                }else {

                }


                showConnectingDialog(PlayerActivity.this.getString(R.string.record_launching));
                if (!_deviceIsRecord) {
                    startRecord();
                } else {

                    _handler.postDelayed(_recordTimeRunnable, 1000);
                    _recordTv.setText(TimeUtil.intToTimeHour(_recordTime));
                    _playerButton.setBackgroundResource(R.mipmap.player_recorder_stop);
                    if (isPreviewInited && !isPlay) {
                        startPlay();
                    }
                }
            }
        });

        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (!_deviceIsRecord) {
                    showConnectingDialog(PlayerActivity.this.getString(R.string.record_launching));
                    startRecord();
                } else {
                    _handler.postDelayed(_recordTimeRunnable, 1000);
                    _recordTv.setText(TimeUtil.intToTimeHour(_recordTime));
                    _playerButton.setBackgroundResource(R.mipmap.player_recorder_stop);
                    if (isPreviewInited && !isPlay) {
                        startPlay();
                    }
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
                if (_player != null) {
                    _player.stop();
                }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnergyInfoEvent(DeviceEnergyInfoEvent event) {
        AppTrace.d(TAG, "onEnergyInfoEvent");
        requestEnergy = false;
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (event != null ) {
                    _energyTv.setText("" + event.percent + "%");
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStorageInfoEvent(DeviceStorageInfoEvent event) {
        AppTrace.d(TAG, "onStorageInfoEvent");
        requestStorage = false;
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (event != null && event.storageBean != null) {
                    updateStorageInfo(event.storageBean);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceUsageInfoEvent(DeviceUsageInfoEvent data) {
        AppTrace.d(TAG, "onDeviceUsageInfoEvent");
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (data != null && data.deviceUsageInfo != null) {
                    updateUsageInfo(data.deviceUsageInfo);
                }
            }
        });
    }

    public void updateUsageInfo(DeviceUsageInfo data) {
        _deviceUsageInfo = data;
        if (_deviceUsageInfo != null) {
            _energyTv.setText("" + _deviceUsageInfo.energy + "%");

            updateStorageInfo(_deviceUsageInfo.storage);
        }

    }


    /* 退出全屏 */
    public void exitFullScreen() {
        // 旋转屏幕
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 将JJPlayerView从Activity的R.id.content移除，加入RecyclerView的ItemView下
        if (PlayerActivity.this != null) {
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
        if (PlayerActivity.this != null) {
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
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                BluetoothManager.mInstance.sendCmdSoftapOpen("", "");
                _handler.postDelayed(_openSoftApRunnable, BluetoothManager.RESP_TIMEOUT);
                _requestSoftAp = true;
            }
        }, 2000);
    }

    private void searchSoftAp() {
        if (mWifiManager == null) {
            mWifiManager = WifiManager.getInstance(PlayerActivity.this);
        }

        mWifiManager.search(_softap_ssid, new WifiManager.SearchWifiListener() {
            @Override
            public void onScanStart() {
                Log.d(TAG, "onScanStart...");
//                        mLoadingView.show("正在扫描附近设备");
                if (!BaseAppUtil.isDestroy(PlayerActivity.this)) {
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
                if (!BaseAppUtil.isDestroy(PlayerActivity.this)) {
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
                if (!BaseAppUtil.isDestroy(PlayerActivity.this)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnectingDialog();
//                            for (WifiInfo info : results) {
//                                Log.d(TAG, info.toString());
//                            }
//                            if (results.isEmpty()) {
////                            tvStatus.setText("未找到热点");
////                            showState(STATE.Layout_NoDevice);
//                            } else {
                            showSoftApDialog();
//                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onSoftApStart(String ssid, String password) {
        AppTrace.d(TAG, "onSoftApStart: ssid=" + ssid + ", password=" + password);
        _softap_ssid = ssid;
        _softap_password = password;
        _softap_status = 1;
        _deviceSoftApLaunched = true;
        _handler.removeCallbacks(_openSoftApRunnable);
        _handler.post(new Runnable() {
            @Override
            public void run() {
                AppTrace.d(TAG, "热点启动成功, ip = " + BleConstants.SOFT_AP_IP);
                searchSoftAp();
            }
        });
    }

    @Override
    public void onSoftApStartFail(String message) {
        _handler.removeCallbacks(_openSoftApRunnable);
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


    private void showSoftApStartDialog() {

        ModalDialog dialog = new ModalDialog(PlayerActivity.this);
        dialog.setMessage(String.format("开启相机热点"));
        dialog.setRightButton(getString(R.string.wifi_open), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                CameraSoftApActivity.startActivityForResult(PlayerActivity.this, _softap_ssid, _softap_password, _softap_status, CameraSoftApActivity.REQUEST_SOFT_AP_PREVIEW);
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

    private void showSoftApDialog() {

        ModalDialog dialog = new ModalDialog(PlayerActivity.this);
        dialog.setMessage(String.format("加入%s的热点网络，预览比赛视频", _softap_ssid));
//        dialog.setMessage(String.format("wifi： %s\n密码：%s", _softap_ssid, _softap_password));
        dialog.setRightButton(getString(R.string.wifi_join), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                HotmeWiFiManager.startWifiSettingPage(PlayerActivity.this, Constants.REQUEST_CODE_PREVIEW_WIFI);
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

    private void disconnectWifi() {
        if (mWifiManager != null) {
            mWifiManager.disconnectNetWork();
        }
    }


    private void showConnectingDialog(String message) {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
            _connectingDialog = null;
        }
        _connectingDialog = new BluetoothLoadingDialog(PlayerActivity.this);
        _connectingDialog.setMessage(message);
        _connectingDialog.show();
    }


    private void dismissConnectingDialog() {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
        }
        _connectingDialog = null;
    }

    private void updateConnectMessage(String message) {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.setMessage(message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void renameDevice(RenameEvent event) {
        AppTrace.d(TAG, "renameDevice");
        if (_deviceInfo != null && event != null && _device != null && _device.address.equalsIgnoreCase(event.address)) {
            _deviceInfo.device_name = event.name;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRkipcStart(RkIpcEvent event) {
        AppTrace.d(TAG, "onRkipcStart");
        _handler.removeCallbacks(_startRkipcTimeOutRunnable);
        dismissConnectingDialog();
        if (event != null) {
            if (event.status == 1) {
                isRkipcRunning = true;
                if (!_deviceSoftApLaunched) {
                    showSoftApStartDialog();
                } else {
                    if (isConnectedSoftAp()) {
                        isPreviewInited = true;
                        startPlay();
                    } else {
                        showSoftApDialog();
                    }
                }

                _deviceStatusBackgroundCheck = true;
                BluetoothManager.mInstance.sendCmdRecordStatus();
                _deviceStatusChecking = true;
                _handler.postDelayed(_recordStatusTimeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);

            } else {
                isRkipcRunning = false;
                ToastUtil.s(PlayerActivity.this, "启动失败");
            }
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLowerPoweroff(DeviceLowerPoweroffEvent event) {
        AppTrace.d(TAG, "renameDevice");
        ModalDialog dialog = new ModalDialog(PlayerActivity.this);
        dialog.setTitle(PlayerActivity.this.getString(R.string.info_device_exit));
        dialog.setMessage(PlayerActivity.this.getString(R.string.info_device_power_off));
        dialog.setRightButton(PlayerActivity.this.getString(R.string.confirm), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                PlayerActivity.this.finish();
                return true;
            }
        });

        dialog.show();
    }

}
