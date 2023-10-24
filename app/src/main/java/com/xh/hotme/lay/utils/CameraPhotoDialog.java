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
public class CameraPhotoDialog extends Dialog {
    // views
    private LinearLayout _dialog_close;

    // listener
    private OnClickListener _listener;
    Context _context;

    Handler _handler;
    TextView _from_camera,_from_album;

    private View.OnClickListener cameraBtnClickListener;
    private View.OnClickListener albumBtnClickListener;
    private View.OnClickListener cancleBtnClickListener;

    public CameraPhotoDialog(@NonNull final Context context) {
        super(context, R.style.hotme_modal_dialog);
        _context = context;
        _handler = new Handler() ;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_camrea_photo, null);
        // views
        _dialog_close = view.findViewById(R.id.dialog_close);
        _dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancleBtnClickListener != null) {
                    cancleBtnClickListener.onClick(v);
                }
                dismiss();
            }
        });

        _from_camera = view.findViewById(R.id.from_camera);
        _from_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraBtnClickListener != null) {
                    cameraBtnClickListener.onClick(v);
                }
                dismiss();
            }
        });
        _from_album = view.findViewById(R.id.from_album);
        _from_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (albumBtnClickListener != null) {
                    albumBtnClickListener.onClick(v);
                }
                dismiss();
            }
        });

        setContentView(view);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams windowparams = window.getAttributes();
        windowparams.width = (int)(DeviceInfo.getWidth(context) * 1.0);
        //float topHeight = getContext().getResources().getDimension(R.dimen.dialog_margin_tb);
        // windowparams.height = (int) (DeviceInfo.getHeight(context) - DensityUtil.dip2px(context, topHeight));
        //设置img
       // Integer width = (int)(DeviceInfo.getWidth(context) * 0.8);
       // _top_img.getLayoutParams().width = width;
       // _top_img.getLayoutParams().height = (int)(width * 0.38);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    public void setCameraBtnClickListener(View.OnClickListener listener) {
        cameraBtnClickListener = listener;
    }

    public void setAlbumBtnClickListener(View.OnClickListener listener) {
        albumBtnClickListener = listener;
    }
    public void setCancleBtnClickListener(View.OnClickListener listener) {
        cameraBtnClickListener = listener;
    }

}
