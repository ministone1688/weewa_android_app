package com.xh.hotme.active;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class PowerDeviceAdapter extends BaseQuickAdapter<Device, BaseViewHolder> {
    private final static String TAG = "PowerDeviceAdapter";
    private ArrayList<Device> mDevices;
    private BluetoothDeviceAdapterCallback mCallback;


    Context _context;

    public PowerDeviceAdapter(Context context, List data) {
        super(R.layout.my_device_list_item, data);
        _context = context;
    }


    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, Device device) {
        int position = baseViewHolder.getPosition();

        String _deviceName = BluetoothManager.getDisplayDeviceName(device);

        ((TextView) baseViewHolder.findView(R.id.tv_device_name)).setText(_deviceName + "(" + device.getAddress() + ")");
        ((TextView) baseViewHolder.findView(R.id.tv_unbind)).setVisibility(View.GONE);

        baseViewHolder.findView(R.id.device_list_item_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onBluetoothDeviceClick(device, position);
                }
            }
        });


    }

    public ArrayList<Device> getDevices() {
        return mDevices;
    }

    public void setDevices(ArrayList<Device> devices) {
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
}
