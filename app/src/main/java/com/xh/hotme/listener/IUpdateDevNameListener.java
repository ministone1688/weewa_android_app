package com.xh.hotme.listener;

import androidx.annotation.Keep;

@Keep
public interface IUpdateDevNameListener {
	void onUpdate(String name);
	void onCancel();
}