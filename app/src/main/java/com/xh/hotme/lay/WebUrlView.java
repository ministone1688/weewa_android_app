package com.xh.hotme.lay;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.xh.hotme.R;
import com.xh.hotme.account.LoginInteract;
import com.xh.hotme.account.MobileLoginView;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.utils.ToastUtil;

public class WebUrlView extends FrameLayout implements View.OnClickListener {
    private static final String TAG = MobileLoginView.class.getSimpleName();

    public WebUrlView(Context context) {
        super(context);
        setupUI(0);
    }

    public WebUrlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupUI(0);
    }

    public WebUrlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupUI(0);
    }

    public WebUrlView(Context context, int layoutId) {
        super(context);
        setupUI(layoutId);
    }

    private void setupUI(int layoutId) {
        if (layoutId == 0) {
            layoutId = R.layout.login_include_login_merge;
        }
        LayoutInflater.from(getContext()).inflate(layoutId, this);
    }

    @Override
    public void onClick(View view) {

    }
}
