package com.xh.hotme.account;

import android.content.Context;

import androidx.annotation.Keep;

import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;


/**
 *
 **/
@Keep
public class DeleteInteract {

    public static void delete(final Context context, final ICommonListener onLoginListener) {

        String url = SdkApi.deleteUser();

        OkHttpUtil.delete(url, null, new OkHttpCallbackDecode<Object>() {
            @Override
            public void onDataSuccess(Object data) {
                AppTrace.d("DeleteInteract", "");
                //保存登录信息
                LoginManager.clearLoginInfo(context);
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //接口回调通知
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
}
