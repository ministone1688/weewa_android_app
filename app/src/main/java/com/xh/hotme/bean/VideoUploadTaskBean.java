package com.xh.hotme.bean;

public class VideoUploadTaskBean {
    public String videoName;  //视频名字
    public long duration;  //视频时长
    public long size; //视频大小
    public int classify; //视频分类
    public String deviceMac;  //设备mac地址
    public String court;   //球场编码
    public String site;   //场地编码
    public String shootTime;   //拍摄时间
    public String competitionName;   //拍摄时间

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getClassify() {
        return classify;
    }

    public void setClassify(int classify) {
        this.classify = classify;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String siteId) {
        this.site = siteId;
    }

    public String getShootTime() {
        return shootTime;
    }

    public void setShootTime(String shootTime) {
        this.shootTime = shootTime;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }
}
