package com.xh.hotme.camera;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.xh.hotme.R;
import com.xh.hotme.active.MyDeviceActivity;
import com.xh.hotme.base.BaseViewFragment;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.bean.CameraVideosBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.IBleVideoFilterNotifyListener;
import com.xh.hotme.databinding.CameraVideoListFragmentBinding;
import com.xh.hotme.icloud.VideoFragment;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.wifi.WifiInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GJK on 2018/11/9.
 */

public class CameraVideoFilterListFragment extends BaseViewFragment<CameraVideoListFragmentBinding> implements IBleDeviceUsageInfoNotifyListener, IBleVideoFilterNotifyListener {
    private static final String TAG = CameraVideoFilterListFragment.class.getSimpleName();


    DeviceUsageInfo _deviceUsageInfo;

    ModalDialog _dialog;

    DeviceUsageInfo _usageInfo;

    DeviceInfo _deviceInfo;
    Device _device;
    WifiInfo _wifiInfo;

    private WifiManager mWifiManager;

    boolean _deviceSoftApLaunched = false;

    BluetoothLoadingDialog _connectingDialog;

    public ArrayList<CameraVideoDateListBean> datas = new ArrayList();
    VideoFilterGroupAdapter adapter;

    Handler _handler;

    int _videoType = 0;

    boolean _enableEdit = false;


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
                if(!isHidden()){
                    if(!datas.isEmpty()){

                        getVideoList();
                    }
                }
        }
    };


    public static CameraVideoFilterListFragment newInstance(int type) {
        CameraVideoFilterListFragment fragment = new CameraVideoFilterListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VIDEO_CATEGORY, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            _videoType = bundle.getInt(Constants.VIDEO_CATEGORY, 0);
        }

        _handler = new Handler();

        BluetoothHandle.addVideoFilterNotifyListener(this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        _handler.removeCallbacksAndMessages(null);
        _handler = null;
        BluetoothHandle.removeVideoFilterNotifyListener(this);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

//        datas.add(new CameraVideoDateListBean());
//        datas.add(new CameraVideoDateListBean());
//        datas.add(new CameraVideoDateListBean());

        viewBinding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VideoFilterGroupAdapter(getContext(), datas);
//        adapter.setOnItemClickListener(new StickyTopAdapter.OnItemClick() {
//            @Override
//            public void onItemClick(int position) {
//                if (!TextUtils.isEmpty(datas.get(position).path)) {
//                    CameraVideoDetailListActivity.start(getActivity(), _usageInfo, _deviceInfo, _device, datas.get(position));
//
//                    //Router.jumpCameraVideoListDetailActivity(CameraVideoListActivity.this, datas.get(position));
//                }
//            }
//        });


        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.video_empty_view, null);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        //添加空视图
        adapter.setEmptyView(emptyView);

        viewBinding.recycleView.setAdapter(adapter);

        getVideoList();
    }

    private void getVideoList(){
        _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
        BluetoothManager.mInstance.sendCmdVideoFilterList(_videoType);
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
                }
            }
        });
    }

    @Override
    public void onTemperature(float temperature) {

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

    @Override
    public void onVideoFilterList(int type, List<CameraVideoDateListBean> data) {
        AppTrace.d(TAG, "onVideoFilterList");
        _handler.removeCallbacks(_timeOutRunnable);
        if (type != _videoType) {
            AppTrace.d(TAG, "onVideoFilterList skip.....");
            return;
        }
        datas.clear();
        datas.addAll(data);
        _handler.post(new Runnable() {
            @Override
            public void run() {

                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onVideoFilterListFail(String msg) {

    }

    public void setEnableEdit(boolean enableEdit) {
        _enableEdit = enableEdit;
        if (adapter != null) {
            adapter.setEnableEdit(enableEdit);
            adapter.notifyDataSetChanged();
        }
    }

    public void setAllSelect() {
//        _enableEdit = enableEdit;
        if (adapter != null) {
            adapter.selectAll();
            adapter.notifyDataSetChanged();
        }
    }


    public void clearAllSelect() {
//        _enableEdit = enableEdit;
        if (adapter != null) {
            adapter.cleanAllSelect();
            adapter.notifyDataSetChanged();
        }
    }

    public List<CameraVideoDateListBean.CameraVideoDateBean> getSelectFile(){
        if (adapter != null) {
            return adapter.getSelectFile();
        }

        return null;
    }
}
