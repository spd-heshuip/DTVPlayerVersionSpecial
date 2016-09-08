package com.eardatek.player.dtvplayer.fragment;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blazevideo.libdtv.ChannelInfo;
import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.actitivy.EardatekVersion2Activity;
import com.eardatek.player.dtvplayer.adapter.RadioChanelGridAdapter;
import com.eardatek.player.dtvplayer.callback.MyEvents;
import com.eardatek.player.dtvplayer.callback.OnDataBaseChangeListener;
import com.eardatek.player.dtvplayer.callback.OnDeleteItem;
import com.eardatek.player.dtvplayer.callback.OnDeleteItemPlayingListener;
import com.eardatek.player.dtvplayer.callback.OnRadioChanelItemClickListener;
import com.eardatek.player.dtvplayer.callback.OnStartDragOrSwipeListener;
import com.eardatek.player.dtvplayer.callback.OnStateChangeListener;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Administrator on 16-3-30.
 */
public class RadioFragment extends StatedFragment implements OnStartDragOrSwipeListener,
        OnDataBaseChangeListener,RadioChanelGridAdapter.OnRadioItemClickListener,
        OnDeleteItemPlayingListener{

    private View mRootViews;
    private RecyclerView mRaioRecycleView;
    private TextView mTextRadioTips;
    private RadioChanelGridAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootViews = inflater.inflate(R.layout.fragment_radio,container,false);

        initView();
        initRadioChanelData();

        return mRootViews;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        EardatekVersion2Activity activity = (EardatekVersion2Activity) getActivity();
        if (activity != null && activity.isVideoPlaying){
            mAdapter.setSelectItem(-1);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAdapter = new RadioChanelGridAdapter(getActivity(), this, this,this,this);
        if (mAdapter.isProgramListEmpty()){
            mTextRadioTips.setVisibility(View.VISIBLE);
        }else {
            mTextRadioTips.setVisibility(View.GONE);
            mRaioRecycleView.setAdapter(mAdapter);
        }
    }

    private void initView(){
        mRaioRecycleView = (RecyclerView) mRootViews.findViewById(R.id.radio_recycle);
        mTextRadioTips = (TextView) mRootViews.findViewById(R.id.radio_empty_tips);

        mRaioRecycleView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mRaioRecycleView.setHasFixedSize(true);
        mRaioRecycleView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initRadioChanelData(){
        mAdapter = new RadioChanelGridAdapter(getActivity(),this,this,this,this);
        if (mAdapter.isProgramListEmpty()){
            mTextRadioTips.setVisibility(View.VISIBLE);
        }else {
            mTextRadioTips.setVisibility(View.GONE);
            mRaioRecycleView.setAdapter(mAdapter);
        }

        ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                ItemTouchHelper.UP) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof OnStateChangeListener) {
                        OnStateChangeListener listener = (OnStateChangeListener) viewHolder;
                        listener.onItemSelected();
                    }
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (viewHolder instanceof OnStateChangeListener) {
                    OnStateChangeListener listener = (OnStateChangeListener) viewHolder;
                    listener.onItemClear();
                }
            }

            //
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    final float alpha = 1.0f - Math.abs(dX) / viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        mItemTouchHelper = new ItemTouchHelper(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRaioRecycleView);
    }

    private void updateDataBase(){
        ChannelInfoDB db = ChannelInfoDB.getInstance();
        List<ChannelInfo> list = db.getAllVideoProgram();
        db.emptyDatabase();
        for (int i = 0;i < mAdapter.getChannelList().size();i++){
            db.addChannelInfo(mAdapter.getChannelList().get(i));
        }
        for (int i = 0;i < list.size();i++){
            db.addChannelInfo(list.get(i));
        }
    }
    @Override
    protected void onSaveState(Bundle bundle) {
        super.onSaveState(bundle);
        bundle.putInt("AdapterPosition", mAdapter.getLastClickPositon());
    }

    @Override
    protected void onRestore(Bundle restoreBundle) {
        super.onRestore(restoreBundle);
        mAdapter.setSelectItem(restoreBundle.getInt("AdapterPosition"));
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDataBaseChange() {
        if (mAdapter.isProgramListEmpty())
            mTextRadioTips.setVisibility(View.VISIBLE);

        updateDataBase();
    }

    @Override
    public void onDeletePlayingItem(String location) {
        if (getActivity() instanceof OnDeleteItem){
            ((OnDeleteItem) getActivity()).onDeleteItem(location,true);
        }
    }

    @Override
    public void onRadioItemClick(int positon) {
        String location = mAdapter.getChannelList().get(positon).getLocation();
        if (getActivity() instanceof OnRadioChanelItemClickListener){
            ((OnRadioChanelItemClickListener) getActivity()).onRadioItemCLick(location, positon);
        }
        MyEvents.postEvent(MyEvents.RADIO_PLAY,null);
    }

    @Override
    public void startDragOrSwipe(RecyclerView.ViewHolder viewholder) {
        mItemTouchHelper.startSwipe(viewholder);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEventBus(MyEvents events) {
        switch(events.getEventType()){
            case MyEvents.DATA_BASE_EMPTY:
                mAdapter.clearData();
                mTextRadioTips.setVisibility(View.VISIBLE);
                mRaioRecycleView.setAdapter(mAdapter);
                break;
            case MyEvents.VIDEO_PLAY:
                mAdapter.setSelectItem(-1);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    public interface OnMovePlayingRadioItemListener{
        void onMovePlayingRadioItem(int toPostion);
    }
}
