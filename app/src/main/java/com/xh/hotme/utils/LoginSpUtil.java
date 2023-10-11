package com.xh.hotme.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.xh.hotme.bean.UserInfoBean;


/**
 * @作者: XQ
 * @创建时间：15-7-20 下午10:08
 * @类说明:登录控制类
 */
@Keep
public class LoginSpUtil {
    public static final String PRFS_USER_TOKEN = "prfs_user_token";
    public static final String PRFS_LOGIN_TIME = "prfs_login_time";
    public static final String PRFS_AGENT_GAME = "prfs_agent_game";
    public static final String PRFS_USER_ID = "prfs_user_id";
    public static final String PRFS_MEM_ID = "prfs_mem_id";
    public static final String PRFS_AVATAR = "prfs_avatar";
    public static final String PRFS_NICKNAME = "prfs_nickname";
    public static final String PRFS_MOBILE = "prfs_mobile";

    public static final String PRFS_CHANNEL_NAME = "prfs_channel_name";


    protected static SharedPreferences login_sp = null;
    protected static String mUserToken;
    protected static long loginTimeStamp;
    protected static String portrait;
    protected static String mNickname;
    protected static String mMobile;
    protected static String mVipInfo;

    public static synchronized void init(Context context) {
        if (login_sp == null) {
            login_sp = context.getSharedPreferences(LoginSpUtil.class.getName(), Context.MODE_PRIVATE);
        }
    }

    public static void saveLoginTime(long timeStamp) {
        login_sp.edit().putLong(PRFS_LOGIN_TIME, timeStamp).commit();
    }

    public static long getLoginTime() {
        if (login_sp != null) {
            return login_sp.getLong(PRFS_LOGIN_TIME, 0);
        }
        return 0;
    }

    public static void saveUserToken(String userToken) {
        if (!TextUtils.isEmpty(userToken)) {
            mUserToken = userToken;
            login_sp.edit().putString(PRFS_USER_TOKEN, mUserToken).commit();

//            SdkConstant.userToken = mUserToken;
        }
        //
    }

    public static String getUserToken() {
        if (TextUtils.isEmpty(mUserToken)) {
            mUserToken = login_sp.getString(PRFS_USER_TOKEN, null);
        }
//        Constants.userToken = mUserToken;
        return mUserToken;
    }

    public static boolean isLogin(Context context) {
        if (login_sp == null) {
            login_sp = context.getSharedPreferences(LoginSpUtil.class.getName(), Context.MODE_PRIVATE);
        }
        return !TextUtils.isEmpty(getUserToken());
    }


    public static boolean isLogin() {
        if (null == login_sp) {
            return false;
        }
        return !TextUtils.isEmpty(getUserToken());
    }

    public static void clearLogin() {
        mUserToken = null;
        portrait = null;
        mNickname = null;
        if (login_sp != null) {
            if (login_sp.edit() != null) {
                login_sp.edit().clear().commit();
            }
        }
        saveLoginTime(0);
        saveUserToken(null);
    }

    public static String getAgentgame() {
        if (mAgentgame == null) {
            mAgentgame = login_sp.getString(PRFS_AGENT_GAME, null);
        }
        return mAgentgame;
    }

    public static void setAgentgame(String agentgame) {
        LoginSpUtil.mAgentgame = agentgame;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_AGENT_GAME, agentgame);
        e.apply();
    }

    protected static String mAgentgame;


    protected static String mUserId;

    public static String getUserId() {
        if (mUserId == null) {
            mUserId = login_sp.getString(PRFS_USER_ID, null);
        }
        return mUserId;
    }

    public static void setUserId(String userId) {
        LoginSpUtil.mUserId = userId;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_USER_ID, userId);
        e.apply();
    }

    protected static String mMemId;

    public static String getMemId() {
        if (mMemId == null) {
            mMemId = login_sp.getString(PRFS_MEM_ID, null);
        }
        return mMemId;
    }

    public static void setMemId(String memId) {
        LoginSpUtil.mMemId = memId;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_MEM_ID, memId);
        e.apply();
    }

    public static final String PRFS_MORE_NUMBER = "prfs_more_number";
    public static final String PRFS_MORE_NUMBER_DATE = "prfs_more_number_date";
    public static final String PRFS_MORE_NUMBER_SHOW = "prfs_more_number_show";

    // global key for some data, can be accessed cross process
    public static final String FILE_LOGIN_INFO_VERSION = "__leto_login_info_version";
    public static final String FILE_MGC_USER_ID = "__leto_mgc_user_id";


    protected static String mChannelName;

    public static String getChannelName() {
        if (mChannelName == null) {
            mChannelName = login_sp.getString(PRFS_CHANNEL_NAME, null);
        }
        return mChannelName;
    }

    public static void setChannelName(String channel_name) {
        LoginSpUtil.mChannelName = channel_name;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_CHANNEL_NAME, channel_name);
        e.apply();
    }

    public static String getPortrait() {
        if (portrait == null) {
            portrait = login_sp.getString(PRFS_AVATAR, null);
        }
        return portrait;
    }

    public static void setPortrait(String portrait) {
        LoginSpUtil.portrait = portrait;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_AVATAR, portrait);
        e.apply();
    }


    public static String getNickname() {
        if (mNickname == null) {
            mNickname = login_sp.getString(PRFS_NICKNAME, null);
        }
        if (TextUtils.isEmpty(mNickname)) {
            return getUserId();
        }
        return mNickname;
    }

    public static void setNickname(String nickname) {
        LoginSpUtil.mNickname = nickname;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_NICKNAME, nickname);
        e.apply();
    }

    public static String getMobile() {
        if (mMobile == null) {
            mMobile = login_sp.getString(PRFS_MOBILE, null);
        }
        return mMobile;
    }

    public static void setMobile(String mobile) {
        LoginSpUtil.mMobile = mobile;
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_MOBILE, mobile);
        e.apply();
    }

    public static void saveLoginInfo(Context context, UserInfoBean data) {
        if (null == data) {
            AppTrace.d("save null user. ");
            return;
        }

        if (login_sp == null) {
            login_sp = context.getSharedPreferences(LoginSpUtil.class.getName(), Context.MODE_PRIVATE);
        }

        portrait = data.avatar;
        mNickname = data.nickName;
        mMemId = String.valueOf(data.userId);
        mMobile = data.phone;

        mUserToken = data.userToken;
        Constants.userToken = mUserToken;
//
        //
        SharedPreferences.Editor e = login_sp.edit();
        e.putString(PRFS_NICKNAME, mNickname);
        e.putString(PRFS_AVATAR, portrait);
        e.putString(PRFS_MEM_ID, mMemId);
        e.putString(PRFS_MOBILE, mMobile);
        e.putString(PRFS_USER_TOKEN, Constants.HEADER_TOKEN);
        e.apply();

        //防止多进程token访问异常，增加文件存储
        AppFileUtil.saveUserInfo(context, new Gson().toJson(data));

    }

}
