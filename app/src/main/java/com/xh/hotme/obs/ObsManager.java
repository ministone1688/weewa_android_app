package com.xh.hotme.obs;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.CompleteMultipartUploadRequest;
import com.obs.services.model.CompleteMultipartUploadResult;
import com.obs.services.model.InitiateMultipartUploadRequest;
import com.obs.services.model.InitiateMultipartUploadResult;
import com.obs.services.model.ListPartsRequest;
import com.obs.services.model.ListPartsResult;
import com.obs.services.model.Multipart;
import com.obs.services.model.PartEtag;
import com.obs.services.model.ProgressListener;
import com.obs.services.model.ProgressStatus;
import com.obs.services.model.UploadFileRequest;
import com.obs.services.model.UploadPartRequest;
import com.obs.services.model.UploadPartResult;
import com.xh.hotme.listener.IProgressListener;
import com.xh.hotme.upload.VideoUploadInteract;

public class ObsManager {

    public final static String TAG = "ObsManager";
    public final static String endPoint = "obs.cn-south-1.myhuaweicloud.com";
    public final static String ak = "XKMPOJ0JTV7GLZIROLNV";
    public final static String sk = "UQr5eGsyqv9jCnbxItyuC345pZOIvzzeAZcZoki9";

    public final static String bucketName = "weewa-3";
    public final static String objectKey = "objectname";


    public final static int STATUS_STARTING = 1;
    public final static int STATUS_PROCESSING = 2;
    public final static int STATUS_STOP = 3;
    public final static int STATUS_COMPLETE = 4;
    public final static int STATUS_FAIL = 4;

    public static void upload(String file, String objectName) {

        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);

        // 创建ObsClient实例
        final ObsClient obsClient = new ObsClient(ak, sk, config);

        // 初始化线程池
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        final File largeFile = new File(file);

        // 初始化分段上传任务
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
        InitiateMultipartUploadResult result = obsClient.initiateMultipartUpload(request);

        final String uploadId = result.getUploadId();
        Log.i("UploadPart", "\t" + uploadId + "\n");

        // 每段上传100MB
        long partSize = 100 * 1024 * 1024L;
        long fileSize = largeFile.length();

        // 计算需要上传的段数
        long partCount = fileSize % partSize == 0 ? fileSize / partSize : fileSize / partSize + 1;

        final List<PartEtag> partEtags = Collections.synchronizedList(new ArrayList<PartEtag>());

        // 执行并发上传段
        for (int i = 0; i < partCount; i++) {
            // 分段在文件中的起始位置
            final long offset = i * partSize;
            // 分段大小
            final long currPartSize = (i + 1 == partCount) ? fileSize - offset : partSize;
            // 分段号
            final int partNumber = i + 1;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(bucketName);
                    uploadPartRequest.setObjectKey(objectKey);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setFile(largeFile);
                    uploadPartRequest.setPartSize(currPartSize);
                    uploadPartRequest.setOffset(offset);
                    uploadPartRequest.setPartNumber(partNumber);

                    uploadPartRequest.setProgressListener(new ProgressListener() {
                        @Override
                        public void progressChanged(ProgressStatus status) {
                            // 获取上传平均速率
                            Log.i("PutObject", "AverageSpeed:" + status.getAverageSpeed());
                            // 获取上传进度百分比
                            Log.i("PutObject", "TransferPercentage:" + status.getTransferPercentage());
                        }
                    });

                    UploadPartResult uploadPartResult;
                    try {
                        uploadPartResult = obsClient.uploadPart(uploadPartRequest);
                        Log.i("UploadPart", "Part#" + partNumber + " done\n");
                        partEtags.add(new PartEtag(uploadPartResult.getEtag(), uploadPartResult.getPartNumber()));
                    } catch (ObsException e) {
                        Log.e("UploadPart", e.getMessage(), e);
                    }
                }
            });
        }

        // 等待上传完成
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e("UploadPart", e.getMessage(), e);
            }
        }

        // 合并段
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, partEtags);
        obsClient.completeMultipartUpload(completeMultipartUploadRequest);

    }


    /**
     * 断点续传
     *
     * @param file
     * @param objectName
     */
    public static void uploadFile(Context context, String file, String objectName, String videoId, IProgressListener listener) {

        ObsConfiguration config = new ObsConfiguration();
        config.setSocketTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setEndPoint(endPoint);

        // 创建ObsClient实例
        final ObsClient obsClient = new ObsClient(ak, sk, config);

        UploadFileRequest request = new UploadFileRequest(bucketName, objectName);
        // 设置待上传的本地文件，其中localfile为待上传的本地文件路径，需要指定到具体的文件名
        request.setUploadFile(file);
        // 设置分段上传时的最大并发数
        request.setTaskNum(10);
        // 设置分段大小为10MB
        request.setPartSize(10 * 1024 * 1024);
        // 开启断点续传模式
        request.setEnableCheckpoint(true);

        request.setProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressStatus status) {
                // 获取上传平均速率
                Log.i("PutObject", "AverageSpeed:" + status.getAverageSpeed());
                // 获取上传进度百分比
                Log.i("PutObject", "TransferPercentage:" + status.getTransferPercentage());

                VideoUploadInteract.uploadStatus(context, videoId, STATUS_PROCESSING, status.getAverageSpeed(), status.getTransferPercentage(), "", null);
                if (listener != null) {
                    listener.onProgressUpdate(status.getTransferPercentage(), status.getTransferredBytes(), status.getTotalBytes() - status.getTransferredBytes());
                }
            }
        });

        try {
            VideoUploadInteract.uploadStatus(context, videoId, STATUS_STARTING, 0, 0, "", null);

            // 进行断点续传上传
            CompleteMultipartUploadResult result = obsClient.uploadFile(request);
            String filePath = result.getObjectUrl();
            VideoUploadInteract.uploadStatus(context, videoId, STATUS_COMPLETE, 0, 0, filePath, null);
            if (listener != null) {
                listener.onComplete();
            }
        } catch (ObsException e) {
            e.printStackTrace();
            // 发生异常时可再次调用断点续传上传接口进行重新上传
            VideoUploadInteract.uploadStatus(context, videoId, STATUS_FAIL, 0, 0, "", null);
            if (listener != null) {
                listener.abort(e.getErrorMessage());
            }
        }
    }
}
