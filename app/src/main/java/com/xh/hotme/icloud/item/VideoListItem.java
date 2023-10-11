package com.xh.hotme.icloud.item;

import com.xh.hotme.bean.IcloudVideoBean;

import java.util.List;

public class VideoListItem extends ListItem{
    private List<IcloudVideoBean> videoList;

    public List<IcloudVideoBean> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<IcloudVideoBean> videoList) {
        this.videoList = videoList;
    }

    @Override
    public int getType() {
        return TYPE_VIDEO_LIST;
    }

    @Override
    public int getItemType() {
        return TYPE_VIDEO_LIST;
    }

    public VideoListItem(List<IcloudVideoBean> dataList){
        this.videoList = dataList;
    }
}
