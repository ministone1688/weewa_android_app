package com.xh.hotme.softap;


import com.alibaba.fastjson.JSONObject;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bluetooth.IBleSoftApNotifyListener;
import com.xh.hotme.databinding.ActivitySoftApInfoBinding;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.QRCodeUtil;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;

public class CameraSoftApInfoActivity extends BaseViewActivity<ActivitySoftApInfoBinding>  {
    private static final String TAG = CameraSoftApInfoActivity.class.getSimpleName();

    private int reqestCode = -1;

    String ssid;
    String password;


    public static void startActivity(Activity context, String ssid, String password) {
        if (null != context) {
            Intent intent = new Intent(context, CameraSoftApInfoActivity.class);
            intent.putExtra(Constants.INTENT_SSID, ssid);
            intent.putExtra(Constants.INTENT_PASSWORD, password);

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
    }

    @Override
    protected void initView() {
        viewBinding.titleBar.tvTitle.setText(getString(R.string.camera_video_transfer));
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
        if (getIntent() != null) {
            ssid = getIntent().getStringExtra(Constants.INTENT_SSID);
            password = getIntent().getStringExtra(Constants.INTENT_PASSWORD);
        }
        viewBinding.ssid.setText(ssid);
        viewBinding.password.setText(password);


        String address = BluetoothManager.getConnectDeviceAddress();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("s", ssid);
        jsonObject.put("p", password);
        jsonObject.put("b", address);

        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int width = DensityUtil.dip2px(CameraSoftApInfoActivity.this, 220);

                        Bitmap bitmap =  QRCodeUtil.generateBitmap(jsonObject.toJSONString(), width, width);

                        viewBinding.qrcode.setImageBitmap(bitmap);
                    }
                });

            }
        }).start();


    }
}
