package com.eardatek.special.player.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.eardatek.special.player.R;
import com.eardatek.special.player.actitivy.EardatekVersion2Activity;
import com.eardatek.special.player.adapter.SwipeableWithButtonAdapter;
import com.eardatek.special.player.callback.MyEvents;
import com.eardatek.special.player.data.AbstractDataProvider;
import com.eardatek.special.player.data.TvDataProvider;
import com.eardatek.special.player.system.DTVApplication;
import com.eardatek.special.player.util.DensityUtil;
import com.eardatek.special.player.util.ListUtil;
import com.eardatek.special.player.util.LogUtil;
import com.eardatek.special.player.util.PreferencesUtils;
import com.eardatek.special.player.widget.CustomEditText;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Collections;

/**
 * Created by Administrator on 16-7-25.
 */
public class SwipeWithButtonFragment extends StatedFragment implements SwipeableWithButtonAdapter.OnItemMoveListenner{

    private static final String TAG = SwipeWithButtonFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    private View mRootView;
    private ViewStub mViewStub;
    private View mTipsLayout;
    SwipeableWithButtonAdapter myItemAdapter;

    public SwipeWithButtonFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_program,container,false);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.gridViewChannels);
        init();
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);

    }

    private void init(){

        mLayoutManager = new LinearLayoutManager(getContext());

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        myItemAdapter = initAdapter();
        myItemAdapter.setTopCountValue(PreferencesUtils.getInt(getActivity(),"topCount"));

        mAdapter = myItemAdapter;

        if (myItemAdapter.getList().size() == 0){
            showTipsLayout(true);
        }else
            showTipsLayout(false);

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);      // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();


        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                restoreSwipedPosition();
                return false;
            }
        });
    }

    private void restoreSwipedPosition(){
//        LogUtil.i("SwipeableWithButtonAdapter","" + ((SwipeableWithButtonAdapter)mAdapter).getSwipedPosition());
        if (((SwipeableWithButtonAdapter)mAdapter).getSwipedPosition() >= 0){
            AbstractDataProvider.Data data = getDataProvider().getItem(((SwipeableWithButtonAdapter)mAdapter).getSwipedPosition());
            if (data.isPinned()) {
                // unpin if tapped the pinned item
//                LogUtil.i("SwipeableWithButtonAdapter","data.isPinned()" + data.isPinned());
                data.setPinned(false);
                mAdapter.notifyItemChanged(((SwipeableWithButtonAdapter)mAdapter).getSwipedPosition());
            }
            ((SwipeableWithButtonAdapter)mAdapter).setSwipedPosition(-1);
        }
    }

    private void showTipsLayout(boolean isShow){
        if (mViewStub == null){
            mViewStub = (ViewStub) mRootView.findViewById(R.id.viewstub);
            mTipsLayout = mViewStub.inflate();

            Button scanBtn = (Button) mTipsLayout.findViewById(R.id.scan_channel);
            scanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyEvents.postEvent(MyEvents.SCAN_CHANNEL,null);
                }
            });
        }
        if (isShow)
            mTipsLayout.setVisibility(View.VISIBLE);
        else
            mTipsLayout.setVisibility(View.GONE);

    }

    private SwipeableWithButtonAdapter initAdapter(){
        final SwipeableWithButtonAdapter myItemAdapter = new SwipeableWithButtonAdapter(getDataProvider());
        myItemAdapter.setEventListener(new SwipeableWithButtonAdapter.EventListener() {
            @Override
            public void onItemPinned(int position) {

            }

            @Override
            public void onItemViewClicked(View v) {
                handleOnItemViewClicked(v);
            }

            @Override
            public void onUnderSwipeableViewButtonClicked(View v) {
                handleOnDeleteButtonClicked(v);
            }

            @Override
            public void onContainerLongClicked(View v) {
                handleContainerLongClick(v);
            }

            @Override
            public void onTopButtonClicked(View v) {
                handleTopButtonClick(v);
            }


        });
        myItemAdapter.setOnItemMoveListenner(this);
        return myItemAdapter;
    }

    private void handleOnItemViewClicked(View v){
        if (((SwipeableWithButtonAdapter)mAdapter).getSwipedPosition() >= 0){
            restoreSwipedPosition();
            return;
        }

        int position = mRecyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION){
            AbstractDataProvider.Data data = getDataProvider().getItem(position);
            if (data.isPinned()) {
                // unpin if tapped the pinned item
                data.setPinned(false);
                mAdapter.notifyItemChanged(position);
                return;
            }
            if (data.isEncrypt()){
                showEncryptDialog();
                return;
            }

            String location = ((SwipeableWithButtonAdapter)mAdapter).getItem(position).getText();
            ((SwipeableWithButtonAdapter)mAdapter).setSelectProgram(((SwipeableWithButtonAdapter)mAdapter).
                    getItem(position).getText(),position);
            if (location != null)
                ((EardatekVersion2Activity) getActivity()).onItemClick(location,position);
        }

    }

    private void handleOnDeleteButtonClicked(View v) {
        int position = mRecyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION) {
            if (((SwipeableWithButtonAdapter)mAdapter).getSwipedPosition() == position){
                ((SwipeableWithButtonAdapter)mAdapter).setSwipedPosition(-1);
            }
            if (((SwipeableWithButtonAdapter)mAdapter).getItem(position).isTop() == 1)
                ((SwipeableWithButtonAdapter)mAdapter).decreaseTopCount();
            if (((SwipeableWithButtonAdapter)mAdapter).getSelectProgram().
                    equals(((SwipeableWithButtonAdapter)mAdapter).getItemProgramName(position))){
                ((SwipeableWithButtonAdapter)mAdapter).setSelectProgram("",0);
                MyEvents events = new MyEvents();
                events.setEventType(MyEvents.DELETE_PLAYING_ITEM);
                EventBus.getDefault().post(events);
            }
            ((SwipeableWithButtonAdapter)mAdapter).removeItemFromDadaBase(position);
            mAdapter.notifyItemRemoved(position);
            if (mAdapter.getItemCount() == 0){
                showTipsLayout(true);
            }
        }

        new Thread(new UpdateDatabaseRunable(this)).start();
    }

    private void handleTopButtonClick(View v){
        final int position = mRecyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION){
            AbstractDataProvider.Data data = getDataProvider().getItem(position);
            if (data.isPinned()) {
                // unpin if tapped the pinned item
                data.setPinned(false);
                mAdapter.notifyItemChanged(position);
            }
            if (((SwipeableWithButtonAdapter)mAdapter).isTop(position) == 1){
                ((SwipeableWithButtonAdapter)mAdapter).cancelTop(position);
                ((SwipeableWithButtonAdapter)mAdapter).decreaseTopCount();
                if (((SwipeableWithButtonAdapter)mAdapter).getSelectProgram().
                        equals(((SwipeableWithButtonAdapter)mAdapter).getItemProgramName(position))){
                    updatePlayingPosition(PreferencesUtils.getInt(getActivity(),"topCount"));
                }
                refreshView();
                new Thread(new UpdateDatabaseRunable(SwipeWithButtonFragment.this)).start();
            }else {
                ((SwipeableWithButtonAdapter)mAdapter).setTop(position);
                ((SwipeableWithButtonAdapter)mAdapter).increaseTopCount();
                if (((SwipeableWithButtonAdapter)mAdapter).getSelectProgram().
                        equals(((SwipeableWithButtonAdapter)mAdapter).getItemProgramName(position))){
                    updatePlayingPosition(0);
                }
                refreshView();
                new Thread(new UpdateDatabaseRunable(SwipeWithButtonFragment.this)).start();
            }
        }
    }

    private void handleContainerLongClick(View v){
        LogUtil.i(TAG,"handleContainerLongClick");
        restoreSwipedPosition();
        final int position = mRecyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION){
            LogUtil.i(TAG,"showRenameDialog");
            showRenameDialog(((SwipeableWithButtonAdapter)mAdapter).getItem(position).getText()
                ,position);
        }
    }

    private void refreshView(){
        Collections.sort( ((SwipeableWithButtonAdapter)mAdapter).getList());
        mAdapter.notifyDataSetChanged();
    }

    private void updatePlayingPosition(int position){
        MyEvents events = new MyEvents();
        events.setEventType(MyEvents.CHANGE_LANDSCAPE_LIST_POSITON);
        events.setData(position);
        EventBus.getDefault().post(events);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void refresh(){
        PreferencesUtils.putInt(getActivity(),"topCount",0);
        release();
        init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refresh();
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    private void release(){
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }


    }

    private void showRenameDialog(final String location,final int position){
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_rename,null);
        final Dialog dialog = new Dialog(getActivity(),R.style.transparentFrameWindowStyle);

        final CustomEditText editText = (CustomEditText) view.findViewById(R.id.name_edit);

        Button confirmButton = (Button) view.findViewById(R.id.confirm);
        Button cancelButton = (Button) view.findViewById(R.id.cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editText.getText().toString();
                if (!TextUtils.isEmpty(name)){
                    getDataProvider().updateItemName(location,name,position);
                    mAdapter.notifyItemChanged(position);
                }
                if (dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog.isShowing()){
                    dialog.dismiss();
                }
            }
        });

        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        //设置窗口显示位置
        window.setGravity(Gravity.CENTER);
        // 设置窗口动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //设置窗口显示位置偏移量
        wl.x = 0;
        wl.y = 0;
        // 使窗口宽度铺满手机屏幕
        wl.width = DensityUtil.dip2px(DTVApplication.getAppContext(),345f);
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.alpha = 0.9f;

        // 窗口属性改变时，显示窗口
        dialog.onWindowAttributesChanged(wl);
        // 点击外围dimiss dialog
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showEncryptDialog(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.encrypt_dialog,
                null);
        final Dialog dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);

        Button ok = (Button) view.findViewById(R.id.yes);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()){
                        dialog.dismiss();
                }
            }
        });

        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        //设置窗口显示位置
        window.setGravity(Gravity.CENTER);
        // 设置窗口动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //设置窗口显示位置偏移量
        wl.x = 0;
        wl.y = 0;
        // 使窗口宽度铺满手机屏幕
        wl.width = DensityUtil.dip2px(DTVApplication.getAppContext(),345f);
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        wl.alpha = 0.9f;

        // 窗口属性改变时，显示窗口
        dialog.onWindowAttributesChanged(wl);
        // 点击外围dimiss dialog
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        release();
        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        mAdapter = null;
        mLayoutManager = null;
        EventBus.getDefault().unregister(this);
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public AbstractDataProvider getDataProvider() {
        return ((EardatekVersion2Activity) getActivity()).getDataProvider();
//        return new TvDataProvider();
    }

    @Override
    public void onItemMove() {
        updatePlayingPosition(((SwipeableWithButtonAdapter)mAdapter).getSelectItem());
        new Thread(new UpdateDatabaseRunable(this)).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEventBus(MyEvents events) {
        switch (events.getEventType()){
            case MyEvents.DATA_BASE_EMPTY:
                ((SwipeableWithButtonAdapter)mAdapter).clearItem();
                PreferencesUtils.putInt(getActivity(),"topCount",0);
                mRecyclerView.setAdapter(mAdapter);
                showTipsLayout(true);
                break;
            case MyEvents.CHANGE_PROGRAM:
                int position = (int) events.getData();
                ((SwipeableWithButtonAdapter)mAdapter).
                        setSelectProgram(((SwipeableWithButtonAdapter)mAdapter).getItemProgramName(position),position);
                break;
        }
    }

    public static class UpdateDatabaseRunable implements Runnable{
        private WeakReference<SwipeWithButtonFragment> mFragment;

        public UpdateDatabaseRunable(SwipeWithButtonFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void run() {
            SwipeWithButtonFragment fragment = mFragment.get();
            if (fragment != null){
                LogUtil.i("ListUtil","start....");
                ListUtil.ListToFile(((SwipeableWithButtonAdapter)fragment.mAdapter).getList(),"Channel.txt");
            }
        }
    }
}
