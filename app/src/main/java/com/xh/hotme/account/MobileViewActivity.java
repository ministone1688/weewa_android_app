package com.xh.hotme.account;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.databinding.ActivityMobileViewBinding;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.StatusBarUtil;


public class MobileViewActivity extends BaseViewActivity<ActivityMobileViewBinding> {
    private static final String TAG = MobileViewActivity.class.getSimpleName();

    private int reqestCode = -1;

    String _mobile;

    public static void start(Context context, String mobile) {
        if (null != context) {
            Intent intent = new Intent(context, MobileViewActivity.class);
            intent.putExtra(Constants.REQUEST_MOBILE, mobile);
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
        if (getIntent() != null) {
            _mobile = getIntent().getStringExtra(Constants.REQUEST_MOBILE);
        }

        viewBinding.titleBar.tvTitle.setText(R.string.mobile);
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });
        if (!TextUtils.isEmpty(_mobile)) {
            if (_mobile.length() == 11) {
                _mobile = _mobile.substring(0, 3) + "****" + _mobile.substring(7, _mobile.length());
            }
            viewBinding.tvMobile.setText(_mobile);
        }
        viewBinding.btnLoginSubmit.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                MobileLoginActivity.start(MobileViewActivity.this, Constants.LOGIN_TYPE_CHANGE_MOBILE);
                finish();
                return true;
            }
        });
    }

    @Override
    protected void initData() {

    }
}
