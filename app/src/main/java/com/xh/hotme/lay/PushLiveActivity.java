package com.xh.hotme.lay;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class PushLiveActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout push_douyin;
    private LinearLayout push_kuaishou;
    private LinearLayout push_shipinhao;
    private LinearLayout push_other;
    private List<CameralistBean.Lists> mDataList = new ArrayList<>();
    private BaseRecyclerAdapter<CameralistBean.Lists> mAdapter;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, PushLiveActivity.class);
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
        setContentView(R.layout.activity_camera_push);

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText("直播管理");
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
        push_douyin = findViewById(R.id.push_douyin);
        push_douyin.setOnClickListener(this);
        push_kuaishou = findViewById(R.id.push_kuaishou);
        push_kuaishou.setOnClickListener(this);
        push_shipinhao = findViewById(R.id.push_shipinhao);
        push_shipinhao.setOnClickListener(this);
        push_other = findViewById(R.id.push_other);
        push_other.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.push_douyin:
                PushSetAcitivty.start(PushLiveActivity.this);
                break;

            case R.id.push_kuaishou:
                PushSetAcitivty.start(PushLiveActivity.this);
                break;

            case R.id.push_shipinhao:
                PushSetAcitivty.start(PushLiveActivity.this);
                break;

            case R.id.push_other:
                PushSetAcitivty.start(PushLiveActivity.this);
                break;
        }
    }

}
