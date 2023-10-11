package com.xh.hotme.bean;

import com.xh.hotme.icloud.item.ListItem;

import java.util.List;


public class LocalVideoItem extends ListItem{

    private List<LocalVideoBean> videoList;

    public List<LocalVideoBean> getVideoList() {
        return videoList;
    }

    public void getVideoList(List<LocalVideoBean> list) {
        videoList = list;
    }

    public LocalVideoItem(List<LocalVideoBean> list){
       this.videoList = list;
    }


    @Override
    public int getItemType() {
        return TYPE_LOCAL_VIDEO_LIST;
    }

    @Override
    public int getType() {
        return TYPE_LOCAL_VIDEO_LIST;
    }
}
