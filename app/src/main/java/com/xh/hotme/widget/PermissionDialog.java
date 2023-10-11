package com.xh.hotme.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xh.hotme.R;
import com.xh.hotme.utils.ClickGuard;


@Keep
public class PermissionDialog extends Dialog {
	// views
	private final TextView _okButton;
	private final TextView _cancelButton;

	// listener
	private OnClickListener _listener;


	public PermissionDialog(@NonNull final Context context) {
		super(context, R.style.hotme_custom_dialog);

		// load content view
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.dialog_permissons_new, null);

		// views
		_okButton = view.findViewById(R.id.leto_ok);
		_cancelButton = view.findViewById(R.id.leto_cancel);


		_cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				if(_listener != null) {
					_listener.onClick(PermissionDialog.this, DialogInterface.BUTTON_NEGATIVE);
				}
				return true;
			}
		});

		// ok button
		_okButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				if(_listener != null) {
					_listener.onClick(PermissionDialog.this, DialogInterface.BUTTON_POSITIVE);
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
		window.setGravity(Gravity.CENTER);
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
	public void setNegativeButtonText(String title) {
		_cancelButton.setText(title);
	}
}
