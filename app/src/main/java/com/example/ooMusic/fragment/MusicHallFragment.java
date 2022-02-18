package com.example.ooMusic.fragment;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ooMusic.adapter.OnlineMusicAdapter;
import com.example.ooMusic.R;

import com.example.ooMusic.bean.OnlineMusicBean;
import com.example.ooMusic.download.DownloadService;
import com.example.ooMusic.App;
import com.example.ooMusic.util.GetUrlListenner;
import com.example.ooMusic.util.MusicListenner;
import com.example.ooMusic.spider.OOMusic;

import java.util.ArrayList;
import java.util.List;

/*音乐馆，获取热门和推荐音乐*/

public class MusicHallFragment extends Fragment implements View.OnClickListener{
    private ListView listView;

    private View headerView;
    List<OnlineMusicBean> mDatas;

    private TextView tv_recommend,tv_hot;
    private View tv_recommend_active,tv_hot_active;
    OnlineMusicAdapter adapter;
    AlertDialog da; //加载的提示框

    SwipeRefreshLayout mswipeRefreshLayout;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_count, container, false);
        initView(root);
        addLVHeaderView(root);
        mDatas = new ArrayList<>();
        adapter = new OnlineMusicAdapter(root.getContext(), mDatas);
        listView.setAdapter(adapter);
        setEventListener();
        loadData();
        //绑定下载服务
        Intent downIntent = new Intent(getActivity(), DownloadService.class);
        getActivity().bindService(downIntent, mDownloadConnection, Context.BIND_AUTO_CREATE);
        return root;
    }


    private void initView(View root){
        listView = root.findViewById(R.id.musicHall_lv);
        mswipeRefreshLayout = root.findViewById(R.id.sp_musichall);
    }

    // 给ListView添加头布局
    private void addLVHeaderView(View root) {
        Log.d("add", "addLVHeaderView");
        headerView = getLayoutInflater().inflate(R.layout.item_musichall_top, null);
        listView.addHeaderView(headerView);
        tv_recommend = headerView.findViewById(R.id.tv_recommend);
        tv_hot = headerView.findViewById(R.id.tv_hot);
        tv_hot_active = root.findViewById(R.id.tv_hot_active);
        tv_recommend_active = root.findViewById(R.id.tv_recommend_active);
        tv_recommend.setOnClickListener(this);
        tv_hot.setOnClickListener(this);
    }
    private void setEventListener() {
        // 设置每一项的点击事件
        adapter.setOnItemClickListener(new OnlineMusicAdapter.OnItemClickListener(){

            //点击了播放
            @Override
            public void OnItemClick(View view, int position){
                OnlineMusicBean onlineMusic = mDatas.get(position);
                onlineMusic.setType(0);
                downloadMusic(onlineMusic);
            }

            //点击了下载
            @Override
            public void OnDownloadClick(View view, int position) {
                OnlineMusicBean onlineMusic = mDatas.get(position);
                onlineMusic.setType(1);
                downloadMusic(onlineMusic);
            }
        });

        mswipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新需执行的操作
                adapter.notifyDataSetChanged();
                mswipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadData(){
        //如果全局缓存中有，则直接取出来即可，不需要重新获取
        if (App.recommendSongList.size()==0){
            getMusicList(0);
        }else {
            mDatas.addAll(App.recommendSongList);
        }
    }


    private void downloadMusic(OnlineMusicBean onlineMusicBean) {
        //首先获取下载地址
        String songMid = onlineMusicBean.getSongMid();
        OOMusic.getUrl(songMid, new GetUrlListenner() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void getUrlFinish(String url) {
                Log.d("url", "getUrlFinish: " + url);
                if (!url.equals("") && !url.equals("vip")){
                    onlineMusicBean.setUrl(url);
                    mDownloadBinder.startDownload(onlineMusicBean);
                }else {
                    //切换到主线程显示Toast
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (url.equals("vip")){
                                Toast.makeText(getActivity(),"该音乐为vip音乐，无法爬取，抱歉！",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getActivity(),"获取下载地址失败，请检查网络！",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }


    private void getMusicList(int type){
        showLoadDialog();
        OOMusic.getRecommendSong(type,new MusicListenner() {
            @Override
            public void getMusicFinish(List<OnlineMusicBean> onlineMusicBeans) {
                //切换到UI主线程更新UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onlineMusicBeans.size() > 0) {
                            Log.d("my", "searchFinish" + onlineMusicBeans.size());
                            mDatas.clear();
                            mDatas.addAll(onlineMusicBeans);
                            if (type==0){ //把推荐歌曲缓存起来，避免重复加载
                                App.recommendSongList.clear();
                                App.recommendSongList.addAll(onlineMusicBeans);
                            }else {
                                App.hotSongList.clear();
                                App.hotSongList.addAll(onlineMusicBeans);
                            }
                            adapter.notifyDataSetChanged();
                            da.dismiss(); //关掉正在加载的提示框
                        }
                    }
                });
            }
        });
    }

    private void showLoadDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(),R.style.AlertDialog); //创建对话框
        dialog.setIcon(R.mipmap.ic_launcher_round);
        dialog.setTitle("");
        dialog.setMessage("正在加载中，请稍等...");
        dialog.setCancelable(false);    //设置是否可以通过点击对话框外区域或者返回按键关闭对话框
        da = dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (da.isShowing()){
                    da.dismiss();
                    Toast.makeText(getActivity(),"获取歌曲列表失败，请检查网络!",Toast.LENGTH_SHORT).show();
                }
            }
        }, 6000);    //如果6s后发现还是加载，则说明加载失败了，把提示框关掉
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time:
                break;
            case R.id.tv_hot:  //点击了收入统计
                tv_hot_active.setVisibility(View.VISIBLE);
                tv_recommend_active.setVisibility(View.GONE);
                if (App.hotSongList.size()==0){
                    getMusicList(1);
                }else {
                    mDatas.clear();
                    mDatas.addAll(App.hotSongList);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.tv_recommend:  //点击了收入统计
                tv_recommend_active.setVisibility(View.VISIBLE);
                tv_hot_active.setVisibility(View.GONE);
                if (App.recommendSongList.size()==0){
                    getMusicList(0);
                }else {
                    mDatas.clear();
                    mDatas.addAll(App.recommendSongList);
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //解绑下载服务，防止内存泄露
        getActivity().unbindService(mDownloadConnection);
    }

}