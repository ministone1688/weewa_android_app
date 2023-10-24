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
import com.xh.hotme.databinding.ActivityMobileLoginBinding;
import com.xh.hotme.event.LoginEvent;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.lay.WeburlActivity;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;


public class MobileLoginActivity extends BaseViewActivity<ActivityMobileLoginBinding> {
    private static final String TAG = MobileLoginActivity.class.getSimpleName();

    private int reqestCode = -1;
    private int _loginType = 0;

    public static void start(Context context) {
        start(context, Constants.LOGIN_TYPE_LOGIN);
    }

    public static void start(Context context, int loginType) {
        if (null != context) {
            Intent intent = new Intent(context, MobileLoginActivity.class);
            intent.putExtra(REQUEST_CODE, loginType);
            context.startActivity(intent);
        }
    }

    public static void startActivityByRequestCode(Activity context, int requestCode) {
        startActivityByRequestCode(context, requestCode, Constants.LOGIN_TYPE_LOGIN);
    }


    public static void startActivityByRequestCode(Activity context, int requestCode, int loginType) {
        if (null != context) {
            Intent intent = new Intent(context, MobileLoginActivity.class);
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
        if (getIntent() != null) {
            reqestCode = getIntent().getIntExtra(REQUEST_CODE, -1);
            _loginType = getIntent().getIntExtra(Constants.LOGIN_TYPE, Constants.LOGIN_TYPE_LOGIN);
        }

        // find views
        TextView titleView = viewBinding.titleBar.tvTitle;
        if (_loginType == Constants.LOGIN_TYPE_CHANGE_MOBILE) {
            titleView.setText(getString(R.string.mobile_btn_update));
        } else {
            titleView.setText(getString(R.string.login_title));
        }

        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        // agreement label underline & click
        viewBinding.userAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        viewBinding.userAgreement.getPaint().setAntiAlias(true);
        viewBinding.privacyAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        viewBinding.privacyAgreement.getPaint().setAntiAlias(true);
        viewBinding.userAgreement.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                //DialogUtil.showAgreement(MobileLoginActivity.this, "user.html");

                Intent intent = new Intent(getApplicationContext(), WeburlActivity.class);
                intent.putExtra("weburl", SdkApi.user_userproxy);
                intent.putExtra("title",getString(R.string.user_proment));
                MyToolUtils.goActivity(MobileLoginActivity.this,intent);

                return true;
            }
        });
        viewBinding.privacyAgreement.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                // DialogUtil.showAgreement(MobileLoginActivity.this, "privacy.html");
                Intent intent = new Intent(getApplicationContext(), WeburlActivity.class);
                intent.putExtra("weburl", SdkApi.user_privateurl);
                intent.putExtra("title",getString(R.string.user_agreement));
                MyToolUtils.goActivity(MobileLoginActivity.this,intent);
                return true;
            }
        });

        // add mobile login view
        MobileLoginView loginView = new MobileLoginView(this, R.layout.login_layout_mobile_view);
        if (_loginType == Constants.LOGIN_TYPE_CHANGE_MOBILE) {
            loginView.setLoginType(Constants.TYPE_UPDATE_PHONE);
            viewBinding.userAgreement.setVisibility(View.GONE);
            viewBinding.privacyAgreement.setVisibility(View.GONE);
        } else {
            loginView.setLoginType(Constants.TYPE_LOGIN);
            viewBinding.userAgreement.setVisibility(View.VISIBLE);
            viewBinding.privacyAgreement.setVisibility(View.VISIBLE);
        }

        viewBinding.content.addView(loginView);
        loginView.setLogListener(new LoginInteract.LoginListener() {
            @Override
            public void onSuccess(LoginResultBean data) {

                getUserInfo();
            }

            @Override
            public void onFail(String code, String message) {
                if (!BaseAppUtil.isDestroy(MobileLoginActivity.this)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.s(MobileLoginActivity.this, message);
                        }
                    });
                }
            }

            @Override
            public void onFinish() {
//                dismissLoading();
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
            ToastUtil.s(MobileLoginActivity.this, "登录成功");
            finish();
        }
    }

    private void getUserInfo() {
        UserInfoInteract.getUserInfo(MobileLoginActivity.this, new UserInfoInteract.UserInfoListener() {
            @Override
            public void onSuccess(UserInfoBean data) {
                LoginManager.saveLoginInfo(MobileLoginActivity.this, data);
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
