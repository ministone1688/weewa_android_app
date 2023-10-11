package com.xh.hotme.http;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;


import com.weewa.lib.IWeewaDelegate;
import com.weewa.lib.WeewaLib;
import com.xh.hotme.HotmeApplication;
import com.xh.hotme.bean.CameraVideosBean;
import com.xh.hotme.event.DownloadEvent;
import com.xh.hotme.event.NetWorkForQEvent;
import com.xh.hotme.softap.WifiManager;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.GsonUtils;
import com.xh.hotme.utils.SpUtil;
import com.xh.hotme.utils.ToastCustom;
import com.xh.hotme.widget.ModalDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * date   : 2023/6/21
 * desc   :连接管理类
 */
public class ConnectLogic {
    String TAG = "ConnectLogic";
    static ConnectLogic logic;

    private String ssid = WifiManager.WEEWA_SOFTAP_NAME;
    private String ssidPwd = "";
    private String connectIp = HotmeApplication.isTest ? "192.168.1.17" : "10.201.126.1";
    //        private String connectIp = "192.168.1.17";
    private String connectPort = "38888";
    private String connectBaseurl = "http://" + connectIp + ":" + connectPort;
    private boolean isConnectIng;//wifi连接状态
    private int connectSessionId = -1;
    private static String basePath = "/data/weewa/video";
    private List<String> files = new ArrayList<>();
    private SessionStatus sessionState = SessionStatus.WAITE;//连接传输状态
    double sessionProgress;//连接传输进度
    double sessionProgressLast;//上次连接传输进度
    long rateSize;//速率
    long fileSize;//文件大小
    final int LOOPERPROGRESS = 101;//进度刷新
    long lastTime = 0;//计算速率的时间
//    CameraVideosBean selectData;

    long startDownTime;//开始下载的时间

    public interface ICallback {
        void onInfo(SessionStatus sessionStatus, double progress, long allSize, long rateSize);
    }

    private ICallback callback;

    //线程
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == LOOPERPROGRESS) {
//                if (System.currentTimeMillis() - lastTime > 1000) {
//                    lastTime = System.currentTimeMillis();
//                }
                try {
                    long time = System.currentTimeMillis() - lastTime;
                    if (sessionProgress >= sessionProgressLast) {
                        rateSize = (long) (fileSize * (sessionProgress - sessionProgressLast) / time * 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                lastTime = System.currentTimeMillis();
                sessionProgressLast = sessionProgress;
                if (callback != null) {
                    callback.onInfo(sessionState, sessionProgress, fileSize, rateSize);
                }
//                EventBus.getDefault().post(new SessionEvent(sessionState, sessionProgress, fileSize, rateSize));
            }
        }
    };

    public enum SessionStatus {
        WAITE,
        CONNECT,
        START,
        DOWNLOADING,
        UNCONNECT,
        Ended
    }

    List<CameraVideosBean> datasSelect = new ArrayList();

    private final int minRefreshTime = 500;
    private long lastRefreshTime;

    public ConnectLogic() {

        try {
            WeewaLib.Companion.shared().setDelegate(new IWeewaDelegate() {
                @Override
                public void sessionWaiting(int id) {
                    AppTrace.d(TAG, "sessionWaiting=" + id);
                    if (connectSessionId == id) {
                        sessionState = SessionStatus.WAITE;
                        mHandler.removeMessages(LOOPERPROGRESS);
                        mHandler.sendEmptyMessage(LOOPERPROGRESS);
                    }

                }

                @Override
                public void sessionConnecting(int id) {
                    AppTrace.d(TAG, "sessionConnecting=" + id);
                    if (connectSessionId == id) {
                        sessionState = SessionStatus.CONNECT;
                    }
                }

                @Override
                public void sessionStarted(int id) {
                    AppTrace.d(TAG, "sessionStarted=" + id);
                    if (connectSessionId == id) {
                        sessionState = SessionStatus.START;
                        sessionProgress = 0;
                        sessionProgressLast = 0;
                        startDownTime=System.currentTimeMillis();
                        mHandler.removeMessages(LOOPERPROGRESS);
                        mHandler.sendEmptyMessage(LOOPERPROGRESS);
                    }
                }

                @Override
                public void sessionProgress(int id, double progress) {
                    AppTrace.d(TAG, "sessionProgress=" + id + "=progress=" + progress);

                    if (connectSessionId == id) {
                        if (sessionState != SessionStatus.Ended) {
                            long curT = SystemClock.elapsedRealtime();
                            if (curT - lastRefreshTime < minRefreshTime) {
                                return;
                            }
                            lastRefreshTime = curT;
                            sessionState = SessionStatus.DOWNLOADING;
                            sessionProgress = progress;
                            mHandler.removeMessages(LOOPERPROGRESS);
                            mHandler.sendEmptyMessage(LOOPERPROGRESS);
                        }

                    }

                }

                @Override
                public void sessionEnded(int id) {
                    AppTrace.d(TAG, "sessionEnded=" + id);
                    if (connectSessionId == id) {
                        sessionState = SessionStatus.Ended;
                        mHandler.removeMessages(LOOPERPROGRESS);
                        mHandler.sendEmptyMessage(LOOPERPROGRESS);

                        EventBus.getDefault().post(new DownloadEvent());

//                        try {
//                            List<CameraVideosBean> lists = new ArrayList<>();
//                            String gson = SpUtil.getInstance(HotmeApplication.getContext()).getString(selectData.path);
//                            if (!TextUtils.isEmpty(gson)) {
//                                lists.addAll(GsonUtils.fromJsonList(gson, CameraVideoDetailListBean.VideosBean.class));
//                            }
//                            if (lists!=null && !lists.isEmpty()) {
//                                for (int j = 0; j < datasSelect.size(); j++) {
//                                    for (int i = 0; i < lists.size(); i++) {
//                                        try {
//                                            if (lists.get(i).link.equals(datasSelect.get(j).link)) {
//                                                continue;
//                                            }
//                                            lists.add(datasSelect.get(j));
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//
//                                    }
//                                }
//                            } else {
//                                lists.addAll(datasSelect);
//                            }
//
//
//                            SpUtil.getInstance(HotmeApplication.getContext()).saveString(selectData.path, GsonUtils.GsonString(lists));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }

                    }
                }

                @Override
                public void sessionError(int id, @NonNull String errMsg) {
                    AppTrace.d(TAG, "sessionError=" + errMsg);
                    if (connectSessionId == id) {
                        sessionState = SessionStatus.UNCONNECT;
                        mHandler.removeMessages(LOOPERPROGRESS);
                        mHandler.sendEmptyMessage(LOOPERPROGRESS);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public long getStartDownTime() {
        return startDownTime;
    }

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    public static String getBasePath() {
        return basePath;
    }

    public static void setBasePath(String path) {
        basePath = path;
    }

    public static synchronized ConnectLogic getInstance() {
        if (logic == null) {
            synchronized (ConnectLogic.class) {
                logic = new ConnectLogic();
            }

        }
        return logic;
    }

    public String getConnectPort() {
        return connectPort;
    }

    public void setConnectPort(String connectPort) {
        this.connectPort = connectPort;
    }

    public String getConnectBaseurl() {
        return connectBaseurl;
    }

    public void setConnectBaseurl(String connectBaseurl) {
        this.connectBaseurl = connectBaseurl;
    }

    public List<CameraVideosBean> getDatasSelect() {
        return datasSelect;
    }

    /**
     * 终止传输文件
     */
    public void unReceive() {
        WeewaLib.Companion.shared().stop(connectSessionId);
        sessionState = SessionStatus.UNCONNECT;
    }


    public void addFiles(List<CameraVideosBean> datasSelect1) {
        this.datasSelect.clear();
        this.datasSelect.addAll(datasSelect1);
        fileSize = 0;
        files.clear();
        for (CameraVideosBean cameraVideoBean : datasSelect1) {
            files.add(cameraVideoBean.link);
            fileSize += cameraVideoBean.size;
        }
    }

    public void addFiles(ArrayList<CameraVideosBean> datasSelect1, List<String> files1) {
//        fileSize = afileSize;
        files.clear();
        files.addAll(files1);
		this.datasSelect.clear();
        this.datasSelect.addAll(datasSelect1);


    }

//    public void addFiles(CameraVideosBean myData, long afileSize, ArrayList<CameraVideosBean> datasSelect1, List<String> files1) {
//        selectData = myData;
//        fileSize = afileSize;
//        files = (files1);
//        this.datasSelect = (datasSelect1);
//
//    }

    /**
     * 传输文件
     */
    public void toReceive() {
        if (files == null || files.isEmpty()) {
            return;
        }
        connectSessionId = WeewaLib.Companion.shared().receive(connectIp, "", files, false);
        AppTrace.d(TAG, "toReceive=" + connectSessionId);
        try {
//            List<CameraVideosBean> lists = new ArrayList<>();
//            String gson = SpUtil.getInstance(HotmeApplication.getContext()).getString(SpUtil.PRFS_VIDEO_LIST);
//            if (!TextUtils.isEmpty(gson)) {
//                lists.addAll(GsonUtils.fromJsonList(gson, CameraVideosBean.class));
//            }
//            if (lists!=null && !lists.isEmpty()) {
//
//                for (int i = 0; i < lists.size(); i++) {
//                    try {
//                        if (selectData.path.equals(lists.get(i).path)) {
//                            continue;
//                        }
//                        lists.add(selectData);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                lists.add(selectData);
//            }
//            SpUtil.getInstance(HotmeApplication.getContext()).saveString(SpUtil.PRFS_VIDEO_LIST, GsonUtils.GsonString(lists));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectIng() {
        return isConnectIng;
    }

    public void setConnectIng(boolean connectIng) {
        isConnectIng = connectIng;
    }

    public boolean isDownLoading() {
        return sessionState == SessionStatus.DOWNLOADING;
    }

    public double getSessionProgress() {
        return sessionProgress;
    }

    public long getRateSize() {
        return rateSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public SessionStatus getSessionState() {
        return sessionState;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSsidPwd() {
        return ssidPwd;
    }

    public void setSsidPwd(String ssidPwd) {
        this.ssidPwd = ssidPwd;
    }

    public String getConnectIp() {
        return connectIp;
    }

    public void setConnectIp(String connectIp) {
        this.connectIp = connectIp;
    }

    /**
     * 连接异常
     */
    ModalDialog errorDialog;

    public void showConnectError(Context context) {
        if (null == errorDialog) {
            if (unConnectDialog != null) {
                unConnectDialog.dismiss();
            }
            errorDialog = new ModalDialog(context);
            errorDialog.setRightButton("重新连接", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorDialog = null;
                    checkWiFiAndGetVideList(context);
                }
            });
            errorDialog.setLeftButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    errorDialog = null;
                }
            });
            errorDialog.show();
        }

    }

    ModalDialog unConnectDialog;

    /**
     * 断开链接
     *
     * @param context
     */
    public void unConnectDialog(Context context) {
        if (null == unConnectDialog) {
            unConnectDialog = new ModalDialog(context);
            unConnectDialog.setRightButton("断开", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unConnectDialog = null;
//                            WifiUtils.getInstance(context).disconnectNetWork();
                }
            });
            unConnectDialog.setLeftButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unConnectDialog = null;
//                            WifiUtils.getInstance(context).disconnectNetWork();
                }
            });
            unConnectDialog.setMessage("确定断开连接");

            unConnectDialog.show();
        }

    }

    public static void checkWiFiAndGetVideList(Context context) {
        String curSsid = WifiManager.getInstance(context).getConnectWifiSsid();
        if (TextUtils.equals(curSsid, ConnectLogic.getInstance().getSsid())) {
//            getVideoList();
            ConnectLogic.getInstance().setConnectIng(true);
            EventBus.getDefault().post(new NetWorkForQEvent(NetWorkForQEvent.AVAILABLE));
        } else {
            ToastCustom.showCustomToast(HotmeApplication.getContext(), "请连接WiFi热点\"" + ConnectLogic.getInstance().getSsid() + "\"");
            new Handler().postDelayed(() -> {
                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }, 1500);
        }
    }
}
