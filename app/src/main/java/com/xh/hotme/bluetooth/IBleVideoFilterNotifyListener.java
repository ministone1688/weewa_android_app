package com.xh.hotme.bluetooth;

import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.bean.CameraVideoFilterListBean;

import java.util.List;

public interface IBleVideoFilterNotifyListener {
    void onVideoFilterList(int type, List<CameraVideoDateListBean> data) ;
    void onVideoFilterListFail(String msg) ;

}
