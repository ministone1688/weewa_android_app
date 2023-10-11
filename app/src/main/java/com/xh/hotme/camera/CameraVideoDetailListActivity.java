package com.xh.hotme.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.CameraVideoDetailListBean;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.bean.CameraVideosBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleVideoDetailListNotifyListener;
import com.xh.hotme.databinding.ActivityCameraVideoDetailListBinding;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class CameraVideoDetailListActivity extends BaseViewActivity<ActivityCameraVideoDetailListBinding> implements IBleDeviceUsageInfoNotifyListener, IBleVideoDetailListNotifyListener {

    private final static String TAG = CameraVideoDetailListActivity.class.getSimpleName();

    DeviceUsageInfo _usageInfo;

    DeviceInfo _deviceInfo;
    Device _device;

    CameraVideosBean videosBean;

    public ArrayList<CameraVideoDetailListBean.VideosBean> datas = new ArrayList();
    CameraVideoDetailAdapter adapter;

    Handler _handler;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, CameraVideoDetailListActivity.class);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, DeviceUsageInfo usageInfo, DeviceInfo deviceInfo, Device device, CameraVideoListBean.VideosBean videosBean) {
        Intent intent = new Intent(ctx, CameraVideoDetailListActivity.class);
        intent.putExtra(Constants.REQUEST_INTENT_USAGE_INFO, usageInfo);
        intent.putExtra(Constants.REQUEST_INTENT_DEVICE_INFO, deviceInfo);
        intent.putExtra(Constants.REQUEST_DEVICE, device);
        intent.putExtra(Constants.REQUEST_VIDEO_BEAN, videosBean);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getIntent() != null) {
            _usageInfo = (DeviceUsageInfo) getIntent().getSerializableExtra(Constants.REQUEST_INTENT_USAGE_INFO);
            _deviceInfo = (DeviceInfo) getIntent().getSerializableExtra(Constants.REQUEST_INTENT_DEVICE_INFO);
            _device = (Device) getIntent().getSerializableExtra(Constants.REQUEST_DEVICE);
            videosBean = (CameraVideosBean) getIntent().getSerializableExtra(Constants.REQUEST_VIDEO_BEAN);
        }


        super.onCreate(savedInstanceState);

        _handler = new Handler();

        BluetoothHandle.addVideoDetailListNotifyListener(this);
    }

    @Override
    protected void initView() {
        viewBinding.titleBar.tvTitle.setText("相机视频");
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

    }

    @Override
    protected void initData() {

        viewBinding.recycleView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        viewBinding.recycleView.setAdapter(adapter = new CameraVideoDetailAdapter(CameraVideoDetailListActivity.this, datas));

        if (videosBean != null) {
//            BluetoothManager.mInstance.sendCmdVideoDetailList(videosBean.path);
        }
    }


    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // for this
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothHandle.removeVideoDetailListNotifyListener(this);

        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onStorageInfo(StorageBean storageBean) {

    }

    @Override
    public void onEnergy(int energy) {

    }

    @Override
    public void onDeviceInfo(DeviceUsageInfo data) {
        _usageInfo = data;
    }

    @Override
    public void onTemperature(float temperature) {

    }

    @Override
    public void onVideoDetailList(List<CameraVideoDetailListBean.VideosBean> data) {
        if (!BaseAppUtil.isDestroy(CameraVideoDetailListActivity.this)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    datas.clear();
                    datas.addAll(data);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onVideoDetailListFail(String msg) {
        AppTrace.d(TAG, "onVideoListFail: " + msg);
    }
}
