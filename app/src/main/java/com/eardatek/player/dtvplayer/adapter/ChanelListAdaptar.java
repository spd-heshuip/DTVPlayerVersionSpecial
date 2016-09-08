package com.eardatek.player.dtvplayer.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.data.TvDataProvider;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.util.ListUtil;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Create By Administrator on 16-1-25 in com.eardatek.player.dtvplayer.adapter.
 * 邮箱：spd_heshuip@163.com;
 */
public class ChanelListAdaptar extends RecyclerView.Adapter<ChanelListAdaptar.ViewHolder> {

    private List<TvDataProvider.ConcreteData> mChanelList = new ArrayList<>();
    private LayoutInflater mInflater;
    private StringBuffer mSelectProgramLocation = new StringBuffer("");

    private OnItemClickListener mListenner;

    private int mSelectPosition = -1;

    public ChanelListAdaptar(Context context,OnItemClickListener listener) {
        loadData();
        this.mInflater = LayoutInflater.from(context);
        this.mListenner = listener;
    }

    public void clearData(){
        if (mChanelList != null && mChanelList.size() > 0)
            mChanelList.clear();
    }

    public void loadData()
    {
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;
        mChanelList = ListUtil.getListFromFile("Channel.txt");
        if (mChanelList.size() == 0){
            for (int i = 0;i < ChannelInfoDB.getInstance().getAllVideoProgram().size();i++){
                String text = ChannelInfoDB.getInstance().getAllVideoProgram().get(i).getLocation();
                mChanelList.add(new TvDataProvider.ConcreteData(i,0,text,swipeReaction));
            }
        }
    }

    public List<TvDataProvider.ConcreteData> getChanelList(){
        if (mChanelList != null && mChanelList.size() > 0){
            return mChanelList;
        }else
            return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chanelname, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TvDataProvider.ConcreteData channelInfo = mChanelList.get(position);

        holder.mChanelName.setText(ChannelInfoDB.getInstance().getChannelInfo(channelInfo.getText()).getTitle());
        holder.mChanelName.setTag(channelInfo.getText());

        if (channelInfo.getText().equals(mSelectProgramLocation.toString())){
            holder.mCard.setBackgroundResource(R.drawable.photo_gallery_pressed);
            mSelectPosition = position;
        }else
            holder.mCard.setBackgroundResource(R.drawable.bg_channelname_fullscreen);
    }

    @Override
    public int getItemCount() {
        return mChanelList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mChanelName;
        private CardView mCard;

        public ViewHolder(View itemView) {
            super(itemView);

            mCard = (CardView) itemView.findViewById(R.id.card_chanel_name);
            mChanelName = (TextView) itemView.findViewById(R.id.chanel_name);
            mChanelName.setOnClickListener(this);
            mCard.setAlpha(0.7f);
        }

        @Override
        public void onClick(View v) {
            String location = v.getTag().toString();
            setSelectProgram(location);
//            mChanelName.setBackgroundResource(R.drawable.select_play_chanel_press);
            mListenner.onClick(location,true,getLayoutPosition());
        }
    }

    public void setSelectProgram(String location){
        mSelectProgramLocation = new StringBuffer(location);
        notifyDataSetChanged();
    }

    public int getSelectPosition(){
        return mSelectPosition;
    }

    public interface OnItemClickListener{
         void onClick(String location,boolean isReplay,int position);
    }
}
