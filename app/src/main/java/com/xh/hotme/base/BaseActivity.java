package com.xh.hotme.base;

import android.os.Bundle;
import android.view.View;


import androidx.annotation.ColorRes;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.xh.hotme.R;
import com.xh.hotme.thrid.UMengManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.widget.LoadingDialog;

/**
 * Created by liu hong liang on 2017/4/27.
 */
@Keep
public class BaseActivity extends FragmentActivity {
    protected static final String TAG = BaseActivity.class.getSimpleName();
    private View titleView;

    LoadingDialog mDialog;

    public boolean isActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityStackManager.getInstance().pushActivity(this);
    }


    /**
     * @return false 延伸到状态栏下面
     */
    public boolean isFitSystemWindows() {
        return false;
    }

    public boolean isDarkStatusBar() {
        return true;
    }

    public boolean shouldTransParent() {
        return true;
    }

    public @ColorRes
    int getStatusBarColor() {
        return android.R.color.transparent;
    }

    public @ColorRes
    int getNavigationBarColor() {
        return android.R.color.transparent;
    }

    /**
     * 改变标题栏显示状态
     *
     * @param show
     */
    public void changeTitleStatus(boolean show) {
        if (titleView == null) {
            AppTrace.e(TAG, "没有设置titleView");
            return;
        }
        if (show) {
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题栏view
     *
     * @param titleView
     */
    public void setTitleView(View titleView) {
        this.titleView = titleView;
    }

    @Override
    public void onResume() {
        super.onResume();
        UMengManager.onResume(this);
        isActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        UMengManager.onPause(this);
        isActive = false;
    }

    @Keep
    public void showLoading() {
        showLoading(false, "");
    }

    @Keep
    public void showLoading(String message) {

        showLoading(false, message);
    }

    @Keep
    public void showLoading(Boolean cancelable, String message) {
        try {
            if (!isDestroyed() && !isFinishing()) {
                if (mDialog == null) {
                    mDialog = new LoadingDialog(this);
                }
                mDialog.setCancelable(cancelable);
                mDialog.show(message);
            }
        } catch (Throwable e) {

        }
    }

    @Keep
    public void dismissLoading() {
        try {
            if (mDialog != null) {
                if (!isDestroyed() && !isFinishing()) {
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                }
                mDialog.destroy();
            }
        } catch (Throwable e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissLoading();

        ActivityStackManager.getInstance().popSingleActivity(this);

    }
}
