package com.xh.hotme.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.FileUtil;


public class UserProviteActivity extends BaseActivity {

    // views
    private ImageView _backBtn;
    private TextView _titleLabel;
    private WebView _webView;

    private final static String PROXY_TYPE = "proxy_type";
    private final static String LOAD_TYPE = "load_type";
    private final static String URL = "url";
    public final static int proxy_type_user = 1;
    public final static int proxy_type_private = 2;

    public final static int load_type_local = 0;
    public final static int load_type_url = 1;


    public static void start(Context context, int type) {
        if (null != context) {
            Intent intent = new Intent(context, UserProviteActivity.class);
            intent.putExtra(PROXY_TYPE, type);
            intent.putExtra(LOAD_TYPE, load_type_local);
            intent.putExtra(URL, "");
            context.startActivity(intent);
        }
    }

    public static void start(Context context, int proxyType, int loadType, String url) {
        if (null != context) {
            Intent intent = new Intent(context, UserProviteActivity.class);
            intent.putExtra(PROXY_TYPE, proxyType);
            intent.putExtra(LOAD_TYPE, loadType);
            intent.putExtra(URL, url);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set status bar color
//		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
//		}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
//			window.getDecorView().setSystemUiVisibility(
//							View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // set content view
        setContentView(R.layout.activity_user_private);

        // find views
        _backBtn = findViewById(R.id.iv_back);
        _titleLabel = findViewById(R.id.tv_title);
        _webView = findViewById(R.id.web);

        // back click
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        // setup webview
        WebSettings settings = _webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        int type = getIntent().getIntExtra(PROXY_TYPE, 1);
        String data = "";
        if (type == 1) {  //用户协议
            data = FileUtil.readAssetsFileContent(UserProviteActivity.this, "user.html");
            // title
            _titleLabel.setText("用户协议");
        } else {   //隐私协议
            data = FileUtil.readAssetsFileContent(UserProviteActivity.this, "privacy.html");
            // title
            _titleLabel.setText("隐私协议");
        }

        int loadType = getIntent().getIntExtra(LOAD_TYPE, 0);
        String url = getIntent().getStringExtra(URL);

        if (loadType == load_type_local) {
            _webView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
        } else {
            _webView.loadUrl(url);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }
}
