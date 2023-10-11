package com.xh.hotme.wifi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;

import com.xh.hotme.HotmeApplication;
import com.xh.hotme.event.NetWorkForQEvent;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

public class HotmeWiFiManager {

    public static void startWifiSettingPage(Context context){
        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }
    public static void startWifiSettingPage(Activity context, int requestCode){
        context.startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), requestCode);
    }

    public   static void checkWiFiAndGetVideList(Context context) {
        String curSsid = WifiManager.getInstance(context).getConnectWifiSsid();
        if (TextUtils.equals(curSsid, ConnectLogic.getInstance().getSsid())) {
//            getVideoList();
            ConnectLogic.getInstance().setConnectIng(true);
            EventBus.getDefault().post(new NetWorkForQEvent(NetWorkForQEvent.AVAILABLE));
        } else {
            ToastUtil.s(HotmeApplication.getContext(), "请连接WiFi热点\""+ ConnectLogic.getInstance().getSsid()+"\"");
            new Handler().postDelayed(() -> {
                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }, 1500);

//            wifiReceiver = new WifiReceiver();
//            IntentFilter intentFilter = new IntentFilter();
//            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//            registerReceiver(wifiReceiver, intentFilter);
        }
    }
}
