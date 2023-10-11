package com.xh.hotme.bean;

import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.List;

@Keep
public class VideoMetaBean implements Serializable {

    /**
     * time : 2023-06-23T10:30:30.000Z
     * address : test address
     * teams : john vs jane
     * competition_name : test name
     * video_width : 1902
     * video_height : 1080
     * thumb : 1.jpg
     * full_video : [{"name":"full_video_01_06251105.mp4","thumb":"1.jpg","duration":1800},{"name":"full_video_02_06251135.mp4","thumb":"1.jpg","duration":1800}]
     * match_highlights : [{"name":"match_highlights_hometeam_01_06251105.mp4","duration":30,"thumb":"1.jpg"},{"name":"match_highlights_hometeam_02_06251135.mp4","duration":30,"thumb":"2.jpg"}]
     * match_playback : [{"name":"match_playback_01_06251100.mp4","thumb":"3.jpg","duration":30},{"name":"match_playback_02_06251145.mp4","duration":30,"thumb":"1.jpg"}]
     * home_goals : [{"name":"home_goals/home_goals_01.mp4","duration":30,"thumb":"home_goals/1.jpg"},{"name":"home_goals/home_goals_02.mp4","duration":30,"thumb":"home_goals/2.jpg"}]
     * away_goals : [{"name":"away_goals/away_goals_01.mp4","duration":30,"thumb":"away_goals/1.jpg"},{"name":"away_goals/away_goals_02.mp4","duration":30,"thumb":"away_goals/2.jpg"}]
     */

    private String time;
    private String address;
    private String teams;
    private String competition_name;
    private int video_width;
    private int video_height;
    private String thumb;
    private List<VideoInfo> full_video;
    private List<VideoInfo> match_highlights;
    private List<VideoInfo> match_playback;
    private List<VideoInfo> home_goals;
    private List<VideoInfo> away_goals;

    @Keep
    public static class VideoInfo implements Serializable {
        /**
         * name : match_playback_01_06251100.mp4
         * thumb : 3.jpg
         * duration : 30
         */

        private String name;
        private String thumb;
        private int duration;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getThumb() {
            return thumb;
        }

        public void setThumb(String thumb) {
            this.thumb = thumb;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTeams() {
        return teams;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }

    public String getCompetition_name() {
        return competition_name;
    }

    public void setCompetition_name(String competition_name) {
        this.competition_name = competition_name;
    }

    public int getVideo_width() {
        return video_width;
    }

    public void setVideo_width(int video_width) {
        this.video_width = video_width;
    }

    public int getVideo_height() {
        return video_height;
    }

    public void setVideo_height(int video_height) {
        this.video_height = video_height;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public List<VideoInfo> getFull_video() {
        return full_video;
    }

    public void setFull_video(List<VideoInfo> full_video) {
        this.full_video = full_video;
    }

    public List<VideoInfo> getMatch_highlights() {
        return match_highlights;
    }

    public void setMatch_highlights(List<VideoInfo> match_highlights) {
        this.match_highlights = match_highlights;
    }

    public List<VideoInfo> getMatch_playback() {
        return match_playback;
    }

    public void setMatch_playback(List<VideoInfo> match_playback) {
        this.match_playback = match_playback;
    }

    public List<VideoInfo> getHome_goals() {
        return home_goals;
    }

    public void setHome_goals(List<VideoInfo> home_goals) {
        this.home_goals = home_goals;
    }

    public List<VideoInfo> getAway_goals() {
        return away_goals;
    }

    public void setAway_goals(List<VideoInfo> away_goals) {
        this.away_goals = away_goals;
    }
}
