package com.xh.hotme.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.StatusBarUtil;


public class AboutMeActivity extends BaseActivity {

	// views
	private ImageView _backBtn;
	private TextView _titleLabel;
	private TextView _appVersionLabel;
	private TextView _letoLabel;
	private TextView _appNameLabel;

	private LinearLayout _agreementLabel;
	private LinearLayout _provacyLabel;


	public static void start(Context context) {
		if(null != context) {
			Intent intent = new Intent(context, AboutMeActivity.class);
			context.startActivity(intent);
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set status bar color
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
		}

		// set content view
		setContentView(R.layout.activity_about_me);

		// find views
		_backBtn = findViewById(R.id.iv_back);
		_titleLabel = findViewById(R.id.tv_title);
		_appNameLabel = findViewById(R.id.app_name);
		_appVersionLabel = findViewById(R.id.tv_app_version);

		_agreementLabel = findViewById(R.id.leto_user_agrement);
		_provacyLabel = findViewById(R.id.leto_provacy);


		String appVersion = BaseAppUtil.getAppVersionName(AboutMeActivity.this);

		_appVersionLabel.setText(String.format("V%s", appVersion));

		// back click
		_backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				finish();
				return true;
			}
		});

		// title
		_titleLabel.setText("关于我们");

		_agreementLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserProviteActivity.start(AboutMeActivity.this, UserProviteActivity.proxy_type_user);
			}
		});


		_provacyLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				UserProviteActivity.start(AboutMeActivity.this, UserProviteActivity.proxy_type_private);
			}
		});

		String appName = String.valueOf(getResources().getText(R.string.app_name));
		_appNameLabel.setText(appName);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();


	}
}
