package com.eardatek.player.dtvplayer.adapter;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blazevideo.libdtv.ChannelInfo;
import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.callback.OnDataBaseChangeListener;
import com.eardatek.player.dtvplayer.callback.OnDeleteItemPlayingListener;
import com.eardatek.player.dtvplayer.callback.OnMoveAndSwipeListener;
import com.eardatek.player.dtvplayer.callback.OnStartDragOrSwipeListener;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.fragment.RadioFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 16-4-6.
 */
public class RadioChanelGridAdapter extends RecyclerView.Adapter<RadioChanelGridAdapter.ViewHolder> implements OnMoveAndSwipeListener{

    private LayoutInflater mInflater;
    private List<ChannelInfo> mRadioChanelList = new ArrayList<>();
    private ChannelInfoDB mDB = ChannelInfoDB.getInstance();

    private OnStartDragOrSwipeListener mStartDragListener;
    private OnDataBaseChangeListener mDataBaseChangeListener;
    private OnRadioItemClickListener mItemListener;
    private OnDeleteItemPlayingListener mOnDeleteItemPlayingListener;

    private RadioFragment.OnMovePlayingRadioItemListener mMoveRadioListener;

    private int mLastClickPositon = -1;
    private int mSelectItem = -1;

    public RadioChanelGridAdapter(Context context, OnStartDragOrSwipeListener startDragListener, OnDataBaseChangeListener dataBaseChangeListener,
                                  OnRadioItemClickListener listener, OnDeleteItemPlayingListener onDeleteItemPlayingListener) {
        mInflater = LayoutInflater.from(context);
        mStartDragListener = startDragListener;
        mDataBaseChangeListener = dataBaseChangeListener;
        mItemListener = listener;
        mOnDeleteItemPlayingListener = onDeleteItemPlayingListener;
        loadRadioData();
    }

    public boolean isProgramListEmpty(){
        if (mRadioChanelList == null || mRadioChanelList.size() == 0)
            return true;
        return false;
    }

    public List<ChannelInfo> getChannelList(){
        return mRadioChanelList;
    }

    public void clearData(){
        if (mRadioChanelList != null && mRadioChanelList.size() > 0)
            mRadioChanelList.clear();
    }

    private void loadRadioData(){
        if (!mRadioChanelList.isEmpty())
            mRadioChanelList.clear();
        mRadioChanelList = mDB.getAllRadioProgram();
    }

    public void setmMoveRadioListener(RadioFragment.OnMovePlayingRadioItemListener listener) {
        this.mMoveRadioListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.video_list_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ChannelInfo channelInfo = mRadioChanelList.get(position);
        holder.mTvTitle.setText(channelInfo.getTitle());
        holder.mMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mStartDragListener.startDragOrSwipe(holder);
                }
                return false;
            }
        });

        if (position == mSelectItem){
            holder.mLayout.setBackgroundResource(R.drawable.photo_program_card_press);
        }else {
            holder.mLayout.setBackgroundResource(R.drawable.photo_program_card_normal);
        }
    }

    public void setSelectItem(int mSelectItem) {
        this.mSelectItem = mSelectItem;
        this.mLastClickPositon = mSelectItem;
    }

    @Override
    public int getItemCount() {
        return mRadioChanelList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        loadRadioData();
        if (fromPosition < toPosition){
            for (int i = fromPosition;i<toPosition;i++){
                Collections.swap(mRadioChanelList, i, i + 1);
            }
        }else {
            for (int i = fromPosition;i>toPosition;i--){
                Collections.swap(mRadioChanelList,i,i-1);
            }
        }
        //交换RecycleView列表中item的位置
        notifyItemMoved(fromPosition, toPosition);
        mDataBaseChangeListener.onDataBaseChange();
        if (mMoveRadioListener != null)
            mMoveRadioListener.onMovePlayingRadioItem(toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int positon) {
        //删除mChaneList的数据
        loadRadioData();
        String tltle = mRadioChanelList.get(positon).getTitle();
        String location = mRadioChanelList.get(positon).getLocation();
        mRadioChanelList.remove(positon);
        mDB.deleteChanelInfo(tltle, location);
//        mDataBaseChangeListener.onDataBaseChange();
        mOnDeleteItemPlayingListener.onDeletePlayingItem(location);
//        notifyItemRemoved(positon);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private RelativeLayout mLayout;
        private TextView mRadioTips;
        private TextView mTvTitle;
        private ImageView mMenu;

        public ViewHolder(View itemView) {
            super(itemView);

            mLayout = (RelativeLayout) itemView.findViewById(R.id.program_layout);
//            mRadioTips = (TextView) itemView.findViewById(R.id.radio_text);
            mTvTitle = (TextView) itemView.findViewById(R.id.chanel_name);
//            mMenu = (ImageView) itemView.findViewById(R.id.ic_menu);

            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectItem(getLayoutPosition());
                    mItemListener.onRadioItemClick(getLayoutPosition());
                    mLayout.setBackgroundResource(R.drawable.photo_program_card_press);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public int getLastClickPositon() {
        return mLastClickPositon;
    }

    public void setLastClickPositon(int mLastClickPositon) {
        this.mLastClickPositon = mLastClickPositon;
    }

    public interface OnRadioItemClickListener {
        void onRadioItemClick(int positon);
    }

}
