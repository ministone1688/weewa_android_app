package com.xh.hotme.bean;

/**
 * Create by zhaozhihui on 2018/10/10
 **/
public class ProgressBean {

    long progress;
    long totalBytesWritten;
    long totalBytesExpectedToWrite;

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getTotalBytesWritten() {
        return totalBytesWritten;
    }

    public void setTotalBytesWritten(long totalBytesWritten) {
        this.totalBytesWritten = totalBytesWritten;
    }

    public long getTotalBytesExpectedToWrite() {
        return totalBytesExpectedToWrite;
    }

    public void setTotalBytesExpectedToWrite(long totalBytesExpectedToWrite) {
        this.totalBytesExpectedToWrite = totalBytesExpectedToWrite;
    }

    public ProgressBean(long totalBytesWritten, long totalBytesExpectedToWrite){
        this.totalBytesWritten = totalBytesWritten;
        this.totalBytesExpectedToWrite =totalBytesExpectedToWrite;
        this.progress = (int) (totalBytesWritten * 1.0f / totalBytesExpectedToWrite * 100);
    }
}
