package com.example.ooMusic;


import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ooMusic.download.Constant;
import com.example.ooMusic.fragment.MusicHallFragment;
import com.example.ooMusic.fragment.HomeFragment;
import com.example.ooMusic.fragment.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);
        //根据选择的位置切换fragment
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new HomeFragment()).commit();
                        break;
                    case R.id.navigation_count:
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new MusicHallFragment()).commit();
                        break;
                    case R.id.navigation_user:
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new UserFragment()).commit();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //刚进页面选择home fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new HomeFragment()).commit();
        findViewById(R.id.main_iv_search).setOnClickListener(this);
        //动态申请权限
        requestAccess();
    }



    private void requestAccess (){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"授权成功",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"授权失败，应用讲无法正常使用",Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_iv_search:
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在退出应用时，将在线播放的缓存的音乐清空
        File tempFile = new File(Constant.TEMP_SONG_FILE);
        if (tempFile.exists()){
            for (File file : tempFile.listFiles()){
                file.delete();
            }
            tempFile.delete();
        }
    }
}