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

import android.os.Environment;
import androidx.annotation.Keep;
import android.util.Log;

import com.xh.hotme.HotmeApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 简单的日志输出类
 */

@Keep
public class AppTrace {
    private static final String TAG = "AppTrace";
    private static boolean isDebugMode = true;

    private static boolean outputToFile = true;

    private static final String PRE = "[weewa]";


    private AppTrace() {
    }

    @Keep
    public static void setDebugMode(boolean isDebug) {
        isDebugMode = isDebug;
    }

    @Keep
    public static boolean isDebugMode() {
        return isDebugMode;
    }

    public static void i(String debugInfo) {
        d(TAG, debugInfo);
    }

    public static void i(String tag, String debugInfo) {
        if (debugInfo == null) {
            return;
        }
        if (isDebugMode()) {
            Log.i(PRE + tag, debugInfo);

            if (isOutputToFile()) {
                writeLogtoFile("i", PRE + tag, debugInfo);
            }
        }
    }

    public static void d(String debugInfo) {
        d(TAG, debugInfo);
    }

    public static void d(String tag, String debugInfo) {
        if (debugInfo == null) {
            return;
        }
        if (isDebugMode()) {
            Log.wtf(PRE + tag, debugInfo);

            if (isOutputToFile()) {
                writeLogtoFile("d", PRE + tag, debugInfo);
            }
        }
    }

    public static void w(String warning) {
        w(TAG, warning);
    }

    public static void w(String tag, String warning) {
        if (warning == null) {
            return;
        }
        if (isDebugMode()) {
            Log.w(PRE + tag, warning);

            if (isOutputToFile()) {
                writeLogtoFile("w", PRE + tag, warning);
            }
        }
    }

    public static void e(String error) {
        e(TAG, error);
    }

    public static void e(String tag, String error) {
        if (error == null) {
            return;
        }
        if (isDebugMode()) {
            Log.e(PRE + tag, error);

            if (isOutputToFile()) {
                writeLogtoFile("e", PRE + tag, error);
            }
        }
    }

    public static void e(Exception exception) {
        e(TAG, exception);
    }

    public static void e(String tag, Exception exception) {
        String stackInfo = getAllStackInformation(exception);
        e(tag, stackInfo);
    }

    /**
     * 获取所有堆栈信息
     *
     * @param ex
     * @return
     */
    private static String getAllStackInformation(Throwable ex) {
        try {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();

            return writer.toString();
        } catch (Throwable e) {
            e("class AppTrace.java - method getAllStackInformation(Throwable) catch error " + e);
        }
        return "unknown: get stack information error";
    }

    @Keep
    public static void setOutputToFile(boolean isDebug) {
        outputToFile = isDebug;
    }

    private static boolean isOutputToFile() {
        return outputToFile;
    }


    private static final String MYLOGFILEName = "Log.txt";// 本类输出的日志文件名称
    private static final String MYLOG_PATH_SDCARD_DIR = "/leto/log";// 日志文件在sdcard中的路径
    private static final int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static final SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static final SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格/......????/???式

    private static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件
        Date nowtime = new Date();
        String needWriteFiel = logfile.format(nowtime);
        String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text;
        File dirPath = AppFileUtil.getAppDir(HotmeApplication.getContext());

        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        //Log.i("创建文件","创建文件");
        File file = new File(dirPath, needWriteFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除制定的日志文件
     */
    public static void delFile() {// 删除日志文件
        String needDelFiel = logfile.format(getDateBefore());
        File dirPath = Environment.getExternalStorageDirectory();
        File file = new File(dirPath, needDelFiel + MYLOGFILEName);// MYLOG_PATH_SDCARD_DIR
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}
