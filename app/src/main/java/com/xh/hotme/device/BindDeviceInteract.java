package com.xh.hotme.device;

import android.content.Context;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.bean.BindDeviceResultBean;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.OkHttpUtil;


/**
 *
 **/
@Keep
public class BindDeviceInteract {
    public static void getDeviceList(final Context context, int pageNumber, int pageSize, final IBindDeviceListener onListener) {
        UserInfoBean userInfoBean = LoginManager.getUserLoginInfo(context);
        String url = SdkApi.getDeviceList() + "?mobile=" + userInfoBean.phone + "&pageNum=" + pageNumber + "&pageSize=" + pageSize;

        OkHttpUtil.get(url, new OkHttpCallbackDecode<BindDeviceResultBean>(null, new TypeToken<BindDeviceResultBean>() {
        }.getType()) {

            @Override
            public void onDataSuccess(BindDeviceResultBean data) {
                if (pageNumber == 1) {
                    AppFileUtil.saveJson(context, new Gson().toJson(data), AppFileUtil.CACHE_DEVICE_BIND_FILE);
                }
                if (onListener != null) {
                    onListener.onDeviceList(data);
                }
            }

            @Override
            public void onFailure(String code, String message) {
                super.onFailure(code, message);
                if (onListener != null) {
                    onListener.onFail(code, message);
                }
            }

            @Override
            public void onFinish() {
                if (onListener != null) {
                    onListener.onFinish();
                }
            }
        });
    }

    public interface IBindDeviceListener {
        void onDeviceList(BindDeviceResultBean data);

        void onFail(String code, String msg);

        void onFinish();
    }
}
