package com.xh.hotme.fragment;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseFragment;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.camera.RecordInfoBean;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.widget.playerview.MyPlayerView;
import com.xh.hotme.wifi.WifiInfo;

import java.util.List;


/**
 * Created by GJK on 2018/11/9.
 */

public class LiveFragment extends BaseFragment implements View.OnClickListener, IBleDeviceUsageInfoNotifyListener {
    private static final String TAG = LiveFragment.class.getSimpleName();
    private static LiveFragment mContentFragment;

    FrameLayout _contentLayout, _contentVerticalLayout;

    LinearLayout _controlLayout, _storageLayout, _energyLayout;

    LinearLayout _panelSetting;
    RelativeLayout _titleBar;

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


    DeviceUsageInfo _deviceUsageInfo;

    ModalDialog _dialog;


    boolean requestEnergy = false;
    boolean requestStorage = false;

    private static boolean useSoftAp = false;
    private static boolean _requestSoftAp = false;

    String _url = "rtmp://" + BleConstants.SOFT_AP_IP + ":1935/live/preview2";


    private final int mResumeWindow = C.INDEX_UNSET;
    private long mResumePosition;

    private final boolean mExoPlayerFullscreen = false;

    private boolean isRecord = false;
    private final boolean _deviceIsRecord = false;
    private final boolean _deviceStatusChecked = false;
    private boolean _deviceStatusChecking = false;
    private boolean _deviceStatusBackgroundCheck = false;

    WifiInfo _wifiInfo;

    private WifiManager mWifiManager;

    boolean _deviceSoftApLaunched = false;

    BluetoothLoadingDialog _connectingDialog;

    Handler _handler;


    private final int DEVICE_USAGE_INFO_DELAY = 60000;

    private final Runnable _deviceUsageInfoRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

            _handler.postDelayed(_deviceUsageInfoRunnable, 1000);
        }
    };

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


    public static LiveFragment newInstance() {
        LiveFragment fragment = new LiveFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        _handler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment_live, container, false);



        return rootView;
    }

    private void initView(View rootView) {
        _contentVerticalLayout = rootView.findViewById(R.id.content_layout_vertical);
        _contentLayout = rootView.findViewById(R.id.content_layout);
        _controlLayout = rootView.findViewById(R.id.control_layout);

        _recordTv = rootView.findViewById(R.id.record_time);
        _energyLayout = rootView.findViewById(R.id.layout_energy);
        _storageLayout = rootView.findViewById(R.id.layout_storage);
        _exitBtn = rootView.findViewById(R.id.btn_exit);
        _playerButton = rootView.findViewById(R.id.btn_recorder);
        _indicatorView = rootView.findViewById(R.id.indicator);

        _countDownView = rootView.findViewById(R.id.lay_count_down);

        _countDownView.setVisibility(View.GONE);

        _playerView = rootView.findViewById(R.id.video_view);

        _titleBar = rootView.findViewById(R.id.title_bar);
        _panelSetting = rootView.findViewById(R.id.panel_setting);
        _storageTv = rootView.findViewById(R.id.tv_storage_info);
        _energyTv = rootView.findViewById(R.id.tv_energy);

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
                    showExitRecordDialog();
                } else {
                    if (_wifiInfo != null) {
                        mWifiManager.disconnectNetWork();
                    }
                    _wifiInfo = null;
                }
                return true;
            }
        });

        _playerButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (useSoftAp) {
                    if (!isRecord) {
                        if (!_deviceStatusChecked) {
                            _deviceStatusChecking = true;
                            _deviceStatusBackgroundCheck = false;
                            BluetoothManager.mInstance.sendCmdRecordStatus();
                            _handler.postDelayed(_timeOutRunnable, 5000);
                            showConnectingDialog(getContext().getString(R.string.record_launching));
                        } else {
                            if (_deviceIsRecord) {
                                showRecordingDialog();
                            } else {
                                //startRecord();
                                //showConnectingDialog(getContext().getString(R.string.record_launching));

                                ToastUtil.s(getContext(), "暂未实现");
                            }
                        }


                    } else {
//                        BluetoothManager.mInstance.sendCmdStopRecord();
//                        showConnectingDialog(getContext().getString(R.string.record_ending));
//                        _handler.postDelayed(_timeOutRunnable, 5000);

                        ToastUtil.s(getContext(), "暂未实现");
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
                    ToastUtil.s(getContext(), "正在查询，请勿重复请求");
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
                    ToastUtil.s(getContext(), "正在查询，请勿重复请求");
                }
                return true;
            }
        });
    }


    @Override
    public void onClick(View v) {

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

    }

    @Override
    public void onEnergy(int energy) {

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



    private void showExitRecordDialog() {
        if (_dialog != null && _dialog.isShowing()) {
            _dialog.dismiss();
        }
        _dialog = null;
        _dialog = new ModalDialog(getContext());
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
        _dialog = new ModalDialog( getContext());
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


    private void connectToWifiSoftAp(WifiInfo wifiInfo) {
        if (mWifiManager != null) {
            mWifiManager.connect(wifiInfo, "", new WifiManager.WifiConnectListener() {
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

                            ModalDialog dialog = new ModalDialog(getContext());
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
                        }
                    });
                }

                @Override
                public void onConnectSuccess(WifiInfo info, int ip) {
                    AppTrace.d(TAG, "connect success....");

                    _wifiInfo = wifiInfo;
                    useSoftAp = true;

                    _url = "rtmp://" + BleConstants.SOFT_AP_IP + ":1935/live/preview2";
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnectingDialog();
                            ToastUtil.s(getContext(), "连接成功");
                        }
                    });
                }
            });
        }
    }

    private void showSoftApDialog(WifiInfo wifiInfo) {

        ModalDialog dialog = new ModalDialog(getContext());
        dialog.setMessage("是否加入热我相机的无线网络");
        dialog.setRightButton(getString(R.string.wifi_join), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                connectToWifiSoftAp(wifiInfo);
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
        _connectingDialog = new BluetoothLoadingDialog(getContext());
        _connectingDialog.setMessage(message);
        _connectingDialog.show();
    }


    private void dismissConnectingDialog() {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
        }
        _connectingDialog = null;
    }



    private void startSoftAp() {
        BluetoothManager.mInstance.sendCmdSoftapOpen(WifiManager.WEEWA_SOFTAP_NAME, "");
        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
        _requestSoftAp = true;
    }

    private void searchSoftAp() {
        if (mWifiManager == null) {
            mWifiManager = WifiManager.getInstance(getActivity());
        }

        mWifiManager.search(WifiManager.WEEWA_SOFTAP_NAME, new WifiManager.SearchWifiListener() {
            @Override
            public void onScanStart() {
                Log.d(TAG, "onScanStart...");
//                        mLoadingView.show("正在扫描附近设备");
                if (!BaseAppUtil.isDestroy(getActivity())) {
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
                if (!BaseAppUtil.isDestroy(getActivity())) {
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
                if (!BaseAppUtil.isDestroy(getActivity())) {
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


    private void startPlay() {
        if (_player == null) {

            ExoTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(  getContext(), trackSelectionFactory);

            _player = new ExoPlayer.Builder( getContext())
                    .setRenderersFactory(new DefaultRenderersFactory( getContext())
                            .setEnableDecoderFallback(true))
                    .setBandwidthMeter(new DefaultBandwidthMeter())
                    .setTrackSelector(trackSelector)
                    .setLoadControl(new DefaultLoadControl())
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
                        case Player.STATE_BUFFERING:
                            AppTrace.d(TAG, "onPlayerStateChanged: 加载中");
                            ToastUtil.s(getContext(), "加载中");
                            break;
                        case Player.STATE_READY:
                            AppTrace.d(TAG, "onPlayerStateChanged: 播放中");
                            ToastUtil.s(getContext(), "播放中");
                            break;
                        case Player.STATE_ENDED:
                            AppTrace.d(TAG, "onPlayerStateChanged: 播放完成");
                            ToastUtil.s(getContext(), "播放完成");
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
                }

            });

            _playerView.requestFocus();
            _playerView.setPlayer(_player);
        } else {
            _player.stop();
        }

        Uri url = Uri.parse(_url);
        AppTrace.d(TAG, "play url: " + _url);
        MediaSource videoSource;
        if ("rtmp".equals(_url.substring(0, 4))) {
            RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
            videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url));
            dataSourceFactory.setTransferListener(new TransferListener() {
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
                    Log.e(TAG, "onBytesTransferred: isNetwork = " + isNetwork);
                }

                @Override
                public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                    Log.e(TAG, "onTransferEnd: isNetwork = " + isNetwork);
                }
            });

        } else {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                    Util.getUserAgent(getContext(), "ExoPlayer"));
            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(url));
        }
        _player.setMediaSource(videoSource);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            _playerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
        }

        // Prepare the player with the source.
        _player.prepare();

        //auto start playing
        _player.setPlayWhenReady(true);
    }
}
