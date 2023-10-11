package com.xh.hotme.http;


import android.text.TextUtils;

import com.xh.hotme.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request.Builder builder = addHeaders(chain.request());
        Request request = builder.build();
        return chain.proceed(request);
    }

    public static Request.Builder addHeaders(Request request) {
        Request.Builder builder = request.newBuilder();

        if (!TextUtils.isEmpty(Constants.HEADER_TOKEN)) {
            builder.addHeader(Constants.HEADER_TOKEN_NAME, SdkApi.TOKEN_PREFIX + Constants.HEADER_TOKEN);
        }

        return builder;
    }
}
