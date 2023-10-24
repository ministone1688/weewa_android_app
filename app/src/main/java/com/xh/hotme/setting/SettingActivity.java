package com.xh.hotme.setting;

import static com.xh.hotme.utils.DataCleanManager.getTotalCacheSize;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tsy.sdk.myokhttp.MyOkHttp;
import com.tsy.sdk.myokhttp.response.RawResponseHandler;
import com.xh.hotme.R;
import com.xh.hotme.account.DeleteUserActivity;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.lay.PushSetAcitivty;
import com.xh.hotme.lay.WeburlActivity;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.lay.utils.UpdateModalDialog;
import com.xh.hotme.lay.utils.WebModalDialog;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.DataCleanManager;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.widget.ModalDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingActivity extends BaseActivity {
	// views
	private ImageView _backBtn;
	private TextView _titleLabel;

	private View _clearCacheView;
	private View _showCoinFloatView;
	private View _csWechatView;
	private View _agreemeView;
	private View _privateView;
	private View _verUpdate;
	private View _aboutMeView;  //关于我们
	private View _deleteAccountView;  //注销账户
	private TextView _wechatLabel;
	private Button _signOutView;

	private TextView _cache_num;
	private TextView _ver_num;

	MyOkHttp mMyOkhttp = new MyOkHttp();
	private UpdateModalDialog _webDialog;

	public static void start(Context context) {
		if(null != context) {
			Intent intent = new Intent(context, SettingActivity.class);
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
		setContentView(R.layout.activity_settings);

		// find views
		_backBtn = findViewById(R.id.iv_back);
		_titleLabel = findViewById(R.id.tv_title);
		_clearCacheView = findViewById(R.id.clear_cache);
		_agreemeView = findViewById(R.id.agreement_view);
		_privateView = findViewById(R.id.leto_provacy);
		_verUpdate = findViewById(R.id.ver_update);
		_aboutMeView = findViewById(R.id.about_me);
		_deleteAccountView = findViewById(R.id.delete_account);

		_cache_num = findViewById(R.id.cache_num);
		getCacheSize();

		_ver_num = findViewById(R.id.ver_num);
		_ver_num.setText(MyToolUtils.getAppVersionName(getApplicationContext()));


		_aboutMeView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				ContactUsActivity.start(SettingActivity.this);
				return true;
			}
		});

		// clear cache
		_clearCacheView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {

				ModalDialog d = new ModalDialog(SettingActivity.this,
						"",
						"确认删除所有缓存？",
						true
						);
				d.setLeftButton("我再想想", new ClickGuard.GuardedOnClickListener() {
					@Override
					public boolean onClicked() {
						d.dismiss();
						return true;
					}
				});
				d.setRightButton("删除", new ClickGuard.GuardedOnClickListener() {
					@Override
					public boolean onClicked() {
						// clear
						DataCleanManager.clearCache(SettingActivity.this);
						_cache_num.setText("0k");
						return true;
					}
				});
				d.show();

				return true;
			}
		});

		// privacy show
//		_agreemeView.setVisibility(MGCSharedModel.isShowPrivacy ? View.VISIBLE : View.GONE);

		// privacy click
		_agreemeView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				//UserProviteActivity.start(SettingActivity.this, UserProviteActivity.proxy_type_user);
				Intent intent = new Intent(getApplicationContext(), WeburlActivity.class);
				intent.putExtra("weburl", SdkApi.user_userproxy);
				intent.putExtra("title",getString(R.string.user_proment));
				MyToolUtils.goActivity(SettingActivity.this,intent);

				return true;
			}
		});

		// privacy click
		_privateView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {

				//UserProviteActivity.start(SettingActivity.this, UserProviteActivity.proxy_type_private);

				Intent intent = new Intent(getApplicationContext(), WeburlActivity.class);
				intent.putExtra("weburl", SdkApi.user_privateurl);
				intent.putExtra("title",getString(R.string.user_agreement));
				MyToolUtils.goActivity(SettingActivity.this,intent);

				return true;
			}
		});

		//版本更新
		_verUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateDialog("版本升级");
			}
		});

		// back click
		_backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				finish();
				return true;
			}
		});

		if(!LoginManager.isSignedIn(SettingActivity.this)){
			_deleteAccountView.setVisibility(View.GONE);
		}

		_deleteAccountView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
			@Override
			public boolean onClicked() {
				DeleteUserActivity.start(SettingActivity.this);
				return true;
			}
		});


		// title
		_titleLabel.setText("设置");
	}

	public void getCacheSize(){
		try {
			String cache = getTotalCacheSize(SettingActivity.this);
			_cache_num.setText(cache);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void updateDialog(String mac) {
		if (_webDialog != null && _webDialog.isShowing()) {
			_webDialog.dismiss();
		}
		_webDialog = null;

		_webDialog = new UpdateModalDialog(SettingActivity.this, "1、修复了bug\r\n2、更新了功能");
		_webDialog.setOnClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		_webDialog.show();
	}

	private void getVerNum(){
		Map<String, String> params = new HashMap<>();
		params.put("version", MyToolUtils.getAppVersionName(getApplicationContext()));
		mMyOkhttp.get()
				.url(SdkApi.upgrade).params(params).tag(this)
				.enqueue(new RawResponseHandler() {
					@Override
					public void onSuccess(int statusCode, String result) {
						HashMap<String, String> mapRes = JSON.parseObject(result, new TypeReference<HashMap<String, String>>() {});
						System.out.println("-------------------------------------"+mapRes);
						if (mapRes.get("code").equals(200)) {

						}
					}
					@Override
					public void onFailure(int statusCode, String error_msg) {
						//Log.d(TAG, "doPost onFailure:" + error_msg);
					}
				});
	}


}
