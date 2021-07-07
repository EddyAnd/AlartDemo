package com.example.alarmdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// ① 创建Adapter
public class NormalAdapter extends RecyclerView.Adapter<NormalAdapter.VH>{
    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder{
        public final TextView title;
        public VH(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
        }
    }
    
    private ArrayList<ItemBean> mDatas;
    public NormalAdapter() {

    }

    public void setClickItem(ClickItem clickItem) {
        this.clickItem = clickItem;
    }

    private ClickItem clickItem;

    public interface  ClickItem{
        void clickItem( ItemBean itemBean);
    }
    public void setDatas(ArrayList<ItemBean> data){
        this.mDatas = data;
        notifyDataSetChanged();
    }

    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(VH holder, final int position) {
        holder.title.setText(mDatas.get(position).title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(clickItem != null){
                   clickItem.clickItem(mDatas.get(position));
               }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mDatas != null){
            return mDatas.size();
        }
       return 0;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new VH(v);
    }
}