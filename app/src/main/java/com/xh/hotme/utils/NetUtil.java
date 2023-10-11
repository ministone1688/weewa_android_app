package com.xh.hotme.utils;

import static android.content.Context.WIFI_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Keep
public class NetUtil {
    /**
     * get network type, following wx api type string definition
     */
    public static String getNetworkType(Context context) {
        if (context == null) {
            return "none";
        }
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony == null) {
            return "none";
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return "none";
        }
        try {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo == null || !netInfo.isAvailable()) {
                return "none";
            }
            int netType = netInfo.getType();
            if (netType == ConnectivityManager.TYPE_WIFI) {
                return "wifi";
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "unknown";
                }
                int networkType = telephony.getNetworkType();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return "2g";
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return "3g";
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return "4g";
                    case TelephonyManager.NETWORK_TYPE_NR:
                        return "5g";
                    default:
                        return "unknown";
                }
            } else {
                return "none";
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return "unknown";
    }

    /**
     * check if any network connected
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (null != netInfo)
            return netInfo.isConnected();
        else {
            return false;
        }
    }


    public static String getWifiMac(Context aContext) {
        WifiManager wifiMan = (WifiManager) aContext.getSystemService(WIFI_SERVICE);
        if (null == wifiMan) return "";
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        if (null == wifiInf) return "";
        String macAddr = wifiInf.getMacAddress();
        // 02:00:00:00:00:00
        if (TextUtils.isEmpty(macAddr) || macAddr.contains("00:00:00")) {
            macAddr = getLocalMac();

        }
        return macAddr;
    }

    private static String getLocalMac() {
        String mac = "02:00:00:00:00:00";// 默认值
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    mac = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return mac;

    }

    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     *
     * @param context
     * @return
     */
    private static String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     *
     * @return
     */
    private static String getMacFromFile() {

        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }


    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }


    public static String getAccessPoint(Context context) {
        String apn = null;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        if (info != null) {
            apn = info.getTypeName();
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    if (apn == null) {
                        apn = "wifi";
                    }
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    if (info.getExtraInfo() != null) {
                        apn = info.getExtraInfo().toLowerCase();
                    }
                    if (apn == null) {
                        apn = "mobile";
                    }
                    break;
                default:
                    if (info.getExtraInfo() != null) {
                        apn = info.getExtraInfo().toLowerCase();
                    }
                    break;
            }
        }
        if (apn == null) {
            apn = "unknown";
        }
        // Date _time = new Date(System.currentTimeMillis());
        // FileUtil.createFileForTest(
        // "/sdcard/proxy.xml",
        // ("adPhoneNumber:: " + adPhoneNumber + " //"
        // + _time.getDay() + ":" + _time.getHours()
        // + ":" + _time.getMinutes() + "::"
        // + _time.getSeconds() + "\r\n").getBytes());
        return apn;
    }

    public static Proxy getApnProxy(Context context) {
        Proxy proxyHere = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);// cm.getActiveNetworkInfo()
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != ni && ni.isConnected()) {
            return proxyHere;
        } else {

            String host = android.net.Proxy.getDefaultHost();
            int port = android.net.Proxy.getDefaultPort();

            // Date _time = new Date(System.currentTimeMillis());
            // FileUtil.createFileForTest(
            // "/sdcard/proxy.xml",
            // ("proxy:: " + host + " "+":"+port+"  //"
            // + _time.getDay() + ":" + _time.getHours()
            // + ":" + _time.getMinutes() + "::"
            // + _time.getSeconds() + "\r\n").getBytes());
            if ((host != null) && (port != -1)) {
                SocketAddress addr = new InetSocketAddress(host, port);
                proxyHere = new Proxy(Proxy.Type.HTTP, addr);
            }
            return proxyHere;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取当前的运营商
     *
     * @param context
     * @return 运营商名字
     */
    public static String getOperator(Context context) {

        String ProvidersName = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                String IMSI = telephonyManager.getSubscriberId();
                AppTrace.d("qweqwes", "运营商代码" + IMSI);
                if (IMSI != null) {
                    if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46004") || IMSI.startsWith("46007")) {
                        ProvidersName = "中国移动";
                    } else if (IMSI.startsWith("46001") || IMSI.startsWith("46006") || IMSI.startsWith("46009")) {
                        ProvidersName = "中国联通";
                    } else if (IMSI.startsWith("46003") || IMSI.startsWith("46005") || IMSI.startsWith("46011")) {
                        ProvidersName = "中国电信";
                    } else if (IMSI.startsWith("46020")) {
                        ProvidersName = "中国铁通";
                    }
                    return ProvidersName;
                } else {
                    return "没有获取到sim卡信息";
                }
            }
        } catch (Throwable e) {

        }
        return "没有获取到sim卡信息";
    }

    /**
     * 获取运营商名字
     *
     * @param context context
     * @return int
     */
    public static String getOperatorName(Context context) {
        /*
         * getSimOperatorName()就可以直接获取到运营商的名字
         * 也可以使用IMSI获取，getSimOperator()，然后根据返回值判断，例如"46000"为移动
         * IMSI相关链接：http://baike.baidu.com/item/imsi
         */
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getSimOperatorName();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // getSimOperatorName就可以直接获取到运营商的名字
        return "";
    }

    /**
     * 获取当前的运营商代码
     *
     * @param context
     * @return 运营商名字
     */
    public static String getNop(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return telephonyManager.getSimOperator();
            }

            return "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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

    /**
     * 获取本地Ip地址
     *
     * @param context
     * @return
     */
    public static String getLocalIpAddress(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            int ipAddress = info.getIpAddress();
            String Ipv4Address = InetAddress
                    .getByName(
                            String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                                    (ipAddress >> 8 & 0xff),
                                    (ipAddress >> 16 & 0xff),
                                    (ipAddress >> 24 & 0xff))).getHostAddress();
            return Ipv4Address;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取当前连接WIFI的SSID
     */
    public String getSSID(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
            if (wm != null) {
                WifiInfo winfo = wm.getConnectionInfo();
                if (winfo != null) {
                    String s = winfo.getSSID();
                    if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                        return s.substring(1, s.length() - 1);
                    }
                }
            }
            return "";
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return "";

    }

    /**
     * 判断是否有网络
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {   //判断网络连接是否打开
            return mNetworkInfo.isConnected();
        }
        return false;
    }

    /* @author suncat
     * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * @return
     */
    public static final boolean ping() {

        String result = null;
        try {
            String ip = "miniapi.mgc-games.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            AppTrace.d("------ping-----", "result content : " + stringBuffer);
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            AppTrace.d("----result---", "result = " + result);
        }
        return false;
    }
}
