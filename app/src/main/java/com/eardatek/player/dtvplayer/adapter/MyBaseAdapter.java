package com.eardatek.player.dtvplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 16-6-29.
 */
public abstract class MyBaseAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    protected List<T> mDatas;
    private Context mContext;

    public MyBaseAdapter(List<T> mDatas,Context context) {
        this.mDatas = mDatas;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(getLayoutId(),parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        T item = getItem(position);
        convert(holder,item);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public T getItem(int positon){
        if (positon >= 0 && positon < mDatas.size())
          return mDatas.get(positon);
        return null;
    }

    public void clearItem(){
        if (mDatas != null && mDatas.size() > 0){
            mDatas.clear();
            notifyDataSetChanged();
        }
    }

    public void addItem(T data){
        if (mDatas != null && data != null){
            mDatas.add(mDatas.size(),data);
            notifyItemInserted(mDatas.size() + 1);
        }
    }

    public void addItems(List<T> datas){
        if (mDatas != null && datas != null && datas.size() > 0){
            mDatas.addAll(mDatas.size(),datas);
            notifyItemRangeInserted(mDatas.size() - datas.size(),datas.size());
        }
    }

    protected abstract int getLayoutId();

    protected abstract void convert(ViewHolder holder,T item);

}
