package com.xh.hotme.device;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xh.hotme.R;
import com.xh.hotme.bean.BindDeviceBean;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.HomeDeviceAdapterCallback;
import com.xh.hotme.utils.BaseAppUtil;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.widget.ModalDialog;

import java.util.List;

public class HomeDeviceAdapter extends BaseQuickAdapter<BindDeviceBean, BaseViewHolder> {

    private HomeDeviceAdapterCallback mCallback;


    ModalDialog mRemovedialog;

    Context _ctx;

    public HomeDeviceAdapter(Context context, List data) {

        super(R.layout.home_fragment_device_list_item, data);
        _ctx = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, BindDeviceBean device) {

        int position = helper.getPosition();

        FrameLayout rootView = helper.getView(R.id.home_fragment_device_list_item_root);

        int left_width = BaseAppUtil.getDeviceWidth(getContext()) - 3 * DensityUtil.dip2px(getContext(), 20);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootView.getLayoutParams();
        params.width = left_width;
        rootView.setLayoutParams(params);

        String deviceName = "";
        if (!TextUtils.isEmpty(device.deviceName)) {
            deviceName = device.deviceName + "(" + device.bluetoothUuid + ")";
            deviceName = BluetoothManager.getDisplayDeviceName(deviceName);
        } else if (device.blueDevice != null && !TextUtils.isEmpty(device.blueDevice.name)) {
            deviceName = device.blueDevice.name + "(" + device.bluetoothUuid + ")";
            deviceName = BluetoothManager.getDisplayDeviceName(deviceName);
        } else {
            deviceName = "weewa(" + device.bluetoothUuid + ")";
        }
        ((TextView) helper.findView(R.id.tv_device_name)).setText(deviceName);

        TextView statusView = (TextView) helper.getView(R.id.tv_device_status);
        if (device.status == Constants.DEVICE_STATUS_CONNECT || device.status == Constants.DEVICE_STATUS_ONLINE) {
            statusView.setText(getContext().getString(R.string.device_online));
            statusView.setTextColor(getContext().getResources().getColor(R.color.text_green));
        } else {
            statusView.setText(getContext().getString(R.string.device_offline));
            statusView.setTextColor(getContext().getResources().getColor(R.color.text_gray));
        }
        TextView disconnectView = (TextView) helper.getView(R.id.tv_disconnect);

        Button connectBtn = helper.findView(R.id.btn_device_connect);
        if (device.status == Constants.DEVICE_STATUS_CONNECT) {
            connectBtn.setText(getContext().getString(R.string.bluetooth_device_list_connected));
            connectBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    if (mCallback != null) {
                        mCallback.onBluetoothDeviceClick(device, position);
                    }
                    return true;
                }
            });

            if (device.blueDevice != null) {

                disconnectView.setVisibility(View.VISIBLE);
                disconnectView.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                    @Override
                    public boolean onClicked() {
                        ModalDialog dialog = new ModalDialog(_ctx);

                        dialog.setMessage(_ctx.getString(R.string.confirm_device_disconnect));
                        dialog.setLeftButton(_ctx.getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
                            @Override
                            public boolean onClicked() {
                                return true;
                            }
                        });
                        dialog.setRightButton(_ctx.getString(R.string.confirm), new ClickGuard.GuardedOnClickListener() {
                            @Override
                            public boolean onClicked() {

                                BluetoothManager.mInstance.disconnectBle(device.blueDevice);
                                device.status = Constants.DEVICE_STATUS_ONLINE;

                                notifyDataSetChanged();

                                return true;
                            }
                        });

                        dialog.show();

                        return true;
                    }
                });
            }
        } else if (device.status == Constants.DEVICE_STATUS_ONLINE) {
            disconnectView.setVisibility(View.GONE);
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
        } else {
            disconnectView.setVisibility(View.GONE);
            connectBtn.setText(getContext().getString(R.string.bluetooth_device_list_connect));
            connectBtn.setOnClickListener(null);
        }

        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                setRemoveButtonStatus(helper, device, position, true);

                return true;
            }
        });

        resetButtonStatus(helper);
    }

    private void resetButtonStatus(BaseViewHolder helper) {
        ImageView removeView = (ImageView) helper.getView(R.id.iv_device_remove);
        removeView.setOnClickListener(null);
        if (removeView.getVisibility() == View.VISIBLE) {
            removeView.setVisibility(View.GONE);
        }
    }

    private void setRemoveButtonStatus(BaseViewHolder helper, BindDeviceBean device, int position, boolean isShow) {
        ImageView removeView = (ImageView) helper.getView(R.id.iv_device_remove);
        if (isShow) {
            removeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mRemovedialog != null && mRemovedialog.isShowing()) {
                        mRemovedialog.dismiss();
                        mRemovedialog = null;
                    }

                    mRemovedialog = new ModalDialog(_ctx);
                    mRemovedialog.setMessage(_ctx.getString(R.string.remove_device_dialog_title));
                    mRemovedialog.setLeftButton(_ctx.getString(R.string.remove_device_dialog_cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRemovedialog.dismiss();
                            resetButtonStatus(helper);
                        }
                    });
                    mRemovedialog.setRightButton(_ctx.getString(R.string.remove_device_dialog_confirm), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mCallback != null) {
                                mCallback.onRemoveDevice(device, position);
                            }
                            mRemovedialog.dismiss();
                        }
                    });

                    mRemovedialog.show();
                }
            });
            if (removeView.getVisibility() == View.GONE) {
                removeView.setVisibility(View.VISIBLE);
            }
        } else {
            removeView.setOnClickListener(null);
            if (removeView.getVisibility() == View.VISIBLE) {
                removeView.setVisibility(View.GONE);
            }
        }
    }

    public HomeDeviceAdapterCallback getCallback() {
        return mCallback;
    }

    public void setCallback(HomeDeviceAdapterCallback callback) {
        this.mCallback = callback;
    }
}
