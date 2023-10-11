package com.xh.hotme.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 
 * @Version 1.0
 * @描述：gson工具类
 */

public class GsonUtils {
    private static  GsonUtils instance;

    public static GsonUtils getInstance() {
        if (instance == null) {
            synchronized (GsonUtils.class) {
                if (instance == null) {
                    instance = new GsonUtils();
                }
            }
        }
        return instance;
    }

    private static Gson gson,gson1;

    public static Gson getGson() {
        if (null == gson) {
//            .setDateFormat("yyyy-MM-dd HH:mm:ss")
//                    .excludeFieldsWithoutExposeAnnotation()
            gson = new GsonBuilder().serializeNulls().create();
        }
        return gson;
    }

    /**
     * 转成json
     *
     * @param object
     * @return
     */
    public static String GsonString(Object object) {
        getGson();
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }

    /**
     * 转成bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T GsonToBean(String gsonString, Class<T> cls) {
        getGson();
        T t = null;
        try {
            if (gson != null) {
                t = gson.fromJson(gsonString, cls);
            }
        }catch (Exception e){
            return t;
        }

        return t;
    }

    /**
     * 转成list
     * 泛型在编译期类型被擦除导致报错
     *
     * @param
     * @param cls
     * @return
     */
//    public static <T> List<T> GsonToList(String gsonString, Class<T> cls) {
//        getGson();
//        List<T> list = null;
//        if (gson != null) {
//            list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
//            }.getType());
//        }
//        return list;
//    }

    public static <T> ArrayList<T> fromJsonList(String json, Class<T> cls) {
        getGson();
        ArrayList<T> mList = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            mList.add(gson.fromJson(elem, cls));
        }
        return mList;
    }

    /**
     * 转成list
     * 解决泛型问题
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public <T> List<T> jsonToList(String json, Class<T> cls) {
        getGson();
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }


    /**
     * 转成list中有map的
     *
     * @param gsonString
     * @return
     */
    public static <T> List<Map<String, T>> GsonToListMaps(String gsonString) {
        getGson();
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }

    /**
     * 转成map的
     *
     * @param gsonString
     * @return
     */
    public static <T> Map<String, T> GsonToMaps(String gsonString) {
        getGson();
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }
    public static  Map<String, Integer> GsonToMaps_Integer(String gsonString) {
        getGson();
        Map<String, Integer> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, Integer>>() {
            }.getType());
        }
        return map;
    }
}
