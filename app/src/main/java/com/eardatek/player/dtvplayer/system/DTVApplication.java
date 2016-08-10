package com.eardatek.player.dtvplayer.system;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.util.CrashHandler;
import com.eardatek.player.dtvplayer.util.LogUtil;

public class DTVApplication extends Application {
    public final static String TAG = "EardatekVersion2";
    private static DTVApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        ChannelInfoDB.getInstance();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtil.i(TAG, "System is running low on memory");

//        BitmapCache.getInstance().clear();
    }

    public static Context getAppContext()
    {
        return instance;
    }

    public static Resources getAppResources()
    {
        return instance.getResources();
    }
}
