package com.xh.hotme.lay.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xh.hotme.R;

public abstract class CameraPhotoDialog extends Dialog implements View.OnClickListener {
    private Context context;

    public CameraPhotoDialog(@NonNull Context context) {
        super(context, R.style.hotme_modal_dialog_ly);//内容样式在这里引入

        this.context = context;
    }

    public CameraPhotoDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CameraPhotoDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_camrea_photo);

        //tv_title = findViewById(R.id.tv_title);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.9); // 宽度设置为屏幕宽度的80%
        //lp.dimAmount=0.0f;//外围遮罩透明度0.0f-1.0f
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.BOTTOM);//内围区域底部显示

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

    }

    protected abstract void confirm();
}
