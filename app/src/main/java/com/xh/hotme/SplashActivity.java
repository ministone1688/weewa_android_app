package com.xh.hotme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;


import com.umeng.socialize.UMShareAPI;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.active.ActiveListActivity;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.databinding.ActivitySplashBinding;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.SpUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.TimeUtil;
import com.xh.hotme.widget.PermissionDialog;
import com.xh.hotme.widget.PrivacyWebDialog;
import com.xh.hotme.widget.ModalDialog;

import java.util.List;


public class SplashActivity extends BaseViewActivity<ActivitySplashBinding> /*implements EasyPermissions.PermissionCallbacks*/ {

    private final static String TAG = SplashActivity.class.getSimpleName();

    private static final String BUNDLE_FRAGMENTS_KEY = "android:support:fragments";

    // messages
    private final int START_MAIN = 0;

    // request code
    private static final int REQUEST_CHECK_PERMISSION = 100;

    // views
    private LinearLayout _splashLayout;

    RelativeLayout _guideView;
    ViewPager _guideVp;
    LinearLayout llGuidePoint;
    TextView guideIbStart;
    private int[] imagePositionArray;//图片资源的数组
    private List<View> viewList;//图片资源的集合

    //实例化原点View
    private ImageView iv_point;
    private ImageView[] ivPointArray;

    private boolean _permissionInited;

    private static boolean isAgreePrivacy = true;
    private static boolean isAgreePermission = true;

    boolean isFirstLaunch = false;

    boolean isShowGuide = false;
    boolean isGuideShowEnd = false;


    private final Handler _handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == START_MAIN) {
                if (SplashActivity.this == null || SplashActivity.this.isDestroyed() || SplashActivity.this.isFinishing()) {
                    return;
                }

                startMain(true);

                if (LoginManager.isLogon(SplashActivity.this)) {
                    MainActivity.start(SplashActivity.this);
                    finish();
                } else {
                    ActiveListActivity.start(SplashActivity.this);
                    finish();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //重建时清除 fragment的状态
            savedInstanceState.remove(BUNDLE_FRAGMENTS_KEY);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getBundleExtra(Constants.PREF_EXTRA) != null) {
            Bundle bundle = getIntent().getBundleExtra(Constants.PREF_EXTRA);
        }
    }

    @Override
    public void initView() {
        _splashLayout = viewBinding.splashLayout;

        _guideView = viewBinding.guideView;;
        _guideVp = viewBinding.guideVp;
        guideIbStart = viewBinding.guideIbStart;

        _guideVp.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (isGuideShowEnd && isShowGuide) {
                    startMain(true);
                }
                return true;
            }
        });

        isFirstLaunch = SpUtil.isFirstLaunch();
        isShowGuide = true;
        isGuideShowEnd = true;
        _splashLayout.setVisibility(View.VISIBLE);
        _guideView.setVisibility(View.GONE);

        StatusBarUtil.setStatusBarTranslucent(this, true);

        long startTimes = SpUtil.getLong(SpUtil.PRFS_LOBOX_INIT_TIMES);
        startTimes += 1;


        SpUtil.saveLong(SpUtil.PRFS_LOBOX_INIT_TIMES, startTimes);

        long dateTimeStamp = SpUtil.getLong(SpUtil.PRFS_DAY_LAUNCH_DATE);
        if (dateTimeStamp == 0) {
            SpUtil.saveLong(SpUtil.PRFS_DAY_INSTALL_DATE, System.currentTimeMillis());
            SpUtil.saveLong(SpUtil.PRFS_DAY_LAUNCH_DATE, System.currentTimeMillis());
            SpUtil.saveLong(SpUtil.PRFS_DAY_LAUNCH_TIME, 1);
        } else {
            long currentTimeStamp = System.currentTimeMillis();
            if (!TimeUtil.isSameDay(dateTimeStamp, currentTimeStamp)) {
                SpUtil.saveLong(SpUtil.PRFS_DAY_LAUNCH_DATE, System.currentTimeMillis());
                SpUtil.saveLong(SpUtil.PRFS_DAY_LAUNCH_TIME, 1);
            } else {
                long loginTimes = SpUtil.getLong(SpUtil.PRFS_DAY_LAUNCH_TIME);
                SpUtil.saveLong(SpUtil.PRFS_DAY_LAUNCH_TIME, loginTimes + 1);
            }
        }

        afterPrivacy();
    }

    @Override
    public void initData() {

    }

    private void showPrivacy(String content) {
        final PrivacyWebDialog privacyDialog = new PrivacyWebDialog(SplashActivity.this, String.format("%s%s", getString(R.string.welcome_to_use), getString(R.string.app_name)), content);
        privacyDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    SpUtil.setPrivateShowStatus(SplashActivity.this, true);

                    isAgreePrivacy = true;

                    if (SpUtil.getPermissionShowStatus()) {
                        isAgreePermission = true;

                        // init permission
                        afterPermissionCheck();

                    } else {
                        showPermissionDialog();
                    }


                } else if (which == DialogInterface.BUTTON_NEGATIVE) {

                    ModalDialog dialog = new ModalDialog(SplashActivity.this);
                    dialog.setMessage("您需要同意《 用户协议与隐私政策 》才能继续使用我们的产品及服务");
                    dialog.setLeftButton("退出应用", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (privacyDialog != null) {
                                privacyDialog.dismiss();
                            }
                            finish();
                        }
                    });
                    dialog.setRightButton("返回", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });
                    dialog.setMessageTextColor("#666666");
                    dialog.setMessageTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    dialog.setLeftButtonTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    dialog.setRightButtonTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    dialog.setLeftButtonTextColor("#999999");
                    dialog.setRightButtonTextColor("#FF3D9AF0");
                    dialog.show();
                }
            }
        });
        privacyDialog.setNegativeButtonText("退出应用");
        privacyDialog.setPositiveButtonTitle("同意");
        privacyDialog.show();

    }


    private void afterPrivacy() {
        isAgreePrivacy = true;

        afterPermissionCheck();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);

        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AppTrace.d(TAG, "onRequestPermissionsResult");

        if (requestCode == REQUEST_CHECK_PERMISSION) {

            startMain(true);
        }
    }

    //    @AfterPermissionGranted(REQUEST_CHECK_PERMISSION)
    void afterPermissionCheck() {
        AppTrace.d(TAG, "afterPermissionCheck");

        // check if we can start main
        _permissionInited = true;

        startMain(false);
    }

    private void startMain(boolean loadedAd) {

        SpUtil.setFirstLaunch(false);

        if (isFirstLaunch) {
            if (isShowGuide && isGuideShowEnd) {
                _handler.sendEmptyMessageDelayed(START_MAIN, 2000);
            }
        } else {
            _handler.sendEmptyMessageDelayed(START_MAIN, 2000);
        }
    }

    private void showPermissionDialog() {
        PermissionDialog permissionDialog = new PermissionDialog(SplashActivity.this);
        permissionDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    isAgreePermission = true;
                    SpUtil.setPermissionShowStatus(SplashActivity.this, true);

                    // init permission
                    afterPermissionCheck();
                }
            }
        });
        permissionDialog.show();

    }

    /**
     * 加载底部圆点
     */
    private void initPoint() {
        //这里实例化LinearLayout
        llGuidePoint = findViewById(R.id.ll_guide_point);
        llGuidePoint.setVisibility(View.GONE);
        //根据ViewPager的item数量实例化数组
        ivPointArray = new ImageView[viewList.size()];
        //循环新建底部圆点ImageView，将生成的ImageView保存到数组中
        int size = viewList.size();
        for (int i = 0; i < size; i++) {
            iv_point = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            //设置小圆点的间距
            lp.setMargins(20, 0, 20, 0);
            iv_point.setLayoutParams(lp);
            ivPointArray[i] = iv_point;
            //第一个页面需要设置为选中状态，这里采用两张不同的图片
//            if (i == 0) {
//                iv_point.setBackgroundResource(R.drawable.guide_view_point_selected);
//            } else {
//                iv_point.setBackgroundResource(R.drawable.guide_view_point_normal);
//            }
            //将数组中的ImageView加入到ViewGroup
            llGuidePoint.addView(ivPointArray[i]);
            iv_point.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    return true;
                }
            });
        }
    }

    /**
     * 加载图片ViewPager
     */
    private void initViewPager() {
//        //实例化图片资源
//        imagePositionArray = new int[]{R.mipmap.guid_1, R.mipmap.guid_2, R.mipmap.guid_3};
//        viewList = new ArrayList<>();
//        //获取一个Layout参数，设置为全屏
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        //循环创建View并加入到集合中
//        int len = imagePositionArray.length;
//        for (int i = 0; i < len; i++) {
//            //new ImageView并设置全屏和图片资源
//            ImageView imageView = new ImageView(this);
//            imageView.setLayoutParams(params);
//            imageView.setBackgroundResource(imagePositionArray[i]);
//            //将ImageView加入到集合中
//            viewList.add(imageView);
//
//            imageView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
//                @Override
//                public boolean onClicked() {
//                    if(isGuideShowEnd && isShowGuide) {
//                        startMain(true);
//                    }
//                    return true;
//                }
//            });
//
//        }
//
//        //View集合初始化好后，设置Adapter
//        _guideVp.setAdapter(new GuidePageAdapter(viewList));
//        //设置滑动监听
//        _guideVp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                //循环设置当前页的标记图
//                int length = imagePositionArray.length;
//                for (int i = 0; i < length; i++) {
//                    ivPointArray[position].setBackgroundResource(R.drawable.guide_view_point_selected);
//                    if (position != i) {
//                        ivPointArray[i].setBackgroundResource(R.drawable.guide_view_point_normal);
//                    }
//                }
//                //判断是否是最后一页，若是则显示按钮
//                if (position == imagePositionArray.length - 1) {
////                    guideIbStart.setVisibility(View.VISIBLE);
//                    isGuideShowEnd = true;
//                } else {
////                    guideIbStart.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
    }
}
