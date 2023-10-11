package com.xh.hotme.active;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xh.hotme.R;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;


@Keep
public class ActiveDialog extends Dialog {
	// views
	private final TextView _okButton;
	private final ImageView _cancelButton;

	// listener
	private OnClickListener _listener;

	EditText _mobileEdit;


	public ActiveDialog(@NonNull final Context context) {
		super(context, R.style.hotme_custom_dialog);

		// load content view
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.active_dialog_login, null);

		// views
		_okButton = view.findViewById(R.id.btn_ok);
		_cancelButton = view.findViewById(R.id.iv_close);
		_mobileEdit = view.findViewById(R.id.et_loginAccount);


		_cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				if(_listener != null) {
					_listener.onClick(ActiveDialog.this, DialogInterface.BUTTON_NEGATIVE);
				}
				return true;
			}
		});

		// ok button
		_okButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				if(_listener != null) {
					_listener.onClick(ActiveDialog.this, DialogInterface.BUTTON_POSITIVE);
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

	public void setNegativeButtonVisible(boolean visible) {
		_cancelButton.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	public void setPositiveButtonTitle(String title) {
		_okButton.setText(title);
	}

}
