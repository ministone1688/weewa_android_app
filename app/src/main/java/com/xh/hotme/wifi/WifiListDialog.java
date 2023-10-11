package com.xh.hotme.wifi;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xh.hotme.R;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.bluetooth.WifiListAdapterCallback;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.utils.DeviceInfo;
import com.xh.hotme.widget.RecycleViewDivider;

import java.util.ArrayList;
import java.util.List;


@Keep
public class WifiListDialog extends Dialog {
    private static final String TAG = WifiListDialog.class.getSimpleName();

    // views
    private LinearLayout _refreshButton;
    private ImageView _cancelButton;

    TextView _titleTv;

    // listener
    private OnClickListener _listener;
    private WifiListAdapterCallback _callback;

    private IBleScanRequestListener _scanRequestListener;

    WifiListAdapter _adapter;
    RecyclerView _listView;

    List<WifiInfo> _dataList = new ArrayList<>();
    IWifiNotifyListener _wifiNotifyListener;

    Handler _handler;

    public WifiListDialog(@NonNull final Context context, String title, List<WifiInfo> wifiList, WifiListAdapterCallback callback) {
        super(context, R.style.hotme_custom_dialog);
        initView(context, title, wifiList, callback);

    }

    public void initView(Context context, String title, List<WifiInfo> wifiList, WifiListAdapterCallback callback){
        _handler = new Handler();

        _callback = callback;

        // load content view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_bluetooth_list, null);

        // views
        _titleTv = view.findViewById(R.id.tv_title);
        _refreshButton = view.findViewById(R.id.refresh);
        _cancelButton = view.findViewById(R.id.iv_close);
        _listView = view.findViewById(R.id.recyclerView);

        _titleTv.setText(title);

        _cancelButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                if (_scanRequestListener != null) {
                    _scanRequestListener.onScanStop(0);
                }
                if (_callback != null) {
                    _callback.onCancel();
                }
                dismiss();
                return true;
            }
        });

//		// ok button
        _refreshButton.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                if (_scanRequestListener != null) {
                    _scanRequestListener.onScanStart(true);
                }
                return true;
            }
        });

        _refreshButton.setVisibility(View.GONE);

        _dataList.addAll(wifiList);

        _listView.setLayoutManager(new LinearLayoutManager(getContext()));
        _listView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL, getContext().getResources().getColor(R.color.bg_gray)));

        _adapter = new WifiListAdapter(context, _dataList);

        _adapter.setCallback(callback);

        _listView.setAdapter(_adapter);
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

    public void setOnClickListener(OnClickListener listener) {
        _listener = listener;
    }


    public void setScanListListener(IBleScanRequestListener l) {
        _scanRequestListener = l;
    }

    public IBleScanRequestListener getScanListListener() {
        return _scanRequestListener;
    }


    public void setWifiListCallback(WifiListAdapterCallback l) {
        _callback = l;
        if (_adapter != null) {
            _adapter.setCallback(_callback);
        }
    }

    public WifiListAdapterCallback getWifiListCallback() {
        return _callback;
    }

}
