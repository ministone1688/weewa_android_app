package com.xh.hotme.listener;

import androidx.annotation.Keep;

import java.io.File;

@Keep
public interface IScanDeviceListListener {
	void onStart();
	void onScanResult();
}