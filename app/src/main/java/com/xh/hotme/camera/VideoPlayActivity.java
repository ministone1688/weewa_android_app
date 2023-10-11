package com.xh.hotme.camera;

import static com.xh.hotme.utils.Constants.VIDEO_URL;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
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

import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.umeng.socialize.UMShareAPI;
import com.xh.hotme.R;

import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.databinding.ActivityVideoPlayerBinding;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;

import com.xh.hotme.widget.playerview.MyPlayerView;

import java.io.File;


public class VideoPlayActivity extends BaseViewActivity<ActivityVideoPlayerBinding>  {

    private final static String TAG = VideoPlayActivity.class.getSimpleName();

    // request code
    private static final int REQUEST_CHECK_PERMISSION = 100;
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    private final String STATE_LAUNCH_SOFT_AP = "launchSoftAp";


    MyPlayerView _playerView;

    ExoPlayer _player;

    String _videoUrl = "";


    private int mResumeWindow = C.INDEX_UNSET;
    private long mResumePosition;

    private boolean mExoPlayerFullscreen = false;

    Handler _handler;


    BluetoothLoadingDialog _connectingDialog;

    int retryCount = 0;

    boolean isPortrait = true;


    private final Runnable _deviceUsageInfoRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

            _handler.postDelayed(_deviceUsageInfoRunnable, 1000);
        }
    };

    public static void start(Context context, String videoUrl) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(Constants.VIDEO_URL, videoUrl);
        context.startActivity(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        outState.putString(Constants.VIDEO_URL, _videoUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
            _videoUrl = savedInstanceState.getString(Constants.VIDEO_URL);
        }

        if (getIntent() != null) {
            _videoUrl = getIntent().getStringExtra(Constants.VIDEO_URL);
        }

        super.onCreate(savedInstanceState);

        _handler = new Handler();
    }

    @Override
    public void initView() {

        _playerView = viewBinding.videoView;

        viewBinding.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if(_playerView!=null && _playerView.getPlayer()!=null){
                    _playerView.getPlayer().release();
                }

                finish();

                return true;
            }
        });

    }

    @Override
    protected void initData() {
        startPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
        AppTrace.d(TAG, "onPause...");

        if (_playerView != null && _playerView.getPlayer() != null) {
            mResumeWindow = _playerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, _playerView.getPlayer().getContentPosition());

            _playerView.getPlayer().release();
        }
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
        if (_player != null) {
            _player.release();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

//        if (!isPortrait) {
//            // 如果当前是横屏，则切换为竖屏
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            isPortrait = true;
//        } else {
//            isPortrait = false;
//            super.onBackPressed();
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);

        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    private void startPlay() {
        retryCount = 0;
        if (_player == null) {

            ExoTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(VideoPlayActivity.this, trackSelectionFactory);

            _player = new ExoPlayer.Builder(VideoPlayActivity.this)
                    .setRenderersFactory(new DefaultRenderersFactory(VideoPlayActivity.this)
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
                           // ToastUtil.s(VideoPlayActivity.this, "加载中");
                            break;
                        case Player.STATE_READY:
                            AppTrace.d(TAG, "onPlayerStateChanged: 播放中");
                           // ToastUtil.s(VideoPlayActivity.this, "播放中");
                            break;
                        case Player.STATE_ENDED:
                            AppTrace.d(TAG, "onPlayerStateChanged: 播放完成");
                          //  ToastUtil.s(VideoPlayActivity.this, "播放完成");
                            break;
                    }

                }

                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason) {
                    AppTrace.e("ExoPlayer", "onPlayWhenReadyChanged:" + reason);
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    AppTrace.e("ExoPlayer", "onPlayerError:" + error.getMessage());
                    switch (error.errorCode) {
                        case ExoPlaybackException.ERROR_CODE_IO_UNSPECIFIED:
                            AppTrace.e(TAG, "TYPE_SOURCE: " + error.getMessage());
                            break;

                        case ExoPlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED:
                            AppTrace.e(TAG, "connect fail: " + error.getMessage());
                            break;
                        case ExoPlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                            AppTrace.e(TAG, "connect time cout: " + error.getMessage());
                            break;
                        case ExoPlaybackException.ERROR_CODE_REMOTE_ERROR:
                            AppTrace.e(TAG, "remote error: " + error.getMessage());
                            break;
                        case ExoPlaybackException.ERROR_CODE_UNSPECIFIED:
                            AppTrace.e(TAG, "unknow error: " + error.getMessage());
                            break;
                    }
                }

            });

            _playerView.requestFocus();
            _playerView.setPlayer(_player);
        } else {
            _player.stop();
        }
        AppTrace.d(TAG, "play url: " + _videoUrl);
        Uri url = Uri.fromFile(new File(_videoUrl));

        DataSpec dataSpec = new DataSpec(url);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };



        //This is the MediaSource representing the media to be played:
        //FOR SD CARD SOURCE:
        MediaSource videoSource = new ProgressiveMediaSource.Factory(factory)
                .createMediaSource(MediaItem.fromUri(url));

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

    /* 退出全屏 */
    public void exitFullScreen() {
        // 旋转屏幕
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 将JJPlayerView从Activity的R.id.content移除，加入RecyclerView的ItemView下
        if (VideoPlayActivity.this != null) {
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
        if (VideoPlayActivity.this != null) {
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

    private void showConnectingDialog(String message) {
        if (_connectingDialog != null && _connectingDialog.isShowing()) {
            _connectingDialog.dismiss();
            _connectingDialog = null;
        }
        _connectingDialog = new BluetoothLoadingDialog(VideoPlayActivity.this);
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
