package com.xh.hotme.http;


import android.text.TextUtils;

import com.xh.hotme.utils.AppTrace;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class LogInterceptor implements Interceptor {

    private static final String TAG = LogInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Charset UTF8 = Charset.forName("UTF-8");
        // 打印请求报文
        Request request = chain.request();
        RequestBody requestBody = request.body();
        String reqBody = null;
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            reqBody = buffer.readString(charset);
        }
        if (isImg(request.url())) {
            return chain.proceed(request);
        }
//        if(BaseApp.getInstance().isDebugProtocol()) {
//            YLog.n(String.format("发送请求\nmethod：%s\nurl：%s\nheaders: %s\nbody：%s",
//                    request.method(), request.url(), request.headers(), reqBody));
//        }se
        long logT = System.currentTimeMillis();
        if (request.headers().get("Content-Disposition") != null && request.headers().get("Content-Disposition").contains("name=\"file\"")) {
            AppTrace.d(String.format("发送请求\nheaders: %s", request.headers()));
        } else {
            AppTrace.d(String.format("发送请求\nmethod：%s\nurl：%s\nheaders: %s\nbody：%s",
                    request.method(), request.url(), request.headers(), reqBody));
        }
        // 打印返回报文
        // 先执行请求，才能够获取报文
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        String respBody = null;
        if (responseBody != null) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            respBody = buffer.clone().readString(charset);
            if (!isJson(respBody)) {
                //认为是加密的
                try {
//                    respBody = AES.decryptAES_oneUrlDecode(respBody);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        String deReqBody = "";
        try {
            if (!TextUtils.isEmpty(reqBody)) {
                String urlDecodeStr = URLDecoder.decode(reqBody);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long costT = System.currentTimeMillis() - logT;
        if (reqBody != null && reqBody.contains("name=\"file\"")) {
            AppTrace.d(String.format("收到响应\n%s %s\n时间开销: %s ms\n请求url：%s\n请求方式：%s\n响应body：%s",
                    response.code(), response.message(), costT, response.request().url(), request.method(), respBody));
        } else if (!TextUtils.isEmpty(deReqBody)) {
            AppTrace.d(String.format("收到响应\n%s %s\n时间开销: %s ms\n请求url：%s\n请求方式：%s\n请求body：%s\n请求body(解密)：%s\n响应body：%s",
                    response.code(), response.message(), costT, response.request().url(), request.method(), reqBody, deReqBody, respBody));
        } else {
            AppTrace.d(String.format("收到响应\n%s %s\n时间开销: %s ms\n请求url：%s\n请求方式：%s\n请求body：%s\n响应body：%s",
                    response.code(), response.message(), costT, response.request().url(), request.method(), reqBody, respBody));
        }
        return response;
    }

    public boolean isJson(String Json) {
        if (Json.startsWith("{") && Json.endsWith("}")) {
            return true;
        } else if (Json.startsWith("[") && Json.endsWith("]")) {
            return true;
        }
        return false;
    }

    private boolean isImg(HttpUrl url) {
        if (url.toString().toLowerCase().contains(".jpg")
                || url.toString().toLowerCase().contains(".gif")
                || url.toString().toLowerCase().contains(".png")
                || url.toString().toLowerCase().contains("thirdqq")
                || url.toString().endsWith(".jfif")
                || url.toString().endsWith(".jpeg")
        ) {
//            YLog.d("IMG", "图片加载->" + url);
            return true;
        }
        return false;
    }
}