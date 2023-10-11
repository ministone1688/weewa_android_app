package com.xh.hotme.active;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.BindDeviceBean;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class MyDeviceAdapter extends BaseQuickAdapter<BindDeviceBean, BaseViewHolder> implements LoadMoreModule {
    private final static String TAG = "MyDeviceAdapter";
    private ArrayList<BindDeviceBean> mDevices;
    private BluetoothDeviceAdapterCallback mCallback;

    Context _context;

    public MyDeviceAdapter(Context context, List<BindDeviceBean> data) {
        super(R.layout.my_device_list_item, data);
        _context = context;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, BindDeviceBean device) {
        int position = baseViewHolder.getPosition();

        String deviceName = "";
        if (!TextUtils.isEmpty(device.deviceName)) {
            deviceName = device.deviceName + "(" + device.bluetoothUuid + ")";
            deviceName = BluetoothManager.getDisplayDeviceName(deviceName);
        } else if (device.blueDevice != null && !TextUtils.isEmpty(device.blueDevice.name)) {
            deviceName = device.blueDevice.name + "(" + device.bluetoothUuid + ")";
            deviceName = BluetoothManager.getDisplayDeviceName(deviceName);
        } else {
            deviceName = BluetoothManager.getBluetoothName(device.bluetoothUuid);
            deviceName = BluetoothManager.getDisplayDeviceName(deviceName);
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = "weewa(" + device.bluetoothUuid + ")";
            }
        }

        ((TextView) baseViewHolder.findView(R.id.tv_device_name)).setText(deviceName);

        TextView unbindView = baseViewHolder.findView(R.id.tv_unbind);

        if(device.status == Constants.DEVICE_STATUS_OFFLINE){
            unbindView.setTextColor(getContext().getResources().getColor(R.color.text_gray));
            unbindView.setOnClickListener(null);
        }else{
            unbindView.setTextColor(getContext().getResources().getColor(R.color.orange));
            unbindView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(device.blueDevice!=null) {
                        if (mCallback != null) {
                            mCallback.onBluetoothDeviceClick(device.blueDevice, position);
                        }
                    }
                }
            });
        }
    }

    public ArrayList<BindDeviceBean> getDevices() {
        return mDevices;
    }

    public void setDevices(ArrayList<BindDeviceBean> devices) {
        this.mDevices = devices;
    }

    public BluetoothDeviceAdapterCallback getCallback() {
        return mCallback;
    }

    public void setCallback(BluetoothDeviceAdapterCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void onDestroy() {
    }
}
