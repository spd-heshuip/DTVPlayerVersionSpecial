package com.eardatek.special.player.system;

import android.app.Application;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.eardatek.special.player.R;
import com.eardatek.special.player.util.CrashHandler;
import com.eardatek.special.player.util.LanguageUtil;
import com.eardatek.special.player.util.LogUtil;
import com.eardatek.special.player.util.Migration;
import com.eardatek.special.player.util.PreferencesUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;


import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DTVApplication extends Application {

    public final static String TAG = "EardatekVersion2";


    public static final String SURFACE_HEIGHT = "surface_height";

    public static StringBuffer WIFI_NAME = new StringBuffer();
    private static DTVApplication instance;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Configure Realm for the application

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(2)
                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration); // Make this Realm the default

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(),"900050375",false);
        LanguageUtil.swtichLanguage();


        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setDebugMode(false);
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                LogUtil.i(TAG,"device token:" + s);
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        LogUtil.i(TAG, "System is running low on memory");
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
        PreferencesUtils.putString(DTVApplication.getAppContext(),"LAST_DEVICE_WIFI_NAME","");
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

    public static Locale getLocale(){
        int lan = PreferencesUtils.getInt(getAppContext(),Constants.LAN_KEY,Constants.LAN_DEFAULT);
        switch (lan){
            case Constants.LAN_DEFAULT:
                if (Locale.getDefault().equals(Locale.SIMPLIFIED_CHINESE))
                    return Locale.SIMPLIFIED_CHINESE;
                else
                    return Locale.US;
            case Constants.LAN_CHINESE_SIMPLE:
                return Locale.SIMPLIFIED_CHINESE;
            case Constants.LAN_ENGLISH:
                return Locale.US;
            default:
                return Locale.SIMPLIFIED_CHINESE;
        }
    }
}
