/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.dingdone.recorder.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;


import java.io.File;
import java.io.IOException;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Provides application storage paths
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.0.0
 */
public final class DDStorageUtils {

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "uil-images";

    public static boolean SAVE_CRASH_LOG = true;

    /**
     * 缓存图片路径
     */
    public static String IMG_DIR = "dingdone/pic";
    /**
     * 缓存视频路径
     */
    public static String VIDEO_DIR = "dingdone/video";

    /**
     * 缓存音频路径
     */
    public static String AUDIO_DIR = "dingdone/audio";

    /**
     * 缓存录音音频路径
     */
    public static String AUDIO_RECORD_DIR = "dingdone/record";

    /**
     * 日志路径
     */
    public static String LOG_DIR = "dingdone/log";

    /**
     * 助手的配置文件路径
     */
    public static String PREVIEW_DIR = "dingdone/preview";
    private static String PREVIEW_CONFIG = "ConfigureFiles";
    private static String PREVIEW_RESOURCE = "resource";

    private DDStorageUtils() {
    }

    public static String getVolleyCacheFileName(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    /**
     * Returns application cache directory. Cache directory will be created on
     * SD card <i>("/Android/data/[app_package_name]/cache" )</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    public static File getCacheDirectory(Context context) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = context.getExternalCacheDir();
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
        }
        return appCacheDir;
    }

    /**
     * Returns individual application cache directory (for only image caching
     * from ImageLoader). Cache directory will be created on SD card <i>(
     * "/Android/data/[app_package_name]/cache/uil-images" )</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    // public static File getIndividualCacheDirectory(Context context) {
    // File cacheDir = getCacheDirectory(context);
    // File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_NAME);
    // if (!individualCacheDir.exists()) {
    // if (!individualCacheDir.mkdir()) {
    // individualCacheDir = cacheDir;
    // }
    // }
    // return individualCacheDir;
    // }

//    /**
//     * Returns specified application cache directory. Cache directory will be
//     * created on SD card by defined path if card is mounted and app has
//     * appropriate permission. Else - Android defines cache directory on
//     * device's file system.
//     *
//     * @param context  Application context
//     * @param cacheDir Cache directory path (e.g.: "AppCacheDir",
//     *                 "AppDir/cache/images")
//     * @return Cache {@link File directory}
//     */
    // public static File getOwnCacheDirectory(Context context, String cacheDir)
    // {
    // File appCacheDir = null;
    // if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
    // hasExternalStoragePermission(context)) {
    // appCacheDir = new File(Environment.getExternalStorageDirectory(),
    // cacheDir);
    // }
    // if (appCacheDir == null || (!appCacheDir.exists() &&
    // !appCacheDir.mkdirs())) {
    // appCacheDir = context.getCacheDir();
    // }
    // return appCacheDir;
    // }
    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 数据缓存路径，根据是否有存储卡，优先获取外部存储，否则系统内部存储 外部：SD card
     * <i>("/Android/data/[app_package_name]/cache" )</i> {like
     * File-getCacheDirectory's path}
     *
     * @param context
     * @return
     */
    public static String getCacheDirectoryPath(Context context) {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = context.getExternalCacheDir().getAbsolutePath() + File.separator;
        } else {
            path = context.getCacheDir() + File.separator;
        }
        return path;
    }

    /**
     * 图片缓存路径，根据是否存在SD卡 {link getCacheDirectoryPath}
     *
     * @param context
     * @return
     */
    public static String getImgCachePath(Context context) {
        String path = getCacheDirectoryPath(context);
        if (!TextUtils.isEmpty(path)) {
            path = path + "img/";
        }
        return path;
    }



    /**
     * 传入文件名，拼接缓存文件路径，获取缓存文件 {缓存路径： getCacheDirectoryPath}
     *
     * @param context
     * @param isNewFile
     * @param fileName
     * @return
     */
    public static File getCacheFile(Context context, boolean isNewFile, String fileName) {
        File file = new File(getCacheDirectoryPath(context) + fileName);
        if (file.exists() || file.length() == 0) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    /**
     * 获取图片文件保存的文件对象，外部存储空间 like: mnt/shell/emulated/0/{path}
     *
     * @param isNewFile
     * @return
     */
    public static File getPicSaveFile(boolean isNewFile, String fileName) {
        return getFile(isNewFile, IMG_DIR, fileName);
    }

    /**
     * 插入图片到相册中
     *
     * @param context
     * @param file
     * @return
     */
    public static void saveImageToGallery(Context context, File file) {
        //保存图片后发送广播通知更新数据库
        Uri uri = Uri.fromFile(file);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    /**
     * 获取Log的存放路径，外部存储空间 like: mnt/shell/emulated/0/{path}
     *
     * @param isNewFile
     * @param fileName
     * @return
     */
    public static File getLogSaveFile(boolean isNewFile, String fileName) {
        return getFile(isNewFile, LOG_DIR, fileName);
    }

    /**
     * 助手预览配置文件保存路径
     *
     * @param isNewFile
     * @param fileName
     * @return
     */
    public static File getPreviewConfigFile(boolean isNewFile, String fileName, String appGuid) {
        StringBuilder builder = new StringBuilder(PREVIEW_DIR);
        builder.append(File.separator);
        builder.append(appGuid);
        builder.append(File.separator);
        builder.append(PREVIEW_CONFIG);
        return getFile(isNewFile, builder.toString(), fileName);
    }

    /**
     * 助手预览配置的资源文件保存路径
     *
     * @param isNewFile
     * @param fileName
     * @return
     */
    public static File getPreviewResFile(boolean isNewFile, String fileName, String appGuid) {
        StringBuilder builder = new StringBuilder(PREVIEW_DIR);
        builder.append(File.separator);
        if (!TextUtils.isEmpty(appGuid)) {
            builder.append(appGuid);
            builder.append(File.separator);
        }
        builder.append(PREVIEW_RESOURCE);
        return getFile(isNewFile, builder.toString(), fileName);
    }

    /**
     * 助手预览配置的资源文件路径名称
     *
     * @return
     */
    public static String getPreviewResFileName(String appGuid) {
        StringBuilder builder = new StringBuilder(PREVIEW_DIR);
        builder.append(File.separator);
        builder.append(appGuid);
        builder.append(File.separator);
        builder.append(PREVIEW_RESOURCE);
        return builder.toString();
    }

    public static File getAudioRecordFile(boolean isNewFile, String fileName) {
        return getFile(isNewFile, AUDIO_RECORD_DIR, fileName);
    }

    /**
     * 判断有无内存卡
     */
    public static boolean hasStorage() {
        String state = android.os.Environment.getExternalStorageState();
        return android.os.Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * 根据路径拼接存储路径（外部存储）
     *
     * @param isNewFile
     * @param dirName
     * @param fileName
     * @return
     */
    public static File getFile(boolean isNewFile, String dirName, String fileName) {
        if (!hasStorage()) {
            return null;
        }
        if (TextUtils.isEmpty(dirName) || TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + dirName + File.separator + fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (file.length() == 0) {
            file.delete();
        }
        if (isNewFile) {
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 根据路径拼接存储路径（外部存储）
     *
     * @param isNewFile
     * @param fileType  图片，音频，视频
     * @param fileName
     * @return
     */
    public static File getOutsideMediaSaveFile(boolean isNewFile, String fileType, String fileName) {
        if (!hasStorage()) {
            return null;
        }
        if (TextUtils.isEmpty(fileType) || TextUtils.isEmpty(fileName)) {
            return null;
        }
        if (!TextUtils.equals(fileType, IMG_DIR) && !TextUtils.equals(fileType, AUDIO_DIR) && !TextUtils.equals(fileType, VIDEO_DIR)) {
            return null;
        }

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileType + File.separator + fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        //部分手机拍照调用时，此处file.delete会导致拍照失败
        /*if (file.length() == 0) {
            file.delete();
        }*/
        if (isNewFile) {
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}
