package com.xh.hotme.camera;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.C;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewFragment;
import com.xh.hotme.bean.CameraVideoListBean;
import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.DeviceUsageInfo;
import com.xh.hotme.bean.StorageBean;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleDeviceUsageInfoNotifyListener;
import com.xh.hotme.databinding.CameraVideoListFragmentBinding;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.TimeUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.BluetoothLoadingDialog;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.widget.TitleItemDecoration;
import com.xh.hotme.wifi.WifiInfo;

import java.util.ArrayList;


/**
 * Created by GJK on 2018/11/9.
 */

public class CameraVideoListFragment extends BaseViewFragment<CameraVideoListFragmentBinding> implements IBleDeviceUsageInfoNotifyListener {
    private static final String TAG = CameraVideoListFragment.class.getSimpleName();


    DeviceUsageInfo _deviceUsageInfo;

    ModalDialog _dialog;


    boolean requestEnergy = false;
    boolean requestStorage = false;


    DeviceUsageInfo _usageInfo;

    DeviceInfo _deviceInfo;
    Device _device;


    private final int mResumeWindow = C.INDEX_UNSET;


    WifiInfo _wifiInfo;

    private WifiManager mWifiManager;

    boolean _deviceSoftApLaunched = false;

    BluetoothLoadingDialog _connectingDialog;

    public ArrayList<CameraVideoListBean.VideosBean> datas = new ArrayList();
    StickyTopAdapter adapter;

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

    public static CameraVideoListFragment newInstance() {
        CameraVideoListFragment fragment = new CameraVideoListFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _handler = new Handler();
    }

    @Override
    protected void initView() {

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
        viewBinding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        /**
         * 自定义 ItemDecoration，实现RecycleView 吸顶
         * */
        viewBinding.recycleView.addItemDecoration(new TitleItemDecoration(getContext()
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
                    CameraVideoDetailListActivity.start(getActivity(), _usageInfo, _deviceInfo, _device, datas.get(position));

                    //Router.jumpCameraVideoListDetailActivity(CameraVideoListActivity.this, datas.get(position));
                }
            }
        });
        viewBinding.recycleView.setAdapter(adapter);
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
//                    _energyTv.setText("" + data.energy + "%");
//
//                    updateStorageInfo(data.storage);
//                    _handler.postDelayed(_deviceUsageInfoRunnable, DEVICE_USAGE_INFO_DELAY);
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
}
