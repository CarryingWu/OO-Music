package com.example.ooMusic.util;

import com.example.ooMusic.bean.OnlineMusicBean;

import java.util.List;

public interface MusicListenner {
    void getMusicFinish(List<OnlineMusicBean> onlineMusicBeans);
}
