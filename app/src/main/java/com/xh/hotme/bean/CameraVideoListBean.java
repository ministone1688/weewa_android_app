package com.xh.hotme.bean;

import java.io.Serializable;
import java.util.List;

/**
 * date   : 2023/6/27
 * desc   :
 * c	string	videolist	固定值
 * videos	Array[Video]		视频列表
 * Video.time	string	2023-06-26 10:30	视频时间
 * Video.address	string	桃源球场	拍摄地点
 * Video.teams	string	向华vs恒大	比赛队伍
 * Video.competition_name	string	向华vs恒大	比赛名称
 * Video.thumb	string	/20230625/1030/桃源_向华v恒大_thumb.jpg	视频缩略图
 * Video.path	string	/20230625/1030	视频文件目录
 * Video.video_width	int	1902	视频分辨率 宽
 * Video.video_height	int	1080	视频分辨率 高
 * Video。full_video_number	int	4	全场视频数
 * Video。highlights_number	int	3	集锦视频数
 * Video。playback_number	int	3	回放视频数
 * Video.home_goals	int	2	主队进球视频数
 * Video.away_goals	int	1	客队进球视频数
 */
public class CameraVideoListBean implements Serializable {


    public String c;
    public List<CameraVideoListBean.VideosBean> videos;

    public static class VideosBean implements Serializable{
        public String time;
        public String address;
        public String teams;
        public String competition_name;
        public String path;
        public String link;
        public String thumb;
        public String video_width;
        public String video_height;
        public String full_video_number;
        public String highlights_number;
        public String playback_number;
        public String home_goals;
        public String away_goals;
    }

}
