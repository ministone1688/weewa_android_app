package com.xh.hotme.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.xh.hotme.R;
import com.xh.hotme.widget.ModalDialog;


/**
 * author janecer 2014-7-23上午9:41:45
 */
@Keep
public class DialogUtil {

    private static Dialog dialog;// 显示对话框

    private static final Handler timeOutHandler = new Handler();

    private static final long TIME_OUT = 10000;

    private static final Runnable _timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (dialog != null && dialog.isShowing()) {
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                }
            } catch (Throwable e) {

            }
        }
    };

    /**
     * 显示对话框
     *
     * @param context
     * @param msg
     */
    public static Dialog showDialog(Context context, String msg) {
        try {
            if (context == null) {
                return null;
            }
            if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
                return null;
            }

            if (dialog != null && dialog.isShowing()) {
                if (dialog.getContext() == context) {
                    return null;
                }
                dialog.dismiss();
            }
            if (dialog == null || dialog.getContext() != context) {
                dialog = new Dialog(context, R.style.customDialog);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout_loading, null);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                TextView tv_msg = (TextView) view.findViewById(R.id.loading_msg);

                tv_msg.setText(msg);// 显示进度信息

                dialog.setContentView(view);
            }

            if (null != dialog && !dialog.isShowing()) {
                dialog.show();

                try {
                    if (timeOutHandler != null && _timeOutRunnable != null) {
                        timeOutHandler.postDelayed(_timeOutRunnable, TIME_OUT);
                    }
                }catch (Throwable e){

                }
            }
            return dialog;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Dialog showDialog(Context context, boolean cansable, String msg) {
        try {
            if (context == null) {
                return null;
            }
            if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
                return null;
            }
            if (dialog != null && dialog.isShowing()) {
                if (dialog.getContext() == context) {
                    return null;
                }
                dialog.dismiss();
                try {
                    if (timeOutHandler != null) {
                        timeOutHandler.removeCallbacksAndMessages(null);
                    }
                }catch (Throwable e){

                }
            }
            if (dialog == null || dialog.getContext() != context) {
                dialog = new Dialog(context, R.style.customDialog);
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout_loading, null);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                TextView tv_msg = (TextView) view.findViewById(R.id.loading_msg);

                tv_msg.setText(msg);// 显示进度信息

                dialog.setContentView(view);
            }

            if (null != dialog && !dialog.isShowing()) {
                dialog.setCancelable(cansable);
                dialog.show();

                try {
                    if (timeOutHandler != null && _timeOutRunnable != null) {
                        timeOutHandler.postDelayed(_timeOutRunnable, TIME_OUT);
                    }
                }catch (Throwable e){

                }
            }
            return dialog;
        } catch (Throwable e) {

        }
        return null;
    }

    /**
     * 隐藏对话框
     */
    public static void dismissDialog() {
        try {
            if (null != dialog && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
            if (timeOutHandler != null) {
                timeOutHandler.removeCallbacksAndMessages(null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加context 判断是否宿主Activity 是否被销毁
     *
     * @param context
     */
    public static void dismissDialog(Context context) {

        try {
            if (context == null) {
                return;
            }
            if (context instanceof Activity && (((Activity) context).isDestroyed() || ((Activity) context).isFinishing())) {
                return;
            }
            if (null != dialog && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
            if (timeOutHandler != null) {
                timeOutHandler.removeCallbacksAndMessages(null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 旋转动画
     *
     * @return
     */
    public static Animation rotaAnimation() {
        RotateAnimation ra = new RotateAnimation(0, 355,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setInterpolator(new LinearInterpolator());
        ra.setDuration(888);
        ra.setRepeatCount(-1);
        ra.setStartOffset(0);
        ra.setRepeatMode(Animation.RESTART);
        return ra;
    }

    /**
     * 判断对话框是否是显示状态
     *
     * @return
     */
    public static boolean isShowing() {
        if (null != dialog) {
            return dialog.isShowing();
        }
        return false;
    }


}
