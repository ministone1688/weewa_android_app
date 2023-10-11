//
// Copyright (c) 2017, ledong.com
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//


package com.xh.hotme.utils;

import android.annotation.SuppressLint;
import android.net.Uri;

import androidx.annotation.Keep;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.listener.IProgressListener;
import com.xh.hotme.listener.ProgressRequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * http请求工具类
 */
@Keep
public class OkHttpUtil {
    private static final String TAG = "OkHttpUtil";

    // shared okhttp client instance
    private static OkHttpClient CLIENT;

    private OkHttpUtil() {
    }

    private static OkHttpClient getOkHttpClient() {
        if (CLIENT == null) {
            CLIENT = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                    .sslSocketFactory(createSSLSocketFactory(), new TrustAllManager())
                    .hostnameVerifier(new TrustAllHostnameVerifier())
                    .build();
        }

        return CLIENT;
    }

    private static OkHttpClient getOkHttpsClient() {

        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory(), new TrustAllManager());
        mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
        mBuilder
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .build();

        OkHttpClient client = mBuilder.build();
        return client;
    }

    /**
     * 执行同步请求
     *
     * @param request 请求对象
     * @return 响应对象
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        OkHttpClient client = getOkHttpClient();
        return client.newCall(request).execute();
    }

    /**
     * 执行异步请求
     *
     * @param request  请求对象
     * @param callback 异步请求的回调
     */
    public static Call enqueue(Request request, Callback callback) {
        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * 执行异步请求
     *
     * @param url      请求对象
     * @param callback 异步请求的回调
     */
    public static Call get(String url, Callback callback) {
        Request.Builder requestBuild = new Request.Builder();
        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }
        Request request = requestBuild.get().url(url).build();

        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * 执行异步请求
     *
     * @param request  请求对象
     * @param callback 异步请求的回调
     */
    public static Call get(Request request, Callback callback) {

        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call asyncCall(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return getOkHttpClient().newCall(request);
    }

    public static Response syncResponse(String url, long start, long end) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                //Range 请求头格式Range: bytes=start-end
                .addHeader("Range", "bytes=" + start + "-" + end)
                .build();
        return getOkHttpClient().newCall(request).execute();
    }

    /**
     * 为url添加参数
     *
     * @param url   原url，必须以http或https开头
     * @param key   参数的键
     * @param value 参数的值
     * @return 原url或附加参数后的url
     */
    public static String appendUrlParam(String url, String key, String value) {
        if (!URLUtil.isNetworkUrl(url) || TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return url;
        }

        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        builder.appendQueryParameter(key, value);
        return builder.build().toString();
    }

    /**
     * 附加一组url参数，不会去重
     *
     * @param url    原url，必须以http或https开头
     * @param params 参数组
     * @return 原url或附加参数后的url
     */
    public static String appendUrlParams(String url, Map<String, String> params) {
        if (!URLUtil.isNetworkUrl(url) || params == null || params.size() == 0) {
            return url;
        }

        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }
        return builder.build().toString();
    }

    /**
     * 附加一组url参数，不会去重
     *
     * @param url    原url，必须以http或https开头
     * @param params 参数组
     * @return 原url或附加参数后的url
     */
    public static String appendUrlParamsWithoutEncode(String url, Map<String, String> params) {
        if (!URLUtil.isNetworkUrl(url) || params == null || params.size() == 0) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        if (!url.endsWith("?")) {
            builder.append("?");
        }
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.append(param.getKey()).append("=").append(param.getValue()).append("&");
        }
        return builder.toString();
    }

    /**
     * 将JSONObject对象转为Map对象
     *
     * @param json json字符串
     * @return 解析转换后的Map对象
     */
    public static Map<String, String> parseJsonToMap(JSONObject json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) {
            return map;
        }

        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = json.optString(key);
            if (!TextUtils.isEmpty(value)) {
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * 把数据源HashMap转换成json
     *
     * @param map
     */
    public static String hashMapToJson(HashMap map) {
        String string = "{";
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry e = (Map.Entry) it.next();
            string += "'" + e.getKey() + "':";
            string += "'" + e.getValue() + "',";
        }
        string = string.substring(0, string.lastIndexOf(","));
        string += "}";
        return string;
    }

    /**
     * 执行异步请求
     *
     * @param request  请求对象
     * @param callback 异步请求的回调
     */
    public static Call downLoadFile(Request request, Callback callback) {
        OkHttpClient client;
        if (request.isHttps()) {
            client = getOkHttpsClient();
        } else {
            client = getOkHttpClient();
        }
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call postFile(String url, String name, JSONObject header, JSONObject formData, final IProgressListener listener, Callback callback, File... files) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        if (null != formData) {
            Iterator<String> sIterator = formData.keys();
            while (sIterator.hasNext()) {
                // 获得key
                String key = sIterator.next();
                // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
                Object value = null;
                try {
                    value = formData.get(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("key: " + key + ",value" + value);
                builder.addFormDataPart(key, String.valueOf(value));
            }
        }

        AppTrace.d("huang", "files[0].getName()==" + files[0].getName());
        //第一个参数要与Servlet中的一致
        if (TextUtils.isEmpty(name)) {
            builder.addFormDataPart("file", files[0].getName(), RequestBody.create(MediaType.parse("multipart/form-data"), files[0]));
        } else {
            builder.addFormDataPart("file", name, RequestBody.create(MediaType.parse("multipart/form-data"), files[0]));

        }

        MultipartBody multipartBody = builder.build();

        final Request.Builder mBuilder = new Request.Builder().url(url);
        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            mBuilder.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        if (null != header) {
            Iterator<String> sIterator = header.keys();
            while (sIterator.hasNext()) {
                // 获得key
                String key = sIterator.next();
                // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
                Object value = null;
                try {
                    value = formData.get(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mBuilder.addHeader(key, String.valueOf(value));
            }
        }
        mBuilder.post(new ProgressRequestBody(multipartBody, listener));

        Request request = mBuilder.build();
        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }


    public static Call postFile(String url, Callback callback, File... files) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        AppTrace.d("huang", "files[0].getName()==" + files[0].getName());
        //第一个参数要与Servlet中的一致
        builder.addFormDataPart("file", files[0].getName(), RequestBody.create(MediaType.parse("*/*"), files[0]));


        MultipartBody multipartBody = builder.build();

        final Request.Builder mBuilder = new Request.Builder().url(url);
        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            mBuilder.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }
        mBuilder.post(multipartBody);

        Request request = mBuilder.build();
        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }


    /**
     * 默认信任所有的证书
     * TODO 最好加上证书认证，主流App都有自己的证书
     *
     * @return
     */
    @SuppressLint("TrulyRandom")
    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Throwable e) {
        }
        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static String buildUserAgent() {
        String agent = "";
//        if (null != SdkConstant.deviceBean) {
//            agent = SdkConstant.deviceBean.getUserua() + " "/* + SdkConstant.deviceBean.getDevice_id()*/;
//        }
//        try {
//            agent += " mgcsdk/" + LetoCore.getVersion() + " mgcframework/" + LetoCore.DEFAULT_FRAMEWORK_VERSION + " channel/" + SdkConstant.MGC_APPID;
//            return URLEncoder.encode(agent, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        return agent;
    }


    /**
     * @param url
     * @param headers
     * @param callback
     */
    public static void put(String url, Map<String, String> headers, Callback callback) {
        JSONObject jsonObject = new JSONObject();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    try {
                        jsonObject.put(entry.getKey(), encodeHeadInfo(entry.getValue()));
                    } catch (Throwable e) {
                    }
                }
            }
        }
        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody body = RequestBody.create(type, jsonObject.toString());

        //创建Json请求参数
        Request.Builder requestBuild = new Request.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        requestBuild.url(url).put(body);

        Request request = requestBuild.build();
        OkHttpUtil.enqueue(request, callback);
    }

    /**
     * @param url
     * @param paramsMap
     * @param headers
     * @param callback
     */
    public static void postData(String url, HashMap<String, String> paramsMap, Map<String, String> headers, Callback callback) {
        JSONObject jsonObject = new JSONObject();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    try {
                        jsonObject.put(entry.getKey(), encodeHeadInfo(entry.getValue()));
                    } catch (Throwable e) {
                    }
                }
            }
        }
        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody body = RequestBody.create(type, jsonObject.toString());

        //创建Json请求参数
        Request.Builder requestBuild = new Request.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        requestBuild.url(url).post(body);

        Request request = requestBuild.build();
        OkHttpUtil.enqueue(request, callback);
    }

    /**
     * @param url
     * @param paramJson
     * @param headers
     * @param callback
     */
    public static void postData(HttpUrl url, String paramJson, Map<String, String> headers, Callback callback) {

        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody body = RequestBody.create(type, paramJson);
        //创建Json请求参数
        Request.Builder requestBuild = new Request.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        requestBuild.url(url).post(body);
        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(requestBuild.build());
        call.enqueue(callback);
    }


    /**
     * @param url
     * @param paramJson
     * @param headers
     * @param callback
     */
    public static void postData(String url, String paramJson, Map<String, String> headers, Callback callback) {

        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody body = RequestBody.create(type, paramJson);
        //创建Json请求参数
        Request.Builder requestBuild = new Request.Builder();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        requestBuild.url(url).post(body);
        Request request = requestBuild.build();
        OkHttpUtil.enqueue(request, callback);
    }

    /**
     * 执行异步请求
     *
     * @param url      请求对象
     * @param callback 异步请求的回调
     */
    public static Call get(String url, Map<String, String> headers, Callback callback) {

        Request.Builder requestBuild = new Request.Builder().url(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(requestBuild.build());
        call.enqueue(callback);
        return call;
    }

    /**
     * 执行异步请求
     *
     * @param url      请求对象
     * @param callback 异步请求的回调
     */
    public static Call get(HttpUrl url, Map<String, String> headers, Callback callback) {

        Request.Builder requestBuild = new Request.Builder().url(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(requestBuild.build());
        call.enqueue(callback);
        return call;
    }


    /**
     * 执行异步请求
     *
     * @param url      请求对象
     * @param callback 异步请求的回调
     */
    public static Call delete(String url, Map<String, String> headers, OkHttpCallbackDecode callback) {
        String userAgent = OkHttpUtil.buildUserAgent();

        Request.Builder requestBuild = new Request.Builder().url(url).delete();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() != null) {
                    requestBuild.addHeader(entry.getKey(), encodeHeadInfo(entry.getValue()));
                } else {
                    requestBuild.addHeader(entry.getKey(), "");
                }
            }
        }

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            requestBuild.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        OkHttpClient client = getOkHttpClient();
        Call call = client.newCall(requestBuild.build());
        call.enqueue(callback);
        return call;
    }

    public static String encodeHeadInfo(String headInfo) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0, length = headInfo.length(); i < length; i++) {
            char c = headInfo.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                stringBuffer.append(String.format("\\u%04x", (int) c));
            } else {
                stringBuffer.append(c);
            }
        }
        return stringBuffer.toString();
    }

    public static String buildUrl(String url, HashMap<String, String> params) {
        Map<String, String> map = new HashMap<>();
        if (params == null) {
            return url;
        }

        StringBuilder buildUrl = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (buildUrl.length() == 0) {
                buildUrl.append(param.getKey() + "=" + param.getValue());
            } else {
                buildUrl.append("&"+param.getKey() + "=" + param.getValue());
            }
        }

        if(url.endsWith("?")){
            return url + buildUrl;
        }else{
            return url + "?" + buildUrl;
        }

    }
}
