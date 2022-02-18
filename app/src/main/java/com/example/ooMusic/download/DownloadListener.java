package com.example.ooMusic.download;


/**
 监听下载过程中的各种状态的接口
 */

public interface DownloadListener {
    void onProgress(int progress); //进度
    void onSuccess(); //成功
    void onDownloaded();//已经下载过的歌曲
    void onFailed(); //失败
    void onPaused();  //暂停
    void onCanceled(); //取消
}
