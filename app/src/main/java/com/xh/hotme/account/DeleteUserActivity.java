package com.xh.hotme.account;


import static com.xh.hotme.utils.Constants.REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.databinding.ActivityDeleteUserBinding;
import com.xh.hotme.databinding.ActivityMobileLoginBinding;
import com.xh.hotme.event.LoginEvent;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;


public class DeleteUserActivity extends BaseViewActivity<ActivityDeleteUserBinding> {
    private static final String TAG = DeleteUserActivity.class.getSimpleName();

    private int reqestCode = -1;
    private int _loginType = 0;

    public static void start(Context context) {
        start(context, Constants.LOGIN_TYPE_LOGIN);
    }

    public static void start(Context context, int loginType) {
        if (null != context) {
            Intent intent = new Intent(context, DeleteUserActivity.class);
            intent.putExtra(REQUEST_CODE, loginType);
            context.startActivity(intent);
        }
    }

    public static void startActivityByRequestCode(Activity context, int requestCode) {
        startActivityByRequestCode(context, requestCode, Constants.LOGIN_TYPE_LOGIN);
    }


    public static void startActivityByRequestCode(Activity context, int requestCode, int loginType) {
        if (null != context) {
            Intent intent = new Intent(context, DeleteUserActivity.class);
            intent.putExtra(REQUEST_CODE, requestCode);
            intent.putExtra(Constants.LOGIN_TYPE, loginType);
            context.startActivityForResult(intent, requestCode);
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

        // find views
        viewBinding.titleBar.tvTitle.setText(getString(R.string.delete_account));

        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        String mobile = LoginManager.getMobile(DeleteUserActivity.this);
        if (mobile.length() == 11) {
            mobile = mobile.substring(0, 3) + "****" + mobile.substring(7, mobile.length());
        }

        // agreement label underline & click
        viewBinding.account.setText(String.format("账号：%s", mobile));

        viewBinding.btnSubmit.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                DeleteInteract.delete(DeleteUserActivity.this, new ICommonListener() {
                    @Override
                    public void onSuccess() {
                        ToastUtil.s(DeleteUserActivity.this, getString(R.string.delete_account_success));
                        finish();
                    }

                    @Override
                    public void onFail(String code, String message) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });

                return true;
            }
        });


    }

    @Override
    protected void initData() {

    }

    public void doFinish(boolean success) {
        if (reqestCode > -1) {
            Intent i = new Intent();
            i.putExtra("result", 0);
            setResult(success ? 1 : 0, i);
            finish();
        } else {
            ToastUtil.s(DeleteUserActivity.this, "登录成功");
            finish();
        }
    }

    private void getUserInfo() {
        UserInfoInteract.getUserInfo(DeleteUserActivity.this, new UserInfoInteract.UserInfoListener() {
            @Override
            public void onSuccess(UserInfoBean data) {
//                LoginManager.saveLoginInfo(MobileLoginActivity.this, data);

                EventBus.getDefault().post(new LoginEvent());
            }

            @Override
            public void onFail(String code, String message) {
                // doFinish(false);
            }

            @Override
            public void onFinish() {
                doFinish(true);
            }
        });
    }
}
