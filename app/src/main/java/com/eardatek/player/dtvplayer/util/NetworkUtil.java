package com.eardatek.player.dtvplayer.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.system.Constants;
import com.eardatek.player.dtvplayer.system.DTVApplication;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Administrator on 16-4-28.
 */
public class NetworkUtil {


    private NetworkUtil() {
        throw new UnsupportedOperationException("NetworkUtil cannot be instantiated");
    }

    /**
     * 判断网络是否连接
     *
     */
    public static boolean isConnected(Context context)  {

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()){
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivity != null && connectivity.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    /**
     * 获取wifi名
     * @param context
     * @return
     */
    public static String getWifiName(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getSSID();
        }

        return null;
    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(activity.getPackageManager()) != null)
            activity.startActivityForResult(intent, Constants.WIFI_REQUEST_CODE);
        else {
            Toast.makeText(DTVApplication.getAppContext(),
                    R.string.wifi_err_tips,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 使用SSL不信任的证书
     */
    public static  void useUntrustedCertificate() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class NetWorkConnectChangedReceiver extends BroadcastReceiver{

        private WifiStateChangedListener mListener;

        public NetWorkConnectChangedReceiver(WifiStateChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //监听wifi的打开与关闭
//            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
//                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
//                switch (wifiState){
//                    case WifiManager.WIFI_STATE_DISABLED:
//                        if (mListener != null){
//                            mListener.onDisabled();
//                        }
//                        break;
//                }
//            }else
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
                String saveName = DTVApplication.getWifiName().toString();
                String wifiName = getWifiName(DTVApplication.getAppContext());
                if (!saveName.equals(wifiName)){
                    if (mListener != null){
                        mListener.onDisabled();
                    }
                }
            }
        }

        public interface WifiStateChangedListener{
            void onAble();
            void onDisabled();
        }

    }
}
