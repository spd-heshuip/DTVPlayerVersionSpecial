package com.eardatek.special.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

/**
 * Created by Administrator on 16-7-7.
 */
public class WifiReceiver{

    private Context mContext;
    private WifiStateListenner mListenner;
    private WifiStateReceiver mReceiver;

    public WifiReceiver(Context context) {
        mContext = context;
        mReceiver = new WifiStateReceiver();
    }

    private class WifiStateReceiver extends BroadcastReceiver{
        private String action;
        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (mListenner != null && action.equals(WifiManager.RSSI_CHANGED_ACTION)){
                mListenner.onWifiStateChange();
            }
        }
    }

    public void begin(WifiStateListenner listenner){
        mListenner = listenner;
        register();
    }

    private void register(){
        IntentFilter intentFilter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver,intentFilter);
    }

    public void unregister(){
        mContext.unregisterReceiver(mReceiver);
    }


    public interface WifiStateListenner{
        void onWifiStateChange();
    }
}
