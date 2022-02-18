package com.example.ooMusic.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.ooMusic.bean.LocalMusicBean;
import com.example.ooMusic.App;
import com.example.ooMusic.MainActivity;
import com.example.ooMusic.R;
import com.example.ooMusic.bean.OnlineMusicBean;
import com.example.ooMusic.util.DBManager;

import java.io.File;
import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 *  下载服务
 */

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private OnlineMusicBean mOnlineMusicBean;
    private DownloadBinder downloadBinder = new DownloadBinder();
    private DownloadListener listener = new DownloadListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onProgress(int progress) {
            if (mOnlineMusicBean.getType()==1){
                getNotificationManager().notify(1, getNotification("正在下载...", progress));
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onSuccess() {
            downloadTask = null;
            //是下载，则需要保存到下载目录，并将歌曲信息写入数据库
            if (mOnlineMusicBean.getType()==1){
                //下载成功通知前台服务通知关闭，并创建一个下载成功的通知
                stopForeground(true);
                getNotificationManager().notify(1, getNotification("下载成功", 100));
                String fileName = mOnlineMusicBean.getSongName()+"-"+ mOnlineMusicBean.getSinger()+".m4a";
                String location =Constant.STORAGE_SONG_FILE+fileName;
                DBManager.saveSong(new LocalMusicBean(mOnlineMusicBean.getSongName(),R.mipmap.fengmian, mOnlineMusicBean.getSinger(),location));
            }else { //在线播放，下载到临时缓存目录，不写入数据库，并立即播放
                String fileName = mOnlineMusicBean.getSongName()+"-"+ mOnlineMusicBean.getSinger()+".m4a";
                String location =Constant.TEMP_SONG_FILE+fileName;
                playOnlineMusic(location);
            }

        }


        private void playOnlineMusic(String location){
            try {
                App.mediaPlayer.reset();
                App.mediaPlayer.setDataSource(location);
                App.mediaPlayer.prepare();
                App.mediaPlayer.start();
                App.signName = mOnlineMusicBean.getSongName();
                App.singer = "-"+ mOnlineMusicBean.getSinger();
                App.currentPlayPosition = -2; //播放在线音乐，将正在播放的音乐index写为-2
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(App.getContext(),"播放在线音乐失败，请重试",Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onDownloaded() {
            downloadTask = null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onFailed() {
            downloadTask = null;

            //下载失败通知前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            if (mOnlineMusicBean.getType()==1){
                getNotificationManager().notify(1, getNotification("下载失败", -1));
                Toast.makeText(DownloadService.this, "下载失败", Toast.LENGTH_SHORT).show();
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onPaused() {
            downloadTask = null;
            if (mOnlineMusicBean.getType()==1){
                getNotificationManager().notify(1, getNotification("下载已暂停：", -1));
                Toast.makeText(DownloadService.this, "下载已暂停", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            if (mOnlineMusicBean.getType()==1){
                Toast.makeText(DownloadService.this, "下载已取消", Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }

    public class DownloadBinder extends Binder {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void startDownload(OnlineMusicBean onlineMusicBean) {
            if (downloadTask == null) {
                mOnlineMusicBean = onlineMusicBean;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(mOnlineMusicBean);
                if (mOnlineMusicBean.getType() == 1){
                    startForeground(1, getNotification("正在下载", 0));
                    Looper.prepare();
                    Toast.makeText(App.getContext(), "开始下载", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }else {
                    Looper.prepare();
                    Toast.makeText(App.getContext(), "音乐缓冲中...", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }

        public void pauseDownload(String songId) {
            //暂停的歌曲是否为当前下载的歌曲
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            //如果该歌曲正在下载，则需要将downloadTask置为null
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            }
            //将该歌曲从文件中删除
            if (mOnlineMusicBean != null) {
                File downloadFile = new File(Constant.STORAGE_SONG_FILE);
                String fileName = mOnlineMusicBean.getSongName()+"-"+ mOnlineMusicBean.getSinger()+".m4a";
                File file = new File(downloadFile, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
            getNotificationManager().cancel(1);
            stopForeground(true);
            Toast.makeText(DownloadService.this, "下载已取消", Toast.LENGTH_SHORT).show();
        }

    }

//    private NotificationManager notificationManager(){
//
//        //NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        final Intent resultIntent = new Intent(App.getContext(), DownloadService.class);
//        resultIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(
//                App.getContext(), 0, resultIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        try {
//            NotificationManager notificationManager = (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                //android 8.0 兼容9.0
//                NotificationChannel channel = new NotificationChannel(App.getContext().getPackageName(), "thetest", NotificationManager.IMPORTANCE_DEFAULT);
//                channel.enableLights(true);
//                channel.setLightColor(App.getContext().getResources().getColor(R.color.grey));
//                channel.setShowBadge(true);
//                channel.setDescription(App.getContext().getString(R.string.app_name));
//                // 设置显示模式
//                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//                notificationManager.createNotificationChannel(channel);
//                Notification.Builder builder = new Notification.Builder(App.getContext(), App.getContext().getPackageName());
//                //设置小图标
//                //builder.setSmallIcon(R.drawable.icon);
//                builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.searchbtn));
//                //设置优先级，低优先级可能被隐藏
//                //builder.setPriority(NotificationCompat.PRIORITY_HIGH);
//                //设置通知时间，默认为系统发出通知的时间，通常不用设置
//                builder.setWhen(System.currentTimeMillis());
//                //设置通知栏能否被清楚，true不能被清除，false可以被清除
//                builder.setOngoing(false);
//                builder.setContentTitle("记账提醒");
//                builder.setGroupSummary(true).setGroup(App.getContext().getString(R.string.app_name));
//                builder.setContentText("记账时间到了，赶快记一笔");
//                builder.setAutoCancel(true);//用户点击就自动消失
//                builder.setChannelId(App.getContext().getPackageName());
//                builder.setContentIntent(resultPendingIntent);
////                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
////                builder.setDefaults(NotificationCompat.DEFAULT_ALL);
//                builder.setCategory(Notification.CATEGORY_REMINDER);
//                builder.setOnlyAlertOnce(true);
//                Notification notification = builder.build();
//                notification.notify();
//            } else {
//                //其余版本
//                Notification.Builder builder = new Notification.Builder(App.getContext());
//                //设置小图标
//                builder.setSmallIcon(R.drawable.searchbtn);
//                //设置通知标题
//                builder.setContentTitle("记账提醒");
//                //设置通知类容
//                builder.setContentText("记账时间到了，赶快记一笔");
//                // 设置优先级，低优先级可能被隐藏
//                //builder.setPriority(NotificationCompat.PRIORITY_HIGH);
//                //设置通知时间，默认为系统发出通知的时间，通常不用设置
//                builder.setWhen(System.currentTimeMillis());
//                //设置通知栏能否被清楚，true不能被清除，false可以被清除
//                builder.setOngoing(false);
//                builder.setAutoCancel(true);//用户点击就自动消失
//                builder.setContentIntent(resultPendingIntent);
//                Notification notification = builder.build();
//            }
//            //发布通知
//            notificationManager.notify(101, notification);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }



    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification(String title, int progress) {
        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel = new NotificationChannel("123", "123",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            getNotificationManager().createNotificationChannel(notificationChannel);
            CharSequence name = "OO音乐下载服务";
            String description = "正在下载中";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(App.getContext().getPackageName(), name, importance);
            channel.setDescription(description);
            getNotificationManager().createNotificationChannel(channel);
            builder = new Notification.Builder(DownloadService.this,App.getContext().getPackageName());
        }else {
            builder = new Notification.Builder(DownloadService.this);
        }
        Intent intent = new Intent(DownloadService.this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(DownloadService.this, 0, intent, 0);
            builder.setSmallIcon(R.mipmap.home); //下载图片
            builder.setContentIntent(pi);
            builder.setContentTitle(title);
            if (progress > 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }

            return builder.build();
    }


    //获取歌曲实际大小，然后判断是否存在于文件中
    public void checkoutFile(OnlineMusicBean song, String downloadUrl){
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
                Call call= client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            long size = response.body().contentLength();
                            String fileName = song.getSongName()+"-"+song.getSinger()+".m4a";
                            File downloadFile = new File(Constant.STORAGE_SONG_FILE);
                            String directory = String.valueOf(downloadFile);
                            File file = new File(fileName, directory);
                            if (file.exists()) {
                                file.delete();
                            }
                            getNotificationManager().cancel(1);
                            stopForeground(true);
                        }
                    }
                });
    }

}
