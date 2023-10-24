package com.xh.hotme.active;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xh.hotme.R;
import com.xh.hotme.account.LoginInteract;
import com.xh.hotme.account.LoginViewCallback;
import com.xh.hotme.account.MobileLoginView;
import com.xh.hotme.bean.LoginResultBean;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleUnbindListener;
import com.xh.hotme.event.RemoveDeviceEvent;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

@Keep
public class MobileUnbindDialog extends Dialog implements IBleUnbindListener {
    // views
    private final FrameLayout _contentLayout;
    private final ImageView _cancelButton;

    // listener
    private OnClickListener _listener;

    String _mac;

    Context _context;
    MobileLoginView _loginView;

    Handler _handler;

    Runnable _smsTimeoutRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    Runnable _unbindTimeoutRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    public MobileUnbindDialog(@NonNull final Context context, String mac) {
        super(context, R.style.hotme_custom_dialog);

        _mac = mac;

        _context = context;

        _handler = new Handler() ;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_mobile_unbind, null);

        // views
        _cancelButton = view.findViewById(R.id.iv_close);
        _contentLayout = view.findViewById(R.id.content);


        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onClick(MobileUnbindDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
                return true;
            }
        });

        // add mobile login view
        _loginView = new MobileLoginView(context, R.layout.login_layout_mobile_view,Constants.TYPE_UNBIND);
        _loginView.findViewById(R.id.tv_login_tip).setVisibility(View.GONE);
        _loginView.setLoginType(Constants.TYPE_UNBIND);
        _loginView.setMac(mac);
        _loginView.setButtonText(getContext().getString(R.string.device_unbind));
        _contentLayout.addView(_loginView);

        _loginView.setLoginCallBack(new LoginViewCallback() {
            @Override
            public void requestCode(String mobile) {
                BluetoothManager.mInstance.sendCmdDeviceUnBindSms(mobile, _mac);
                _handler.postDelayed(_smsTimeoutRunnable, BluetoothManager.RESP_TIMEOUT);
            }

            @Override
            public void requestLogin(String mobile, String code) {
                BluetoothManager.mInstance.sendCmdDeviceUnBind(mobile, _mac, code);
                _handler.postDelayed(_unbindTimeoutRunnable, BluetoothManager.RESP_TIMEOUT);
            }

            @Override
            public void onClose() {

            }
        });

        _loginView.setLogListener(new LoginInteract.LoginListener() {
            @Override
            public void onSuccess(LoginResultBean data) {

                EventBus.getDefault().post(new RemoveDeviceEvent(_mac));
                dismiss();
            }

            @Override
            public void onFail(String code, String message) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.s(_context, message);
                    }
                });
            }

            @Override
            public void onFinish() {

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



    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        BluetoothHandle.addDeviceUnbindNotifyListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BluetoothHandle.removeDeviceUnbindNotifyListener(this);
    }


    public void setOnClickListener(OnClickListener listener) {
        _listener = listener;
    }

    public void setNegativeButtonVisible(boolean visible) {
        _cancelButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSendUnbindSms() {
        _handler.removeCallbacks(_smsTimeoutRunnable);
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (_loginView != null) {
                    _loginView.postSendCodeSuc();
                }
            }
        });
    }

    @Override
    public void onSendUnbindSmsFail(String message) {
        _handler.removeCallbacks(_smsTimeoutRunnable);

        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (_loginView != null) {
                    _loginView.postSendCodeFail(message);
                }
            }
        });
    }

    @Override
    public void onUnbind() {
        _handler.removeCallbacks(_unbindTimeoutRunnable);

        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new RemoveDeviceEvent(_mac));
                dismiss();
            }
        });

    }

    @Override
    public void onUnbindFail(String message) {
        _handler.removeCallbacks(_unbindTimeoutRunnable);

        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (_loginView != null) {
                    _loginView.postLoginFail(message);
                }
            }
        });
    }
}
