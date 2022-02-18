package com.example.ooMusic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ooMusic.adapter.OnlineMusicAdapter;
import com.example.ooMusic.bean.OnlineMusicBean;
import com.example.ooMusic.download.DownloadService;
import com.example.ooMusic.util.GetUrlListenner;
import com.example.ooMusic.util.MusicListenner;
import com.example.ooMusic.spider.OOMusic;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listView;
    TextView tvTip;
    EditText etKeyWord;
    List<OnlineMusicBean> mDatas;
    OnlineMusicAdapter adapter;

    public DownloadService.DownloadBinder mDownloadBinder;

    //绑定下载服务
    private ServiceConnection mDownloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDownloadBinder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();

        //绑定下载服务
        Intent downIntent = new Intent(SearchActivity.this, DownloadService.class);
        bindService(downIntent, mDownloadConnection, Context.BIND_AUTO_CREATE);
    }

    private void initView(){
        listView = findViewById(R.id.search_lv);
        tvTip = findViewById(R.id.tv_tip);
        etKeyWord = findViewById(R.id.edit_keyword);
        mDatas = new ArrayList<>();
        adapter = new OnlineMusicAdapter(SearchActivity.this, mDatas);
        listView.setAdapter(adapter);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.search_btn).setOnClickListener(this);
        setEventListener();
    }


    private void setEventListener() {
        // 设置每一项的点击事件
        adapter.setOnItemClickListener(new OnlineMusicAdapter.OnItemClickListener(){

            //在线播放
            @Override
            public void OnItemClick(View view, int position){
                OnlineMusicBean onlineMusic = mDatas.get(position);
                onlineMusic.setType(0);
                downloadMusic(onlineMusic);
            }

            //下载歌曲
            @Override
            public void OnDownloadClick(View view, int position) {
                OnlineMusicBean onlineMusic = mDatas.get(position);
                onlineMusic.setType(1);
                downloadMusic(onlineMusic);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_btn:
                finish();
                break;
            case R.id.search_btn:
                searchMusic();
                break;
            default:
                break;
        }
    }


    private void searchMusic(){
        try {
            String keyWord = etKeyWord.getText().toString();
            OOMusic.searchMusic(keyWord, new MusicListenner() {
                @Override
                public void getMusicFinish(List<OnlineMusicBean> onlineMusicBeans) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (onlineMusicBeans.size() > 0) {
                                Log.d("finish", "searchFinish" + onlineMusicBeans.size());
                                mDatas.clear();
                                mDatas.addAll(onlineMusicBeans);
                                //把listView显示，隐藏提示
                                listView.setVisibility(View.VISIBLE);
                                tvTip.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            }else {
                                //把listView隐藏，显示提示
                                listView.setVisibility(View.GONE);
                                tvTip.setText("没有搜索到任何结果，换其他的试试吧~");
                                tvTip.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            });
        }catch (Exception e){
            Log.d("btn", "搜索失败");
        }
    }

    private void downloadMusic(OnlineMusicBean onlineMusicBean){
        //首先获取下载地址
        String songMid = onlineMusicBean.getSongMid();
        OOMusic.getUrl(songMid, new GetUrlListenner() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void getUrlFinish(String url) {
                Log.d("url", "getUrlFinish: "+url);
                if (!url.equals("") && !url.equals("vip")){
                    onlineMusicBean.setUrl(url);
                    mDownloadBinder.startDownload(onlineMusicBean);
                }else {
                    //切换到主线程显示Toast
                    runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              if (url.equals("vip")){
                                  Toast.makeText(SearchActivity.this,"该音乐为vip音乐，无法爬取，抱歉！",Toast.LENGTH_SHORT).show();
                              }else {
                                  Toast.makeText(SearchActivity.this,"获取下载地址失败，请检查网络！",Toast.LENGTH_SHORT).show();
                              }
                          }
                      });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //解绑下载服务
        unbindService(mDownloadConnection);
    }

}