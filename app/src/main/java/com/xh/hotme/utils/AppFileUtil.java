package com.xh.hotme.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.bean.UserInfoBean;

import java.io.File;


/**
 * Create by zhaozhihui on 2019/2/22
 **/
@Keep
public class AppFileUtil {


    public static final String CACHE_ROOT = "temp";
    public static final String CACHE_RXVOLLEY = "rxvolley";
    public static final String CACHE_GAME_INFO = "game_info";
    public static final String DEFAULT_USER_INFO = "DEFAULT_USER_INFO";
    public static final String CACHE_APP_ID = "channel_id";

    public static final String CACHE_DEVICE_FILE = "device_list";

    public static final String CACHE_DEVICE_BIND_FILE = "device_bind_list";

    public static final String CACHE_DEVICE_VIDEO = "device_video";

    public static final String ROOT_VIDEO = "videos";

    /**
     * 保存任意json字符串到本地缓存, 不区分用户
     */
    public static void saveJson(Context context, String jsonString, String fileName) {
        if (context == null) {
            return;
        }
        File cacheFile = getAppDir(context);
        if (cacheFile == null) {
            return;
        }
        File thirdUser = new File(cacheFile, fileName);
        FileUtil.write(thirdUser, jsonString, "utf-8");
    }

    public static void saveJson(String jsonString, File file) {
        FileUtil.write(file, jsonString, "utf-8");
    }

    public static void saveString(Context context, String str, String fileName) {
        if (context == null) {
            return;
        }
        File cacheFile = getAppDir(context);
        if (cacheFile == null) {
            return;
        }
        File thirdUser = new File(cacheFile, fileName);
        FileUtil.write(thirdUser, str, "utf-8");
    }

    public static void saveInt(Context context, int i, String fileName) {
        File cacheFile = getAppDir(context);
        if (cacheFile == null) {
            return;
        }
        File file = new File(cacheFile, fileName);
        FileUtil.write(file, String.valueOf(i), "utf-8");
    }

    public static void saveIntForCurrentUser(Context context, int i, String fileName) {
        File userDir = getCurrentUserCacheDir(context);
        if (userDir == null) {
            return;
        }
        File file = new File(userDir, fileName);
        FileUtil.write(file, String.valueOf(i), "utf-8");
    }

    public static void saveLongForCurrentUser(Context context, long i, String fileName) {
        File userDir = getCurrentUserCacheDir(context);
        if (userDir == null) {
            return;
        }
        File file = new File(userDir, fileName);
        FileUtil.write(file, String.valueOf(i), "utf-8");
    }

    /**
     * 保存任意json字符串到当前用户的本地缓存
     */
    public static void saveJsonForCurrentUser(Context context, String fileName, String jsonContent) {
        File userDir = getCurrentUserCacheDir(context);
        if (userDir == null) {
            return;
        }
        File gameFile = new File(userDir, fileName);

        boolean isSucc = FileUtil.write(gameFile, jsonContent, "utf-8", false);
        if (!isSucc) {
            AppTrace.d("Page", "save json fail for current user. ----fileName = " + fileName);
        }
    }

    public static boolean hasCacheFile(Context context, String fileName) {
        try {
            File cacheFile = getAppDir(context);
            if (cacheFile == null) {
                return false;
            }
            File file = new File(cacheFile, fileName);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static String loadStringFromFile(Context context, String fileName) {
        File cacheFile = getAppDir(context);
        if (cacheFile == null) {
            return "";
        }
        File file = new File(cacheFile, fileName);
        return FileUtil.readContent(file);
    }

    public static String loadStringFromFile(Context context, File file) {
        return FileUtil.readContent(file);
    }

    public static String loadStringForCurrentUser(Context context, String fileName) {
        File userDir = getCurrentUserCacheDir(context);
        if (userDir == null) {
            return "";
        }
        File file = new File(userDir, fileName);
        return FileUtil.readContent(file);
    }

    public static UserInfoBean loadUserInfo(Context context) {
        UserInfoBean userInfo = null;

        String content = loadStringFromFile(context, DEFAULT_USER_INFO);
        if (!TextUtils.isEmpty(content)) {
            try {
                userInfo = new Gson().fromJson(content, new TypeToken<UserInfoBean>() {
                }.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            AppTrace.d("load local user info is null");
        }
        return userInfo;
    }

    public static void saveUserInfo(Context context, String content) {
        if(TextUtils.isEmpty(content)){
            AppTrace.w("save user info is null");
            return;
        }
        AppFileUtil.saveJson(context, content, AppFileUtil.DEFAULT_USER_INFO);
    }



    public static void deleteUserInfo(Context context, String fileName) {
        if (context == null) {
            return;
        }
        File cacheFile = getAppDir(context);
        if (cacheFile == null) {
            return;
        }
        File thirdUser = new File(cacheFile, fileName);
        if (thirdUser != null && thirdUser.exists()) {
            thirdUser.delete();

        }
    }

    /**
     * 针对于每个用户的临时文件目录
     */
    public static File getUserCacheDir(Context context, String userId) {
        File rootDir = getAppDir(context);
        if (rootDir == null) {
            return null;
        }
        File userDir = new File(rootDir, userId);
        if (userDir != null && (!userDir.exists() || !userDir.isDirectory())) {
            userDir.mkdirs();
        }
        return userDir;
    }


    /**
     * 获取存储框架js代码的根目录
     *
     * @param context 上下文
     * @return 框架根目录文件
     */
    public static File getAppDir(Context context) {
        File appDir = context.getFilesDir();
        if (!appDir.exists() || !appDir.isDirectory()) {
            appDir.mkdirs();
        }
        return appDir;
    }



    /**
     * 当前用户的临时文件目录
     */
    public static File getCurrentUserCacheDir(Context context) {
        String userId = LoginManager.getUserId(context);
        return getUserCacheDir(context, userId);
    }

    public static File getCacheDir(Context context) {
        if (context == null) {
            return null;
        }

        File rootDir =  getAppDir(context);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            rootDir.mkdirs();
        }

        File cacheFile = new File(rootDir, CACHE_ROOT);
        if (cacheFile != null && !cacheFile.exists()) {
            cacheFile.mkdir();
        }
        return cacheFile;
    }


    public static void deleteFile(Context context, String fileName) {
        if (context == null) {
            return;
        }
        File cacheFile = context.getFilesDir();
        if (cacheFile != null) {
            File file = new File(cacheFile, fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    //获取视频根目录
    public static File getVideoDir(Context context) {
        if (context == null) {
            return null;
        }

        File rootDir =  getAppDir(context);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            rootDir.mkdirs();
        }

        File cacheFile = new File(rootDir, ROOT_VIDEO);
        if (cacheFile != null && !cacheFile.exists()) {
            cacheFile.mkdir();
        }
        return cacheFile;
    }


    //获取视频根目录
    public static File getCameraDir(Context context, String cameraDir) {
        if (context == null) {
            return null;
        }

        File videoDir =  getVideoDir(context);
        if (!videoDir.exists() || !videoDir.isDirectory()) {
            videoDir.mkdirs();
        }

        File cacheFile = new File(videoDir, cameraDir);
        if (cacheFile != null && !cacheFile.exists()) {
            cacheFile.mkdir();
        }
        return cacheFile;
    }

    /*

     */
    public static void saveVideoJson(Context context, String jsonString, File videoJsonFile) {
        if (context == null) {
            return;
        }
        File videoDir = getVideoDir(context);
        if (videoDir == null) {
            videoDir.mkdirs();
            return;
        }
        File parent = videoJsonFile.getParentFile();
        if(!parent.exists()){
            parent.mkdir();
        }

        FileUtil.write(videoJsonFile, jsonString, "utf-8");
    }

    /*
    */
//    public static void saveVideoJson(Context context, String jsonString, String fileName) {
//        if (context == null) {
//            return;
//        }
//        File videoDir = getVideoDir(context);
//        if (videoDir == null) {
//            return;
//        }
//        File thirdUser = new File(videoDir, fileName);
//        FileUtil.write(thirdUser, jsonString, "utf-8");
//    }


    public static String loadStringFromVideoFile(Context context, String cameraName, String fileName) {
        File videoDir = getVideoDir(context);
        if (videoDir == null) {
            return "";
        }

        File cameraFile = new File(videoDir, cameraName);
        if (cameraFile == null) {
            return "";
        }

        File file = new File(cameraFile, fileName);
        return FileUtil.readContent(file);
    }

}
