package com.xh.hotme.account;

import android.content.Context;

import com.google.gson.Gson;
import com.xh.hotme.bean.SmsSendRequestBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;
import com.xh.hotme.utils.RegExpUtil;
import com.xh.hotme.utils.ToastUtil;


/**
 * Create by zhaozhihui on 2018/11/14
 **/
public class SendSmsInteract {


    public static void sendLoginSMS(Context context, String account, final SendSmsListener onSendSmsListener) {
        sendSMS(context, account, Constants.TYPE_LOGIN, onSendSmsListener);
    }


    public static void sendUpdateMobileSMS(Context context, String account, final SendSmsListener onSendSmsListener) {
        sendSMS(context, account, Constants.TYPE_UPDATE_PHONE, onSendSmsListener);
    }

    public static void sendSMS(Context context, String account, String smsType, final SendSmsListener onSendSmsListener) {
        if (!RegExpUtil.isMobileNumber(account)) {
            ToastUtil.s(context, "请输入正确的手机号");
            return;
        }

        SmsSendRequestBean requestBean = new SmsSendRequestBean();
        requestBean.setSmsType(smsType);
        requestBean.setMobile(account);

        OkHttpUtil.postData(SdkApi.getSmsSend(),  new Gson().toJson(requestBean), null,  new OkHttpCallbackDecode() {
            @Override
            public void onDataSuccess(Object data) {

                //开始计时控件
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //接口回调通知
                        if (null != onSendSmsListener) {
                            onSendSmsListener.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(String code, String message) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onSendSmsListener != null) {
                            onSendSmsListener.onFail(code, message);
                        }
                    }
                });
            }
            @Override
            public void onFinish(){
                super.onFinish();
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onSendSmsListener != null) {
                            onSendSmsListener.onFinish();
                        }
                    }
                });

            }
        });

    }


    public interface SendSmsListener {
        void onSuccess();

        void onFail(String code, String message);

        void onFinish();
    }
}
