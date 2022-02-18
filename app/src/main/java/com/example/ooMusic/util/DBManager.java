package com.example.ooMusic.util;

import android.content.Context;

import com.example.ooMusic.bean.LocalMusicBean;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

/*数据库的操作类*/

public class DBManager {

    public static void initDBManager(Context context){
        LitePal.initialize(context);
    }

    public static List<LocalMusicBean> getAllSong(){
        return LitePal.findAll(LocalMusicBean.class);
    }

    public static boolean saveSong(LocalMusicBean song){
        return song.save();
    }

    public static boolean deleteSong(LocalMusicBean song){
        String location = song.getLocation();
        File file = new File(location);
        if (file.exists()){
            file.delete();
        }
        return LitePal.delete(LocalMusicBean.class,song.getId())>0;
    }
}
