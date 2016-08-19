package com.eardatek.player.dtvplayer.system;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.util.CrashHandler;
import com.eardatek.player.dtvplayer.util.LogUtil;

public class DTVApplication extends Application {
    public final static String TAG = "EardatekVersion2";

    public static final int WIFI_REQUEST_CODE = 1;
    public static final int SCAN_CHANNEL_REQUEST_CODE = 2;
    public static final String SURFACE_HEIGHT = "surface_height";

    public static StringBuffer WIFI_NAME = null;

    private static DTVApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        ChannelInfoDB.getInstance();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogUtil.i(TAG, "System is running low on memory");
//        System.gc();
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

    public static void setWifiName(String wifiName) {
        WIFI_NAME = new StringBuffer(wifiName);
    }

    public static StringBuffer getWifiName() {
        return WIFI_NAME;
    }
}
