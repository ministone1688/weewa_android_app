package com.xh.hotme.bean;

import java.util.List;

public class VideoCategoryBean {
    public String date;
    public List<VideoBean> videoList;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<VideoBean> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<VideoBean> video) {
        this.videoList = video;
    }
}
