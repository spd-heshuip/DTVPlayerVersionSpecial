package com.eardatek.special.player.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eardatek.special.player.R;
import com.eardatek.special.player.bean.EpgItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Create By Administrator on 16-3-10 in com.eardatek.player.dtvplayer.adapter.
 * 邮箱：spd_heshuip@163.com;
 */
public class EpgListAdapter extends RecyclerView.Adapter<EpgListAdapter.ViewHolder>{

    private static final String TAG = EpgListAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_RIGHT = 0;
    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_HEAD = 2;
    private int VIEW_TYPE = 3;

    private LayoutInflater mInflater;

    private List<EpgItem> mEpgList = new ArrayList<>();

    public EpgListAdapter(Context context,List<EpgItem> epgItemList) {
        mInflater = LayoutInflater.from(context);

        mEpgList = epgItemList;

    }

    public void clearData(){
        if (mEpgList != null && mEpgList.size() > 0)
            mEpgList.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_LEFT){
            view = mInflater.inflate(R.layout.activity_left_epg,parent,false);
        }else if (viewType == VIEW_TYPE_RIGHT){
            view = mInflater.inflate(R.layout.activity_right_epg,parent,false);
        }else {
            view = mInflater.inflate(R.layout.activity_epg_head,parent,false);
        }
        VIEW_TYPE = viewType;
        return new ViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EpgItem item = mEpgList.get(position);
        if (VIEW_TYPE == VIEW_TYPE_HEAD)
            holder.mTextHead.setText(item.mText);
        else {
            holder.mTextTime.setText(item.mTime);
            holder.mTextProgramName.setText(item.mDuration);
            holder.mTextDetail.setText(item.mText);
        }
    }

    @Override
    public int getItemViewType(int position) {
        EpgItem item = mEpgList.get(position);
        if (item.mType == 0){
            VIEW_TYPE = VIEW_TYPE_HEAD;
            return VIEW_TYPE_HEAD;
        }
        else {
            if (position % 2 == VIEW_TYPE_RIGHT){
                VIEW_TYPE = VIEW_TYPE_RIGHT;
                return VIEW_TYPE_RIGHT;
            }else {
                VIEW_TYPE = VIEW_TYPE_LEFT;
                return VIEW_TYPE_LEFT;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mEpgList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextHead;

        private TextView mTextTime;
        private TextView mTextProgramName;
        private TextView mTextDetail;

        public ViewHolder(View itemView,int viewType) {
            super(itemView);

            if (viewType == VIEW_TYPE_HEAD){
                mTextHead = (TextView) itemView.findViewById(R.id.group_list_item_text);
            }else {
                mTextTime = (TextView) itemView.findViewById(R.id.epg_time);
                mTextProgramName = (TextView) itemView.findViewById(R.id.epg_name);
                mTextDetail = (TextView) itemView.findViewById(R.id.epg_program_detail);
            }
        }
    }
}
