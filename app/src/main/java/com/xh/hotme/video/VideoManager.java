package com.xh.hotme.video;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xh.hotme.HotmeApplication;
import com.xh.hotme.bean.CameraVideoDateListBean;
import com.xh.hotme.bean.CameraVideoFilterListBean;
import com.xh.hotme.bean.CameraVideosBean;
import com.xh.hotme.bean.LocalVideoBean;
import com.xh.hotme.bean.LocalVideoItem;
import com.xh.hotme.bean.VideoMetaBean;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.icloud.item.DateItem;
import com.xh.hotme.icloud.item.ListItem;
import com.xh.hotme.utils.AppFileUtil;
import com.xh.hotme.utils.FileUtil;
import com.xh.hotme.utils.GsonUtils;
import com.xh.hotme.utils.RegExpUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 按照相机的视频存储接口，保存视频到手机本地
 */
public class VideoManager {

    private final static String META_FILE_NAME = "match_meta.data";


    private static VideoManager _instance;

    public static VideoManager getInstance() {
        if (_instance == null) {
            _instance = new VideoManager();
        }
        return _instance;
    }

    public VideoManager() {

    }

    private boolean isVideoDir(File[] videoFiles) {
        if (videoFiles == null) {
            return false;
        }

        for (int j = 0; j < videoFiles.length; j++) {
            String fileName = videoFiles[j].getName();
            if (!TextUtils.isEmpty(fileName) && fileName.equalsIgnoreCase(META_FILE_NAME)) {
                return true;
            }
        }
        return false;
    }


    private VideoMetaBean getVideoMeta(File videoPath) {

        if (videoPath == null || !videoPath.exists()) {
            return null;
        }

        File metaFile = new File(videoPath, META_FILE_NAME);
        if (metaFile == null || !metaFile.exists()) {
            return null;
        }

        String content = AppFileUtil.loadStringFromFile(HotmeApplication.getContext(), metaFile);

        VideoMetaBean videoMetaBean = GsonUtils.GsonToBean(content, VideoMetaBean.class);

        return videoMetaBean;
    }


    /**
     * @param type {@link VideoType }
     * @return
     */
    public List<ListItem> listVideo(VideoType type) {
        List<ListItem> res = new ArrayList<>();

        File videoRoot = AppFileUtil.getVideoDir(HotmeApplication.getContext());
        File[] rootFiles = videoRoot.listFiles();
        if (rootFiles.length > 0) {
            for (int i = 0; i < rootFiles.length; i++) {
                if (rootFiles[i].isDirectory()) {
                    File dateDir = rootFiles[i];
                    String fileName = dateDir.getName();  //日期名称
                    if (RegExpUtil.isDateDir(fileName)) {
                        File[] videoDirs = dateDir.listFiles();
                        for (int j = 0; j < videoDirs.length; j++) {
                            File videoPath = videoDirs[j];
                            if (videoPath.isDirectory()) {   //时间场次
                                File[] videoFiles = videoPath.listFiles();
                                if (isVideoDir(videoFiles)) {
                                    VideoMetaBean videoMetaBean = getVideoMeta(videoPath);
                                    if (videoMetaBean != null) {
                                        List<LocalVideoBean> videoBeanList = getVideoList(videoMetaBean, type, videoPath.getPath());
                                        if (videoBeanList != null && !videoBeanList.isEmpty()) {
                                            res.add(new DateItem(fileName));
                                            res.add(new LocalVideoItem(videoBeanList));
                                        }

                                    }
                                }

                            }
                        }
                    }
                }

            }
        }


        return res;
    }

    public List<LocalVideoBean> getVideoList(VideoMetaBean videoMetaBean, VideoType type, String videoPath) {
        List<VideoMetaBean.VideoInfo> videoInfoList;
        switch (type) {
            case full:
                videoInfoList = videoMetaBean.getFull_video();
                return convertVideoInfo(videoPath, videoInfoList);
            case highlights:

                videoInfoList = videoMetaBean.getMatch_highlights();
                return convertVideoInfo(videoPath, videoInfoList);
            case playback:

                videoInfoList = videoMetaBean.getMatch_playback();
                return convertVideoInfo(videoPath, videoInfoList);

            default:
                break;

        }
        return null;
    }

    private List<LocalVideoBean> convertVideoInfo(String videoPath, List<VideoMetaBean.VideoInfo> videoInfoList) {
        List<LocalVideoBean> icloudVideoBeanList = new ArrayList<>();

        if (videoInfoList == null || videoInfoList.isEmpty()) {
            return icloudVideoBeanList;
        }

        for (VideoMetaBean.VideoInfo videoInfo : videoInfoList) {
            if (videoInfo != null) {
                LocalVideoBean localVideoBean = new LocalVideoBean();

                localVideoBean.clone(videoInfo);
                localVideoBean.setThumb(videoPath + "/" + videoInfo.getThumb());
                localVideoBean.path = videoPath + "/" + videoInfo.getName();
                File videoFile = new File(localVideoBean.path);
                if (videoFile != null && videoFile.exists()) {
                    localVideoBean.size = FileUtil.getFileSize(localVideoBean.path);
                    icloudVideoBeanList.add(localVideoBean);
                }
            }
        }

        return icloudVideoBeanList;
    }


    public static List<CameraVideoDateListBean> convertVideoInfo(List<CameraVideoFilterListBean.CameraVideoGroupBean> videoGroupList) {
        List<CameraVideoDateListBean> videoDateBeanList = new ArrayList<>();

        if (videoGroupList == null || videoGroupList.isEmpty()) {
            return videoDateBeanList;
        }

        for (CameraVideoFilterListBean.CameraVideoGroupBean videoGroupInfo : videoGroupList) {
            if (videoGroupInfo != null) {
                String date = videoGroupInfo.path.split("/")[0];
                CameraVideoDateListBean cameraVideoDateListBean = null;
                boolean isNewDate = true;
                if (videoDateBeanList.isEmpty()) {
                    cameraVideoDateListBean = new CameraVideoDateListBean();
                    cameraVideoDateListBean.date = date;
                    cameraVideoDateListBean.groups = new ArrayList<>();
                } else {
                    for (CameraVideoDateListBean videoDateListBean : videoDateBeanList) {
                        if (videoDateListBean.date.equalsIgnoreCase(date)) {
                            cameraVideoDateListBean = videoDateListBean;
                            isNewDate = false;
                            break;
                        }
                    }
                    if (cameraVideoDateListBean == null) {
                        cameraVideoDateListBean = new CameraVideoDateListBean();
                        cameraVideoDateListBean.date = date;
                        cameraVideoDateListBean.groups = new ArrayList<>();
                    }
                }

                List<CameraVideosBean> videosBeanList = videoGroupInfo.videos;
                if (videosBeanList != null && videosBeanList.size() > 0) {
                    for (CameraVideosBean videosBean : videosBeanList) {
                        CameraVideoDateListBean.CameraVideoDateBean dateBean = new CameraVideoDateListBean.CameraVideoDateBean();
                        dateBean.duration = videosBean.duration;
                        dateBean.link = videosBean.link;
                        dateBean.name = videosBean.name;
                        dateBean.size = videosBean.size;
                        dateBean.type = videosBean.type;
                        dateBean.thumb = videosBean.thumb;
                        dateBean.video_width = videosBean.video_width;
                        dateBean.video_height = videosBean.video_height;
                        dateBean.path = videoGroupInfo.path;
                        cameraVideoDateListBean.groups.add(dateBean);

                    }
                }
                if (isNewDate) {
                    videoDateBeanList.add(cameraVideoDateListBean);
                }

            }
        }

        return videoDateBeanList;
    }


    public static List<CameraVideoDateListBean> loadLocalVideoList(int type) {
        List<CameraVideoDateListBean> videoDateBeanList = new ArrayList<>();

        File videoDir = AppFileUtil.getVideoDir(HotmeApplication.getContext()).getAbsoluteFile();
        File[] rootFiles = videoDir.listFiles();
        if (rootFiles == null || rootFiles.length == 0) {
            return videoDateBeanList;
        }

        for (File cameraFile: rootFiles ) {
            List<CameraVideoDateListBean> cameraVideoDateListBeanList = new ArrayList<>();
            if(cameraFile.isDirectory()){
                String cameraName = cameraFile.getName();
                String gsonString = AppFileUtil.loadStringFromVideoFile(HotmeApplication.getContext(), cameraName,AppFileUtil.CACHE_DEVICE_VIDEO + "_" + type);
                if (TextUtils.isEmpty(gsonString)) {
                    continue;
                }

                try {
                    List<CameraVideoFilterListBean.CameraVideoGroupBean> groupList = new Gson().fromJson(gsonString, new TypeToken<List<CameraVideoFilterListBean.CameraVideoGroupBean>>() {
                    }.getType());

                    // 验证文件存在
                    if (groupList != null && groupList.size() > 0) {
                        cameraVideoDateListBeanList = convertVideoInfo(groupList);
                        Iterator<CameraVideoDateListBean> it = cameraVideoDateListBeanList.iterator();
                        while (it.hasNext()) {
                            CameraVideoDateListBean dateGroupBean = it.next();
                            if (dateGroupBean != null && dateGroupBean.groups != null && dateGroupBean.groups.size() > 0) {
                                List<CameraVideoDateListBean.CameraVideoDateBean> videoDateList = dateGroupBean.groups;
                                Iterator<CameraVideoDateListBean.CameraVideoDateBean> videoDateIte = videoDateList.iterator();
                                while (videoDateIte.hasNext()) {
                                    CameraVideoDateListBean.CameraVideoDateBean dateVideoBean = videoDateIte.next();
                                    File videoPath = new File(cameraFile, dateVideoBean.link);
                                    if (videoPath == null || !videoPath.exists()) {
                                        videoDateIte.remove();
                                    }
                                }
                                if (dateGroupBean.groups.size() == 0) {
                                    it.remove();
                                }

                            } else {
                                it.remove();
                            }
                        }
                    }

                    videoDateBeanList.addAll(cameraVideoDateListBeanList);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }


        return videoDateBeanList;
    }

    public static void saveVideoFilterInfo(int type, List<CameraVideoFilterListBean.CameraVideoGroupBean> list) {

        if (list == null || list.size() == 0) {
            return;
        }

        String cacheVideoFileName = AppFileUtil.CACHE_DEVICE_VIDEO + "_" + type;

        String cameraName = BluetoothManager.getConnectDeviceName();

        File cacheFile = new File(AppFileUtil.getCameraDir(HotmeApplication.getContext(), cameraName), cacheVideoFileName);
        if (cacheFile == null || !cacheFile.exists()) {
            AppFileUtil.saveVideoJson(HotmeApplication.getContext(), GsonUtils.GsonString(list), cacheFile);
            return;
        }

        String content = FileUtil.readContent(cacheFile);
        if (TextUtils.isEmpty(content)) {
            AppFileUtil.saveVideoJson(HotmeApplication.getContext(), GsonUtils.GsonString(list), cacheFile);
            return;
        }
        List<CameraVideoFilterListBean.CameraVideoGroupBean> groupLocalList = new Gson().fromJson(content, new TypeToken<List<CameraVideoFilterListBean.CameraVideoGroupBean>>() {
        }.getType());
        if (groupLocalList == null || groupLocalList.size() == 0) {
            AppFileUtil.saveVideoJson(HotmeApplication.getContext(), GsonUtils.GsonString(list), cacheFile);
            return;
        }

        for (CameraVideoFilterListBean.CameraVideoGroupBean videoGroupBean : list) {
            if (videoGroupBean != null && videoGroupBean.videos != null && videoGroupBean.videos.size() > 0) {
                String path = videoGroupBean.path;
                boolean hasGroupDate = false;
                for (CameraVideoFilterListBean.CameraVideoGroupBean localGroupBean : groupLocalList) {
                    if (localGroupBean.path.equalsIgnoreCase(path)) {
                        List<CameraVideosBean> videosList = videoGroupBean.videos;
                        List<CameraVideosBean> localList = localGroupBean.videos;
                        hasGroupDate = true;
                        for (CameraVideosBean cameraVideosBean : videosList) {
                            String link = cameraVideosBean.link;
                            boolean isExitVideo = false;
                            for (CameraVideosBean localVideoBean : localList) {
                                if (localVideoBean.link.equalsIgnoreCase(link)) {
                                    isExitVideo = true;
                                    break;
                                }
                            }
                            if (!isExitVideo) {
                                localGroupBean.videos.add(cameraVideosBean);
                            }
                        }
                    }
                }

                if (!hasGroupDate) {
                    groupLocalList.add(videoGroupBean);
                }
            }
        }

        AppFileUtil.saveVideoJson(HotmeApplication.getContext(), GsonUtils.GsonString(groupLocalList), cacheFile);
    }

}
