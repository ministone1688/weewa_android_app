package com.xh.hotme.utils;

import android.content.Context;
import android.content.Intent;

import com.xh.hotme.bean.DeviceInfo;
import com.xh.hotme.bean.NetworkBean;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.camera.IjkPlayerActivity;
import com.xh.hotme.camera.PlayerActivity;

public class PlayUtil {
    public static void startPlay(Context context, DeviceInfo deviceInfo, Device device) {
        PlayerActivity.start(context, deviceInfo, device);
//        IjkPlayerActivity.start(context, deviceInfo, device);

    }

    public static void startPlay(Context context, DeviceInfo deviceInfo, Device device, NetworkBean network ) {
        PlayerActivity.start(context, deviceInfo, device, network);
//        IjkPlayerActivity.start(context, deviceInfo, device);

    }
}
