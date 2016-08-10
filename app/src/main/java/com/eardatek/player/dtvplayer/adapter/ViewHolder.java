package com.eardatek.player.dtvplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 16-6-29.
 */
public class ViewHolder extends RecyclerView.ViewHolder{

    protected SparseArray<View> mViews = new SparseArray<>();

    public ViewHolder(View itemView) {
        super(itemView);
    }

    public int getmPosition() {
        int  position = getLayoutPosition();
        return position;
    }

    public <T extends View> T getView(int resId){
        View view = mViews.get(resId);
        if (view == null){
            view = itemView.findViewById(resId);
            mViews.put(resId,view);
        }

        return (T)view;
    }

    public void setText(int viewId, String text){
        if (TextUtils.isEmpty(text))
            return;
        TextView tv = getView(viewId);
        tv.setText(text);
    }

    public void setText(int viewId,int resourceId){
        TextView tv = getView(viewId);
        tv.setText(resourceId);
    }

    public void setImage(int viewId,int resId){
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
    }

    public void setViewOnclickListenner(int viewId,View.OnClickListener listener){
        View view = getView(viewId);
        view.setOnClickListener(listener);
    }
}
