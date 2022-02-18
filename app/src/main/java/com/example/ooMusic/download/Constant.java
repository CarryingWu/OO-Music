package com.example.ooMusic.download;

import android.os.Environment;

/**
 * 定义了一些常量的类，主要用于下载相关，所以定义在了download包中
 */

public class Constant {
    //网络与非网络歌曲
    public static final int SONG_ONLINE = 0;
    public static final int SONG_LOCAL = 1;

    //存储位置
    public static String STORAGE_SONG_FILE= Environment.getExternalStorageDirectory() + "/ooMusic/download/";
    public static String TEMP_SONG_FILE= Environment.getExternalStorageDirectory() + "/ooMusic/temp/";

    //下载状态
    public final static int TYPE_DOWNLOADING = 0;
    public final static int TYPE_DOWNLOAD_PAUSED = 1;
    public final static int TYPE_DOWNLOAD_CANCELED = 2;
    public final static int TYPE_DOWNLOAD_SUCCESS = 3;
    public final static int TYPE_DOWNLOAD_FAILED = 4;
    public final static int TYPE_DOWNLOADED = 5;
    public final static int TYPE_DOWNLOAD_ADD=6;
}

