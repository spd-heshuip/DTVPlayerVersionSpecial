package com.eardatek.player.dtvplayer.widget;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by Administrator on 16-4-27.
 */
public class CustomToast {

    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mToast.cancel();
        }
    };

    public static void showToast(Context context,String text,int duration){
        mHandler.removeCallbacks(mRunnable);
        if (mToast != null)
            mToast.setText(text);
        else
            mToast = Toast.makeText(context,text,Toast.LENGTH_SHORT);
        mHandler.postDelayed(mRunnable,duration);
        mToast.show();
    }

    public static void showToast(Context context,int resId,int duration){
        showToast(context,context.getResources().getString(resId),duration);
    }
}
