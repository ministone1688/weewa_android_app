package com.xh.hotme.lay;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.active.ActiveListActivity;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.databinding.ActivityCameraListBinding;
import com.xh.hotme.databinding.ActivityMobileViewBinding;
import com.xh.hotme.databinding.ActivityPhotoSetBinding;
import com.xh.hotme.lay.adaper.BaseRecyclerAdapter;
import com.xh.hotme.lay.adaper.BaseViewHolder;
import com.xh.hotme.lay.adaper.MyGridLayoutManager;
import com.xh.hotme.lay.javabean.CameralistBean;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class CameraListActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView camera_lsit;
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
        super.onCreate(savedInstanceState);
        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }
        // set content view
        setContentView(R.layout.activity_camera_list);

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText("相机列表");
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });


        initView();

    }

    public void initView(){
        camera_lsit = findViewById(R.id.camera_lsit);
        MyGridLayoutManager LineViewx=new MyGridLayoutManager(getBaseContext(),1);
        camera_lsit.setLayoutManager(LineViewx);
        mAdapter = new ListAdaper(getApplicationContext(),R.layout.lay_list_camera, mDataList);
        View vw = LayoutInflater.from(getApplicationContext()).inflate(R.layout.lay_empty_view, null, false);
        TextView noinfo_txt = vw.findViewById(R.id.empty_text);
        noinfo_txt.setText("暂无相机信息");
        mAdapter.setEmptyView(vw);
        camera_lsit.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

    }

    public class ListAdaper extends BaseRecyclerAdapter<CameralistBean.Lists> {

        public ListAdaper(Context context, int layoutResId, List<CameralistBean.Lists> data) {
            super(context, layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder holder, CameralistBean.Lists item) {

        }

    }
}
