package com.example.ooMusic.spider;


import android.util.Log;

import com.example.ooMusic.bean.OnlineMusicBean;
import com.example.ooMusic.util.JsonUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*QQ音乐主要的网络请求爬虫类*/

public class QQMusicSpider {

    /*根据关键词搜索音乐
    * 可搜索歌手，歌名
    * */
    public static List<OnlineMusicBean> searchMusic(String keyWord) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?n=30&w="+keyWord+"&format=json";
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        String res = response.body().string();
        List<OnlineMusicBean> onlineMusicBeans = JsonUtil.getMusicList(res);
        return onlineMusicBeans;
    }

    /*根据歌曲的mid，获取一首歌曲的URL，用于下载和播放*/
    public static String getUrl(String mid) throws IOException{
        OkHttpClient client = new OkHttpClient();
        String url = "https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22"+mid+"%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D";
        Log.d("url", "postUrl: "+url);
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        String res = response.body().string();
        Log.d("url", "getUrl: "+res);
        String songUrl = JsonUtil.getMusicUrl(res);
        return songUrl;
    }

    /*获取随机热门推荐歌曲*/
    public static List<OnlineMusicBean> getSongList(int type) throws IOException{
        OkHttpClient client = new OkHttpClient();
        String url = null;
        if (type == 0){ //获取推荐歌曲
            url = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?g_tk=5381&uin=0&format=json&inCharset=utf-8&outCharset=utf-8%C2%ACice=0&platform=h5&needNewCode=1&tpl=3&page=detail&type=top&topid=36&_=1520777874472";
        }else { //获取排行歌曲
            url = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?g_tk=5381&uin=0&format=json&inCharset=utf-8&outCharset=utf-8%C2%ACice=0&platform=h5&needNewCode=1&tpl=3&page=detail&type=top&topid=27&_=1519963122923";
        }
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        String res = response.body().string();
        Log.d("url", "getUrl: "+res);
        List<OnlineMusicBean> onlineMusicBeans = JsonUtil.getMusicHallList(res);


        return onlineMusicBeans;
    }

}
