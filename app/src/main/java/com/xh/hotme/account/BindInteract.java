package com.xh.hotme.account;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.xh.hotme.R;
import com.xh.hotme.bean.BindResultBean;
import com.xh.hotme.bean.LoginMobileRequestBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;
import com.xh.hotme.utils.RegExpUtil;


/**
 * Create by zhaozhihui on 2018/11/14
 **/
@Keep
public class BindInteract {

    public static void checkSmsCode(final Context context, final String account, String smscode, final LoginListener onLoginListener) {
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

        String url = SdkApi.checkDeviceSmsCode()+"?mobile="+account+"&code="+smscode;

        OkHttpUtil.get(url, null, new OkHttpCallbackDecode<BindResultBean>() {
            @Override
            public void onDataSuccess(BindResultBean data) {
                if (data != null) {
                    AppTrace.d("BindInteract", "mac = " + data.getMac());
                    //保存登录信息

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
                    }});
            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onLoginListener != null) {
                            onLoginListener.onFinish();
                        }
                    }});
            }
        });

    }

    @Keep
    public interface LoginListener {
        void onSuccess(BindResultBean data);

        void onFail(String code, String message);

        void onFinish();
    }
}
