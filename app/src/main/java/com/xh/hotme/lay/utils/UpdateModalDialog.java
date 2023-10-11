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
import android.widget.TextView;

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
public class UpdateModalDialog extends Dialog {
    // views
    private final ImageView _cancelButton;

    // listener
    private OnClickListener _listener;

    String _mac;

    Context _context;

    Handler _handler;
    ImageView _top_img;
    TextView _up_tips;

    public UpdateModalDialog(@NonNull final Context context, String mac) {
        super(context, R.style.hotme_modal_dialog);
        _mac = mac;
        _context = context;
        _handler = new Handler() ;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_update_ver, null);
        // views
        _cancelButton = view.findViewById(R.id.iv_close);
        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onClick(UpdateModalDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
                return true;
            }
        });

        _top_img = view.findViewById(R.id.top_img);
        _up_tips = view.findViewById(R.id.up_tips);
        _up_tips.setText(_mac);
        setContentView(view);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams windowparams = window.getAttributes();
        //windowparams.width = (int)(DeviceInfo.getWidth(context) * 0.9);
        //float topHeight = getContext().getResources().getDimension(R.dimen.dialog_margin_tb);
       // windowparams.height = (int) (DeviceInfo.getHeight(context) - DensityUtil.dip2px(context, topHeight));
        //设置img
        Integer width = (int)(DeviceInfo.getWidth(context) * 0.8);
        _top_img.getLayoutParams().width = width;
        _top_img.getLayoutParams().height = (int)(width * 0.38);
    }

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

}
