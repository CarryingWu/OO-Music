package com.example.ooMusic;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;

import com.example.ooMusic.bean.OnlineMusicBean;
import com.example.ooMusic.util.DBManager;


import java.util.ArrayList;
import java.util.List;

/**
 * 获取全局Context以及定义全局变量
 */

public class App extends Application {


    private static Context context;
    public static MediaPlayer mediaPlayer;

    public static int currentPlayPosition = -1;

    public static int currentPausePositionInSong = 0;

    public static String signName;
    public static String singer;


    public static List<OnlineMusicBean> recommendSongList;
    public static List<OnlineMusicBean> hotSongList;

    /*进行全局初始化*/
    @Override
    public void onCreate(){  //此函数会在应用启动时最先执行
        super.onCreate();
        context=getApplicationContext();
        DBManager.initDBManager(context);
        mediaPlayer = new MediaPlayer();
        recommendSongList = new ArrayList<>();
        hotSongList = new ArrayList<>();
    }


    public static Context getContext(){
        return context;
    }
}
