package com.xh.hotme.listener;

import androidx.annotation.Keep;

/**
 * Create by zhaozhihui on 2018/10/10
 **/
@Keep
public interface IProgressListener {
    void onProgressUpdate(long progress, long totalBytesWritten, long totalBytesExpectedToWrite);
    void onComplete();
    void abort(String message);
}
