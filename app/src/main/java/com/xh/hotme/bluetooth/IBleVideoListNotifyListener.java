package com.xh.hotme.bluetooth;

import com.xh.hotme.bean.CameraVideoListBean;

import java.util.List;

public interface IBleVideoListNotifyListener {
    void onVideoList(List<CameraVideoListBean.VideosBean> data) ;
    void onVideoListFail(String msg) ;

}
