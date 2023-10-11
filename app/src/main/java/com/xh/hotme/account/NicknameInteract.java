package com.xh.hotme.account;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.xh.hotme.R;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;
import com.xh.hotme.utils.RegExpUtil;

/**
 * Create by zhaozhihui on 2018/11/14
 **/
@Keep
public class NicknameInteract {

    public static void updateNickname(final Context context, final String nickName, final ICommonListener listener) {

        if (TextUtils.isEmpty(nickName)) {
            if (listener != null) {
                listener.onFail("-2", context.getString(R.string.login_error_verify_code_null));
            }
            return;
        }

        String url = SdkApi.setNickName() + "?nickName=" + nickName;

        OkHttpUtil.put(url, null, new OkHttpCallbackDecode<Object>() {
            @Override
            public void onDataSuccess(Object data) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    }
                });

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
        });

    }


}
