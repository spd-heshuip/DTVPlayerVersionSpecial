package com.eardatek.player.dtvplayer.callback;

/**
 * 作者：Create By Administrator on 16-2-20 in com.eardatek.player.dtvplayer.callback.
 * 邮箱：spd_heshuip@163.com;
 */
public interface OnMoveAndSwipeListener {
    boolean onItemMove(int fromPosition,int toPosition);
    void onItemDismiss(int positon);
}
