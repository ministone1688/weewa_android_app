package com.xh.hotme.listener;

import android.graphics.drawable.Drawable;

import androidx.annotation.Keep;

/**
 * Create by zhaozhihui on 2019-06-06
 **/
@Keep
public interface IGlideLoadListener {
    void onResourceReady(Drawable resource);
}
