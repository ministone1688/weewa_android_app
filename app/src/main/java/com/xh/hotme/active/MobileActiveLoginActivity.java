package com.xh.hotme.active;


import static com.xh.hotme.utils.Constants.REQUEST_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.MainActivity;
import com.xh.hotme.R;
import com.xh.hotme.account.BindInteract;
import com.xh.hotme.account.LoginInteract;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.UserInfoInteract;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.BindResultBean;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleActiveLoginListener;
import com.xh.hotme.device.ConnectActivity;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastCustom;
import com.xh.hotme.utils.ToastUtil;


public class MobileActiveLoginActivity extends BaseActivity implements IBleActiveLoginListener {
    private static final String TAG = MobileActiveLoginActivity.class.getSimpleName();
    // views
    private ImageView _backBtn;
    private FrameLayout _content;
    private EditText _smsEditText;
    private TextView _mobileLabel, _loginLabel;
    private TextView _userAgreementLabel;
    private TextView _privacyAgreementLabel;


    private String _mobile;
    private int reqestCode = -1;

    private int _loginType = 0;


    Handler _handler;

    Runnable _timeoutRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    public static void start(Context context, String mobile, int type) {
        if (null != context) {
            Intent intent = new Intent(context, MobileActiveLoginActivity.class);
            intent.putExtra(Constants.REQUEST_MOBILE, mobile);
            intent.putExtra(Constants.ACTIVE_LOGIN, type);
            context.startActivity(intent);
        }
    }

    public static void startActivityByRequestCode(Activity context, int requestCode) {
        if (null != context) {
            Intent intent = new Intent(context, MobileActiveLoginActivity.class);
            intent.putExtra(REQUEST_CODE, requestCode);

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

        _handler = new Handler();

        // set content view
        setContentView(R.layout.activity_mobile_active);

        if (getIntent() != null) {
            reqestCode = getIntent().getIntExtra(REQUEST_CODE, -1);
            _mobile = getIntent().getStringExtra(Constants.REQUEST_MOBILE);
            _loginType = getIntent().getIntExtra(Constants.ACTIVE_LOGIN, Constants.ACTIVE_TYPE_ACTIVE);
        }

        // find views
        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText(getString(R.string.login));
        _backBtn = findViewById(R.id.iv_back);
        _content = findViewById(R.id.content);
        _mobileLabel = findViewById(R.id.tv_mobile);
        _smsEditText = findViewById(R.id.et_sms_code);
        _loginLabel = findViewById(R.id.btn_loginSubmit);
        _userAgreementLabel = findViewById(R.id.user_agreement);
        _privacyAgreementLabel = findViewById(R.id.privacy_agreement);

        // back click
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        _mobileLabel.setText(_mobile);

        // agreement label underline & click
        _userAgreementLabel.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        _userAgreementLabel.getPaint().setAntiAlias(true);
        _privacyAgreementLabel.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        _privacyAgreementLabel.getPaint().setAntiAlias(true);
        _userAgreementLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                //DialogUtil.showAgreement(MobileActiveLoginActivity.this, "user.html");
                return true;
            }
        });
        _privacyAgreementLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                // DialogUtil.showAgreement(MobileActiveLoginActivity.this, "privacy.html");
                return true;
            }
        });

        _loginLabel.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                String sms = _smsEditText.getText().toString();
                if (TextUtils.isEmpty(sms)) {
                    ToastUtil.s(MobileActiveLoginActivity.this, getString(R.string.login_hint_input_sms));
                    return true;
                }

                if (_loginType == Constants.ACTIVE_TYPE_LOGIN) {
                    LoginInteract.submitLogin(MobileActiveLoginActivity.this, _mobile, sms, new LoginInteract.LoginListener() {
                        @Override
                        public void onSuccess(LoginResultBean data) {
                            getUserInfo();
                        }

                        @Override
                        public void onFail(String code, String message) {
                            if (!BaseAppUtil.isDestroy(MobileActiveLoginActivity.this)) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastCustom.showCustomToast(MobileActiveLoginActivity.this, message);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFinish() {

                        }
                    });


                } else {

                    BindInteract.checkSmsCode(MobileActiveLoginActivity.this, _mobile, sms, new BindInteract.LoginListener() {
                        @Override
                        public void onSuccess(BindResultBean data) {

                            BluetoothManager.mInstance.sendCmdDeviceActiveBind(_mobile, data.getMac());

                            _handler.postDelayed(_timeoutRunnable, BluetoothManager.RESP_TIMEOUT);

                        }

                        @Override
                        public void onFail(String code, String message) {
                            if (!BaseAppUtil.isDestroy(MobileActiveLoginActivity.this)) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastCustom.showCustomToast(MobileActiveLoginActivity.this, message);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                }
                return true;
            }
        });
        BluetoothHandle.addDeviceActiveLoginNotifyListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothHandle.removeDeviceActiveLoginNotifyListener(this);
    }

    public void doFinish(boolean success) {
        if (reqestCode > -1) {
            Intent i = new Intent();
            i.putExtra("result", 0);
            setResult(success ? 1 : 0, i);
            finish();
        } else {
            ToastUtil.s(MobileActiveLoginActivity.this, "登录成功");
            finish();
        }
    }

    private void getUserInfo() {
        UserInfoInteract.getUserInfo(MobileActiveLoginActivity.this, new UserInfoInteract.UserInfoListener() {
            @Override
            public void onSuccess(UserInfoBean data) {

                if (!BaseAppUtil.isDestroy(MobileActiveLoginActivity.this)) {
                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.start(MobileActiveLoginActivity.this);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onFail(String code, String message) {
                if (!BaseAppUtil.isDestroy(MobileActiveLoginActivity.this)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastCustom.showCustomToast(MobileActiveLoginActivity.this, message);
                        }
                    });
                }
            }

            @Override
            public void onFinish() {
                doFinish(true);
            }
        });
    }

    @Override
    public void onActive(LoginResultBean loginResultBean) {
        LoginManager.saveUserToken(loginResultBean.getToken());
        UserInfoInteract.getUserInfo(MobileActiveLoginActivity.this, new UserInfoInteract.UserInfoListener() {
            @Override
            public void onSuccess(UserInfoBean data) {
                AppTrace.i(TAG, "getUserInfo ：" + data.toString());
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectActivity.start(MobileActiveLoginActivity.this, null, Constants.ACTIVE_STEP_BIND_SUCCESS);

                        finish();
                    }
                });
            }

            @Override
            public void onFail(String code, String message) {
                AppTrace.i(TAG, "get user info error: " + message);
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.s(MobileActiveLoginActivity.this, "获取用户信息失败" + message);
                    }
                });
            }

            @Override
            public void onFinish() {

            }
        });
    }

    @Override
    public void onActiveFail(String message) {
        AppTrace.i(TAG, "onActiveFail fail: " + message);
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.s(MobileActiveLoginActivity.this, message);
            }
        });
    }
}
