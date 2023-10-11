package com.xh.hotme.account;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.LoginSpUtil;
import com.xh.hotme.utils.TimeUtil;


/**
 * Create by zhaozhihui on 2019/3/9
 **/

/**
 * manage user data. file and user sharepreference
 */
@Keep
public class LoginManager {

    public static UserInfoBean loginUserInfo;
    public static long loginTime;
    public static final int loginExpiredTime = 1;  //单位1天


    public static void init(Context context) {
        LoginSpUtil.init(context);
        //loading user info file
        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);
        if (null != userInfo) {
            LoginSpUtil.setUserId(String.valueOf(userInfo.userId));
            LoginSpUtil.saveUserToken(userInfo.userToken);
            Constants.HEADER_TOKEN = userInfo.userToken;
            loginUserInfo = userInfo;
        }

        loginTime = LoginSpUtil.getLoginTime();
    }

    public static UserInfoBean getUserLoginInfo(Context context) {
        if (loginUserInfo != null) {
            return loginUserInfo;
        }

        if(Constants.TEST_MODE){
            loginUserInfo = new UserInfoBean();
            loginUserInfo.nickName ="测试";
            loginUserInfo.phone ="13800000000";
            loginUserInfo.userToken = Constants.TEST_TOKEN;
            loginUserInfo.userId = 1;
            return loginUserInfo;
        }

        return AppFileUtil.loadUserInfo(context);
    }

    public static long getLoginTime() {

        if (loginTime == 0) {
            loginTime = LoginSpUtil.getLoginTime();
        }
        return loginTime;
    }

    public static void updateLoginTime() {

        //如果没有登录，则返回
        if (TextUtils.isEmpty(getUserToken())) {
            return;
        }

        long curTimeStamp = System.currentTimeMillis();
        loginTime = curTimeStamp;
        LoginSpUtil.saveLoginTime(curTimeStamp);

    }

    //判断是否过期，过期时间1天
    public static boolean isLoginExpired(Context context) {
        if (loginTime == 0) {
            return true;
        }

        boolean valid = TimeUtil.isOnTime(loginTime, loginExpiredTime);
        return  !valid;

    }

    //判断是否登录过，无须判断是否过期
    public static boolean isLogon(Context context) {
        /*if(Constants.TEST_MODE){
            return true;
        }*/
        return !TextUtils.isEmpty(getUserToken(context));
    }

    public static boolean isSignedIn(Context context) {
        /*if(Constants.TEST_MODE){
            return true;
        }*/
        return !TextUtils.isEmpty(getUserToken(context)) && !isLoginExpired(context);
    }

    /*
     * leto里是以手机号或者是mgc_开始的临时账号，作为uid
     */
    public static String getUserId(Context context) {
        //
        LoginSpUtil.init(context);

        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);

        if (null != userInfo) {

            return "" + userInfo.userId;
        }

        return "";
    }


    public static String getUserToken() {

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            return Constants.HEADER_TOKEN;
        }

        return LoginSpUtil.getUserToken();
    }

    public static String getUserToken(Context context) {
        if(Constants.TEST_MODE){
            return Constants.TEST_TOKEN;
        }

        LoginSpUtil.init(context);
        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            return Constants.HEADER_TOKEN;
        }

        return LoginSpUtil.getUserToken();
    }

    public static void saveLoginInfo(Context context, UserInfoBean data) {
        if (null == data) {
            return;
        }
        LoginSpUtil.saveLoginInfo(context, data);
        loginUserInfo = data;
    }

    public static String getNickname(Context context) {
        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);
        if (null != userInfo && !TextUtils.isEmpty(userInfo.nickName)) {
            return userInfo.nickName;
        } else {
            LoginSpUtil.init(context);
            if (!TextUtils.isEmpty(LoginSpUtil.getNickname())) {
                return LoginSpUtil.getNickname();
            }
        }
        return "";
    }

    public static boolean setNickname(Context context, String nickname) {
        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);
        if (null != userInfo) {
            userInfo.nickName = nickname;
        } else {
            return false;
        }
        return true;
    }

    public static String getMobile(Context context) {
        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);
        if (null != userInfo && !TextUtils.isEmpty(userInfo.phone)) {
            return userInfo.phone;
        } else {
            LoginSpUtil.init(context);
            if (!TextUtils.isEmpty(LoginSpUtil.getMobile())) {
                return LoginSpUtil.getMobile();
            }
        }
        return "";
    }

    public static String getPortrait(Context context) {
        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);
        if (null != userInfo && !TextUtils.isEmpty(userInfo.avatar)) {
            return userInfo.avatar;
        } else {
            LoginSpUtil.init(context);
            if (!TextUtils.isEmpty(LoginSpUtil.getPortrait())) {
                return LoginSpUtil.getPortrait();
            }
        }
        return "";
    }

    public static boolean setPortrait(Context context, String portrait) {
        UserInfoBean userInfo = AppFileUtil.loadUserInfo(context);
        if (null != userInfo) {
            userInfo.avatar = portrait;
        } else {
            return false;
        }
        return true;
    }

    public static void clearLoginInfo(Context context) {
        AppFileUtil.deleteUserInfo(context, AppFileUtil.DEFAULT_USER_INFO);
        LoginSpUtil.clearLogin();
        Constants.HEADER_TOKEN = "";
        loginUserInfo = null;

        loginTime = 0;
        LoginSpUtil.saveLoginTime(0);
    }


    public static void saveUserToken(String token) {
        LoginSpUtil.saveUserToken(token);
        Constants.HEADER_TOKEN = token;
    }
}
