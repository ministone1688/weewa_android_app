package com.xh.hotme.active;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xh.hotme.R;
import com.xh.hotme.account.SendSmsInteract;
import com.xh.hotme.bluetooth.BleConstants;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.IBleActiveSmsListener;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.ToastCustom;
import com.xh.hotme.utils.ToastUtil;

@Keep
public class MobileActiveSmsDialog extends Dialog implements IBleActiveSmsListener {
    // views
    private final ImageView _cancelButton;

    TextView _loginView, _titleView, _statusView;

    EditText _etMobile;

    LinearLayout _statusLayout;
    // listener
    private IActiveSmsListener _listener;

    String info_timeout;

    Device _device;
    String _mobile;

    Context _context;

    Handler _handler;

    Runnable _smsTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            ToastCustom.showCustomToast(_context, info_timeout);
            MainHandler.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    _statusLayout.setVisibility(View.VISIBLE);
                    _statusView.setText(info_timeout);
                }
            });
        }
    };

    public MobileActiveSmsDialog(@NonNull final Context context, Device device) {
        super(context, R.style.hotme_custom_dialog);

        _context = context;

        _device = device;

        _handler = new Handler();

        info_timeout = getContext().getString(R.string.request_timeout);

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_mobile_sms, null);

        // views
        _cancelButton = view.findViewById(R.id.iv_close);
        _titleView = view.findViewById(R.id.tv_title);
        _etMobile = view.findViewById(R.id.et_mobile);
        ImageView clearView = view.findViewById(R.id.clear);
        _loginView = view.findViewById(R.id.btn_loginSubmit);
        _statusLayout = view.findViewById(R.id.ll_status);
        _statusView = view.findViewById(R.id.login_status);

        clearView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                _etMobile.getText().clear();

                return true;
            }
        });

        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                _handler.removeCallbacksAndMessages(null);

                dismiss();
                return true;
            }
        });

        _loginView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                _statusView.setText("");
                _statusLayout.setVisibility(View.GONE);
                _mobile = _etMobile.getText().toString();
                if (TextUtils.isEmpty(_mobile)) {
                    ToastUtil.s(_context, _context.getString(R.string.login_error_phone_format));
                    _statusView.setText(_context.getString(R.string.login_error_phone_format));
                    _statusLayout.setVisibility(View.VISIBLE);
                    return true;
                }
                if (_device == null) {
                    ToastUtil.s(_context, _context.getString(R.string.login_error_connect_device));
                    return true;
                }

                BluetoothManager.mInstance.sendCmdDeviceActiveSms(_mobile, _device.address);
                _handler.postDelayed(_smsTimeoutRunnable, BluetoothManager.RESP_TIMEOUT);

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


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        BluetoothHandle.addDeviceActiveSmsNotifyListener(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BluetoothHandle.removeDeviceActiveSmsNotifyListener(this);
    }


    public void setOnClickListener(IActiveSmsListener listener) {
        _listener = listener;
    }


    @Override
    public void onSendActiveSms(String code) {
        _handler.removeCallbacks(_smsTimeoutRunnable);

        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (code.equalsIgnoreCase(BleConstants.BLE_SMS_SUCCESS_DEVICE_BOUND) ) {
                    SendSmsInteract.sendLoginSMS(getContext(), _mobile, new SendSmsInteract.SendSmsListener() {
                        @Override
                        public void onSuccess() {
                            if (_listener != null) {
                                _listener.onActiveSms(_mobile, BleConstants.BLE_SMS_SUCCESS_LOGIN);
                            }
                        }

                        @Override
                        public void onFail(String code, String message) {
                             postSendSmsFail(message);
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                } else {
                    if (_listener != null) {
                        _listener.onActiveSms(_mobile, code);
                    }
                }
            }
        });
    }

    private void postSendSmsFail(String message){
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                _statusView.setText(message);
                _statusLayout.setVisibility(View.VISIBLE);

                if (_listener != null) {
                    _listener.onActiveSmsFail(_mobile, message);
                }
            }
        });
    }

    @Override
    public void onSendActiveSmsFail(String message) {
        _handler.removeCallbacks(_smsTimeoutRunnable);

        postSendSmsFail(message);
    }

    public interface IActiveSmsListener {
        void onActiveSms(String mobile, String code);

        void onActiveSmsFail(String mobile, String message);

    }
}
