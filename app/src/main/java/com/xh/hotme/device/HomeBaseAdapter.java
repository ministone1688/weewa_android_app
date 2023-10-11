package com.xh.hotme.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import com.xh.hotme.R;
import com.xh.hotme.active.ActiveListActivity;
import com.xh.hotme.bean.BindDeviceBean;
import com.xh.hotme.bean.HomeDataBean;
import com.xh.hotme.bluetooth.BluetoothDeviceAdapterCallback;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.bluetooth.HomeDeviceAdapterCallback;
import com.xh.hotme.bluetooth.IBleScanRequestListener;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.Constants;
import com.xh.hotme.utils.DensityUtil;
import com.xh.hotme.widget.BluetoothDialog;
import com.xh.hotme.widget.SpacesItemDecoration;

import java.util.List;

/**
 * Create by zhaozhihui on 2021/5/12
 **/
public class HomeBaseAdapter extends BaseMultiItemQuickAdapter<HomeDataBean, BaseViewHolder> {
    // context
    private final Context _ctx;

    public static final int TYPE_DEVICE = 0;

    public static final int TYPE_VIDEO = 1;

    public IBleScanRequestListener _scanRequestListener;

    BluetoothDeviceAdapterCallback _callBack;


    public HomeBaseAdapter(Context context, List data) {
        super(data);
        _ctx = context;
        addItemType(TYPE_DEVICE, R.layout.home_fragment_layout_device);
        addItemType(TYPE_VIDEO, R.layout.home_fragment_layout_video);
    }

    public void setScanRequestListener(IBleScanRequestListener l) {
        _scanRequestListener = l;
    }

    public IBleScanRequestListener getScanRequestListener() {
        return _scanRequestListener;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, HomeDataBean homeDataBean) {
        switch (baseViewHolder.getItemViewType()) {
            case TYPE_DEVICE:

                setDeviceView(baseViewHolder, homeDataBean);
                break;
            case TYPE_VIDEO:

                setVideoView(baseViewHolder, homeDataBean);
                break;
        }
    }

    public void setDeviceView(BaseViewHolder helper, HomeDataBean homeDataBean) {


        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager2.setSmoothScrollbarEnabled(false);
        linearLayoutManager2.setAutoMeasureEnabled(true);

        RecyclerView recyclerView0 = helper.getView(R.id.recyclerView);
        recyclerView0.setLayoutManager(linearLayoutManager2);
        recyclerView0.setHasFixedSize(true);
        recyclerView0.setNestedScrollingEnabled(false);
        recyclerView0.setFocusable(false);

        if (recyclerView0.getItemDecorationCount() == 0) {
            SpacesItemDecoration decoration = new SpacesItemDecoration(DensityUtil.dip2px(getContext(), 10) / 2, false);
            recyclerView0.addItemDecoration(decoration);
        }

        if (recyclerView0.getAdapter() == null) {
            HomeDeviceAdapter adapter = new HomeDeviceAdapter(_ctx, homeDataBean.getDataList());
            adapter.setCallback(new HomeDeviceAdapterCallback() {
                @Override
                public void onLeScanStart() {
                    if (_scanRequestListener != null) {
                        _scanRequestListener.onScanStart(false);
                    }
                }

                @Override
                public void onLeScan(Device device, int position) {

                }

                @Override
                public void onLeScanStop(int size) {

                }

                @Override
                public void onBluetoothDeviceClick(BindDeviceBean device, int position) {
                    if (_scanRequestListener != null) {
                        _scanRequestListener.onBluetoothDeviceClick(device.blueDevice, position);
                    }
                }

                @Override
                public void onRemoveDevice(BindDeviceBean device, int position) {
                    if (_scanRequestListener != null) {
                        _scanRequestListener.onRemove(device.blueDevice, position);
                    }
                }

                @Override
                public void openDevice(Device device) {

                }
            });

            View emptyView = LayoutInflater.from(_ctx).inflate(R.layout.home_fragment_device_no, null);
            emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            //添加空视图
            adapter.setEmptyView(emptyView);

            emptyView.findViewById(R.id.btn_device_add).setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    if (!BluetoothManager.mInstance.enableBlueTooth()) {
                        BluetoothDialog dialog = new BluetoothDialog(_ctx);
                        dialog.setOnClickListener(new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    ((Activity) _ctx).startActivityForResult(enableBtIntent, Constants.REQUEST_CODE_SCAN);
                                }

                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    } else {
                        ActiveListActivity.start(_ctx);
                    }

                    return true;
                }
            });

            recyclerView0.setAdapter(adapter);
        } else {
            recyclerView0.getAdapter().notifyDataSetChanged();
        }
    }


    public void setVideoView(BaseViewHolder helper, HomeDataBean homeDataBean) {

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager2.setSmoothScrollbarEnabled(false);
        linearLayoutManager2.setAutoMeasureEnabled(true);

        RecyclerView recyclerView0 = helper.getView(R.id.rv_video_list);
        recyclerView0.setLayoutManager(linearLayoutManager2);
        recyclerView0.setHasFixedSize(true);
        recyclerView0.setNestedScrollingEnabled(false);
        recyclerView0.setFocusable(false);
        recyclerView0.setAdapter(new HomeVideoAdapter(_ctx, homeDataBean.getDataList()));
        if (recyclerView0.getItemDecorationCount() == 0) {
            SpacesItemDecoration decoration = new SpacesItemDecoration(DensityUtil.dip2px(getContext(), 10) / 2, false);
            recyclerView0.addItemDecoration(decoration);
        }
    }

    public BluetoothDeviceAdapterCallback getCallback() {
        return _callBack;
    }

    public void setCallback(BluetoothDeviceAdapterCallback callback) {
        this._callBack = callback;
    }
}