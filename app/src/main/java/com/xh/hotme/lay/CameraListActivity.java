package com.xh.hotme.lay;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.xh.hotme.R;

import com.xh.hotme.account.LoginManager;
import com.xh.hotme.active.DeviceListFragment;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.BindResultBean;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.lay.adaper.BaseRecyclerAdapter;
import com.xh.hotme.lay.adaper.BaseViewHolder;
import com.xh.hotme.lay.adaper.MyGridLayoutManager;
import com.xh.hotme.lay.javabean.CameralistBean;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;
import com.xh.hotme.utils.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CameraListActivity extends BaseActivity {

    RecyclerView _camera_list;
    private List<CameralistBean.Lists> mDataList = new ArrayList<>();
    private BaseRecyclerAdapter<CameralistBean.Lists> mAdapter;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, CameraListActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_list);
        _camera_list = findViewById(R.id.camera_list);

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText("选择相机");
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        Integer _count = mDataList.size()>0 ? 2 : 1;
        MyGridLayoutManager lineView= new MyGridLayoutManager(getApplicationContext(),_count);
        _camera_list.setLayoutManager(lineView);
        mAdapter = new ListAdaper(getApplicationContext(),R.layout.item_mybind_camera, mDataList);
        View vw = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_empty_view, null, false);
        TextView noinfo_txt = vw.findViewById(R.id.empty_text);
        noinfo_txt.setText("您还没有相机");
        mAdapter.setEmptyView(vw);
        _camera_list.setAdapter(mAdapter);

        getMyBindCameraList();
    }

    private void getMyBindCameraList() {
        UserInfoBean loginInfo = LoginManager.getUserLoginInfo(getApplication());
        String mobile = loginInfo.phone;
        String url = SdkApi.getDeviceList()+"?mobile="+mobile;
        OkHttpUtil.get(url, null, new OkHttpCallbackDecode<CameralistBean>() {
            @Override
            public void onDataSuccess(CameralistBean data) {

                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataList.clear();
                        mDataList.addAll(data.getLists());
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onFailure(String code, String message) {

            }

            @Override
            public void onFinish() {

            }
        });

    }

    public class ListAdaper extends BaseRecyclerAdapter<CameralistBean.Lists> {
        public ListAdaper(Context context, int layoutResId, List<CameralistBean.Lists> data) {
            super(context, layoutResId, data);
        }

        @Override
        protected void convert(final BaseViewHolder holder, final CameralistBean.Lists item) {
            //选择
        }

    }


}
