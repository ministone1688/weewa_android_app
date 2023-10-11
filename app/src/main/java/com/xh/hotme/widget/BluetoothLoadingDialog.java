package com.xh.hotme.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xh.hotme.R;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;


@Keep
public class BluetoothLoadingDialog extends Dialog {
	// views
	private final ImageView _cancelButton;

	private final TextView _messageTv;

	// listener
	private OnClickListener _listener;


	public BluetoothLoadingDialog(@NonNull final Context context) {
		super(context, R.style.hotme_custom_dialog);

		// load content view
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.dialog_open_bluetooth_loading, null);

		// views
		_cancelButton = view.findViewById(R.id.iv_close);


		// views
		_messageTv = view.findViewById(R.id.message);

		_cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				if(_listener != null) {
					_listener.onClick(BluetoothLoadingDialog.this, DialogInterface.BUTTON_NEGATIVE);
				}
				dismiss();
				return true;
			}
		});


		// set content view
		setContentView(view);

		setCancelable(false);
		setCanceledOnTouchOutside(false);

		Window window = getWindow();
		window.setGravity(Gravity.BOTTOM);
		WindowManager.LayoutParams windowparams = window.getAttributes();
		windowparams.width = DeviceInfo.getWidth(context);


		float topHeight = getContext().getResources().getDimension(R.dimen.dialog_margin_top);
		windowparams.height = (int) (DeviceInfo.getHeight(context) - DensityUtil.dip2px(context, topHeight));

	}

	public void setOnClickListener(OnClickListener listener){
		_listener = listener;
	}

	public void setMessage(String message){
		if(!TextUtils.isEmpty(message)){
			_messageTv.setText(message);
			_messageTv.setVisibility(View.VISIBLE);
		}
	}
}
