package com.xh.hotme.event;

/**
 * date   : 2023/6/26
 * desc   :热点网络回调
 */
public class NetWorkForQEvent {

    public int type;

    public static final int AVAILABLE = 1;
    public static final int UNAVAILABLE = 2;
    public static final int NET_CAPABILITY_INTERNET = 3;
    public static final int NET_CAPABILITY_VALIDATED = 4;
    public static final int UNCONNECT = 5;

    public NetWorkForQEvent(int type) {
        this.type = type;
    }
}
