package com.xh.hotme.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.listener.OnTabSelectListener;
import com.weewa.lib.Prefs;
import com.weewa.lib.WeewaLib;
import com.xh.hotme.HotmeApplication;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.bean.CameraVideosBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleVideoFilterNotifyListener;
import com.xh.hotme.bluetooth.IBleVideoListNotifyListener;
import com.xh.hotme.databinding.ActivityCameraFilterVideoListBinding;
import com.xh.hotme.databinding.ActivityCameraVideoListBinding;
import com.xh.hotme.databinding.IcloudFragmentIcloudStorageBinding;
import com.xh.hotme.fragment.CouldFragment;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.icloud.ICouldStorageFragment;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.TimeUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.TitleItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CameraVideoFilterListActivity extends BaseViewActivity<ActivityCameraFilterVideoListBinding> {

    private final static String TAG = CameraVideoFilterListActivity.class.getSimpleName();


    List<CameraVideoFilterListFragment> mFragmentList = new ArrayList<>();

    String[] titleName = new String[]{"精彩集锦", "整场回放"};

    DeviceUsageInfo _usageInfo;

    DeviceInfo _deviceInfo;
    Device _device;

    Handler _handler;

    boolean enableEdit = false;


    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, CameraVideoFilterListActivity.class);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, DeviceUsageInfo usageInfo, DeviceInfo deviceInfo, Device device) {
        Intent intent = new Intent(ctx, CameraVideoFilterListActivity.class);
        intent.putExtra(Constants.REQUEST_INTENT_USAGE_INFO, usageInfo);
        intent.putExtra(Constants.REQUEST_INTENT_DEVICE_INFO, deviceInfo);
        intent.putExtra(Constants.REQUEST_DEVICE, device);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getIntent() != null) {
            _usageInfo = (DeviceUsageInfo) getIntent().getSerializableExtra(Constants.REQUEST_INTENT_USAGE_INFO);
            _deviceInfo = (DeviceInfo) getIntent().getSerializableExtra(Constants.REQUEST_INTENT_DEVICE_INFO);
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
        }
        super.onCreate(savedInstanceState);

        _handler = new Handler();
    }

    @Override
    protected void initView() {
        viewBinding.titleBar.tvTitle.setText("相机视频");
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (enableEdit) {
                    enableEdit = false;
                    for (CameraVideoFilterListFragment fragment : mFragmentList) {
                        fragment.setEnableEdit(enableEdit);
                        fragment.clearAllSelect();
                    }
                    viewBinding.titleBar.tvTitle.setText("相机视频");
                    viewBinding.titleBar.tvRight.setText("");

                    viewBinding.titleBar.ivBack.setImageResource(R.mipmap.common_title_back);

                    showBottom(false);
                } else {
                    finish();
                }

                return true;
            }
        });
        viewBinding.titleBar.tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CameraVideoFilterListFragment fragment : mFragmentList) {
                    fragment.setAllSelect();
                }
            }
        });
        viewBinding.titleBar.ivRight.setImageResource(R.mipmap.video_edit);
        viewBinding.titleBar.ivRight.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                enableEdit = true;
                viewBinding.titleBar.tvTitle.setText("选择视频");
                viewBinding.titleBar.tvRight.setText("全选");
                for (CameraVideoFilterListFragment fragment : mFragmentList) {
                    fragment.setEnableEdit(enableEdit);
                }
                showBottom(true);
                viewBinding.titleBar.ivBack.setImageResource(R.mipmap.common_title_close);

                return false;
            }
        });
        showBottom(false);
        viewBinding.videoTabGroup.tabDownload.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                List<CameraVideosBean> selectFileList = new ArrayList<>();
                for (CameraVideoFilterListFragment fragment : mFragmentList) {
                    List<CameraVideoDateListBean.CameraVideoDateBean> fileList = fragment.getSelectFile();
                    if (fileList != null && fileList.size() > 0) {
                        selectFileList.addAll(fileList);
                    }
                }
                ConnectLogic.getInstance().addFiles(selectFileList);

                //设置下载目录
                String cameraName = BluetoothManager.getConnectDeviceName();
                File cameraDir = AppFileUtil.getCameraDir(HotmeApplication.getContext(), cameraName);
                Prefs.INSTANCE.setDownloadPath(Prefs.INSTANCE.defaultPreference(HotmeApplication.getContext()), cameraDir.getPath());


                AppTrace.d(TAG, "bsePath: " + ConnectLogic.getBasePath());
                ConnectLogic.getInstance().toReceive();
                ToastUtil.s(CameraVideoFilterListActivity.this, "正在下载");
                return true;
            }
        });

        viewBinding.videoTabGroup.tabUpload.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                ToastUtil.s(CameraVideoFilterListActivity.this, "正在上传");

                return true;
            }
        });

        viewBinding.videoTabGroup.tabDelete.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                ToastUtil.s(CameraVideoFilterListActivity.this, "正在删除");
                return true;
            }
        });


        viewBinding.viewPager.setOffscreenPageLimit(3);
        viewBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewBinding.tabs.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // setup pager
        viewBinding.viewPager.setAdapter(new VideoPagerAdapter(getSupportFragmentManager()));


        viewBinding.tabs.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewBinding.viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        // set tabs
        viewBinding.tabs.setTabData(titleName);
        viewBinding.tabs.setCurrentTab(0);
    }

    @Override
    protected void initData() {

        mFragmentList.add(CameraVideoFilterListFragment.newInstance(Constants.VIDEO_CATEGORY_TOP));
        mFragmentList.add(CameraVideoFilterListFragment.newInstance(Constants.VIDEO_CATEGORY_GALLERY));

    }

    private class VideoPagerAdapter extends FragmentPagerAdapter {
        public VideoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return titleName.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleName[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

    }

    private void showBottom(boolean show) {
        viewBinding.videoTabGroup.videoEditLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
