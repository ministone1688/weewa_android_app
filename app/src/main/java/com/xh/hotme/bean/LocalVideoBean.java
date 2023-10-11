package com.xh.hotme.bean;

public class LocalVideoBean extends VideoMetaBean.VideoInfo {
    public long size;
    public String path;

    public LocalVideoBean clone(VideoMetaBean.VideoInfo videoInfo){

        LocalVideoBean localVideoBean = new LocalVideoBean();
        localVideoBean.setName(videoInfo.getName());
        localVideoBean.setDuration(videoInfo.getDuration());
        localVideoBean.setThumb(videoInfo.getThumb());

        return localVideoBean;
    }
}
