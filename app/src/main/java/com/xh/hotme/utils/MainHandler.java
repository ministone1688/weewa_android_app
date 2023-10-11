package com.xh.hotme.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Keep;

/**
 * Create by zhaozhihui on 2018/10/13
 **/
@Keep
public class MainHandler extends Handler {
    private static volatile MainHandler instance;

    public static MainHandler getInstance() {
        if (null == instance) {
            synchronized (MainHandler.class) {
                if (null == instance) {
                    instance = new MainHandler();
                }
            }
        }
        return instance;
    }

    private MainHandler() {
        super(Looper.getMainLooper());
    }

    public static void runOnUIThread(Runnable r) {
        runOnUIThread(r, 0);
    }

    public static void runOnUIThread(Runnable r, int delayMillis) {
        MainHandler.getInstance().postDelayed(r, delayMillis);
    }

    public static void removeUITask(Runnable r) {
        MainHandler.getInstance().removeCallbacks(r);
    }
}