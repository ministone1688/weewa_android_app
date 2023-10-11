package com.xh.hotme.account;

import android.content.Context;

import androidx.annotation.Keep;

import com.xh.hotme.bean.UserAvatarBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;

import java.io.File;

/**
 * Create by zhaozhihui on 2018/11/14
 **/
@Keep
public class UserAvatarInteract {

    public static void modifyPortrait(Context ctx, File file, UserAvatarListener listener) {
        OkHttpUtil.postFile(SdkApi.setAvatar(),  new OkHttpCallbackDecode<UserAvatarBean>() {
            @Override
            public void onDataSuccess(UserAvatarBean data) {
                if (data != null) {

                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            //接口回调通知
                            if (listener != null) {
                                listener.onSuccess(data);
                            }
                        }
                    });

                } else {
                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFail("-1", "data is null");
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
                        if (listener != null) {
                            listener.onFail(code, message);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            }
        }, file);
    }

    @Keep
    public interface UserAvatarListener {
        void onSuccess(UserAvatarBean data);

        void onFail(String code, String message);

        void onFinish();
    }
}
