package com.xh.hotme.camera;

import java.io.Serializable;

public class RecordInfoBean implements Serializable {
    public String place;
    public String name;   //录制名称
    public long maxRate;  // 最大码率
    public int width;  // 视频宽度
    public int height;  // 视频高度
    public String author;  // 拍摄作者
    public long time;  //拍摄时间

    public String city="";  //城市
    public String motionType="";  //运动类型
    public String hostTeam="";  //主队名称
    public String guestTeam="";  //客队名称
    public String placeType="";  //场地类型

    public RecordInfoBean(String name, long rate){
        this.name = name;
        this.maxRate = rate;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(long maxRate) {
        this.maxRate = maxRate;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
