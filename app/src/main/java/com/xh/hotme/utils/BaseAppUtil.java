package com.xh.hotme.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;

import android.text.ClipboardManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.WindowManager;


import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static android.content.Context.ACTIVITY_SERVICE;

import com.xh.hotme.provider.HotmeFileProvider;

/**
 * 当前程序是否后台运行
 * 当前手机是否处于睡眠
 * 当前网络是否已连接
 * 当前网络是否wifi状态
 * 安装apk
 * 初始化view的高度
 * 初始化view的高度后不可见
 * 判断是否为手机
 * 获取屏幕宽度
 * 获取屏幕高度
 * 获取设备的IMEI
 * 获取设备的mac地址
 * 获取当前应用的版本号
 * 收集设备信息并以Properties返回
 * 收集设备信息并以String返回
 */
@Keep
public class BaseAppUtil {
    private static final String TAG = BaseAppUtil.class.getSimpleName();

    public static int getDeviceWidth(Context context) {
        if (context == null) {
            return 1080;
        }
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getWidth();
    }

    public static int getDeviceHeight(Context context) {
        if (context == null) {
            return 1920;
        }
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }

    public static String getAppVersionName(Context context) {
        String versionName = "1.0";
        try {
            if (context == null) {
                return versionName;
            }
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException var2) {
            var2.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static int getAppVersionCode(Context context) {
        int version = 1;

        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException var2) {
            var2.printStackTrace();
        }

        return version;
    }

    public static Properties collectDeviceInfo(Context context) {
        Properties mDeviceCrashInfo = new Properties();
        if (context == null) {
            return mDeviceCrashInfo;
        }

        try {
            PackageManager pm = context.getPackageManager();
            @SuppressLint("WrongConstant") PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
            if (pi != null) {
                mDeviceCrashInfo.put("versionName", pi.versionName == null ? "not set" : pi.versionName);
                mDeviceCrashInfo.put("versionCode", pi.versionCode);
            }
        } catch (PackageManager.NameNotFoundException var9) {
        }

        Field[] fields = Build.class.getDeclaredFields();
        Field[] var11 = fields;
        int var4 = fields.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Field field = var11[var5];

            try {
                field.setAccessible(true);
                mDeviceCrashInfo.put(field.getName(), field.get(null));
            } catch (Exception var8) {
            }
        }

        return mDeviceCrashInfo;
    }

    public static String collectDeviceInfoStr(Context context) {
        Properties prop = collectDeviceInfo(context);
        Set deviceInfos = prop.keySet();
        StringBuilder deviceInfoStr = new StringBuilder("{\n");
        Iterator iter = deviceInfos.iterator();

        while (iter.hasNext()) {
            Object item = iter.next();
            deviceInfoStr.append("\t\t\t" + item + ":" + prop.get(item) + ", \n");
        }

        deviceInfoStr.append("}");
        return deviceInfoStr.toString();
    }

    /**
     * 网络是否是连接状态
     *
     * @return true表示可能，false网络不可用
     */
    public static boolean isNetWorkConneted(Context ctx) {
        try {
            ConnectivityManager cmr = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cmr != null) {
                NetworkInfo networkinfo = cmr.getActiveNetworkInfo();

                if (networkinfo != null && networkinfo.isConnectedOrConnecting()) {
                    return networkinfo.isAvailable();
                }
            }
        } catch (Exception e) {
            //Toast.makeText(ctx, "网络连接错误，请检查当前网络状态！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
        //return null != networkinfo && networkinfo.isConnectedOrConnecting();
    }

    public static void copyToSystem(Context context, String content) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(content);
    }

    /**
     * 获取一个默认的
     *
     * @param context
     * @return
     */
    public static File getDefaultSaveRootPath(Context context, String name) {
        File sdFile = null;
        try {
            if (context == null) {
                return sdFile;
            }
            sdFile = context.getExternalCacheDir();
            if (null == sdFile || !sdFile.exists() || !sdFile.isDirectory()) {
                sdFile = context.getCacheDir();
            }
            sdFile = new File(sdFile, name);
            if (null != sdFile && !sdFile.exists()) {
                sdFile.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sdFile = new File(context.getCacheDir(), name);
                if (!sdFile.exists()) {
                    sdFile.mkdirs();
                }
            } catch (Throwable throwable) {

            }
        }
        return sdFile;
    }


    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    //检测service是否在运行
    public static boolean isServiceWorked(Context context, String serviceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    //检测activity所在应用是否在栈顶
    public static boolean isForeground(Context context, String PackageName) {
        try {
            ActivityManager myManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> task = myManager.getRunningTasks(1);
            if (task == null || task.size() == 0) {
                return false;
            }
            ComponentName componentInfo = task.get(0).topActivity;
            if (componentInfo.getPackageName().equals(PackageName))
                return true;
        } catch (Throwable e) {

        }
        return false;
    }


    /**
     * 返回app运行状态
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return int 1:前台 2:后台 0:不存在
     */
    public static int isAppAlive(Context context, String packageName) {
        try {
            ActivityManager myManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfos = myManager.getRunningTasks(20);
            if (taskInfos == null || taskInfos.size() == 0) {
                return 0;
            }

            ComponentName componentInfo = taskInfos.get(0).topActivity;
            if (componentInfo.getPackageName().equals(packageName)) {
                return 1;
            } else {
                // 判断程序是否在栈里
                for (ActivityManager.RunningTaskInfo info : taskInfos) {
                    if (info.topActivity.getPackageName().equals(packageName)) {
                        return 2;
                    }
                }
                return 0;// 栈里找不到，返回3
            }
        } catch (Throwable e) {

        }
        return 0;
    }

    //检测activity是否在栈顶
    public static boolean isForegroundActivity(Activity context, String className) {
        try {
            ActivityManager myManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> task = myManager.getRunningTasks(1);
            if (task == null || task.size() == 0) {
                return false;
            }
            ComponentName componentInfo = task.get(0).topActivity;
            String componentInfoClassName = componentInfo.getClassName();
            if (componentInfoClassName.equals(className))
                return true;
        } catch (Throwable e) {

        }
        return false;
    }

    /**
     * 判断某个app进程是否在运行
     *
     * @param context
     * @param appInfo
     * @return
     */
    public static boolean isRunningProcess(Context context, String appInfo) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppPs = myManager.getRunningAppProcesses();
        if (runningAppPs != null && runningAppPs.size() > 0) {
            return runningAppPs.contains(appInfo);
        }
        return false;
    }

    /**
     * 判断一个Activity是否正在运行
     *
     * @param pkg     pkg为应用包名
     * @param cls     cls为类名eg
     * @param context
     * @return
     */
    public static boolean isClsRunning(Context context, String pkg, String cls) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        ActivityManager.RunningTaskInfo task = tasks.get(0);
        if (task != null) {
            if (TextUtils.equals(task.baseActivity.getPackageName(), pkg) &&
                    TextUtils.equals(task.baseActivity.getClassName(), cls)) {
                return true;
            }
            return TextUtils.equals(task.topActivity.getPackageName(), pkg) &&
                    TextUtils.equals(task.topActivity.getClassName(), cls);
        }
        return false;

    }

    public boolean killProcess(Context context, String processName) {
        ActivityManager am = ((ActivityManager) context.getSystemService(ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();

        String mainProcessName = context.getPackageName() + ":" + processName;
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (mainProcessName.equals(info.processName)) {
                android.os.Process.killProcess(info.pid);
                return true;
            }
        }

        return false;
    }

    public boolean isProcessRunning(Context context, String processName) {
        ActivityManager am = ((ActivityManager) context.getSystemService(ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();

        String mainProcessName = context.getPackageName() + ":" + processName;
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (mainProcessName.equals(info.processName)) {
                return true;
            }
        }

        return false;
    }

    public static String getMetaStringValue(Context context, String key) {
        String value = "";
        Object obj;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != appInfo.metaData) {
                try {
                    obj = appInfo.metaData.get(key);
                } catch (Throwable e) {
                    obj = null;
                }

                // if value is pure number, getString won't return string! so check it
                // and re-get int
                // 另外, 长数字字符串获取后会变成科学计数法, 所以在长数字字符串前面可以追加一个前缀
                // 返回时将自动去掉这个前缀
                if (null != obj) {
                    value = String.valueOf(obj);
                    if (value.startsWith("__MGC_META_PREFIX_")) {
                        value = value.substring("__MGC_META_PREFIX_".length());
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return value;
    }

    public static int getMetaIntValue(Context context, String key) {
        int value = 0;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Object obj = null;
            if (null != appInfo.metaData) {
                try {
                    obj = appInfo.metaData.get(key);
                } catch (Throwable e) {
                }
            }

            if (null == obj) {
                return value;
            }
            if (obj instanceof Integer) {
                value = ((Integer) obj).intValue();
            } else if (obj instanceof String) {
                if (obj.toString().length() != 0) {
                    value = Integer.parseInt((String) obj);
                }
            }


        } catch (Throwable e) {
            e.printStackTrace();
        }

        return value;
    }

    public static boolean getMetaBooleanValue(Context context, String key) {
        boolean value = false;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Object obj = null;
            if (null != appInfo.metaData) {
                try {
                    obj = appInfo.metaData.get(key);
                } catch (Throwable e) {
                }
            }

            if (obj instanceof Integer) {
                value = ((Integer) obj).intValue() != 0;
            } else if (obj instanceof Boolean) {
                value = (Boolean) obj;
            } else if (obj instanceof String) {
                value = Boolean.parseBoolean((String) obj);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return value;
    }

    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    public static boolean isInstallApp(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            return context.getPackageManager().getLaunchIntentForPackage(packageName) != null;
        } catch (Throwable e) {
        }

        return false;
    }

    private static Intent getIntentByPackageName(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    public static boolean openAppByPackageName(Context context, String packageName) {
        Intent intent = getIntentByPackageName(context, packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean isHasInstallPermissionWithO(Context context) {
        if (context == null) {
            return false;
        }

        return context.getPackageManager().canRequestPackageInstalls();
    }


    public static void startInstallPermissionSettingActivity(Activity context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent();
        //获取当前apk包URI，并设置到intent中（这一步设置，可让“未知应用权限设置界面”只显示当前应用的设置项）
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        intent.setData(packageURI);
        //设置不同版本跳转未知应用的动作
        if (Build.VERSION.SDK_INT >= 26) {
            //intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
            intent.setAction(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        } else {
            intent.setAction(android.provider.Settings.ACTION_SECURITY_SETTINGS);
        }
        context.startActivity(intent);
    }

    public static void startInstallPermissionSettingActivity(Activity context, int requestCode) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent();
        //获取当前apk包URI，并设置到intent中（这一步设置，可让“未知应用权限设置界面”只显示当前应用的设置项）
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        intent.setData(packageURI);
        //设置不同版本跳转未知应用的动作
        if (Build.VERSION.SDK_INT >= 26) {
            intent.setAction(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        } else {
            intent.setAction(android.provider.Settings.ACTION_SECURITY_SETTINGS);
        }
        context.startActivityForResult(intent, requestCode);
    }


    /**
     * 安装apk by文件
     *
     * @param context
     * @param file
     */
    public static void installApk(Context context, File file) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            //判断是否是AndroidN以及更高的版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = HotmeFileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".leto.fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                grantUriPermission(context, contentUri, intent);
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                context.startActivity(intent);
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void grantUriPermission(Context context, Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }

    //判断当前应用是否是debug状态
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否有deeplink
     *
     * @param context
     * @return
     */
    public static boolean hasDeepLink(final Context context, final Intent intent) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * @description: 启动一个app
     */
    public static void openAppWithUrl(Context mContext, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取系统内存，单位KB
     *
     * @param context
     * @return
     */
    public static long getTotalMemory(Context context) {
        //获得ActivityManager服务的对象
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        //获得MemoryInfo对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo);
        long memSize = memoryInfo.totalMem;
        return memSize;
    }

    public static String getTotalMemoryPrettyString(Context context) {
        return Formatter.formatFileSize(context, getTotalMemory(context));// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 根据包名获取App的名字
     *
     * @param pkgName 包名
     */
    public static String getAppName(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkgName, 0);
            return info.loadLabel(pm).toString();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据包名获取App的Icon
     *
     * @param pkgName 包名
     */
    public static Drawable getAppIcon(Context context, String pkgName) {
        try {
            if (null != pkgName) {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo info = pm.getApplicationInfo(pkgName, 0);
                return info.loadIcon(pm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //判断Activity是否Destroy
    public static boolean isDestroy(Activity activity) {
        return activity == null || activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed());
    }

    public static boolean supportSystem() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AppTrace.w(TAG, "The SDK need android miniSdkversion>=23.");
            return false;
        }
        return true;

    }


    public static boolean isServiceExisted(Context var0, String var1) {
        ActivityManager var2 = (ActivityManager) var0.getSystemService(ACTIVITY_SERVICE);
        List var3 = var2.getRunningServices(2147483647);
        if (var3.size() <= 0) {
            return false;
        } else {
            for (int var4 = 0; var4 < var3.size(); ++var4) {
                ActivityManager.RunningServiceInfo var5 = (ActivityManager.RunningServiceInfo) var3.get(var4);
                ComponentName var6 = var5.service;
                if (var6.getClassName().equals(var1)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static int getScreenOrientation(Context context) {
        Configuration newConfig = context.getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            return Configuration.ORIENTATION_LANDSCAPE;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            return Configuration.ORIENTATION_PORTRAIT;
        }
        return Configuration.ORIENTATION_LANDSCAPE;
    }
}
