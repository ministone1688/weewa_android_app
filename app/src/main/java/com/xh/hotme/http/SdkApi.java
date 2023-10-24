package com.xh.hotme.http;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;


import com.xh.hotme.utils.AppTrace;


/**
 * Created by liu hong liang on 2016/11/9.
 */
@Keep
public class SdkApi {
    private static final String TAG = SdkApi.class.getSimpleName();

    private static final String HOST_DOMAIN = "weewa.rewoo360.com";
    private static final String HOST_SCHEME = "https://" + HOST_DOMAIN;

    public static final String TOKEN_PREFIX = "Bearer ";


    public static final String endpoints_login = "/hotme-api/app/user/mobileLogin";
    public static final String endpoints_userinfo_get = "/hotme-api/app/user/userInfo";
    public static final String endpoints_userinfo_update = "/hotme-api/app/user";
    public static final String endpoints_avatar = "/hotme-api/system/user/profile/avatar";
    public static final String endpoints_update_phone = "/hotme-api/system/user/profile/updatePhone";

    public static final String endpoints_logout = "/hotme-api/logout";
    public static final String endpoints_user_delete = "/hotme-api/app/user";

    public static final String endpoints_send_sms_code = "/hotme-api/hm/device/sendSmsCode";
    public static final String endpoints_check_sms_code = "/hotme-api/hm/device/checkSmsCode";
    public static final String endpoints_device_unbind = "/hotme-api/hm/device/unbind";
    public static final String endpoints_device_list = "/hotme-api/hm/device/list";
    public static final String endpoints_video_list = "/hotme-api/hm/video/list";
    public static final String endpoints_video_preload = "/hotme-api/hm/video/preUpload";
    public static final String endpoints_video_upload_progress = "/hotme-api/hm/video/uploadProgress";

    public static boolean isTestServer;


    public static String getDomain() {
        return HOST_DOMAIN;
    }

    public static String getRequestUrl() {
        return HOST_SCHEME;
    }

    public static String getSmsSend() {
        return getRequestUrl() + endpoints_send_sms_code;
    }

    //一键登陆or注册
    public static String getLoginRegister() {
        printUrl(endpoints_login);
        return getRequestUrl() + endpoints_login;
    }

    //获取用户信息
    public static String getUserInfo() {
        printUrl(endpoints_userinfo_get);
        return getRequestUrl() + endpoints_userinfo_get;
    }

    //设置昵称
    public static String setNickName() {
        printUrl(endpoints_userinfo_update);
        return getRequestUrl() + endpoints_userinfo_update;
    }

    //设置用户头像
    public static String setAvatar() {
        return getRequestUrl() + endpoints_avatar;
    }

    //修改手机号
    public static String updateMobile() {
        return getRequestUrl() + endpoints_update_phone;
    }

    //退出登陆
    public static String logout() {
        return getRequestUrl() + endpoints_logout;
    }

    //注销账号
    public static String deleteUser() {
        return getRequestUrl() + endpoints_user_delete;
    }

    //用户收到绑定验证码之后向服务端发送校验验证码请求，并且获取设备mac地址
    public static String checkDeviceSmsCode() {
        return getRequestUrl() + endpoints_check_sms_code;
    }

    //解绑设备
    public static String unbindDevice() {
        return getRequestUrl() + endpoints_device_unbind;
    }

    //绑定的设备列表
    public static String getDeviceList() {
        return getRequestUrl() + endpoints_device_list;
    }

    //绑定的设备列表
    public static String getVideoList() {
        return getRequestUrl() + endpoints_video_list;
    }

    //预上传视频
    public static String preUpload() {
        return getRequestUrl() + endpoints_video_preload;
    }

    //预上传视频
    public static String uploadProgress() {
        return getRequestUrl() + endpoints_video_upload_progress;
    }


    private static void printUrl(String path) {
        AppTrace.e(TAG, "http_url=" + getRequestUrl() + path);
    }

    //用户协议
    public static final String user_userproxy = getRequestUrl() + "/hotme-api/app/userproxy";
    //隐私政策
    public static final String user_privateurl = getRequestUrl() + "/hotme-api/app/privateurl";
	 //如何获取推流地址
    public static final String user_pushUrl = getRequestUrl() + "/hotme-api/app/getPushUrl";
    //如何使用热我
    public static final String user_useCamera = getRequestUrl() + "/hotme-api/app/getPushUrl";
    //最新版本
    public static final String upgrade = getRequestUrl() + "/hotme-api/app/upgrade";
    //获取直播推流信息
    public static final String getPushAddress = getRequestUrl() + "/hotme-api/hm/deviceLive/getPushAddress";
    //保存推流地址
    public static final String savePushAddress = getRequestUrl() + "/hotme-api/hm/deviceLive/savePushAddress";


}
