package com.example.ooMusic.bean;

import org.litepal.crud.LitePalSupport;

/**
在线音乐的信息类
 */


public class OnlineMusicBean extends LitePalSupport {
    private String songMid; //这是获取音乐的播放地址的关键参数，在返回歌曲的详细信息中可以提取到
    private String songName;
    private String singer;
    private String url; //歌曲的下载/播放URL
    private int type; //这里申明type是为了配合下载服务，如果是0，则为在线播放，不需要保存到数据库中，是1则为下载

    //无参构造
    public OnlineMusicBean(){
    }

    public OnlineMusicBean(String songMid, String songName, String singer){
        this.songMid = songMid;
        this.songName = songName;
        this.singer = singer;
    }


    public String getSongMid() {
        return songMid;
    }

    public void setSongMid(String songMid) {
        this.songMid = songMid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
