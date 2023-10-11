package com.xh.hotme.lay;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.active.MobileUnbindDialog;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.lay.utils.WebModalDialog;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.StatusBarUtil;

public class PushSetAcitivty extends BaseActivity implements View.OnClickListener {

    private TextView ly_push_tips;
    private TextView btn_submit;
    private Context _ctx;

    private WebModalDialog _webDialog;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, PushSetAcitivty.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _ctx = getApplication();
        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }
        // set content view
        setContentView(R.layout.activity_push_set);

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText("推流设置");
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        ly_push_tips = findViewById(R.id.ly_push_tips);
        ly_push_tips.setOnClickListener(this);
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ly_push_tips:
                //如何获取
                showTipsDialog("如何获取推流地址");
                break;

            case R.id.btn_submit:

                break;
        }
    }

    private void showTipsDialog(String mac) {
        if (_webDialog != null && _webDialog.isShowing()) {
            _webDialog.dismiss();
        }
        _webDialog = null;

        _webDialog = new WebModalDialog(PushSetAcitivty.this, SdkApi.user_pushUrl);
        _webDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        _webDialog.show();
    }

}
