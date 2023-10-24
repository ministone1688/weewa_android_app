package com.xh.hotme.lay.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xh.hotme.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by meixi on 2020/11/12.
 */

public class ToastUtil {

    private static Toast toast;

    public static void showToastCenter(Context ctx, String str) {
        Toast toast = Toast.makeText(ctx, str, Toast.LENGTH_SHORT);
        toast.setGravity(0, Gravity.CENTER, Gravity.CENTER);
        toast.show();
    }

    public static void showToastLong(Context ctx, String str) {
        Toast.makeText(ctx, str, Toast.LENGTH_LONG).show();
    }

    public static void showToastShort(Context ctx, String str) {
        Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Activity activity, String toastContent, int image, int time) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_stat_layout, null);
        TextView content = layout.findViewById(R.id.tv_content);
        content.setText(toastContent);
        ImageView imageView = layout.findViewById(R.id.iamge);
        imageView.setImageResource(image);

        if (toast==null){
            toast = new Toast(activity);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
//        toast.show();
        showMyToast(toast,time);
    }

    public static void showMyToast(final Toast toast, final int cnt) {
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        },0,3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }
}