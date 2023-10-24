package com.xh.hotme.lay.utils;

import static com.umeng.commonsdk.stateless.UMSLEnvelopeBuild.mContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.xh.hotme.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/3/20 0020.
 */

public class MyToolUtils {
    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    //图片加载glide
    public static void glideShowImg(Context content, String imgpath, ImageView imgview){
        RequestOptions options = new RequestOptions();
        options.skipMemoryCache(true);
        options.fitCenter();
        options.error(R.mipmap.nopic);
        options.diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(content).load(imgpath).apply(options).into(imgview);
    }

    //通用跳转效果
    public static void goActivity(Activity activity, Intent intent){
        activity.startActivity(intent);
        //activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    //获取本地json
    public static String getJson(String fileName, Context context){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line=bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    //数组转字符串
    public static String arrayToString(List<String> str){
        String html = str.toString().replace("[","").replace("]","").replace(" ","");
        return html;
    }

    //数组转字符串
    public static String arrayToStringTw(List<String> str,String st){
        String html = str.toString().replace("[","").replace("]","").replace(" ","").replace(",",st);
        return html;
    }

    /**
     * 判断qq是否可用
     *
     * @param context
     * @return
     */
    public static boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断2个时间大小
     * yyyy-MM-dd HH:mm 格式（自己可以修改成想要的时间格式）
     * @param startTime
     * @param endTime
     * @return
     */
    public static int timeCompare(String startTime, String endTime){
        int i=0; //注意：传过来的时间格式必须要和这里填入的时间格式相同
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try {
            Date date1 = dateFormat.parse(startTime);//开始时间
            Date date2 = dateFormat.parse(endTime);//结束时间
            // 1 结束时间小于开始时间 2 开始时间与结束时间相同 3 结束时间大于开始时间
            if (date2.getTime()<date1.getTime()){
                //结束时间小于开始时间
                i= 1;
            }else if (date2.getTime()==date1.getTime()){
                //开始时间与结束时间相同
                i= 2;
            }else if (date2.getTime()>date1.getTime()){
                //结束时间大于开始时间
                i= 3;
            }
        } catch (Exception e) {

        }
        return  i;
    }

    /**
            * 获取版本号
     *
             * @return
             */
    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            //versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 隐藏键盘的方法
     *
     * @param context
     */
    public static void hideKeyboard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 隐藏软键盘
        imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);
    }

    //自动聚焦
    public static void showSoftInputFromWindow(Activity context, EditText editText){
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void myToast(Context context, String str){
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setPadding(10,10,10,10);
        v.setTextColor(Color.WHITE);     //设置字体颜色
        toast.show();
    }
    public static void myToast(Context context, String str, int showTime){
        Toast toast = Toast.makeText(context, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setPadding(30,50,30,50);
        v.setTextColor(Color.WHITE);     //设置字体颜色
        toast.show();
    }

}
