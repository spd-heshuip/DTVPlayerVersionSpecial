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
import com.eardatek.player.dtvplayer.fragment.ProgramFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作者：Create By Administrator on 16-2-23 in com.eardatek.player.dtvplayer.adapter.
 * 邮箱：spd_heshuip@163.com;
 */
public class TvChannelGridAdapter  extends RecyclerView.Adapter<TvChannelGridAdapter.ViewHolder> implements OnMoveAndSwipeListener{

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ChannelInfo> mChannelList;
    private ChannelInfoDB mDB = ChannelInfoDB.getInstance();
    private OnStartDragOrSwipeListener mStartDragListener;
    private OnDataBaseChangeListener mDataBaseChangeListener;

    private OnItemClickListener mItemListener;

    private OnDeleteItemPlayingListener mOnDeleteItemPlayingListener;

    private ProgramFragment.OnMovePlayingItemListener mMovePlayingItemListener;

    private int mLastClickPositon = -1;

    private int mSelectItem = -1;

    public TvChannelGridAdapter(Context context, OnStartDragOrSwipeListener startDragListener, OnDataBaseChangeListener dataBaseChangeListener,
                                OnItemClickListener listener, OnDeleteItemPlayingListener onDeleteItemPlayingListener) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mStartDragListener = startDragListener;
        mDataBaseChangeListener = dataBaseChangeListener;
        mItemListener = listener;
        mOnDeleteItemPlayingListener = onDeleteItemPlayingListener;
        mChannelList = new ArrayList<>();
        loadData();
    }

    public boolean isProgramListEmpty(){
        if (mChannelList == null || mChannelList.size() == 0)
            return true;
        return false;
    }

    public List<ChannelInfo> getChannelList(){
        return mChannelList;
    }

    public void clearData(){
        if (mChannelList != null && mChannelList.size() > 0)
            mChannelList.clear();
    }

    /**
     * load program list from database
     */
    public void loadData()
    {
        if (!mChannelList.isEmpty())
            mChannelList.clear();
        mChannelList = mDB.getAllVideoProgram();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.video_list_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ChannelInfo channelInfo = mChannelList.get(position);
        holder.mRadioTips.setText("");
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

    public void setMovePlayingItemListener(ProgramFragment.OnMovePlayingItemListener listener) {
        this.mMovePlayingItemListener = listener;
    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //交换mChanelList数据的位置
        loadData();
        if (fromPosition < toPosition){
            for (int i = fromPosition;i<toPosition;i++){
                Collections.swap(mChannelList,i,i+1);
            }
        }else {
            for (int i = fromPosition;i>toPosition;i--){
                Collections.swap(mChannelList,i,i-1);
            }
        }
        //交换RecycleView列表中item的位置
        notifyItemMoved(fromPosition, toPosition);
        mDataBaseChangeListener.onDataBaseChange();
        if (mMovePlayingItemListener != null)
            mMovePlayingItemListener.onMoveItem(toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int positon) {
        //删除mChaneList的数据
        loadData();
        String tltle = mChannelList.get(positon).getTitle();
        String location = mChannelList.get(positon).getLocation();
        mChannelList.remove(positon);
        mDB.deleteChanelInfo(tltle, location);
//        mDataBaseChangeListener.onDataBaseChange();
        mOnDeleteItemPlayingListener.onDeletePlayingItem(location);
        notifyDataSetChanged();
    }

    public int getLastClickPositon() {
        return mLastClickPositon;
    }

    public void setSelectItem(int mSelectItem) {
        this.mSelectItem = mSelectItem;
        this.mLastClickPositon = mSelectItem;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private RelativeLayout mLayout;
        private TextView mRadioTips;
        private TextView mTvTitle;
        private ImageView mMenu;

        public ViewHolder(View itemView) {
            super(itemView);

            mLayout = (RelativeLayout) itemView.findViewById(R.id.program_layout);
            mTvTitle = (TextView) itemView.findViewById(R.id.chanel_name);
            mMenu = (ImageView) itemView.findViewById(R.id.ic_menu);

            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectItem(getLayoutPosition());
                    mItemListener.onItemClick(getLayoutPosition());
                    mLayout.setBackgroundResource(R.drawable.photo_program_card_press);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int positon);
    }
}
