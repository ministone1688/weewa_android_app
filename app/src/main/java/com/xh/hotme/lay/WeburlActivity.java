package com.xh.hotme.lay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.xh.hotme.R;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.lay.utils.TextUtil;
import com.xh.hotme.utils.ClickGuard;

public class WeburlActivity extends BaseActivity {

    protected AgentWeb mAgentWeb;
    private LinearLayout mLinearLayout;
    private String webUrl;
    private String title;

    TextView titleView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weburl);

        Intent intert=getIntent();
        webUrl = intert.getStringExtra("weburl");
        title = intert.getStringExtra("title");
        if(TextUtil.isNullOrEmpty(webUrl)){
            finish();
        }

        titleView = findViewById(R.id.tv_title);
        titleView.setText(title);
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        mLinearLayout = (LinearLayout) this.findViewById(R.id.container);

        initView();
    }

    protected void initView(){
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
                .createAgentWeb()
                .ready()
                .go(webUrl);
        //设置自适应屏幕，两者合用
        mAgentWeb.getAgentWebSettings().getWebSettings().setUseWideViewPort(true); //将图片调整到适合webview的大小
        mAgentWeb.getAgentWebSettings().getWebSettings().setLoadWithOverviewMode(true); // 缩放至屏幕的大小
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do you  work
            if(mAgentWeb!=null){
                //注入对象
                mAgentWeb.getJsInterfaceHolder().addJavaObject("android",new AndroidInterface(mAgentWeb,WeburlActivity.this));
            }
        }
    };
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if(!TextUtil.isNullOrEmpty(title) && title.indexOf("http")==-1){
                titleView.setText(title);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Log.i("Info", "onResult:" + requestCode + " onResult:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mAgentWeb.destroy();
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
    }

    public class AndroidInterface {
        private Handler deliver = new Handler(Looper.getMainLooper());
        private AgentWeb agent;
        private Context context;

        public AndroidInterface(AgentWeb agent, Context context) {
            this.agent = agent;
            this.context = context;
        }

        @JavascriptInterface
        public void goToFaish(){
            finish();
        }

    }

}
