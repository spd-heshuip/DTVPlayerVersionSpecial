package com.eardatek.player.dtvplayer.callback;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * 作者：Create By Administrator on 16-2-20 in com.eardatek.player.dtvplayer.callback.
 * 邮箱：spd_heshuip@163.com;
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{

    private OnMoveAndSwipeListener mAdapter;

    public SimpleItemTouchHelperCallback(OnMoveAndSwipeListener listener) {
        mAdapter = listener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            //设置拖拽方向为上下
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //设置侧滑方向为从左到右或从右到左都可以
            final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            //将方向参数设置进去
            return makeMovementFlags(dragFlags,swipeFlags);
        }else {
            //设置拖拽方向为上下左右
            final int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN|
                    ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
            //不支持侧滑
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags,swipeFlags);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //回调adapter中的onItemMove方法
        mAdapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }
    /**当我们侧滑item时会回调此方法*/
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //回调adapter中的onItemDismiss方法
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
