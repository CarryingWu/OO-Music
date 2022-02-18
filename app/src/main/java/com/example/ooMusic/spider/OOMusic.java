package com.example.ooMusic.spider;

import android.util.Log;
import com.example.ooMusic.bean.OnlineMusicBean;
import com.example.ooMusic.util.GetUrlListenner;
import com.example.ooMusic.util.MusicListenner;

import java.util.List;

public class OOMusic {

    /*通过关键词搜索音乐*/
    public static void searchMusic(String keyWord, MusicListenner musicListenner){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<OnlineMusicBean> onlineMusicBeans = null;
                try {
                    onlineMusicBeans = QQMusicSpider.searchMusic(keyWord);
                    Log.d("url", "onlineMusicBeans: "+ onlineMusicBeans.get(0).getSongMid());
                    musicListenner.getMusicFinish(onlineMusicBeans);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*获取音乐的播放/下载地址*/
    public static void getUrl(String songMid, GetUrlListenner getUrlFinish){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = QQMusicSpider.getUrl(songMid);
                    getUrlFinish.getUrlFinish(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*获取推荐音乐和排行榜音乐*/
    public static void getRecommendSong(int type, MusicListenner musicListenner){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<OnlineMusicBean> onlineMusicBeans = null;
                try {
                    onlineMusicBeans = QQMusicSpider.getSongList(type);
                    //Log.d("url", "onlineMusicBeans: "+onlineMusicBeans.get(0).getSongMid());
                    musicListenner.getMusicFinish(onlineMusicBeans);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
