package com.xh.hotme.account;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;


import com.xh.hotme.R;
import com.xh.hotme.bean.LoginMobileRequestBean;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;
import com.xh.hotme.utils.RegExpUtil;


/**
 * Create by zhaozhihui on 2018/11/14
 **/
@Keep
public class LoginInteract {

    public static void submitLogin(final Context context, final String account, String smscode, final LoginListener onLoginListener) {
        if (!RegExpUtil.isMobileNumber(account)) {
            if (onLoginListener != null) {
                onLoginListener.onFail("-1", context.getString(R.string.login_error_phone_format));
            }
            return;
        }
        if (TextUtils.isEmpty(smscode)) {
            if (onLoginListener != null) {
                onLoginListener.onFail("-2", context.getString(R.string.login_error_verify_code_null));
            }
            return;
        }

        LoginMobileRequestBean loginMobileRequestBean = new LoginMobileRequestBean();
        loginMobileRequestBean.setMobile(account);
        loginMobileRequestBean.setSmscode(smscode);

        String url = SdkApi.getLoginRegister() + "?mobile=" + account + "&code=" + smscode;

        OkHttpUtil.get(url, null, new OkHttpCallbackDecode<LoginResultBean>() {
            @Override
            public void onDataSuccess(LoginResultBean data) {
                if (data != null) {
                    AppTrace.d("LoginInteract", "token = " + data.getToken());

                    LoginManager.saveUserToken(data.getToken());
                    Constants.HEADER_TOKEN = data.getToken();

                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            //接口回调通知
                            if (onLoginListener != null) {
                                onLoginListener.onSuccess(data);
                            }
                        }
                    });

                } else {
                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (onLoginListener != null) {
                                onLoginListener.onFail("-1", "data is null");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String code, String message) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoginListener != null) {
                            onLoginListener.onFail(code, message);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoginListener != null) {
                            onLoginListener.onFinish();
                        }
                    }
                });
            }
        });

    }


    public static void submitUpdateMobile(final Context context, final String account, String smscode, final ICommonListener onLoginListener) {
        if (!RegExpUtil.isMobileNumber(account)) {
            if (onLoginListener != null) {
                onLoginListener.onFail("-1", context.getString(R.string.login_error_phone_format));
            }
            return;
        }
        if (TextUtils.isEmpty(smscode)) {
            if (onLoginListener != null) {
                onLoginListener.onFail("-2", context.getString(R.string.login_error_verify_code_null));
            }
            return;
        }

        LoginMobileRequestBean loginMobileRequestBean = new LoginMobileRequestBean();
        loginMobileRequestBean.setMobile(account);
        loginMobileRequestBean.setSmscode(smscode);

        String url = SdkApi.updateMobile() + "?phone=" + account + "&code=" + smscode;

        OkHttpUtil.get(url, null, new OkHttpCallbackDecode<LoginResultBean>() {
            @Override
            public void onDataSuccess(LoginResultBean data) {
                AppTrace.d("LoginInteract", "token = " + data.getToken());
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoginListener != null) {
                            onLoginListener.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(String code, String message) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoginListener != null) {
                            onLoginListener.onFail(code, message);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoginListener != null) {
                            onLoginListener.onFinish();
                        }
                    }
                });
            }
        });

    }


    public static void logout(final Context context, final ICommonListener onLoginListener) {
        OkHttpUtil.postData(SdkApi.logout(), "", null, new OkHttpCallbackDecode<Object>() {
            @Override
            public void onDataSuccess(Object data) {

                LoginManager.clearLoginInfo(context);

                if (onLoginListener != null) {
                    onLoginListener.onSuccess();
                }
            }

            @Override
            public void onFailure(String code, String message) {

                if (onLoginListener != null) {
                    onLoginListener.onFail(code, message);
                }
            }

            @Override
            public void onFinish() {

            }
        });

    }

    @Keep
    public interface LoginListener {
        void onSuccess(LoginResultBean data);

        void onFail(String code, String message);

        void onFinish();
    }
}
