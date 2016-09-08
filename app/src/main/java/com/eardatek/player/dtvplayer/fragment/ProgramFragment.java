package com.eardatek.player.dtvplayer.fragment;

import android.content.Context;
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
import android.view.ViewStub;

import com.blazevideo.libdtv.ChannelInfo;
import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.actitivy.EardatekVersion2Activity;
import com.eardatek.player.dtvplayer.adapter.TvChannelGridAdapter;
import com.eardatek.player.dtvplayer.callback.MyEvents;
import com.eardatek.player.dtvplayer.callback.OnDataBaseChangeListener;
import com.eardatek.player.dtvplayer.callback.OnDeleteItem;
import com.eardatek.player.dtvplayer.callback.OnDeleteItemPlayingListener;
import com.eardatek.player.dtvplayer.callback.OnTvChanelItemClickListener;
import com.eardatek.player.dtvplayer.callback.OnStartDragOrSwipeListener;
import com.eardatek.player.dtvplayer.callback.OnStateChangeListener;
import com.eardatek.player.dtvplayer.callback.OnchangeProgramListener;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Administrator on 16-3-30.
 */
public class ProgramFragment extends StatedFragment implements OnStartDragOrSwipeListener,
        OnDataBaseChangeListener,TvChannelGridAdapter.OnItemClickListener,
        OnDeleteItemPlayingListener,OnchangeProgramListener{
    private static final String TAG = ProgramFragment.class.getSimpleName();

    private View mRootView;
    private RecyclerView mProgramGrid;
    private ItemTouchHelper mItemTouchHelper;
    private TvChannelGridAdapter mAdapter;
    private ViewStub mViewStub;
    private View mView;
    private OnMovePlayingItemListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnMovePlayingItemListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i(TAG, "OncreateView");
        mRootView = inflater.inflate(R.layout.fragment_program,container,false);

        initRecycleView(mRootView);
        initProgramGridData();

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtil.i(TAG, "onActivityCreated");
        EventBus.getDefault().register(this);
        super.onActivityCreated(savedInstanceState);
        EardatekVersion2Activity activity = (EardatekVersion2Activity) getActivity();
        if (activity != null && activity.isRadioPlaying){
            mAdapter.setSelectItem(-1);
            mAdapter.notifyDataSetChanged();
        }
        if (activity != null && activity.isChangedProgram()){
            activity.setIsChangedProgram(false);
            mAdapter.setSelectItem(activity.getmTvGridAdapterPosition());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LogUtil.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        LogUtil.i(TAG, "onDestroyView");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEventBus(MyEvents events) {
        switch (events.getEventType()){
            case MyEvents.RADIO_PLAY:
                LogUtil.i(TAG,"radio play");
                mAdapter.setSelectItem(-1);
                mAdapter.notifyDataSetChanged();
                break;
            case MyEvents.DATA_BASE_EMPTY:
                mAdapter.clearData();
                mProgramGrid.setAdapter(mAdapter);
                showTipsLayout();
                break;
            case MyEvents.CHANGE_PROGRAM:
                mAdapter.setSelectItem((Integer) events.getData());
                mAdapter.notifyDataSetChanged();
                break;
            case MyEvents.FIRST_PLAY:
                mAdapter.setSelectItem(0);
                mAdapter.notifyItemChanged(0);
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG,"Fragment Actiity");
        mAdapter = new TvChannelGridAdapter(getActivity(), this, this,this,this);
        if (mAdapter.isProgramListEmpty()){
            showTipsLayout();
        }else {
            if (mView != null)
                mView.setVisibility(View.GONE);
            mProgramGrid.setAdapter(mAdapter);
        }
    }

    private void updateDataBase(){
        ChannelInfoDB db = ChannelInfoDB.getInstance();
        List<ChannelInfo> list = db.getAllRadioProgram();
        db.emptyDatabase();
        for (int i = 0;i < mAdapter.getChannelList().size();i++){
            db.addChannelInfo(mAdapter.getChannelList().get(i));
        }
        for (int i = 0;i < list.size();i++){
            db.addChannelInfo(list.get(i));
        }
    }

    private void initRecycleView(View view){
        mProgramGrid = (RecyclerView)view.findViewById(R.id.gridViewChannels);
        mProgramGrid.setHasFixedSize(true);
        mProgramGrid.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mProgramGrid.setItemAnimator(new DefaultItemAnimator());
    }

    private void initProgramGridData(){
        mAdapter = new TvChannelGridAdapter(getActivity(), this, this,this,this);
        mAdapter.setMovePlayingItemListener(mListener);
        if (mAdapter.isProgramListEmpty()){
            showTipsLayout();
        }else {
            mProgramGrid.setAdapter(mAdapter);
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
        mItemTouchHelper.attachToRecyclerView(mProgramGrid);
        mProgramGrid.setFocusable(false);
    }

    private void showTipsLayout(){
        if (mViewStub == null){
            mViewStub = (ViewStub) mRootView.findViewById(R.id.viewstub);
            mView = mViewStub.inflate();
            mView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void startDragOrSwipe(RecyclerView.ViewHolder viewholder) {
        mItemTouchHelper.startSwipe(viewholder);
    }

    @Override
    public void onDataBaseChange() {
        if (mAdapter.isProgramListEmpty()){
            showTipsLayout();
        }
        updateDataBase();
    }

    @Override
    public void onDeletePlayingItem(String location) {
        if (getActivity() instanceof OnDeleteItem){
            ((OnDeleteItem) getActivity()).onDeleteItem(location,false);
        }
    }

    @Override
    public void onItemClick(int positon) {
        String location = mAdapter.getChannelList().get(positon).getLocation();
        if (getActivity() instanceof OnTvChanelItemClickListener){
            ((OnTvChanelItemClickListener) getActivity()).onItemClick(location, positon);
        }
        MyEvents.postEvent(MyEvents.VIDEO_PLAY,null);
    }

    @Override
    public void onChangeProgram(int position) {
        mAdapter.setSelectItem(position);
        mAdapter.notifyDataSetChanged();
    }

    public interface OnMovePlayingItemListener {
         void onMoveItem(int toPosition);
    }

}
