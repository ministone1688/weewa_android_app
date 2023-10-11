package com.xh.hotme;

import android.app.Activity;
import android.app.Application;
import android.content.Context;


import androidx.multidex.MultiDex;

import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.obs.services.LogConfigurator;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.weewa.lib.Prefs;
import com.weewa.lib.WeewaLib;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.LoginSpUtil;

import java.io.File;


public class HotmeApplication extends Application {

    private static final String TAG = "HotmeApplication";

    public static boolean isTest = false;

    private static HotmeApplication sInstance;

    private static Context smContext;

    public static HotmeApplication getInstance() {
        return sInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MdidSdkHelper.InitSdk(HotmeApplication.this, true, new IIdentifierListener() {
                @Override
                public void OnSupport(boolean b, IdSupplier idSupplier) {
                    if (idSupplier != null && idSupplier.getOAID() != null) {
                        AppTrace.d(TAG, "oaid = " + idSupplier.getOAID());
                    } else {
                        AppTrace.d(TAG, "oaid init fail");
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        LoginManager.init(HotmeApplication.this);

        Constants.TEST_MODE = BaseAppUtil.getMetaBooleanValue(HotmeApplication.this, "TEST_MODE");

        MultiDex.install(this);

        UMConfigure.setLogEnabled(false);
        UMConfigure.preInit(this, Constants.UMENG_APP_ID
                , "umeng");

        //初始化组件化基础库, 所有友盟业务SDK都必须调用此初始化接口。
        UMConfigure.init(this,  Constants.UMENG_APP_ID, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        // 微信设置
        PlatformConfig.setWeixin(Constants.WEIXIN_APP_ID, Constants.WEIXIN_SECRECT_KEY);
        PlatformConfig.setWXFileProvider("com.xh.hotme.fileprovider");

        BluetoothManager.newInstance(this);

        // 设置日志的级别。默认为LogConfigurator.WARN
        LogConfigurator.setLogLevel(LogConfigurator.INFO);

    // 设置保留日志文件的个数。默认为10
        LogConfigurator.setLogFileRolloverCount(5);

    // 设置每个日志文件的大小，单位：字节。默认为不限制
        LogConfigurator.setLogFileSize(1024 * 1024 * 10);

        // 设置日志文件存放的目录。默认存放在SD卡的logs目录下
        LogConfigurator.setLogFileDir("/storage/sdcard");

        // 开启日志
        LogConfigurator.enableLog();

        // 关闭日志
//        LogConfigurator.disableLog();

        File videoFile = AppFileUtil.getVideoDir(HotmeApplication.this);
        Prefs.INSTANCE.setDownloadPath(Prefs.INSTANCE.defaultPreference(HotmeApplication.this), videoFile.getPath());
        WeewaLib.Companion.shared().setRemoteDirectory(ConnectLogic.getBasePath());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
    public static Context getContext() {
        if (smContext == null) {
            smContext = getInstance().getApplicationContext();
        }
        return smContext;
    }


}
