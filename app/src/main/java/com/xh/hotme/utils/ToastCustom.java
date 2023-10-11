package com.xh.hotme.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xh.hotme.R;


public class ToastCustom extends Toast {

    private static Toast mToast;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public ToastCustom(Context context) {
        super(context);
    }


    public static void  showCustomToast(Context context, String content) {

        //获取系统的LayoutInflater
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast_custom, null);

        TextView tv_content = view.findViewById(R.id.tv_message);
        tv_content.setText(content);

        ImageView iv_icon = view.findViewById(R.id.iv_icon);
        iv_icon.setVisibility(View.GONE);

        //实例化toast
        mToast = new Toast(context);
        mToast.setView(view);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER,0,0);
        mToast.show();
    }



}
