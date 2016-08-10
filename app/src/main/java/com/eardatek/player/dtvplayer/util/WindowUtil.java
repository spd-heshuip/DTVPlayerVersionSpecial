package com.eardatek.player.dtvplayer.util;

import android.app.Activity;
import android.view.WindowManager;

/**
 * 作者：Create By Administrator on 16-3-7 in com.eardatek.player.dtvplayer.util.
 * 邮箱：spd_heshuip@163.com;
 */
public class WindowUtil {
    /**
     * 动态显示隐藏标题栏
     * @param enable
     */
    public static void fullScreen(boolean enable,Activity activity) {
        if (enable) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(lp);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attr);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}
