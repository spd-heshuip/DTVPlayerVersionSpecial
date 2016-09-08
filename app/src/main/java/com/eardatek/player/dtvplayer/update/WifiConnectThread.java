package com.eardatek.player.dtvplayer.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import com.eardatek.player.dtvplayer.util.NetworkUtil;

import java.util.List;

/**
 * Created by tomato on 2016/8/22.
 */
public class WifiConnectThread extends Thread {
    private Context context;
    private Handler handler;
    private String e_ssid;
    private String password;
    private int type;

    private WifiManager mWifiManager;

    private static final String TAG = "WifiConnectThread";
    private static final int OVERTIME_LIMIT  =   10 * 2;

    private static final int BASE = 9;
    public static final int WAIT_WIFI_OVERTIME   = BASE + 0;
    public static final int TARGET_CONNECT       = BASE + 1;
    public static final int CONNECT_ERR       = BASE + 2;

    public WifiConnectThread(Context context,Handler handler, String e_ssid, String password, int type){
        this.context = context;
        this.e_ssid = e_ssid;
        this.password = password;
        this.type = type;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.handler = handler;

    }
    private int waitForConnect(){
        int count = 0;
        while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            count ++;
            if(count >= OVERTIME_LIMIT)  return 0;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        return 1;
    }
    private int waitForDisonnect(){
        int count = 0;
        while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED) {
            count ++;
            if(count >= OVERTIME_LIMIT)  return 0;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }
    private int waitForResults(){
        mWifiManager.startScan();
        int result_count = 0;
        int count = 0;
        List<ScanResult> scanResult = null;
        while (result_count <= 0 || scanResult == null){
            count ++;
            if(count >= OVERTIME_LIMIT)  return 0;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            scanResult = mWifiManager.getScanResults();
            result_count = scanResult.size();
        }
        return 1;
    }
    @Override
    public void run() {
        if(!mWifiManager.isWifiEnabled())   mWifiManager.setWifiEnabled(true);//打开wifi
        if(waitForConnect() == 0) MessageHelper.sendMsg(handler,WAIT_WIFI_OVERTIME);

        addNetwork(CreateWifiInfo(e_ssid,password,type));


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(NetworkUtil.getWifiName(context) == e_ssid)
            MessageHelper.sendMsg(handler,TARGET_CONNECT);
        else
            MessageHelper.sendMsg(handler,CONNECT_ERR);


    }

    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        SSID.replace("\"","");
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if(tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if(Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+Password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\""+Password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration IsExsits(String SSID)
    {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }

    // 添加一个网络并连接
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b =  mWifiManager.enableNetwork(wcgID, true);
        return  b;
    }
}
