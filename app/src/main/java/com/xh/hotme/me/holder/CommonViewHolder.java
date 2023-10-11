package com.xh.hotme.me.holder;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.recyclerview.widget.RecyclerView;

/**
 * view holder的通用基类, 封装了一些通用字段和功能
 */
@Keep
public abstract class CommonViewHolder<T> extends RecyclerView.ViewHolder {
	public CommonViewHolder(View itemView) {
		super(itemView);
	}

	public abstract void onBind(T model, int position);

	public View getItemView() {
		return itemView;
	}

	public ViewGroup _adContainer;
	public void  setAdContainer(ViewGroup adContainer){
		_adContainer =  adContainer;
	}


	public void refresh(){

	}

	public void onDestroy(){

	}


}
