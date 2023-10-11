package com.xh.hotme.utils;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import androidx.annotation.Keep;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;



import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * Create by zhaozhihui on 2018/10/12
 **/
@Keep
public class DeviceInfo {
    private static final String TAG ="DeviceInfo";

    /**
     * phnoe model
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * phone brand
     */
    public static String getPhoneBrand() {
        if (Build.BOARD == null) {
            return "unknown";
        }
        return Build.BRAND;
    }

    public static String getVersionCode(Context context) {
        return getVersionCode(context.getPackageName(), context) + "";
    }

    /**
     * 当前应用versionCode
     */
    public static int getVersionCode(String packageName, Context context) {
        int versionCode = 0;
        PackageManager mPackageManger = context.getPackageManager();
        try {
            versionCode = mPackageManger.getPackageInfo(packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            versionCode = 0;
        }

        return versionCode;
    }

    /**
     * 获取手机序列号
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class
                    .forName("android.android.android.android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            serial = null;
            // e.printStackTrace();
        }
        return serial;
    }

    // like 21
    public static int SdkLevel() {
        return Build.VERSION.SDK_INT;
    }

    // like 5.1.1
    public static String SdkRelease() {
        return Build.VERSION.RELEASE;
    }

    public static String getMEID(Context context) {
        String meid = "";

        try {
            TelephonyManager mTelePhonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelePhonyManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            meid = mTelePhonyManager.getMeid();
                        } catch (Exception e) {
                        }
                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return meid;
    }


    public static String getIMEI(Context context, String def) {
        String imei = "";
        try {
            //如果不能获取imei，则直接返回

            TelephonyManager mTelePhonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelePhonyManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    imei = mTelePhonyManager.getDeviceId();
                }
            }


            if (TextUtils.isEmpty(imei) || imei.equals("000000000000000")
                    || imei.length() < 10) {
                imei = def;
            }

        } catch (Exception e) {
            if (TextUtils.isEmpty(imei)) {
                imei = def;
            }
        }
        return imei;
    }

    /**
     * 获取手机IMEI
     */
    public static String getIMEI(Context context) {
        String imei = "";

        try {
            //如果不能获取imei，则直接返回

            TelephonyManager mTelePhonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelePhonyManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    imei = mTelePhonyManager.getDeviceId();
                }
            }

            if (TextUtils.isEmpty(imei) || imei.equals("000000000000000")
                    || imei.length() < 10) {
                imei = getIMEISubstitute(context);
            }

        } catch (Exception e) {
            if (TextUtils.isEmpty(imei)) {
                imei = "000000000000000";
            }
        }
        return imei;
    }

    public static String getIMEISubstitute(Context context) {

        String imeiSub = getAndroidID(context);
        if (imeiSub.isEmpty()) {
            imeiSub = getSerialNumber() + getBuildID();
        }
        if (imeiSub.isEmpty()) {
            imeiSub = "000000000000000";
        }
        return imeiSub;
    }

    public static String getAndroidID(Context context) {
        AppTrace.d(TAG, "getAndroidID");
        String androidId= Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return androidId;
    }

    public static String getBuildID() {
        String m_szDevIDShort = "TD"
                + // we make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10;
        return m_szDevIDShort;
    }

    public static String getDeviceMark(Context context) {
        String tmDevice, tmSerial, androidId;
        tmDevice = "";
        // XXX: 没有获得权限的时候, 可能得不到getSimSerialNumber, 导致这个方法可能在不同条件下返回不一样的字符串
        // 暂时注释掉这个字段
//        tmSerial = DeviceInfo.getSimSerialNumber(context);
//        androidId = DeviceUtil.getAndroidId(context);

        String serial = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            serial = Build.SERIAL;
        }
        String m_szLongID = tmDevice + /*tmSerial +*/  serial
                + getBuildID();


        AppTrace.d("Leto", "device_id: " + tmDevice + "  ===SimSerialNumber: "
                + /*tmSerial +*/" ===androidId: " + " ===serial: " + serial + " ===build: " + getBuildID());

        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        // get md5 bytes
        byte[] p_md5Data = m.digest();
        // create a hex string
        String m_szUniqueID = "";
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
            // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }

        // hex string to uppercase
        return m_szUniqueID.toUpperCase();
    }


    // 得到手机的IMSI号，需要context参数
    public static String getIMSI(Context context) {
        String imsi = null;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager mTelephonyMgr = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelephonyMgr != null) {
                    imsi = mTelephonyMgr.getSubscriberId();
                }
            }
            if (TextUtils.isEmpty(imsi)) {
                imsi = "";
            }

        } catch (Exception e) {
            imsi = "";
        }

        return imsi;
    }

    public static String getIMSI(Context context, String def) {
        String imsi = "";
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager mTelephonyMgr = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelephonyMgr != null) {
                    imsi = mTelephonyMgr.getSubscriberId();
                }
            }
            if (TextUtils.isEmpty(imsi)) {
                imsi = def;
            }

        } catch (Exception e) {
            imsi = def;
        }

        return imsi;
    }

    /**
     * get devicesid
     */

    public static String getDeviceId() {
        String serial = null;
        try {
            Class<?> c = Class
                    .forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }


    /**
     * 系统语言
     *
     * @return
     */
    public static String getMobileLanguage(Context context) {
        String language = context.getResources().getConfiguration().locale
                .getLanguage();
        return language;
    }

    //默认是ISO-2
    public static String getCountry(Context context) {
        String able = context.getResources().getConfiguration().locale
                .getCountry();
        return able;
    }

    public static String getCountryISO3(Context context) {
        String able = context.getResources().getConfiguration().locale
                .getISO3Country();
        return able;
    }

    public static String getSimCountryIso(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telManager.getSimCountryIso();
    }

    /**
     * get phone 分辨率
     */

    public static String getPhoneDensity(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density + "";
    }

    public static String getPhoneDensityDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.densityDpi + "";
    }

    public static String getPhoneWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        return width + "";
    }

    public static String getPhoneHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        return "" + height;
    }

    public static int getWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        return width;
    }

    public static int getHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        return height;
    }

    // 获取屏幕分辨率

    public static String getPhoneResolution(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return width + "x" + height;
    }

    public static String getSimSerialNumber(Context context) {
        TelephonyManager mTelePhonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String sn = null;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                sn = mTelePhonyManager.getSimSerialNumber();
            } else {
                sn = "";
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sn == null)
            sn = "";
        return sn;
    }

    public static int getScreenChange(Context context) {

        Configuration mConfiguration = context.getResources()
                .getConfiguration(); // 获取设置的配置信息
        int ori = mConfiguration.orientation; // 获取屏幕方向

        if (ori == Configuration.ORIENTATION_LANDSCAPE) {

            // 横屏
            return 1;
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {

            // 竖屏
            return 0;
        }
        return 0;
    }

    public static String getAndroidIDMethod(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    public static String getCarrier() {
        return Build.MANUFACTURER;
    }

    public static boolean isSystem(Context context) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context, context.getPackageName());
    }

    /**
     * whether packageName is system application
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isSystemApplication(Context context,
                                              String packageName) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context.getPackageManager(), packageName);
    }

    /**
     * whether packageName is system application
     *
     * @param packageManager
     * @param packageName
     * @return <ul>
     * <li>if packageManager is null, return false</li>
     * <li>if package name is null or is empty, return false</li>
     * <li>if package name not exit, return false</li>
     * <li>if package name exit, but not system app, return false</li>
     * <li>else return true</li>
     * </ul>
     */
    public static boolean isSystemApplication(PackageManager packageManager,
                                              String packageName) {
        if (packageManager == null || packageName == null
                || packageName.length() == 0) {
            return false;
        }

        try {
            ApplicationInfo app = packageManager.getApplicationInfo(
                    packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (NameNotFoundException e) {
        }
        return false;
    }

    public static String getBaseIMEI(Context context) {

        String imei = "";
        try {
            TelephonyManager mTelePhonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelePhonyManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    imei = mTelePhonyManager.getDeviceId();
                }
            }
        } catch (Exception e) {

        }

        return imei;
    }


    public static String getUserAgent(Context context) {
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    private static String parsePacketName(String buff) {
        int begin = buff.indexOf('{') + 1;
        int end = buff.indexOf('}');
        if (begin > end)
            return null;
        String temp = buff.substring(begin + 1, end);
        String[] words = temp.split(" ");
        if (words.length != 4)
            return null;
        return ComponentName.unflattenFromString(words[2]).getPackageName();
    }


    public static String getIPAddress(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                    try {
//                    Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en != null && en.hasMoreElements(); ) {
                            NetworkInterface intf = en.nextElement();
                            if (intf != null) {
                                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr != null && enumIpAddr.hasMoreElements(); ) {
                                    InetAddress inetAddress = enumIpAddr.nextElement();
                                    if (inetAddress != null && !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                        return inetAddress.getHostAddress();
                                    }
                                }
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }


                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                    return ipAddress;
                }
            } else {
                //当前无网络连接,请在设置中打开网络
            }
        }catch (Throwable e){

        }
        return "0.0.0.0";
    }


    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
