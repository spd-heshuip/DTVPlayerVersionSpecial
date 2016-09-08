package com.eardatek.player.dtvplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * 作者：Create By Administrator on 16-3-7 in com.eardatek.player.dtvplayer.util.
 * 邮箱：spd_heshuip@163.com;
 */
public class WindowUtil {
    /**
     * 动态显示隐藏标题栏
     *
     * @param enable
     */
    public static void fullScreen(boolean enable, Activity activity) {
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

    /**
     * 获取系统是否打开自动旋转
     * @param context
     * @return
     */
    public static boolean isAutoRotateOn(Context context) {
        return (android.provider.Settings.System.getInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
    }

    public static int convert2Orientation(int rotation){
        int orientation;
        if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if ((rotation > 45) && (rotation <= 135)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if ((rotation > 135) && (rotation <= 225)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        } else if ((rotation > 225) && (rotation <= 315)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return orientation;
    }
}
