package com.xh.hotme.account;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xh.hotme.R;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.RegExpUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.LoadingDialog;

/**
 * Created by Liuhongliangsdk on 2016/11/11.
 */
public class MobileLoginView extends FrameLayout implements View.OnClickListener {
    private static final String TAG = MobileLoginView.class.getSimpleName();
    //登陆
    private Activity loginActivity;
    private EditText mgc_et_loginAccount;
    private EditText mgc_et_sms_code;
    private TextView mgc_btn_loginSubmit;
    private Button mgc_btn_send_sms_code;

    private TextView login_status;
    private ImageView clear_view;

    private LinearLayout layout_status;

    Handler handler = new Handler();

    private LoginInteract.LoginListener onLoginListener;


    LoginViewCallback _loginCallBack;

    SendSmsInteract.SendSmsListener onSmsListener;

    private LoadingDialog loadingDialog;

    private String _loginType = Constants.TYPE_LOGIN;
    private String _mac = "";
    private String _sn = "";
    private String _deviceModel = "";
    private String _bluetoothUuid = "";
    private String _deviceName = "";
    private String _deviceType = "";


    private void startCodeTime(int time) {
        mgc_btn_send_sms_code.setTag(time);
        if (time <= 0) {
            mgc_btn_send_sms_code.setText("获取验证码");
            mgc_btn_send_sms_code.setClickable(true);
            return;
        } else {
            mgc_btn_send_sms_code.setClickable(false);
            mgc_btn_send_sms_code.setText(time + "秒");
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int delayTime = (int) mgc_btn_send_sms_code.getTag();
                startCodeTime(--delayTime);

            }
        }, 1000);
    }

    public MobileLoginView(Context context) {
        super(context);
        setupUI(0,"");
    }

    public MobileLoginView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI(0,"");
    }

    public MobileLoginView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupUI(0,"");
    }

    public MobileLoginView(Context context, int layoutId) {
        super(context);
        setupUI(layoutId,"");
    }

    public MobileLoginView(Context context, int layoutId,String login_type) {
        super(context);
        setupUI(layoutId,login_type);
    }

    private void setupUI(int layoutId,String login_type) {

        loginActivity = (Activity) getContext();
        if (layoutId == 0) {
            layoutId = R.layout.login_include_login_merge;
        }
        LayoutInflater.from(getContext()).inflate(layoutId, this);
        if(!TextUtils.isEmpty(login_type)){
            _loginType = login_type;
        }


        layout_status = findViewById(R.id.ll_status);
        login_status = findViewById(R.id.login_status);
        clear_view = findViewById(R.id.clear);

        mgc_et_loginAccount = findViewById(R.id.et_loginAccount);
        mgc_et_sms_code = findViewById(R.id.et_sms_code);
        mgc_btn_loginSubmit = findViewById(R.id.btn_loginSubmit);
        mgc_btn_send_sms_code = findViewById(R.id.btn_send_sms_code);
        mgc_btn_loginSubmit.setOnClickListener(this);
        mgc_btn_send_sms_code.setOnClickListener(this);

        onLoginListener = new LoginInteract.LoginListener() {
            @Override
            public void onSuccess(LoginResultBean data) {
                if (TextUtils.isEmpty(mLoginMessage)) {
                    ToastUtil.s(loginActivity.getApplicationContext(), "登录成功");
                } else {
                    ToastUtil.s(loginActivity.getApplicationContext(), mLoginMessage);
                }
                loginActivity.finish();
            }

            @Override
            public void onFail(String code, String message) {

                postLoginFail(message);
            }

            @Override
            public void onFinish() {

                dismissLoading();
            }
        };

        clear_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mgc_et_loginAccount.setText("");
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //自动设置相应的布局尺寸

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mgc_btn_loginSubmit.getId()) {
            layout_status.setVisibility(View.GONE);
            String mobile = mgc_et_loginAccount.getText().toString().trim();
            String code = mgc_et_sms_code.getText().toString().trim();
            if (!RegExpUtil.isMobileNumber(mobile)) {
                layout_status.setVisibility(VISIBLE);
                login_status.setText("请输入正确的手机号");
                return;
            }
            if (TextUtils.isEmpty(code)) {
                layout_status.setVisibility(VISIBLE);
                login_status.setText("验证码不能为空");
                return;
            }
            if (_loginCallBack != null) {
                _loginCallBack.requestLogin(mobile, code);
            } else {
                if (_loginType.equalsIgnoreCase(Constants.TYPE_LOGIN)) {
                    LoginInteract.submitLogin(loginActivity, mobile, code, onLoginListener);
                } else if (_loginType.equalsIgnoreCase(Constants.TYPE_UNBIND)) {
                    UnbindInteract.submitUnbind(loginActivity, mobile, code, _mac, onLoginListener);
                } else if (_loginType.equalsIgnoreCase(Constants.TYPE_UPDATE_PHONE)) {
                    LoginInteract.submitUpdateMobile(loginActivity, mobile, code, new ICommonListener() {
                        @Override
                        public void onSuccess() {
                            if (onLoginListener != null) {
                                onLoginListener.onSuccess(null);
                            }
                        }

                        @Override
                        public void onFail(String code, String message) {
                            if (onLoginListener != null) {
                                onLoginListener.onFail(code, message);
                            }
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                }
            }
        } else if (view.getId() == mgc_btn_send_sms_code.getId()) {
            layout_status.setVisibility(View.GONE);
            String mobile = mgc_et_loginAccount.getText().toString().trim();
            if (!RegExpUtil.isMobileNumber(mobile)) {
                layout_status.setVisibility(VISIBLE);
                login_status.setText("请输入正确的手机号");
                return;
            }

            if (_loginCallBack != null) {
                _loginCallBack.requestCode(mobile);
            } else {
                if (_loginType.equalsIgnoreCase(Constants.TYPE_LOGIN)) {
                    SendSmsInteract.sendLoginSMS(loginActivity, mobile, new SendSmsInteract.SendSmsListener() {
                        @Override
                        public void onSuccess() {
                            postSendCodeSuc();
                        }

                        @Override
                        public void onFail(String code, String message) {
                            postSendCodeFail(message);
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                } else if (_loginType.equalsIgnoreCase(Constants.TYPE_UNBIND)) {

                    SendSmsInteract.sendUnbindSMS(loginActivity, mobile, new SendSmsInteract.SendSmsListener() {
                        @Override
                        public void onSuccess() {
                            postSendCodeSuc();
                        }

                        @Override
                        public void onFail(String code, String message) {
                            postSendCodeFail(message);
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                }else if (_loginType.equalsIgnoreCase(Constants.TYPE_UPDATE_PHONE)) {
                    SendSmsInteract.sendUpdateMobileSMS(loginActivity, mobile, new SendSmsInteract.SendSmsListener() {
                        @Override
                        public void onSuccess() {
                            postSendCodeSuc();
                        }

                        @Override
                        public void onFail(String code, String message) {
                            postSendCodeFail(message);
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                }
            }
        }
    }

    public void postSendCodeSuc() {
        startCodeTime(Constants.sms_count_down);
        mgc_et_loginAccount.clearFocus();
        mgc_et_sms_code.requestFocus();
    }


    public void postSendCodeFail(String message) {
        layout_status.setVisibility(VISIBLE);
        login_status.setText("验证码发送失败");
    }

    public void postLoginSuccess() {
        if (TextUtils.isEmpty(mLoginMessage)) {
            ToastUtil.s(loginActivity.getApplicationContext(), "登录成功");
        } else {
            ToastUtil.s(loginActivity.getApplicationContext(), mLoginMessage);
        }
    }

    public void postLoginFail(String message) {
        layout_status.setVisibility(VISIBLE);
        login_status.setText(message != null ? message : "登录失败");
    }


    private String mLoginMessage;

    public void setLoginMessage(String message) {
        this.mLoginMessage = message;
    }

    public void setLogListener(LoginInteract.LoginListener l) {
        onLoginListener = l;
    }


    public void setSmsListener(SendSmsInteract.SendSmsListener l) {
        onSmsListener = l;
    }


    public void setLoginCallBack(LoginViewCallback l) {
        _loginCallBack = l;
    }

    public void showLoading(Context context, String message) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(context);
        }
        loadingDialog.show();
    }


    public void showLoading(Context context) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(context);
        }
        loadingDialog.show();
    }

    public void dismissLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public void setButtonText(String message) {

        mgc_btn_loginSubmit.setText(message);
    }

    public void setLoginType(String loginType) {
        _loginType = loginType;
    }

    public void setMac(String mac) {
        _mac = mac;
    }

    public void setSn(String sn) {
        _sn = sn;
    }

    public void setDeviceModel(String model) {
        _deviceModel = model;
    }

    public void setBluetoothUuid(String uuid) {
        _bluetoothUuid = uuid;
    }

    public void setDeviceName(String name) {
        _deviceName = name;
    }

    public void setDeviceType(String type) {
        _deviceType = type;
    }
}
