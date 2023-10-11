package com.xh.hotme.lay.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.xh.hotme.R;
import com.xh.hotme.account.LoginInteract;
import com.xh.hotme.account.LoginViewCallback;
import com.xh.hotme.account.MobileLoginView;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleUnbindListener;
import com.xh.hotme.event.RemoveDeviceEvent;
import com.xh.hotme.lay.WebUrlView;
import com.xh.hotme.lay.WeburlActivity;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

@Keep
public class WebModalDialog extends Dialog {
    // views
    private final FrameLayout _contentLayout;
    private final ImageView _cancelButton;

    // listener
    private OnClickListener _listener;

    String _mac;

    Context _context;
    WebUrlView _webView;

    Handler _handler;

    protected AgentWeb mAgentWeb;
    private LinearLayout mLinearLayout;
    private String webUrl;

    public WebModalDialog(@NonNull final Context context, String mac) {
        super(context, R.style.hotme_modal_dialog);

        _mac = mac;

        _context = context;

        _handler = new Handler() ;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_web_url, null);

        // views
        _cancelButton = view.findViewById(R.id.iv_close);
        _contentLayout = view.findViewById(R.id.content);
        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onClick(WebModalDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
                return true;
            }
        });

        // add mobile login view
        _webView = new WebUrlView(context, R.layout.item_web_url);
        _contentLayout.addView(_webView);
        // set content view
        setContentView(view);

        mLinearLayout = (LinearLayout) _webView.findViewById(R.id.container);
        initView();

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams windowparams = window.getAttributes();
       windowparams.width = (int)(DeviceInfo.getWidth(context) * 0.9);
       float topHeight = getContext().getResources().getDimension(R.dimen.dialog_margin_tb);
       windowparams.height = (int) (DeviceInfo.getHeight(context) - DensityUtil.dip2px(context, topHeight));
    }


    protected void initView(){
        mAgentWeb = AgentWeb.with((Activity) _context)
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
                .go(_mac);
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
                mAgentWeb.getJsInterfaceHolder().addJavaObject("android",new WebModalDialog.AndroidInterface(mAgentWeb,_context));
            }
        }
    };
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    };



    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    public void setOnClickListener(OnClickListener listener) {
        _listener = listener;
    }

    public void setNegativeButtonVisible(boolean visible) {
        _cancelButton.setVisibility(visible ? View.VISIBLE : View.GONE);
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
            dismiss();
        }

    }

}
