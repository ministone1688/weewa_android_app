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

import android.content.Context;
import android.net.Uri;
import android.system.Os;
import android.text.TextUtils;

import androidx.annotation.Keep;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * 文件操作工具类
 */

@Keep
public class FileUtil {
    private static final String TAG = "FileUtil";

    public interface FileMode {
        int MODE_ISUID = 04000;
        int MODE_ISGID = 02000;
        int MODE_ISVTX = 01000;
        int MODE_IRUSR = 00400;
        int MODE_IWUSR = 00200;
        int MODE_IXUSR = 00100;
        int MODE_IRGRP = 00040;
        int MODE_IWGRP = 00020;
        int MODE_IXGRP = 00010;
        int MODE_IROTH = 00004;
        int MODE_IWOTH = 00002;
        int MODE_IXOTH = 00001;

        int MODE_755 = MODE_IRUSR | MODE_IWUSR | MODE_IXUSR
            | MODE_IRGRP | MODE_IXGRP
            | MODE_IROTH | MODE_IXOTH;
        int MODE_777 = MODE_755 | MODE_IWGRP | MODE_IWOTH;
    }

    private FileUtil() {
    }

    /**
     * 复制文件，支持文件夹复制
     *
     * @param srcPath  原文件路径
     * @param destPath 目标文件路径
     * @return true：复制成功，否则亦然
     */
    public static boolean copyAll(String srcPath, String destPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(destPath)) {
            throw new IllegalArgumentException("srcPath and destPath can not be null");
        }
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            throw new IllegalArgumentException("src file not exists");
        }

        if (srcFile.isFile()) {
            return copyFile(srcPath, destPath);
        }

        File[] files = srcFile.listFiles();
        if (files != null) {
            for (File file : files) {
                copyAll(file.getAbsolutePath(), new File(destPath, file.getName()).getPath());
            }
        }

        return true;
    }

    /**
     * 复制文件
     *
     * @param srcPath  原文件路径
     * @param destPath 目标文件路径
     * @return true：复制成功，否则亦然
     */
    public static boolean copyFile(String srcPath, String destPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(destPath)) {
            throw new IllegalArgumentException("srcPath and destPath can not be null");
        }
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            throw new IllegalArgumentException("src file not exists");
        }

        File destFile = new File(destPath);
        File destDir = destFile.getParentFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        if (destFile.exists()) {
            destFile.delete();
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            AppTrace.d(TAG, "copyAll file exception : " + e.getMessage());
            return false;
        } finally {
            IOUtil.closeAll(in, out);
        }
        return true;
    }


    /**
     * 删除文件，如果是目录的话则会递归删除
     *
     * @param path 文件路径
     */
    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            file.delete();
            return;
        }

        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteFile(f.getAbsolutePath());
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param file 被读取的文件
     * @return 文件内容的字符串
     */
    public static String readContent(File file) {
        if (file == null || !file.exists()) {
            return "";
        }

        ByteArrayOutputStream out = null;
        FileInputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            in = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            return out.toString();
        } catch (Exception e) {
            AppTrace.e(TAG, "readContent exception" + e.getMessage());
        } finally {
            IOUtil.closeAll(out, in);
        }

        return "";
    }

    /**
     * 获取Assets文件的内容
     *
     * @param context
     * @param localHtmlPath 本地页面路径
     * @return 页面内容
     */
    public static String readAssetsFileContent(Context context, String localHtmlPath) {
        if (context == null || TextUtils.isEmpty(localHtmlPath)) {
            return "";
        }
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            in = context.getAssets().open(localHtmlPath);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            return out.toString();
        } catch (Exception e) {
            AppTrace.e(TAG, "readAssetsFileContent exception" + e.getMessage());
        } finally {
            IOUtil.closeAll(out, in);
        }
        return "";
    }

    /**
     * 获取输入流的md5值
     *
     * @param in 输入流
     * @return md5值
     */
    public static String getMD5(InputStream in) {
        if (in == null) {
            return null;
        }

        MessageDigest md;
        byte[] buffer = new byte[4096];
        try {
            md = MessageDigest.getInstance("MD5");
            int len;
            while ((len = in.read(buffer)) >= 0) {
                md.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return bytesToHexString(md.digest());
    }

    /**
     * 获取文件的md5值
     *
     * @param file 被获取md5的文件
     * @return 文件的md5值
     */
    public static String getMD5(File file) {

        if (file == null || !file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in = null;
        byte[] buffer = new byte[4096];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) >= 0) {
                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtil.closeAll(in);
        }
        return bytesToHexString(digest.digest());
    }

    /**
     * 获取文件的SHA1值
     *
     * @param filePath 文件路径
     * @return 文件的SHA1值
     */
    public static String getSHA1(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[4096];
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] sha1Bytes = digest.digest();
            return bytesToHexString(sha1Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            IOUtil.closeAll(inputStream);
        }
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param src 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static void chmod(String path, int mode) throws Exception {
        // call os chmod
        try {
            Os.chmod(path, mode);
            return;
        } catch (Exception e) {
        }

        // if os chmod failed, call runtime instead
        File file = new File(path);
        String cmd = "chmod ";
        if (file.isDirectory()) {
            cmd += " -R ";
        }
        String cmode = String.format("%o", mode);
        Runtime.getRuntime().exec(cmd + cmode + " " + path).waitFor();
    }

    /**
     * 获取文件后缀
     *
     * @param path 文件路径
     * @return 文件后缀
     */
    public static String getExtension(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        int start = path.lastIndexOf(".");
        if (start != -1) {
            return path.substring(start);
        }
        return "";
    }

//    /**
//     * 根据文件后缀名获得对应的MIME类型。 * @param file
//     */
//    private String getMIMEType(File file) {
//        String type = "*/*";
//        String fName = file.getName();    //获取后缀名前的分隔符"."在fName中的位置。
//        int dotIndex = fName.lastIndexOf(".");
//        if (dotIndex < 0) {
//            return type;
//        }    /* 获取文件的后缀名 */
//        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
//        if (end == "") return type;
//        //在MIME和文件类型的匹配表中找到对应的MIME类型。
//        for (int i = 0; i < MIME_MapTable.length; i++) {
//            if (end.equals(MIME_MapTable[i][0]))
//                type = MIME_MapTable[i][1];
//        }
//        return type;
//    }

    /**
     * 获取文件大小
     *
     * @param path 文件路径
     * @return 文件大小或-1
     */
    public static long getFileSize(String path) {
        if (path == null || path.trim().length() == 0) {
            return -1;
        }

        File file = new File(path);
        return file.exists() && file.isFile() ? file.length() : -1;
    }

    /**
     * 将文件转换为uri字符串返回
     *
     * @param file 被转换的文件
     * @return uri字符串
     */
    public static String toUriString(File file) {
        return Uri.fromFile(file).toString();
    }

    /**
     * 将文件路径转换为uri字符串
     *
     * @param path 被转换的文件路径
     * @return uri字符串
     */
    public static String toUriString(String path) {
        return Uri.fromFile(new File(path)).toString();
    }


    public static long getFileSizeInServer(String urlString) throws Exception {
        long lenght = 0;
        String url = URLEncoder.encode(urlString, "UTF-8");
        URL mUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Encoding", "identity");
        conn.setRequestProperty("Referer", url);
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            lenght = conn.getContentLength();
        }
        return lenght;
    }

    /**
     * 递归创建文件夹
     *
     * @param dir
     * @return
     */
    public static boolean createDir(File dir, boolean isCreateParent) {
        try {
            String parentDir = dir.getParent();
            if (isCreateParent && !new File(parentDir).exists()) {
                createDir(new File(parentDir), isCreateParent);
            }
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return true;
        } catch (Exception e) {
            AppTrace.e(TAG, "createDir error: " + e.getMessage());
            return false;
        }
    }


    /**
     * 创建文件夹
     *
     * @param dir
     * @return
     */
    public static boolean createDir(File dir) {
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return true;
        } catch (Exception e) {
            AppTrace.e(TAG, "createDir error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建文件
     *
     * @param file
     * @return
     */
    public static File createNewFile(File file) {
        try {
            if (file.exists()) {
                return file;
            }

            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            AppTrace.e(TAG, "createNewFile error: " + e.getMessage());
            return null;
        }
        return file;
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void rmdir(File file, boolean recursive) {
        if (file.isFile()) {
            return;
        }
        if (file.isDirectory()) {
            if (recursive) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    rmdir(f, recursive);
                }
            }
            file.delete();
        }
    }

    /**
     * 创建文件
     *
     * @param path
     */
    public static File createNewFile(String path) {
        File file = new File(path);
        return createNewFile(file);
    }// end method createText()

    /**
     * 向Text文件中写入内容
     *
     * @param path
     * @param content
     * @return
     */
    public static boolean write(String path, String content, String encoding) {
        return write(path, content, encoding, false);
    }

    public static boolean write(String path, String content, String encoding, boolean append) {
        return write(new File(path), content, encoding, append);
    }

    public static boolean write(File file, String content, String encoding) {
        return write(file, content, encoding, false);
    }

    /**
     * 写入文件
     *
     * @param file
     * @param content
     * @param append
     * @return
     */
    public static boolean write(File file, String content, String encoding, boolean append) {
        if (file == null || TextUtils.isEmpty(content)) {
            return false;
        }
        if (!file.exists()) {
            file = createNewFile(file);
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, append);
            out.write(content.getBytes(TextUtils.isEmpty(encoding) ? "iso-8859-1" : encoding));
            out.flush();
        } catch (Exception e) {
            AppTrace.e(TAG, "write error: " + e.getMessage());
            return false;
        } finally {
            IOUtil.closeAll(out);
        }

        return true;
    }

    /**
     * 读取字节形式的原始文件内容
     *
     * @param file 被读取的文件
     * @return 文件内容的字节数组
     */
    public static byte[] readRawContent(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        ByteArrayOutputStream out = null;
        FileInputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            in = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } catch (Exception e) {
            AppTrace.e(TAG, "readRawContent exception: " + e.getMessage());
        } finally {
            IOUtil.closeAll(out, in);
        }

        return null;
    }

    /**
     * 读取文件内容
     *
     * @param file 被读取的文件
     * @return 文件内容的字符串
     */
    public static String readContent(File file, String encoding) {
        if (file == null || !file.exists()) {
            return "";
        }

        ByteArrayOutputStream out = null;
        FileInputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            in = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            return out.toString(encoding);
        } catch (Exception e) {
            AppTrace.e(TAG, "readContent exception: " + e.getMessage());
        } finally {
            IOUtil.closeAll(out, in);
        }

        return "";
    }

    /**
     * 取得文件大小
     *
     * @param f
     * @return
     * @throws Exception
     */
    @SuppressWarnings("resource")
    public static long getFileSizes(File f) throws Exception {
        long size = 0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            size = fis.available();
        } else {
            f.createNewFile();
        }
        return size;
    }

    /**
     * 得到目录所有文件名称
     * 读取目录内文件列表
     *
     * @param dir
     * @return
     */
    public static ArrayList<String> readdir(File dir, String root, boolean recursive) {
        ArrayList<String> allFiles = new ArrayList<>();
        // 递归取得目录下的所有文件及文件夹
        File[] files = dir.listFiles();
        if (null == files || files.length == 0) return new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileItem fItem = getFileItem(file, root);
            allFiles.add(fItem.filePath);
            if (file.isDirectory() && recursive) {
                ArrayList<FileItem> items = getAllFiles(file, root, recursive);
                for (FileItem item : items) {
                    allFiles.add(item.filePath);
                }
            }
        }
        return allFiles;
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    public static void renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return;
        }

        if (TextUtils.isEmpty(newPath)) {
            return;
        }

        File file = new File(oldPath);
        file.renameTo(new File(newPath));
    }

    /**
     * 得到所有文件
     *
     * @param dir
     * @return
     */
    public static ArrayList<FileItem> getAllFiles(File dir, String root, boolean recursive) {
        ArrayList<FileItem> allFiles = new ArrayList<>();
        _getAllFiles(allFiles, dir, root, recursive);
        return allFiles;
    }

    private static void _getAllFiles(ArrayList<FileItem> allFiles, File dir, String root, boolean recursive) {
        // 递归取得目录下的所有文件及文件夹
        File[] files = dir.listFiles();
        if (null == files || files.length == 0) return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileItem item = getFileItem(files[i], root);
            allFiles.add(item);
            if (file.isDirectory() && recursive) {
                _getAllFiles(allFiles, file, root, recursive);
            }
        }
    }

    public static FileItem getFileItem(File file, String root) {

        FileItem item = new FileItem();
        if (TextUtils.isEmpty(root)) {
            item.filePath = file.getPath();
        } else {
            item.filePath = file.getPath().replace(root, "");
        }
        item.size = getFileSize(file.getPath());
        item.createTime = file.lastModified();
        return item;
    }

    public static class FileItem {
        long size;
        String filePath;
        long createTime;
    }
}
