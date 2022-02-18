package com.example.ooMusic.util;

import com.example.ooMusic.bean.OnlineMusicBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*爬取到的json数据的解析类，使用JSONObject进行解析*/

public class JsonUtil {
    public static List<OnlineMusicBean> getMusicList(String jsonData){
        List<OnlineMusicBean> onlineMusicBeans = new ArrayList<>();

        try {
            JSONObject mainObject = new JSONObject(jsonData);
            JSONObject dataObject =mainObject.getJSONObject("data");
            JSONArray songList = dataObject.getJSONObject("song").getJSONArray("list");

            for (int i=0;i<songList.length();i++){
                JSONObject songObject = songList.getJSONObject(i);
                String songMid = songObject.getString("songmid");
                String songName = songObject.getString("songname");
                String signer = "";
                JSONArray signerList = songObject.getJSONArray("singer");
                for (int j =0;j<signerList.length();j++){
                    JSONObject signerObject = signerList.getJSONObject(j);
                    if (signer.equals("")){
                        signer += signerObject.getString("name");
                    }else {
                        signer += "/" + signerObject.getString("name");
                    }
                }

                onlineMusicBeans.add(new OnlineMusicBean(songMid,songName,signer));
            }
            return onlineMusicBeans;
        }catch (JSONException e){
            return onlineMusicBeans;
        }

    }

    public static String getMusicUrl(String jsonData){
        String url = "";
        try {
            JSONObject mainObject = new JSONObject(jsonData);
            JSONObject dataObject =mainObject.getJSONObject("req_0").getJSONObject("data");
            JSONArray sipList = dataObject.getJSONArray("sip");
            url = sipList.getString(0); //播放地址的头部
            JSONObject midUrlInfoObject = dataObject.getJSONArray("midurlinfo").getJSONObject(0);
            String purl = midUrlInfoObject.getString("purl");
            if (purl.equals("")){
                return "vip"; //该音乐为vip音乐，无法爬虫获取到播放地址
            }
            url+=purl;
            return url;
        }catch (Exception e){
            return "";
        }
    }


    public static List<OnlineMusicBean> getMusicHallList(String jsonData){
        List<OnlineMusicBean> onlineMusicBeans = new ArrayList<>();
        try {
            JSONObject mainObject = new JSONObject(jsonData);
            JSONArray songList = mainObject.getJSONArray("songlist");
            for (int i=0;i<songList.length();i++){
                JSONObject songObject = songList.getJSONObject(i).getJSONObject("data");
                String songMid = songObject.getString("songmid");
                String songName = songObject.getString("songname");
                String signer = "";
                JSONArray signerList = songObject.getJSONArray("singer");
                for (int j =0;j<signerList.length();j++){
                    JSONObject signerObject = signerList.getJSONObject(j);
                    if (signer.equals("")){
                        signer += signerObject.getString("name");
                    }else {
                        signer += "/" + signerObject.getString("name");
                    }
                }

                onlineMusicBeans.add(new OnlineMusicBean(songMid,songName,signer));
            }
            return onlineMusicBeans;
        }catch (JSONException e){
            return onlineMusicBeans;
        }

    }

}
