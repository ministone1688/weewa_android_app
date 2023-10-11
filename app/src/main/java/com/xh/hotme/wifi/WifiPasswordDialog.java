package com.xh.hotme.wifi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.DialogUtil;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.ToastUtil;

import java.util.List;


@Keep
public class WifiPasswordDialog extends Dialog implements IWifiNotifyListener {
    private static final String TAG = "BluetoothListDialog";

    // views
    private final TextView _refreshButton;
    private final ImageView _cancelButton;

    TextView _titleTv;

    // listener
    private OnClickListener _listener;


    private final EditText _passwordEt;

    Handler _handler;

    String _ssid;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //
            DialogUtil.dismissDialog();
        }
    };

    public WifiPasswordDialog(@NonNull final Context context, String ssid) {
        super(context, R.style.hotme_custom_dialog);

        _handler = new Handler();

        _ssid = ssid;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.wifi_dialog_setup, null);

        // views
        _titleTv = view.findViewById(R.id.tv_title);
        _refreshButton = view.findViewById(R.id.tv_refresh);
        _cancelButton = view.findViewById(R.id.iv_close);
        _passwordEt = view.findViewById(R.id.et_password);

        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onClick(WifiPasswordDialog.this, DialogInterface.BUTTON_NEGATIVE);
                }
                dismiss();

                return true;
            }
        });

        _titleTv.setText(_ssid);
//		// ok button
        _refreshButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                String password = _passwordEt.getText().toString();
                if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(password)) {
                    ToastUtil.s(getContext(), "请输入ssid和密码");
                    return true;
                }

                boolean isSuc = BluetoothManager.mInstance.sendCmdSetup(ssid, password);
                if (isSuc) {
                    _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT);
                    DialogUtil.showDialog(getContext(), "正在连接");
                } else {
                    AppTrace.d(TAG, "请检查网络连接");
                }

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
        windowparams.height = DeviceInfo.getHeight(context) - DensityUtil.dip2px(context, 20);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        BluetoothHandle.addWifiNotifyListener(this);

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        BluetoothHandle.removeWifiNotifyListener(this);
        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        _listener = listener;
    }


    @Override
    public void onWifiList(List<WifiInfo> device) {

    }

    @Override
    public void onSetupStatus(int status, String message) {
        _handler.removeCallbacks(_timeOutRunnable);
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                DialogUtil.dismissDialog();
                if (status == 1) {
                    if (_listener != null) {
                        _listener.onClick(WifiPasswordDialog.this, DialogInterface.BUTTON_POSITIVE);
                    }
                    dismiss();
                } else {
                    ToastUtil.s(getContext(), "配网失败: " + message);
                }
            }
        });

    }
}
