package com.xh.hotme.lay;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.active.MobileUnbindDialog;
import com.xh.hotme.active.MyDeviceActivity;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.databinding.ActivityMobileViewBinding;
import com.xh.hotme.databinding.ActivityPhotoSetBinding;
import com.xh.hotme.softap.CameraNetworkSettingActivity;
import com.xh.hotme.softap.CameraSoftApActivity;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DialogUtil;
import com.xh.hotme.utils.StatusBarUtil;

public class PhotoSetActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout photo_select;
    private LinearLayout btn_photo_open;
    private LinearLayout btn_photo_close;
    private LinearLayout btn_photo_fastup;
    private LinearLayout btn_photo_unbind;
    private ImageView _backBtn;

    MobileUnbindDialog _unbindDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }
        // set content view
        setContentView(R.layout.activity_photo_set);

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText("相机功能管理");
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        photo_select = findViewById(R.id.photo_select);
        photo_select.setOnClickListener(this);
        btn_photo_open = findViewById(R.id.btn_photo_open);
        btn_photo_open.setOnClickListener(this);
        btn_photo_close = findViewById(R.id.btn_photo_close);
        btn_photo_close.setOnClickListener(this);
        btn_photo_fastup = findViewById(R.id.btn_photo_fastup);
        btn_photo_fastup.setOnClickListener(this);
        btn_photo_unbind = findViewById(R.id.btn_photo_unbind);
        btn_photo_unbind.setOnClickListener(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.photo_select:
                CameraListActivity.start(getBaseContext());
            break;
            //开机
            case R.id.btn_photo_open:
                break;
            //关机
            case R.id.btn_photo_close:
                break;
            //快传
            case R.id.btn_photo_fastup:
                CameraSoftApActivity.start(getApplicationContext(), CameraSoftApActivity.REQUEST_SOFT_AP_CAMERA_INFO);
                break;
            //解绑
            case R.id.btn_photo_unbind:
                showUnbindDialog("解绑");
                break;
        }
    }

    private void showUnbindDialog(String mac) {
        if (_unbindDialog != null && _unbindDialog.isShowing()) {
            _unbindDialog.dismiss();
        }
        _unbindDialog = null;

        _unbindDialog = new MobileUnbindDialog(PhotoSetActivity.this, mac);
        _unbindDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        _unbindDialog.show();
    }

}
