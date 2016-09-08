package com.eardatek.player.dtvplayer.update;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

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

            URL url = new URL("http://www.eardatek.com/UploadFiles/dtvboxupdate/update.asp");
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



            //通过handler返回versioninfo
            VersionInfo versionInfo = new VersionInfo(version, filename,size,sha1, urlAdd);
            MessageHelper.sendMsg(handler, VERSIONREQ_FIN, versionInfo);
            inputStream.close();
            bufferedReader.close();

        } catch (Exception e) {

            Log.e(TAG,e.toString());
            MessageHelper.sendMsg(handler,VERSION_REQ_ERR, e.toString());

        }
    }


}
