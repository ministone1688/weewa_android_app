package com.xh.hotme.bean;

import java.io.Serializable;
import java.util.List;

/**
 * {
 *             path: "20220222",
 *             videos: [
 *                 这里面和video_detail_list的video一样
 *             ]
 *         }
 */
public class CameraVideoDateListBean implements Serializable {

    public String date;
    public List<CameraVideoDateBean> groups;

    public static class CameraVideoDateBean extends CameraVideosBean {
        public String path;
    }
}
