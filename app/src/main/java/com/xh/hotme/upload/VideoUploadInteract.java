package com.xh.hotme.upload;

import android.content.Context;

import com.google.gson.Gson;
import com.xh.hotme.bean.VideoUploadProgressBean;
import com.xh.hotme.bean.VideoUploadTaskBean;
import com.xh.hotme.bean.VideoUploadTaskResultBean;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.OkHttpUtil;

public class VideoUploadInteract {

    public static void preUpload(Context context, VideoUploadTaskBean videoBean, IVideoPreUploadListener listener) {

        if (videoBean == null) {
            return;
        }

        OkHttpUtil.postData(SdkApi.preUpload(), new Gson().toJson(videoBean), null, new OkHttpCallbackDecode<VideoUploadTaskResultBean>() {
            @Override
            public void onDataSuccess(VideoUploadTaskResultBean data) {
                if (data != null) {
                    AppTrace.d("LoginInteract", "token = " + data.videoId);

                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            //接口回调通知
                            if (listener != null) {
                                listener.onSuccess(data);
                            }
                        }
                    });

                } else {
                    MainHandler.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFail("-1", "data is null");
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String code, String message) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFail(code, message);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            }
        });
    }

    public static void uploadStatus(Context context, String videoId, int status, double speed, long progress, String path, ICommonListener listener) {

        VideoUploadProgressBean progressBean = new VideoUploadProgressBean();
        progressBean.uploadStatus = status;
        progressBean.uploadSpeed = speed;
        progressBean.videoId = videoId;
        progressBean.uploadProgress = progress;
        progressBean.path = path;

        OkHttpUtil.postData(SdkApi.uploadProgress(), new Gson().toJson(progressBean), null, new OkHttpCallbackDecode<Object>() {
            @Override
            public void onDataSuccess(Object data) {

                AppTrace.i("uploadStatus", "ok");

                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        //接口回调通知
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    }
                });
            }

            @Override
            public void onFailure(String code, String message) {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFail(code, message);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                MainHandler.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onFinish();
                        }
                    }
                });
            }
        });
    }


    public interface IVideoPreUploadListener {
        void onSuccess(VideoUploadTaskResultBean data);

        void onFail(String code, String message);

        void onFinish();
    }
}
