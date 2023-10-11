package com.xh.hotme.bean;

public class VideoUploadProgressBean {
    public String videoId;  //视频名字
    public int uploadStatus;  //上传状态（1、开始上传；2、上传中；3、暂停上传；4、上传成功；5、上传失败）
    public double uploadSpeed; //上传速度
    public long uploadProgress; //上传进度
    public String path; //视频路径

}
