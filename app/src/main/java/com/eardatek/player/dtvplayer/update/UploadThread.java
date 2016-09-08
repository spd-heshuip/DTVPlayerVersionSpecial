package com.eardatek.player.dtvplayer.update;


import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;


import com.eardatek.player.dtvplayer.actitivy.HomePageActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by tomato on 2016/8/18.
 */
public class UploadThread extends Thread {
    private static final String TAG = "UploadThread";
    private String host;
    private int port;
    private Handler handler;
    private VersionInfo versionInfo;

    private String filePath;
    private Lock lock;
    private boolean runflag = true;
    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;

    private static final int UPLOAD_FILE_PIC_SIZE = 1024;
    private static final int BASE   = 12;
    public static final int UPLOAD_START        = BASE + 0;//信号：上传线程开始
    public static final int UPLOAD_CONNECT_OK   = BASE + 1;//信号：连接成功
    public static final int UPLOAD_CONNECT_FAIL = BASE + 2;//信号：连接失败
    public static final int UPLOAD_SERVER_OK     = BASE + 3;//信号：服务器验证成功
    public static final int UPLOAD_SERVER_FAIL  = BASE + 4;//信号：服务器验证失败
    public static final int UPLOAD_FILE_INFO    = BASE + 5;//信号：发送文件信息
    public static final int UPLOAD_READY        = BASE + 6;//信号：准备上传
    public static final int UPLOAD_NOT_READY    = BASE + 7;//信号：无法准备上传
    public static final int UPLOAD_FILE_START   = BASE + 8;//信号：开始文件上传
    public static final int UPLOAD_FILE_COUNT   = BASE + 9;//信号：文件上传进度，返回 obj = 每次上传数据字节数
    public static final int UPLOAD_FIN          = BASE + 10;//信号：上传结束
    public static final int UPDATE_CHECK_OK     = BASE + 11;//信号：文件检验成功
    public static final int UPDATE_CHECK_FAIL   = BASE + 12;//信号：文件校验失败
    public static final int UPLOAD_ERR          = BASE + 13;//信号：上传错误，返回obj = 错误信息
    public static final int UPDATE_START         = BASE + 14;//信号：更新开始
    public static final int UPDATE_FIN         = BASE + 15;//信号：更新完毕
    public static final int UPDATE_ERR         = BASE + 16;//信号：更新错误，返回obj = 错误信息


    // 异步结束线程
    public synchronized void stopTread(){
        setRunflag(false);
    }

    // 实例化上传线程
    //@param handler 接收线程信号的handler
    //@param host 服务器IP
    //@param port 服务器服务端口
    //@param versionInfo 固件版本信息
    public UploadThread(@NonNull Handler handler,@NonNull String host, int port ,@NonNull VersionInfo versionInfo){
        assert(!host.isEmpty());
        this.host = host;
        this.port = port;
        this.handler = handler;
        this.versionInfo = versionInfo;
        this.filePath = Environment.getExternalStorageDirectory() + "/DTVPlayer/" + versionInfo.getFilename();//文件存储路径
        this.lock = new ReentrantLock();
    }

    private  boolean getRunflag(){
        assert(lock != null);
        lock.lock();
        boolean temp = this.runflag;
        lock.unlock();
        return temp;
    }
    private  void setRunflag(boolean runflag){
        assert(lock != null);
        lock.lock();
        this.runflag = runflag;
        lock.unlock();
    }
    @Override
    public void run() {
        try {
            MessageHelper.sendMsg(handler,UPLOAD_START);
            SocketAddress address = new InetSocketAddress(host,port);
            socket = new Socket();//创建socket
            socket.setSoTimeout(2000);
            socket.connect(address, 10000);

            /******检查是否连接成功*******/
            if(socket.isConnected()){
                MessageHelper.sendMsg(handler,UPLOAD_CONNECT_OK);
            }
            else{
                MessageHelper.sendMsg(handler,UPLOAD_CONNECT_FAIL);
                return ;
            }
            in = socket.getInputStream();//输入流
            out = socket.getOutputStream();//输出流


            /******校验服务器*******/
            byte[] ack = new byte[10];
            in.read(ack,0,10);
            String str = new String(ack,0,5,"ASCII");

            if(str.equals("SerOK")) {
                MessageHelper.sendMsg(handler,UPLOAD_SERVER_OK);
            }else{
                MessageHelper.sendMsg(handler,UPLOAD_SERVER_FAIL);
                in.close();
                out.close();
                socket.close();
                return;
            }

            /*****发送上传文件信息******/
            StringBuilder sendinfo = new StringBuilder();
            sendinfo.append("<" + "INFO");
            sendinfo.append("<" + versionInfo.getFilename());
            sendinfo.append("<" + versionInfo.getSize().toString());
            sendinfo.append("<" + versionInfo.getSha1());
            byte[] send = sendinfo.toString().getBytes();
            out.write(send,0,send.length);

            MessageHelper.sendMsg(handler,UPLOAD_FILE_INFO);


            /*****等待服务器准备******/
            ack = new byte[10];
            in.read(ack,0,10);
            str = new String(ack,0,5,"ASCII");

            if(str.equals("READY")) {
                MessageHelper.sendMsg(handler,UPLOAD_READY);
            }else{
                MessageHelper.sendMsg(handler,UPLOAD_NOT_READY);
                in.close();
                out.close();
                socket.close();
                return;
            }
            /////////////

            if (getRunflag() == false) {//允许结束点
                in.close();
                out.close();
                socket.close();
                return;
            }
            /*****开始发送文件******/
            byte[] buffer = new byte[UPLOAD_FILE_PIC_SIZE];
            File file = new File(filePath);
            InputStream inputStream = new FileInputStream(file);
            int count;
            MessageHelper.sendMsg(handler,UPLOAD_FILE_START);

            while((count = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                MessageHelper.sendMsg(handler, UPLOAD_FILE_COUNT, count);
                if (getRunflag() == false) {//允许结束点
                    inputStream.close();
                    in.close();
                    out.close();
                    socket.close();
                    return;
                }
            }
            inputStream.close();
            out.flush();
            MessageHelper.sendMsg(handler, UPLOAD_FIN);//上传完成
            /*****等待文件校验******/
            socket.setSoTimeout(1000 * 60);//更新脚本运行时间可能比较长，需要加大读超时时间，最多一分钟

            ack = new byte[10];
            int read_temp = in.read(ack,0,10);
            str = new String(ack,0,5,"ASCII");

            if(str.equals("CHECK")) {
                MessageHelper.sendMsg(handler,UPDATE_CHECK_OK);
            }else{
                MessageHelper.sendMsg(handler,UPDATE_CHECK_FAIL);
                in.close();
                out.close();
                socket.close();
                return;
            }
            /********开始更新，等待结果************/
            MessageHelper.sendMsg(handler, UPDATE_START);
            ack = new byte[10];
            in.read(ack,0,10);
            str = new String(ack,0,1,"ASCII");

            if(str.isEmpty())
            {
                MessageHelper.sendMsg(handler, UPDATE_ERR, "-1");
                in.close();
                out.close();
                socket.close();
                return;
            }

            if(!str.equals("1")) {
                MessageHelper.sendMsg(handler, UPDATE_ERR, str);
                in.close();
                out.close();
                socket.close();
                return;
            }

            in.close();
            out.close();
            socket.close();

            MessageHelper.sendMsg(handler, UPDATE_FIN);

        } catch (IOException e) {
            e.printStackTrace();
            MessageHelper.sendMsg(handler, UPLOAD_ERR, e.toString());
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
            MessageHelper.sendMsg(handler, UPDATE_ERR, "-1");
        }

    }

}
