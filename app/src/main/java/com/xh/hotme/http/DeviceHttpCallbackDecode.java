package com.xh.hotme.http;

import android.text.TextUtils;

import androidx.annotation.Keep;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.xh.hotme.utils.AppTrace;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * OKHttp decode
 */
@Keep
public abstract class DeviceHttpCallbackDecode<E> implements Callback {
    private static final String TAG = "OKHttp";

    private final Type _resultType;

    public DeviceHttpCallbackDecode() {
        this(null);
    }



    public DeviceHttpCallbackDecode( Type resultType) {
        _resultType = resultType;
    }


    @Override
    public void onFailure(Call call, IOException e) {
        AppTrace.d(TAG, "IOException =" + e.getLocalizedMessage());
        try {
            onFailure("-1", e != null ? e.getMessage() : "server exception");
        } catch (Throwable t){
            t.printStackTrace();
        }
        finally {
            try {
                onFinish();
            } catch (Throwable thr) {

            }
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            if (response == null || response.body() == null) {
                onFailure("-1", "server response is null");
                return;
            }
            String t = response.body().string();
            if(TextUtils.isEmpty(t) || t.equalsIgnoreCase("null")){
                onFailure("-1", "server response body is null");
                return;
            }

            try {
                if (response.body() != null) {
                    response.body().close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            AppTrace.d(TAG, "onResponse =" + t);

            JSONObject object = JSONObject.parseObject(t);
            onDataSuccess(object);

        } catch (JSONException e) {
            e.printStackTrace();
            try {
                onFailure("-2", "Json exception.");
            } catch (Throwable te) {

            }
        } catch (JsonParseException e) {
            e.printStackTrace();
            try {
                onFailure("-3", "Json parse exception.");
            } catch (Throwable te) {

            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                onFailure("-10", "Inner error, please try again later.");
            } catch (Throwable te) {

            }
        } finally {
            try {
                onFinish();
            } catch (Throwable e) {

            }

        }
    }

    public void onFinish() {

    }

    public abstract void onDataSuccess(JSONObject data);

    public void onFailure(String code, String msg) {

    }

    protected Class<E> getTClass() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type resultType = type.getActualTypeArguments()[0];
        if (resultType instanceof Class) {
            return (Class<E>) resultType;
        } else {
            // 处理集合
            try {
                Field field = resultType.getClass().getDeclaredField("rawTypeName");
                field.setAccessible(true);
                String rawTypeName = (String) field.get(resultType);
                return (Class<E>) Class.forName(rawTypeName);
            } catch (Exception e) {
                return (Class<E>) Collection.class;
            }
        }
    }
}
