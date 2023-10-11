package com.xh.hotme.listener;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import com.xh.hotme.bean.ProgressBean;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Create by zhaozhihui on 2018/10/10
 **/
public class ProgressRequestBody extends RequestBody {
    public static final int UPDATE = 0x01;
    private final RequestBody requestBody;
    private final IProgressListener mListener;
    private BufferedSink bufferedSink;
    private MyHandler myHandler;

    public ProgressRequestBody(RequestBody body, IProgressListener listener) {
        requestBody = body;
        mListener = listener;
        if (myHandler == null) {
            myHandler = new MyHandler();
        }
    }

    class MyHandler extends Handler {
        //放在主线程中显示
        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE) {
                ProgressBean progressModel = (ProgressBean) msg.obj;
                if (mListener != null)
                    mListener.onProgressUpdate(progressModel.getProgress(), progressModel.getTotalBytesWritten(), progressModel.getTotalBytesExpectedToWrite());
            }
        }


    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //刷新
        bufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {

        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                //回调
                Message msg = Message.obtain();
                msg.what = UPDATE;
                msg.obj = new ProgressBean(bytesWritten, contentLength);
                myHandler.sendMessage(msg);
            }
        };
    }


}
