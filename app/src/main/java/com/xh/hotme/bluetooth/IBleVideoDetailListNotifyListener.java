package com.xh.hotme.bluetooth;

import com.xh.hotme.bean.CameraVideoDetailListBean;
import com.xh.hotme.bean.CameraVideosBean;

import java.util.List;

public interface IBleVideoDetailListNotifyListener {
    void onVideoDetailList(List<CameraVideoDetailListBean.VideosBean> data) ;
    void onVideoDetailListFail(String msg) ;

}
