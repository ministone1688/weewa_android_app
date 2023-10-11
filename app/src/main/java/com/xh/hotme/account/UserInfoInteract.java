package com.xh.hotme.account;

import android.content.Context;

import androidx.annotation.Keep;

import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;

/**
 * Create by zhaozhihui on 2018/11/14
 **/
@Keep
public class UserInfoInteract {

    public static void getUserInfo(final Context context, final UserInfoListener onLoginListener) {

        OkHttpUtil.get(SdkApi.getUserInfo(), new OkHttpCallbackDecode<UserInfoBean>() {
            @Override
            public void onDataSuccess(UserInfoBean data) {
                if (data != null) {
                    //保存登录信息
                    data.userToken = Constants.HEADER_TOKEN;
                    LoginManager.saveLoginInfo(context, data);

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

    @Keep
    public interface UserInfoListener {
        void onSuccess(UserInfoBean data);

        void onFail(String code, String message);

        void onFinish();
    }
}
