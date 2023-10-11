package com.xh.hotme.base;

import android.app.Activity;
import android.view.View;

import com.xh.hotme.databinding.CommonIncludeHeaderBinding;

public class TitleLayoutHelper {

    public static void initTitle(CommonIncludeHeaderBinding titleBinding, String title, View.OnClickListener listener) {
        titleBinding.tvTitle.setText(title);
        titleBinding.ivBack.setOnClickListener(listener);
    }
}
