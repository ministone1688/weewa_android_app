package com.xh.hotme.setting;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xh.hotme.R;
import com.xh.hotme.me.holder.CommonViewHolder;


public class SimpleUserInfoHolder extends CommonViewHolder<Pair<String, String>> {
	private final TextView _nameLabel;
	private final TextView _valueLabel;

	public static SimpleUserInfoHolder create(Context ctx, ViewGroup parent) {
		// load game row, and leave a gap so that next column can be seen
		View view = LayoutInflater.from(ctx)
			.inflate(R.layout.list_item_simple_user_info, parent, false);
		return new SimpleUserInfoHolder(view);
	}

	public SimpleUserInfoHolder(View view) {
		super(view);

		Context ctx = view.getContext();
		_nameLabel = view.findViewById(R.id.name);
		_valueLabel = view.findViewById(R.id.value);
	}

	@Override
	public void onBind(Pair<String, String> model, int position) {
		// name
		_nameLabel.setText(model.first);

		// value
		_valueLabel.setText(model.second);
	}
}