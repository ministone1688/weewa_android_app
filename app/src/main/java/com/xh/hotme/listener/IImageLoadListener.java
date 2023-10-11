package com.xh.hotme.listener;

import androidx.annotation.Keep;

import java.io.File;

@Keep
public interface IImageLoadListener {
	void onStart();
	void onComplete(File result);
}