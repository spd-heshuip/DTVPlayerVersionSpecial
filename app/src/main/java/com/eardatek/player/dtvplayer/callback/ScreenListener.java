package com.eardatek.player.dtvplayer.callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

/**
 * 作者：Create By Administrator on 16-3-29 in com.eardatek.player.dtvplayer.callback.
 * 邮箱：spd_heshuip@163.com;
 */
public class ScreenListener {

    private Context mContext;
    private ScreenBrocastReceiver mReceiver;
    private ScreenStateListener mListener;

    public ScreenListener(Context context) {
        this.mContext = context;
        this.mReceiver = new ScreenBrocastReceiver();
    }

    private class ScreenBrocastReceiver extends BroadcastReceiver{
        private String action = null;
        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action))
                mListener.onScreenOff();
            else if(Intent.ACTION_SCREEN_ON.equals(action))
                mListener.onScreenOn();
            else if (Intent.ACTION_USER_PRESENT.equals(action))
                mListener.onUserPresent();
        }
    }

    public void begin(ScreenStateListener listener){
        mListener = listener;
        registerListener();
        getScreenState();
    }

    private void getScreenState(){
        boolean isScreenOn;
        PowerManager manager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            isScreenOn = manager.isInteractive();
        }else
            isScreenOn = manager.isScreenOn();
        if (isScreenOn){
            if (mListener != null){
                mListener.onScreenOn();
            }
        }else {
            if (mListener != null)
                mListener.onScreenOff();
        }
    }

    private void registerListener(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mReceiver,intentFilter);
    }

    public void unRegisterListener(){
        mContext.unregisterReceiver(mReceiver);
    }

    public interface ScreenStateListener{
         void onScreenOn();
         void onScreenOff();
         void onUserPresent();
    }
}
