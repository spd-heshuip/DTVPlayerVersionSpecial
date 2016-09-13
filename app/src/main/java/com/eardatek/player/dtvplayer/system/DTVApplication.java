package com.eardatek.player.dtvplayer.system;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.util.CrashHandler;
import com.eardatek.player.dtvplayer.util.LogUtil;
import com.tencent.bugly.crashreport.CrashReport;

public class DTVApplication extends Application {
    public final static String TAG = "EardatekVersion2";


    public static final String SURFACE_HEIGHT = "surface_height";

    public static StringBuffer WIFI_NAME = new StringBuffer();
    private static DTVApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(),"900050375",false);
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

    public static String getVersionInfo(){

        PackageManager manager = instance.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(instance.getPackageName(),0);
            String version = info.versionName;
            return instance.getString(R.string.version_name) + version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return instance.getString(R.string.version_name);
        }
    }
}
