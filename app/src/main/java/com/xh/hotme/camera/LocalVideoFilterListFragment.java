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
import com.xh.hotme.base.BaseViewFragment;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.databinding.CameraVideoListFragmentBinding;
import com.xh.hotme.event.DownloadEvent;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.video.VideoManager;
import com.xh.hotme.widget.BluetoothLoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by GJK on 2018/11/9.
 */

public class LocalVideoFilterListFragment extends BaseViewFragment<CameraVideoListFragmentBinding> {
    private static final String TAG = LocalVideoFilterListFragment.class.getSimpleName();

    BluetoothLoadingDialog _connectingDialog;

    public ArrayList<CameraVideoDateListBean> datas = new ArrayList();
    VideoFilterGroupAdapter adapter;

    Handler _handler;

    int _videoType = 0;

    boolean _enableEdit = false;
    boolean _playMode = false;

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


    public static LocalVideoFilterListFragment newInstance(int type) {
        LocalVideoFilterListFragment fragment = new LocalVideoFilterListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VIDEO_CATEGORY, type);
        fragment.setArguments(bundle);
        return fragment;
    }
    public static LocalVideoFilterListFragment newInstance(int type, boolean play) {
        LocalVideoFilterListFragment fragment = new LocalVideoFilterListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.VIDEO_CATEGORY, type);
        bundle.putBoolean(Constants.VIDEO_PLAY_MODE, play);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            _videoType = bundle.getInt(Constants.VIDEO_CATEGORY, 0);
            _playMode = bundle.getBoolean(Constants.VIDEO_PLAY_MODE, false);
        }

        _handler = new Handler();

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }

        _handler.removeCallbacksAndMessages(null);
        _handler = null;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

//        datas.add(new CameraVideoDateListBean());
//        datas.add(new CameraVideoDateListBean());
//        datas.add(new CameraVideoDateListBean());

//        WifiUtils.getInstance(getContext()).connectWifiNoPwsForHot(ConnectLogic.getInstance().getSsid());
        viewBinding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new VideoFilterGroupAdapter(getContext(), datas, _playMode);
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


        loadLoalVideoList();
    }

    private void loadLoalVideoList(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<CameraVideoDateListBean> localList = VideoManager.loadLocalVideoList(_videoType);
                if (localList != null && localList.size() > 0) {
                    datas.clear();
                    datas.addAll(localList);
                    if (!BaseAppUtil.isDestroy(getActivity())) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }

            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshVideoList(DownloadEvent event){
        loadLoalVideoList();
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


    public void onVideoFilterList(int type, List<CameraVideoDateListBean> data) {
        AppTrace.d(TAG, "onVideoFilterList");
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

    public List<CameraVideoDateListBean.CameraVideoDateBean> getSelectFile() {
        if (adapter != null) {
            return adapter.getSelectFile();
        }

        return null;
    }
}
