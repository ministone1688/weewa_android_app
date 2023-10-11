package com.xh.hotme.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Create by zhaozhihui on 2019-11-30
 **/
public class SpUtil {

    public static final String PRFS_FIRST_LAUNCH = "prfs_first_launch";
    public static final String PRFS_FIRST_LAUNCH_TIME = "prfs_first_launch_time";  //计算次留
    public static final String PRFS_SHAKE_TIMES = "prfs_shake_times";
    public static final String PRFS_LAST_SHAKE_TIME = "prfs_last_shake_time";
    public static final String PRFS_BUBBLE_TIMES = "prfs_bubble_times";
    public static final String PRFS_HBRAIN_TIMES = "prfs_hbrain_times";
    public static final String PRFS_HBRAIN_LAST_TIME = "prfs_hbrain_last_time";

    public static final String PRFS_DAY_INSTALL_DATE = "prfs_day_install_date";  //安装日期
    public static final String PRFS_DAY_LAUNCH_DATE = "prfs_day_launch_date";  //启动日期
    public static final String PRFS_DAY_LAUNCH_TIME = "prfs_day_launch_time";  //启动次数


    public static final String PRFS_LOBOX_INIT_TIMES = "prfs_lebox_init_times";

    public static final String PRFS_SHOW_PRIVACY_CONTENT = "prfs_privacy_content";
    public static final String PRFS_SHOW_PERMISSION_CONTENT = "prfs_permission_content";


    public static final String PRFS_TAOBAO_ACCCESS_TOKEN = "prfs_taobao_access_token";  //安装日期
    public static final String PRFS_TAOBAO_ACCCESS_EXPIRE_TIME = "prfs_taobao_access_expire_time";  //安装日期

    public static final String PRFS_VIDEO_LIST = "prfs_video_list";  //安装日期



    private static SpUtil spUtil;
    protected static SharedPreferences _SP = null;

    public static SpUtil getInstance(Context context) {
        if (spUtil == null) {
            spUtil = new SpUtil();
        }
        if (_SP == null) {
            _SP = context.getApplicationContext().getSharedPreferences(SpUtil.class.getName(), Context.MODE_PRIVATE);
        }
        return spUtil;
    }

    public static synchronized void init(Context context) {
        if (_SP == null) {
            _SP = context.getSharedPreferences(SpUtil.class.getName(), Context.MODE_PRIVATE);
        }
    }

    public static void setFirstLaunch(boolean isFirst) {

        if (_SP != null) {
            _SP.edit().putBoolean(PRFS_FIRST_LAUNCH, isFirst).apply();
        }
    }

    public static boolean isFirstLaunch() {
        if (_SP != null) {
            return _SP.getBoolean(PRFS_FIRST_LAUNCH, true);
        }
        return true;
    }

    public static void setFirstLaunchTimeStamp(long time) {

        if (_SP != null) {
            _SP.edit().putLong(PRFS_FIRST_LAUNCH_TIME, time).apply();
        }
    }

    public static long getFirstLaunchTimeStamp() {
        if (_SP != null) {
            return _SP.getLong(PRFS_FIRST_LAUNCH_TIME, 0);
        }
        return 0;
    }

    public static int todayShakeTimes(String gameId) {
        if (_SP != null) {
            String key = PRFS_SHAKE_TIMES + "_" + gameId + "_" + getDay();
            return _SP.getInt(key, 0);
        }
        return 0;
    }

    public static long lastShakeTime(String gameId) {
        if (_SP != null) {
            String key = PRFS_LAST_SHAKE_TIME + "_" + gameId + "_" + getDay();
            return _SP.getLong(key, 0);
        }
        return 0;
    }

    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());

    private static String getDay() {
        return dayFormat.format(new Date());
    }

    public static void shakeOnce(String gameId) {
        if (_SP != null) {
            String key = PRFS_SHAKE_TIMES + "_" + gameId + "_" + getDay();
            _SP.edit().putInt(key, todayShakeTimes(gameId) + 1).apply();

            String lastShakeTime = PRFS_LAST_SHAKE_TIME + "_" + gameId + "_" + getDay();
            _SP.edit().putLong(lastShakeTime, System.currentTimeMillis()).apply();
        }
    }

    public static int todayBubbleTimes(String gameId) {
        if (_SP != null) {
            String key = PRFS_BUBBLE_TIMES + "_" + gameId + "_" + getDay();
            return _SP.getInt(key, 0);
        }
        return 0;
    }

    public static void bubbleOnce(String gameId) {
        if (_SP != null) {
            String key = PRFS_BUBBLE_TIMES + "_" + gameId + "_" + getDay();
            _SP.edit().putInt(key, todayBubbleTimes(gameId) + 1).apply();
        }
    }

    public static int todayHbrainTimes(String gameId) {
        if (_SP != null) {
            String key = PRFS_HBRAIN_TIMES + "_" + gameId + "_" + getDay();
            return _SP.getInt(key, 0);
        }
        return 0;
    }

    public static void hbrainOnce(String gameId) {
        if (_SP != null) {
            String key = PRFS_HBRAIN_TIMES + "_" + gameId + "_" + getDay();
            _SP.edit().putInt(key, todayHbrainTimes(gameId) + 1).apply();

            String lastTimeKey = PRFS_HBRAIN_LAST_TIME + "_" + gameId + "_" + getDay();
            _SP.edit().putLong(lastTimeKey, System.currentTimeMillis()).apply();
        }
    }

    public static long hbrainLastTime(String gameId) {
        if (_SP != null) {
            String key = PRFS_HBRAIN_LAST_TIME + "_" + gameId + "_" + getDay();
            return _SP.getLong(key, 0);
        }
        return 0;
    }

    public static void saveString(String key, String value) {
        if (_SP != null) {
            _SP.edit().putString(key, value).apply();
        }
    }

    public static String getString(String key) {
        if (_SP != null) {
            return _SP.getString(key, "");
        }
        return "";
    }

    public static void saveInt(String key, int value) {
        if (_SP != null) {
            _SP.edit().putInt(key, value).apply();
        }
    }

    public static int getInt(String key) {
        if (_SP != null) {
            return _SP.getInt(key, 0);
        }
        return 0;
    }

    public static void saveBoolean(String key, boolean value) {
        if (_SP != null) {
            _SP.edit().putBoolean(key, value).apply();
        }
    }

    public static Boolean getBoolean(String key) {
        if (_SP != null) {
            return _SP.getBoolean(key, false);
        }
        return false;
    }

    public static void saveLong(String key, long value) {
        if (_SP != null) {
            _SP.edit().putLong(key, value).apply();
        }
    }

    public static long getLong(String key) {
        if (_SP != null) {
            return _SP.getLong(key, 0);
        }
        return 0;
    }

    public static void setPrivateShowStatus(Context context, boolean isShowed) {
        SharedPreferences.Editor e = _SP.edit();
        e.putBoolean(PRFS_SHOW_PRIVACY_CONTENT, isShowed);
        e.apply();
    }

    public static boolean getPrivateShowStatus() {
        if (_SP != null) {
            return _SP.getBoolean(PRFS_SHOW_PRIVACY_CONTENT, false);
        }
        return true;
    }

    public static void setPermissionShowStatus(Context context, boolean isShowed) {
        if (_SP != null) {
            SharedPreferences.Editor e = _SP.edit();
            e.putBoolean(PRFS_SHOW_PERMISSION_CONTENT, isShowed);
            e.apply();
        }
    }

    public static boolean getPermissionShowStatus() {
        if (_SP != null) {
            return _SP.getBoolean(PRFS_SHOW_PERMISSION_CONTENT, false);
        }
        return true;
    }


}
