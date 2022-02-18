package com.example.ooMusic.bean;
import org.litepal.crud.LitePalSupport;

public class LocalMusicBean extends LitePalSupport {
    private int id;   //ID
    private String songName;   //歌名
    private String singer;   //歌手
    private String location ;  //音乐路径

    //无参构造
    public LocalMusicBean() {

    }

    //有参构造
    public LocalMusicBean(String songName, int image, String singer, String location) {
        this.songName = songName;
        this.singer = singer;
        this.location = location;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
