package com.eardatek.player.dtvplayer.adapter;

import android.content.Context;
import android.view.View;

import com.blazevideo.libdtv.ChannelInfo;
import com.eardatek.player.dtvplayer.R;

import java.util.List;

/**
 * Created by Administrator on 16-6-29.
 */
public class ChannelAdapter extends MyBaseAdapter<ChannelInfo> implements View.OnClickListener{


    public ChannelAdapter(List<ChannelInfo> mDatas, Context context) {
        super(mDatas, context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chanelname;
    }

    @Override
    protected void convert(ViewHolder holder, ChannelInfo item) {
        holder.setText(R.id.chanel_name,item.getTitle());
        holder.getView(R.id.chanel_name).setTag(item.getLocation());
        holder.setViewOnclickListenner(R.id.chanel_name,this);

        holder.getView(R.id.card_chanel_name).setAlpha(0.7f);
    }

    @Override
    public void onClick(View v) {

    }
}
