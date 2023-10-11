package com.xh.hotme.bluetooth;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.camera.PlayerActivity;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.widget.ModalDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GJK on 2018/11/9.
 */

public class BluetoothDeviceAdapter extends BaseQuickAdapter<Device, BaseViewHolder> {
    private final static String TAG = "BluetoothDeviceAdapter";
    private ArrayList<Device> mDevices;
    private BluetoothDeviceAdapterCallback mCallback;

    Context _context;

    public BluetoothDeviceAdapter(Context context, List data) {
        super(R.layout.bluetooth_device_list_item, data);
        _context = context;
    }


    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, Device device) {

        String deviceName = "";
        if (!TextUtils.isEmpty(device.getName())) {
            deviceName = device.getName() + "(" + device.getAddress() + ")";
            deviceName = BluetoothManager.getDisplayDeviceName(deviceName);
        }  else {
            deviceName = "weewa(" + device.getAddress() + ")";
        }
        ((TextView) baseViewHolder.findView(R.id.tv_device_name)).setText(deviceName);

        int position = baseViewHolder.getPosition();

        TextView disconnectBtn = (TextView) baseViewHolder.getView(R.id.tv_disconnect);

        TextView statusView = (TextView) baseViewHolder.getView(R.id.tv_device_status);
        if (device.status == Constants.DEVICE_STATUS_OFFLINE) {
            statusView.setText(getContext().getString(R.string.device_offline));
            statusView.setTextColor(getContext().getResources().getColor(R.color.text_gray));
        } else {
            statusView.setText(getContext().getString(R.string.device_online));
            statusView.setTextColor(getContext().getResources().getColor(R.color.text_green));
        }

        Button connectBtn = baseViewHolder.findView(R.id.btn_device_connect);
        if (device.status == Constants.DEVICE_STATUS_CONNECT) {
            connectBtn.setText(getContext().getString(R.string.bluetooth_device_list_connected));
            connectBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    if (mCallback != null) {
                        mCallback.openDevice(device);
                    }
                    return true;
                }
            });

            disconnectBtn.setVisibility(View.VISIBLE);
            disconnectBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {

                    ModalDialog dialog = new ModalDialog(_context);

                    dialog.setMessage(_context.getString(R.string.confirm_device_disconnect));
                    dialog.setLeftButton(_context.getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
                        @Override
                        public boolean onClicked() {
                            return true;
                        }
                    });
                    dialog.setRightButton(_context.getString(R.string.confirm), new ClickGuard.GuardedOnClickListener() {
                        @Override
                        public boolean onClicked() {

                            BluetoothManager.mInstance.disconnectBle(device);

                            device.status = Constants.DEVICE_STATUS_ONLINE;

                            notifyDataSetChanged();

                            return true;
                        }
                    });

                    dialog.show();
                    return true;
                }
            });

        } else {
            connectBtn.setText(getContext().getString(R.string.bluetooth_device_list_connect));
            connectBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    if (mCallback != null) {
                        mCallback.onBluetoothDeviceClick(device, position);
                    }
                    return true;
                }
            });

            disconnectBtn.setVisibility(View.GONE);
            disconnectBtn.setOnClickListener(null);
        }


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
