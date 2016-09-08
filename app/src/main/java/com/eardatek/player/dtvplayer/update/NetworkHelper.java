package com.eardatek.player.dtvplayer.update;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.CalendarContract;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by tomato on 2016/8/17.
 */
public class NetworkHelper {
    /*
    private static NetworkHelper networkHelper = null;
    public static NetworkHelper getInstance(){
        if( networkHelper == null){
            networkHelper = new NetworkHelper();
        }
        return networkHelper;
    }*/

    public static void toggleWiFi(Context context, boolean enabled) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wm.setWifiEnabled(enabled);
    }

    public static void toggleMobileData(Context context, boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Class<?> conMgrClass = null;  // ConnectivityManager类
        Field conMgrField = null;       // ConnectivityManager类中的字段
        Object iConMgr = null;          // IConnectivityManager类的引用
        Class<?> iConMgrClass = null;     // IConnectivityManager类
        Method setMobileDataEnabledMethod = null; // setMobileDataEnabled方法

        try {
            // 取得ConnectivityManager类
            conMgrClass = Class.forName(conMgr.getClass().getName());
            // 取得ConnectivityManager类中的对象mService
            conMgrField = conMgrClass.getDeclaredField("mService");
            // 设置mService可访问
            conMgrField.setAccessible(true);
            // 取得mService的实例化类IConnectivityManager
            iConMgr = conMgrField.get(conMgr);
            // 取得IConnectivityManager类
            iConMgrClass = Class.forName(iConMgr.getClass().getName());
            // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            // 设置setMobileDataEnabled方法可访问
            setMobileDataEnabledMethod.setAccessible(true);
            // 调用setMobileDataEnabled方法
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


}
