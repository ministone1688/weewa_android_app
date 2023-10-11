package com.xh.hotme.thrid;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Keep;
import androidx.fragment.app.FragmentActivity;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.xh.hotme.utils.AppTrace;

import java.util.Map;

/**
 * Create by zhaozhihui on 2019/4/3
 **/
@Keep
public class UMengManager {

    private static  final String TAG = UMengManager.class.getSimpleName();

    private static boolean _skipInit = false;

    //统计初始化
    public static void init(Context context) {
        if(_skipInit){
            return;
        }
        try {
            if(!UMConfigure.getInitStatus()){
                UMConfigure.init(context.getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, null);
            }
            UMConfigure.setProcessEvent(true);
        }catch (Exception e){

        }

        /*
         * 友盟默认是LEGACY_AUTO模式, 但是新版本的友盟默认是AUTO模式
         * 如果设置成AUTO模式, 影响了沙盒统计时长的逻辑, 不知道原因, 为了沙盒时长统计的问题, 设置为LEGACY_AUTO
         * 模式, 如果这个修改影响了统计页面啥的, 建议手动调用友盟的page start/end方法
         */
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO);

        // interval 单位为毫秒，如果想设定为40秒，interval应为 40*1000.
        //修改session时长为60秒，能避免大部分视频广告返回后产生新的seesion。
        MobclickAgent.setSessionContinueMillis(60000);
    }

    public static void sentEvent(Context context, String event, Map<String, String> value){
        MobclickAgent.onEvent(context, event, value);
        AppTrace.i(TAG, "report event: "+ event);
    }


    public static void onResume(FragmentActivity context){
        AppTrace.i(TAG, "UMENG onResume");
        MobclickAgent.onResume(context);
    }

    public static void onPause(FragmentActivity context){
        AppTrace.i(TAG, "UMENG onPause");
        MobclickAgent.onPause(context);
    }

    public static void onResume(Activity context){
        AppTrace.i(TAG, "UMENG onResume");
        MobclickAgent.onResume(context);
    }

    public static void onPause(Activity context){
        AppTrace.i(TAG, "UMENG onPause");
        MobclickAgent.onPause(context);
    }

    public static void onResume(Context context){
        AppTrace.i(TAG, "UMENG onResume");
        MobclickAgent.onResume(context);
    }

    public static void onPause(Context context){
        AppTrace.i(TAG, "UMENG onPause");
        MobclickAgent.onPause(context);
    }

    public static void skipInit( boolean skip){
        _skipInit = skip;
    }
}
