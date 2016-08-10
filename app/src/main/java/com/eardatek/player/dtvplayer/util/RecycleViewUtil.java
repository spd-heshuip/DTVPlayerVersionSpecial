package com.eardatek.player.dtvplayer.util;

import android.view.View;
import android.view.ViewGroup;

/**
 * 作者：Create By Administrator on 16-3-1 in com.eardatek.player.dtvplayer.util.
 * 邮箱：spd_heshuip@163.com;
 */
public class RecycleViewUtil {

    /**
     * 在ViewGroup中根据id进行查找
     * @param viewGroup
     * @param id 如：R.id.tv_name
     * @return
     */
    public static View findViewInViewGroupById(ViewGroup viewGroup,int id){
        for (int i = 0; i < viewGroup.getChildCount();i++){
            View view = viewGroup.getChildAt(i);
            if (view.getId() == id){
                return view;
            }else {
                if (view instanceof  ViewGroup){
                    return findViewInViewGroupById((ViewGroup) view, id);
                }
            }
        }
        return null;
    }

}
