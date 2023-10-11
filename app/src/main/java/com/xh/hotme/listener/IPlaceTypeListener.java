package com.xh.hotme.listener;

import androidx.annotation.Keep;

import com.xh.hotme.bean.TextModelBean;

@Keep
public interface IPlaceTypeListener {
	void onSelect(TextModelBean placeBean, TextModelBean ageBean);

	void onCancel();
}