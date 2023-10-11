package com.xh.hotme.base;

import androidx.viewbinding.ViewBinding;

import com.xh.hotme.R;


/**
 * @Description:
 * @Author: Maclay
 * @Date: 2023/2/5 15:22
 */
public abstract class AppTitleActivity<T extends ViewBinding> extends BaseViewActivity {
    public boolean isFitSystemWindows() {
        return true;
    }

    public boolean shouldTransParent() {
        return true;
    }

    @Override
    public boolean isDarkStatusBar() {
        return true;
    }

    @Override
    public int getStatusBarColor() {
        return R.color.white;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.white;
    }
}