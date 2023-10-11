package com.xh.hotme.account;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.xh.hotme.R;
import com.xh.hotme.bean.UnbindRequestBean;
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
public class UnbindInteract {

    public static void submitUnbind(final Context context, final String account, String smscode, String mac, final LoginInteract.LoginListener onLoginListener) {
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

        UnbindRequestBean loginMobileRequestBean = new UnbindRequestBean();
        loginMobileRequestBean.setMobile(account);
        loginMobileRequestBean.setCode(smscode);
        loginMobileRequestBean.setMac(mac);

        OkHttpUtil.postData(SdkApi.unbindDevice(), new Gson().toJson(loginMobileRequestBean), null, new OkHttpCallbackDecode<Object>() {
            @Override
            public void onDataSuccess(Object data) {
                if (data != null) {
                    AppTrace.d("UnbindInteract", "onDataSuccess");

                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            //接口回调通知
                            if (onLoginListener != null) {
                                onLoginListener.onSuccess(null);
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
}
