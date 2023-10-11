package com.xh.hotme.listener;

import androidx.annotation.Keep;

import com.xh.hotme.bean.VideoTypeBean;

@Keep
public interface IVideoTypeListener {
	void onSelect(VideoTypeBean videoTypeBean);

	void onCancel();
}