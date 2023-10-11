package com.xh.hotme.camera;

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

import com.xh.hotme.R;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.IBleDeviceNameNotifyListener;
import com.xh.hotme.listener.IUpdateDevNameListener;

import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.ToastUtil;


@Keep
public class SetDeviceNameDialog extends Dialog implements IBleDeviceNameNotifyListener {
    private static final String TAG = "BluetoothListDialog";

    // views
    private final ImageView _cancelButton;

    EditText _nameEditText;
    TextView _submitView;

    // listener
    private IUpdateDevNameListener _listener;


    Handler _handler;

    boolean isScanning = false;

    Context _context;

    String _name;

    private final Runnable _timeOutRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            //

        }
    };


    public SetDeviceNameDialog(@NonNull final Context context, String name) {
        super(context, R.style.hotme_custom_dialog);

        _handler = new Handler();

        _context = context;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_device_name, null);

        // views
        _cancelButton = view.findViewById(R.id.iv_close);
        _nameEditText = view.findViewById(R.id.et_name);
        _submitView = view.findViewById(R.id.btn_submit);

        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_listener != null) {
                    _listener.onCancel();
                }
                dismiss();
                return true;
            }
        });

        _submitView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                String name = _nameEditText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.s(context, getContext().getString(R.string.info_input_device_name));
                    return true;
                }

                if (!TextUtils.isEmpty(_name) && _name.equalsIgnoreCase(name)) {
                    ToastUtil.s(context, getContext().getString(R.string.info_device_name_the_same_as_device));
                    return true;
                }

                _name = name;

                 BluetoothManager.mInstance.sendCmdUpdateName(name);

                _handler.postDelayed(_timeOutRunnable, BluetoothManager.RESP_TIMEOUT_SHORT);
                return true;
            }
        });

        if (!TextUtils.isEmpty(name)) {
            _nameEditText.setText(name);
            _name = name;
        }


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

        BluetoothHandle.addDeviceNameNotifyListener(this);

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        BluetoothHandle.removeDeviceNameNotifyListener(this);

    }

    public void setOnClickListener(IUpdateDevNameListener listener) {
        _listener = listener;
    }


    @Override
    public void onDeviceName() {
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (_listener != null) {
                        _listener.onUpdate(_name);
                    }
                    ToastUtil.s(_context, _context.getString(R.string.info_update_dev_name_suc));
                    dismiss();
                } catch (Throwable e) {

                }
            }
        });
    }

    @Override
    public void onDeviceNameFail(String msg) {
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.s(_context, "修改失败：" + msg);
            }
        });
    }
}
