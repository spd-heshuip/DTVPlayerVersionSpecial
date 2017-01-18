package com.eardatek.special.player.update;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.eardatek.special.player.system.Constants;
import com.eardatek.special.player.system.DTVApplication;
import com.eardatek.special.player.util.PreferencesUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tomato on 2016/8/17.
 * 用于版本请求
 * 通过Handler Message形式返回版本信息
 */
public class VersionReqThread extends Thread{

    private Handler handler;

    private static final int  BASH = 1;//信号基数
    public static final int   VERSIONREQ_START  = BASH + 0;//信号：请问开始
    public static final int   VERSIONREQ_FIN  = BASH  + 1;//信号：请求结束，并返回obj = VersionInfo版本信息
    public static final int   VERSION_REQ_ERR = BASH + 2;//信号：请求错误,返回obj = 错误信息

    private static final String TAG = "VersionReqThread";

    //构建函数
    // 实例化一个版本请求线程
    //@param handler 接收线程信号的handler
    public VersionReqThread(@NonNull Handler handler){
        this.handler = handler;
    }

    @Override
    public void run() {
        HttpURLConnection mConnection;
        try {
            //开始请求

            MessageHelper.sendMsg(handler,VERSIONREQ_START);

            String baseUrl = null;
            String hardwareType = PreferencesUtils.getString(DTVApplication.getAppContext(), Constants.DEVICE_TYPE);
            switch (hardwareType) {
                case "DTMB":
                    baseUrl = "http://www.eardatek.com/UploadFiles/dtvboxupdate/update.asp";
                    break;
                case "DVB-T/T2":
                    baseUrl = "http://www.eardatek.com/UploadFiles/dtvboxupdate_dvbt/update.asp";
                    break;
                case "ATSC":
                    baseUrl = "http://www.eardatek.com/UploadFiles/dtvboxupdate_atsc/update.asp";
                    break;
                case "ISDBT":
                    baseUrl = "http://www.eardatek.com/UploadFiles/dtvboxupdate_isdbt/update.asp";
                    break;
            }
            if (TextUtils.isEmpty(baseUrl)){
                MessageHelper.sendMsg(handler,VERSION_REQ_ERR, "Hardware Error!");
                return;
            }

            URL url = new URL(baseUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod("GET");
            mConnection.setConnectTimeout(8000);
            mConnection.setReadTimeout(8000);

            InputStream inputStream = mConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String ver = bufferedReader.readLine();//读取版本号（字符串）
            Double version =  Double.parseDouble(ver);//转换版本号（浮点数）

            String filename = bufferedReader.readLine();//读取文件名

            String filesize = bufferedReader.readLine();//读取文件大小（字符串）
            Long size = Long.parseLong(filesize);//转换文件大小（长整形，bytes）

            String urlAdd = bufferedReader.readLine();//读取下载地址

            String sha1U = bufferedReader.readLine();//读取SHA1
            String sha1 = sha1U.toLowerCase();

            String deviceType = bufferedReader.readLine();//读取硬件类型

            //通过handler返回versioninfo
            VersionInfo versionInfo = new VersionInfo(version, filename,size,sha1, urlAdd,deviceType);
            MessageHelper.sendMsg(handler, VERSIONREQ_FIN, versionInfo);
            inputStream.close();
            bufferedReader.close();

        } catch (Exception e) {

            Log.e(TAG,e.toString());
            MessageHelper.sendMsg(handler,VERSION_REQ_ERR, e.toString());

        }
    }


}
