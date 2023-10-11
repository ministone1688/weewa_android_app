package com.xh.hotme.bean;

import java.io.Serializable;
import java.util.List;

/**
 * date   : 2023/6/27
 * desc   :
 * c	string	video_detail_list	固定值
 * videos	Arrays[Video]		视频文件列表
 * Video.type	int	0	[0, 1, 2,3]0 全场视频，1精彩集锦
 * 2 精彩回放 3主队射门，4客队射门
 * Video.name	string	全场视频_01	视频名称
 * Video.duration	int	30	视频时长，单位秒
 * Video.link	string	/2023/桃源_向华v恒大	视频文件路径
 * Video.video_width	int	1902	视频分辨率 宽
 * Video.video_height	int	1080	视频分辨率 高
 */
public class CameraVideoFilterListBean implements Serializable {

    public String c;
    public List<CameraVideoGroupBean> groups;


    /**
     * {
     *             path: "20220222/1010",
     *             videos: [
     *                 这里面和video_detail_list的video一样
     *             ]
     *         }
     */
    public static class CameraVideoGroupBean implements Serializable {
        public String path;
        public List<CameraVideosBean> videos;
    }

}
