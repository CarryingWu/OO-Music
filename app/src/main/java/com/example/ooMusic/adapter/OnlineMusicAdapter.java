package com.example.ooMusic.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ooMusic.R;
import com.example.ooMusic.bean.OnlineMusicBean;

import java.util.List;

public class OnlineMusicAdapter extends BaseAdapter {
    Context context;
    List<OnlineMusicBean> mDatas;
    LayoutInflater inflater;
    public OnlineMusicAdapter(Context context, List<OnlineMusicBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        inflater = LayoutInflater.from(context);
    }
    OnItemClickListener onItemClickListener;



    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        public void OnItemClick(View view, int position);
        public void OnDownloadClick(View view, int position);
    }
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_mainlv,parent,false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        OnlineMusicBean bean = mDatas.get(position);
        holder.idTv.setText(String.valueOf(position+1));
        holder.nameIv.setText(bean.getSongName());
        holder.singerTv.setText("歌手："+bean.getSinger());

        //点击了下载按钮的事件
        holder.downloadIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.OnDownloadClick(v,position);
            }
        });

        //点击其他部分的事件
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.OnItemClick(v,position);
            }
        });
        return convertView;
    }

    class ViewHolder{
        ImageView playIv,downloadIv;
        TextView idTv,nameIv,singerTv;
        public ViewHolder(View view){
            idTv = view.findViewById(R.id.item_mainlv_tv_id);
            nameIv = view.findViewById(R.id.item_mainlv_tv_title);
            singerTv = view.findViewById(R.id.item_mainlv_tv_signer);
            playIv = view.findViewById(R.id.item_mainlv_iv_play);
            downloadIv = view.findViewById(R.id.item_mainlv_iv_download);
        }
    }
}

