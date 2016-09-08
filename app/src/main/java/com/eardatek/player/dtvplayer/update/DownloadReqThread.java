package com.eardatek.player.dtvplayer.update;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tomato on 2016/8/17.
 * 用于http文件下载
 */
public class DownloadReqThread extends Thread {
    private VersionInfo versionInfo;
    private Handler handler;

    private Lock lock;
    private boolean runflag = true;

    private static final String path = "DTVPlayer";
    public static final String DOWNLOAD_DIR = Environment.getExternalStorageDirectory() + "/" + path ;
    private final String TAG = "DownloadReqThread";

    private final static int DOWNLOAD_PIC_SIZE = 1024;
    private final static int BASH = 4;
    public static final int DOWNLOAD_START  = BASH + 0;//信号：开始下载
    public static final int DOWNLOAD_EXIST  = BASH + 1;//信号：文件已经存在，返回 arg1 * 100000 + arg2 = 下载文件大小
    public static final int DOWNLOAD_FIN    = BASH + 2;//信号：下载结束，返回，返回 arg1 * 100000 + arg2 = 下载文件大小
    public static final int DOWNLOAD_COUNT  = BASH + 3;//信号：文件下载进度，返回 obj = 每次下载了得字节数
    public static final int DOWNLOAD_ERR    = BASH + 4;//信号：下载失败，返回obj = 错误信息

    //构建函数
    //实例化一个http下载线程
    //@param handler 接收线程信号的handler
    //@param versionInfo 固件版本信息
    //!! 文件最终保存在SD卡目录/DTVPlayer/下
    public DownloadReqThread(Handler handler,VersionInfo versionInfo){
        assert(handler != null);
        assert(versionInfo != null);
        this.versionInfo = versionInfo;
        this.handler = handler;

        this.lock = new ReentrantLock();
    }
    //@function 异步停止线程
    public synchronized void stopThread(){
        setRunflag(false);
    }

    private  void setRunflag(boolean runflag) {
        assert(lock != null);
        lock.lock();
        this.runflag = runflag;
        lock.unlock();
    }
    private  boolean getRunflag(){
        assert(lock != null);
        lock.lock();
        boolean temp = this.runflag;
        lock.unlock();
        return temp;
    }

    @Override
    public void run() {
        MessageHelper.sendMsg(handler, DOWNLOAD_START);
        OutputStream outputStream;
        String pathName = "";
        try {
            URL url = new URL(versionInfo.getUrl());
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();//http连接
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(3000);

            File dirfile = new File(DOWNLOAD_DIR);//检查目录
            if( ! dirfile.exists() && !dirfile.isDirectory())//如果目录不存在则创建
            {
                dirfile.mkdir();
            }
            pathName = DOWNLOAD_DIR + "/" + versionInfo.getFilename();//文件存储路径
            File file = new File(pathName);
            InputStream input = conn.getInputStream();
            if(file.exists()) {
                Log.d(TAG, "file exists");
                MessageHelper.sendMsg(handler, DOWNLOAD_EXIST, (int)(file.length()/100000), (int)(file.length()%100000), pathName);
                input.close();
                conn.disconnect();
                return ;
            }
            else {
                Log.d(TAG, "file download");
                file.createNewFile();//新建文件
                outputStream = new FileOutputStream(file);
                //读取文件
                byte[] buffer = new byte[DOWNLOAD_PIC_SIZE];

                if(getRunflag() == false){//允许结束点
                    input.close();
                    outputStream.close();
                    conn.disconnect();
                    file.delete();
                    return;
                }
                long write_count = 0;//记录总写入数据字节数
                int count;//记录每轮数据读取字节数
                while((count = input.read(buffer)) != -1){

                    outputStream.write(buffer,0,count);
                    write_count += count;//累计下载文件字节数
                    MessageHelper.sendMsg(handler, DOWNLOAD_COUNT, count);
                    if(getRunflag() == false){//允许结束点
                        input.close();
                        outputStream.close();
                        file.delete();
                        return;
                    }
                }
                outputStream.flush();//刷新
                try {
                    outputStream.close();//关闭文件写入
                    input.close();//关闭http读取
                    MessageHelper.sendMsg(handler, DOWNLOAD_FIN, (int)(file.length()/100000), (int)(file.length()%100000), pathName);
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageHelper.sendMsg(handler,DOWNLOAD_ERR, e.toString());
                }
            }

        }/* catch (MalformedURLException e) {
            e.printStackTrace();
            MessageHelper.sendMsg(handler,DOWNLOAD_ERR, e.toString());
        }catch (IOException e){
            e.printStackTrace();
            MessageHelper.sendMsg(handler,DOWNLOAD_ERR, e.toString());
        }*/
        catch (Exception e){
            e.printStackTrace();
            MessageHelper.sendMsg(handler,DOWNLOAD_ERR, e.toString());
        }


    }
}
