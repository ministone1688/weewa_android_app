package com.xh.hotme.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.core.app.NotificationManagerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Create by zhaozhihui on 2019/2/26
 **/
@Keep
public class ToastUtil {

    private static Toast toast;

    public ToastUtil() {
    }


    public static void s(Context context, CharSequence message) {
        if (context == null || TextUtils.isEmpty(message)) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }

        // 如果错误信息是java的异常这种, 不直接显示, 而是显示一些用户能理解的文字
        if (message instanceof String) {
            String msg = (String) message;
            if (msg.indexOf("Exception") != -1) {
                message = "网络不给力, 请稍后再试";
            }
        }

        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
            toast.setGravity(17, 0, 0);
        }
        toast.setText(message);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    public static void s(Context context, int resMsg) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext().getApplicationContext(), "", Toast.LENGTH_SHORT);
            toast.setGravity(17, 0, 0);
        }

        toast.setText(resMsg);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    public static void showShort(Context context, int message) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), null, Toast.LENGTH_SHORT);
            toast.setGravity(17, 0, 0);
        }
        toast.setText(message);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    public static void showLong(Context context, CharSequence message) {
        if (context == null || TextUtils.isEmpty(message)) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), null, Toast.LENGTH_LONG);
            toast.setGravity(17, 0, 0);
        }
        toast.setText(message);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    public static void showLong(Context context, int message) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), null, Toast.LENGTH_LONG);
            toast.setGravity(17, 0, 0);
        }
        toast.setText(message);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }

    }

    public static void show(Context context, CharSequence message, int duration) {
        if (context == null || TextUtils.isEmpty(message)) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), null, duration);
            toast.setGravity(17, 0, 0);
        }
        toast.setText(message);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    public static void show(Context context, int message, int duration) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
            return;
        }
        if (null == toast) {
            toast = Toast.makeText(context.getApplicationContext(), message, duration);
        }
        toast.setText(message);

        if (isNotificationEnabled(context)) {
            toast.show();
        } else {
            showSystemToast(toast);
        }
    }

    public static void hideToast() {
        if (null != toast) {
            toast.cancel();
        }

    }

    public static void ImageToast(Context context, int ImageResourceId, CharSequence text, int duration) {
        if (context == null || TextUtils.isEmpty(text)) {
            return;
        }

        toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.setGravity(17, 0, 0);
        View toastView = toast.getView();
        ImageView img = new ImageView(context);
        img.setImageResource(ImageResourceId);
        LinearLayout ll = new LinearLayout(context);
        ll.addView(img);
        ll.addView(toastView);
        toast.setView(ll);
        toast.show();
    }

    private static Object iNotificationManagerObj;

    /**
     * 显示系统Toast
     */
    private static void showSystemToast(Toast toast) {
        try {
            Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
            getServiceMethod.setAccessible(true);
            //hook INotificationManager
            if (iNotificationManagerObj == null) {
                iNotificationManagerObj = getServiceMethod.invoke(null);

                Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
                Object iNotificationManagerProxy = Proxy.newProxyInstance(toast.getClass().getClassLoader(), new Class[]{iNotificationManagerCls}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            //强制使用系统Toast
                            if ("enqueueToast".equals(method.getName())
                                    || "enqueueToastEx".equals(method.getName())) {  //华为p20 pro上为enqueueToastEx
                                args[0] = "android";
                            }
                            return method.invoke(iNotificationManagerObj, args);
                        } catch (Throwable e) {

                        }
                        return null;
                    }
                });
                Field sServiceFiled = Toast.class.getDeclaredField("sService");
                sServiceFiled.setAccessible(true);
                sServiceFiled.set(null, iNotificationManagerProxy);
            }
            toast.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息通知是否开启
     *
     * @return
     */
    private static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
        return areNotificationsEnabled;
    }
}
