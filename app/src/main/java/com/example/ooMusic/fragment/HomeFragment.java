package com.example.ooMusic.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ooMusic.adapter.LocalMusicAdapter;
import com.example.ooMusic.bean.LocalMusicBean;
import com.example.ooMusic.R;
import com.example.ooMusic.App;
import com.example.ooMusic.util.DBManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment implements View.OnClickListener {
    ListView todayLv;  //展示今日收支情况的ListView
    Button editBtn;
    //声明数据源
    List<LocalMusicBean> mDatas;
    LocalMusicAdapter adapter;
    //头布局相关控件
    View headerView;
    SwipeRefreshLayout mswipeRefreshLayout;
    MediaPlayer mediaPlayer;
    TextView topTitleTv,topSingerTv,topTotolTime,topPlayTime;
    ImageView topPlayIv,topPrevIv,topNextIv,topIconIv;

    ProgressBar progressBar;
    Timer timer;
    TimerTask timerTask;
    Animation rotate; //旋转动画


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initView(root);
        addLVHeaderView(root);
        mDatas = new ArrayList<>();
        mediaPlayer = App.mediaPlayer;
        adapter = new LocalMusicAdapter(root.getContext(), mDatas);
        todayLv.setAdapter(adapter);
        setEventListener();

        //设置图片的旋转动画
        rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.play);
        return root;
    }


    private void setEventListener() {
        // 设置每一项的点击事件
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                App.currentPlayPosition = position;
                LocalMusicBean musicBean = mDatas.get(position);
                playMusicInMusicBean(musicBean);
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

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (App.currentPlayPosition>=0){
                    if (App.currentPlayPosition == mDatas.size()-1){
                        App.currentPlayPosition=0;
                    }else {
                        App.currentPlayPosition+=1;
                    }
                    LocalMusicBean nextBean = mDatas.get(App.currentPlayPosition);
                    playMusicInMusicBean(nextBean);
                }else {
                    topPlayIv.setImageResource(R.mipmap.start);
                    topIconIv.clearAnimation();
                    timer.cancel();
                }
            }
        });
    }


    private void loadData(){
        mDatas.clear();
//        mDatas.add(new AccountBean("光年之外",R.mipmap.fengmian,"邓紫棋","/sdcard/Pictures/爱人.m4a"));
//        mDatas.add(new AccountBean("晴天",R.mipmap.fengmian,"周杰伦","123"));
//        mDatas.add(new AccountBean("丑八怪",R.mipmap.fengmian,"薛之谦","123"));
//        mDatas.add(new AccountBean("告白气球",R.mipmap.fengmian,"周杰伦","123"));
//        mDatas.add(new AccountBean("恋人心",R.mipmap.fengmian,"魏新雨","123"));

        mDatas.addAll(DBManager.getAllSong()); //从数据库中取出所有歌曲的信息
        //Log.d("loca", "loadData: "+ mDatas.get(0).getLocation());
    }



    private void initView(View root) {
        todayLv = root.findViewById(R.id.main_lv);
        setLVLongClickListener();
        mswipeRefreshLayout  = root.findViewById(R.id.swipeRefresh);
    }

    //设置ListView的长按事件
    private void setLVLongClickListener() {
        todayLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {  //点击了头布局
                    return false;
                }
                int pos = position-1;
                LocalMusicBean clickBean = mDatas.get(pos);  //获取正在被点击的这条账目
                //弹出提示用户是否删除的对话框
                showDeleteItemDialog(clickBean);
                return false;
            }
        });
    }
    // 弹出是否删除某一条记录的对话框
    private void showDeleteItemDialog(final LocalMusicBean clickBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.AlertDialog);
        builder.setTitle("提示信息").setMessage("确定要删除这首歌吗？")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //执行删除的操作
                        DBManager.deleteSong(clickBean);
                        mDatas.remove(clickBean);   //实时刷新，移除集合当中的对象
                        adapter.notifyDataSetChanged();   //提示适配器更新数据
                        loadData(); //重新加载数据
                    }
                });
        builder.create().show();
    }

    // 给ListView添加头布局
    private void addLVHeaderView(View root) {
        headerView = getLayoutInflater().inflate(R.layout.item_mainlv_top, null);
        todayLv.addHeaderView(headerView);
        topTitleTv = headerView.findViewById(R.id.local_music_bottom_tv_song);
        topSingerTv = headerView.findViewById(R.id.item_mainlv_top_tv_singer);
        topPlayIv = headerView.findViewById(R.id.item_music_bottom_iv_play);
        topPrevIv = headerView.findViewById(R.id.item_music_bottom_iv_last);
        topNextIv = headerView.findViewById(R.id.item_music_bottom_iv_next);
        topIconIv = headerView.findViewById(R.id.item_mainlv_top_iv_icon);
        //topIconIv.setAnimation(rotate);
        topTotolTime = headerView.findViewById(R.id.tv_totoltime);
        topPlayTime = headerView.findViewById(R.id.tv_playtime);
        progressBar = root.findViewById(R.id.pb_music);
        //设置下进度条的颜色，默认的不好看
        ClipDrawable drawable = new ClipDrawable(new ColorDrawable(Color.RED), Gravity.LEFT, ClipDrawable.HORIZONTAL);
        progressBar.setProgressDrawable(drawable);
        topPrevIv.setOnClickListener(this);
        topNextIv.setOnClickListener(this);
        topPlayIv.setOnClickListener(this);

    }

     //当fragment被重新加载时执行刷新恢复操作
    @Override
    public void onStart() {
        super.onStart();
        mDatas.clear();
        mDatas.addAll(DBManager.getAllSong()); //刷新音乐列表
        adapter.notifyDataSetChanged();

        //还原当前正在播放的信息
        if (App.signName != null) {
            topTitleTv.setText(App.signName);
            topSingerTv.setText(App.singer);
            if (mediaPlayer.isPlaying()) {
                topPlayIv.setImageResource(R.mipmap.stop);
                topIconIv.startAnimation(rotate);
            }
        }
        setProgressBar();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_music_bottom_iv_last:

                //说明未选择音乐或在播放在线音乐
                if (App.currentPlayPosition<0){
                    return;
                }
                // 切换上一首音乐
                if (App.currentPlayPosition == 0) {
                    Toast.makeText(getActivity(),"前边没有啦，已经是第一首了！",Toast.LENGTH_SHORT).show();
                    return;
                }
                App.currentPlayPosition = App.currentPlayPosition - 1;
                LocalMusicBean lastBean = mDatas.get(App.currentPlayPosition);
                playMusicInMusicBean(lastBean);
                break;
            case R.id.item_music_bottom_iv_next:
                //说明未选择音乐或在播放在线音乐，不能进行下一首的操作
                if (App.currentPlayPosition < 0){
                    return;
                }
                // 切换下一首音乐
                if (App.currentPlayPosition == mDatas.size()-1) {
                    Toast.makeText(getActivity(),"后边没有啦，已经是最后一首了！",Toast.LENGTH_SHORT).show();
                    return;
                }
                App.currentPlayPosition = App.currentPlayPosition + 1;
                LocalMusicBean nextBean = mDatas.get(App.currentPausePositionInSong);
                playMusicInMusicBean(nextBean);

                break;
            case R.id.item_music_bottom_iv_play:
                if (App.currentPlayPosition == -1) {
                    // 并没有选中播放的音乐
                    Toast.makeText(getActivity(), "请先选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    // 此时正在播放，则暂停音乐
                    pauseMusic();
                }else {
                    // 此时没有播放音乐，点击开始播放
                    playMusic();
                }
                break;
            default:
                break;
        }
    }


    public void playMusicInMusicBean(LocalMusicBean musicBean) {

        // 根据传入对象播放音乐
        // 设置头部显示的歌手名称和歌曲名
        topTitleTv.setText(musicBean.getSongName());
        topSingerTv.setText("-"+musicBean.getSinger());
        App.signName = musicBean.getSongName();
        App.singer = musicBean.getSinger();
        stopMusic();
        // 重置多媒体播放器
        mediaPlayer.reset();
        // 设置新的播放路径
        try {
            mediaPlayer.setDataSource(musicBean.getLocation());
            playMusic();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 点击播放按钮
     * 两种情况
     * 1.播放音乐
     * 2.从暂停状态开始播放
     */
    private void playMusic() {
        // 播放音乐 两种情况
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (App.currentPausePositionInSong == 0) {
                // 从头开始播放
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    Toast.makeText(getActivity(),"当前无有效歌曲信息",Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // 从暂停状态到播放
                mediaPlayer.seekTo(App.currentPausePositionInSong);
                mediaPlayer.start();
            }
            setProgressBar();
            topPlayIv.setImageResource(R.mipmap.stop);
            topIconIv.startAnimation(rotate);
        }
    }

    private String formatTime(int length) {

        Date date = new Date(length);//调用Date方法获值

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");//规定需要形式

        String TotalTime = simpleDateFormat.format(date);//转化为需要形式

        return TotalTime;

    }

    private void setProgressBar(){

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            String timeNum = formatTime(mediaPlayer.getDuration());
            topTotolTime.setText(timeNum);

            progressBar.setMax(mediaPlayer.getDuration());
            if (timer != null) {
                timer = null;
                timerTask = null;
            }
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(getActivity()!=null){ //这里不加这个判断有几率获取不到Activity而导致程序崩溃
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(mediaPlayer.getCurrentPosition());
                                topPlayTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                                if (topIconIv.getAnimation()==null){
                                    topIconIv.startAnimation(rotate);
                                }

                            }
                        });
                    }
                }
            };
            timer.schedule(timerTask, 0, 1000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer!=null){
            timer.cancel();
        }
    }

    private void stopMusic(){
        // 停止播放
        if (mediaPlayer!=null){
            App.currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            topPlayIv.setImageResource(R.mipmap.start);
            topIconIv.clearAnimation();
        }
    }
    private void pauseMusic() {
        // 暂停音乐
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            App.currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            topPlayIv.setImageResource(R.mipmap.start);
            topIconIv.clearAnimation();
            timer.cancel();
        }
    }

}