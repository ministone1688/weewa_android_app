package com.xh.hotme.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.bluetooth.IBleVideoListNotifyListener;
import com.xh.hotme.databinding.ActivityCameraVideoListBinding;

import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.TimeUtil;
import com.xh.hotme.widget.TitleItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class CameraVideoListActivity extends BaseViewActivity<ActivityCameraVideoListBinding> implements IBleDeviceUsageInfoNotifyListener , IBleVideoListNotifyListener{

    private final static String TAG = CameraVideoListActivity.class.getSimpleName();

    DeviceUsageInfo _usageInfo;

    DeviceInfo _deviceInfo;
    Device _device;

    public ArrayList<CameraVideoListBean.VideosBean> datas = new ArrayList();
    StickyTopAdapter adapter;

    Handler _handler;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, CameraVideoListActivity.class);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, DeviceUsageInfo usageInfo, DeviceInfo deviceInfo, Device device) {
        Intent intent = new Intent(ctx, CameraVideoListActivity.class);
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

        BluetoothHandle.addVideoListNotifyListener(this);
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
        viewBinding.titleBar.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getVideoList();
            }
        });
    }

    @Override
    protected void initData() {

        CameraVideoListBean.VideosBean bean = new CameraVideoListBean.VideosBean();
        bean.time = TimeUtil.getCurrentTime(System.currentTimeMillis());
        datas.add(bean);
        datas.add(new CameraVideoListBean.VideosBean());
        datas.add(new CameraVideoListBean.VideosBean());
        datas.add(new CameraVideoListBean.VideosBean());

//        WifiUtils.getInstance(getContext()).connectWifiNoPwsForHot(ConnectLogic.getInstance().getSsid());
        viewBinding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        /**
         * 自定义 ItemDecoration，实现RecycleView 吸顶
         * */
        viewBinding.recycleView.addItemDecoration(new TitleItemDecoration(this
                , new TitleItemDecoration.TitleDecorationCallback() {
            @Override
            public boolean isHeadItem(int curPosition, int nextposition) {
                if (curPosition == 0) {
                    return true;
                }
                if (nextposition != 0 && nextposition >= datas.size()) {
                    return false;
                }
                CameraVideoListBean.VideosBean cur = datas.get(curPosition);
                CameraVideoListBean.VideosBean next = datas.get(nextposition);
                try {
                    if (cur.time.split("T")[0].equals(next.time.split("T")[0]))
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }

            @NonNull
            @Override
            public String getHeadTitle(int position) {
                try {
                    CameraVideoListBean.VideosBean cur = datas.get(position);
                    String time = cur.time.split("T")[0];
                    if (TimeUtil.isToday(time))
                        return "今天";
                    else
                        return time;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            }
        }));
        adapter = new StickyTopAdapter(datas);
        adapter.setOnItemClickListener(new StickyTopAdapter.OnItemClick() {
            @Override
            public void onItemClick(int position) {
                if (!TextUtils.isEmpty(datas.get(position).path)) {
                    CameraVideoDetailListActivity.start(CameraVideoListActivity.this, _usageInfo, _deviceInfo, _device, datas.get(position));

                    //Router.jumpCameraVideoListDetailActivity(CameraVideoListActivity.this, datas.get(position));
                }
            }
        });
        viewBinding.recycleView.setAdapter(adapter);

        getVideoList();
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
        BluetoothHandle.removeVideoListNotifyListener(this);

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

    private void getVideoList() {
        BluetoothManager.mInstance.sendCmdVideoList();


//        DeviceHttpManager.sendCmdVideoList(new IBleVideoListNotifyListener() {
//            @Override
//            public void onVideoList(List<CameraVideoListBean.VideosBean> data) {
//
//                if (!BaseAppUtil.isDestroy(CameraVideoListActivity.this)) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            datas.clear();
//                            datas.addAll(data);
//                            adapter.notifyDataSetChanged();
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onVideoListFail(String msg) {
//                AppTrace.d(TAG, "onVideoListFail: " + msg);
//            }
//        });
    }

    @Override
    public void onVideoList(List<CameraVideoListBean.VideosBean> data) {
        if (!BaseAppUtil.isDestroy(CameraVideoListActivity.this)) {
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
    public void onVideoListFail(String msg) {
        AppTrace.d(TAG, "onVideoListFail: " + msg);
    }
}
