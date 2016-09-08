package com.eardatek.player.dtvplayer.actitivy;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.system.Constants;
import com.eardatek.player.dtvplayer.system.DTVApplication;
import com.eardatek.player.dtvplayer.update.DownloadReqThread;
import com.eardatek.player.dtvplayer.update.MessageHelper;
import com.eardatek.player.dtvplayer.update.UploadThread;
import com.eardatek.player.dtvplayer.update.VersionInfo;
import com.eardatek.player.dtvplayer.update.VersionReqThread;
import com.eardatek.player.dtvplayer.update.WifiConnectThread;
import com.eardatek.player.dtvplayer.util.LogUtil;
import com.eardatek.player.dtvplayer.util.NetworkUtil;
import com.eardatek.player.dtvplayer.util.PreferencesUtils;
import com.eardatek.player.dtvplayer.widget.ProgressWheel;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Created by Administrator on 16-7-31.
 */


public class HomePageActivity extends BaseActivity{

    private static final String TAG = HomePageActivity.class.getSimpleName();

    private static final int UPDATE_PROGRESS = 1;
    private static final int ERROR_STATE = 2;
    private static final int CHECK_STATE = 3;

    private MyHandler mHandler;
    private UpdateFirmwareHandler mUpdateHandler;
    private ProgressWheel mProgress;
    private TextView mConnect;
    private Button mNext;
    private ImageView mImageState;

    private VersionInfo versionInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mHandler = new MyHandler(HomePageActivity.this);
        init();
    }


    private void init(){
        mNext = (Button) findViewById(R.id.next);
        mConnect = (TextView) findViewById(R.id.connecting);
        mProgress = (ProgressWheel) findViewById(R.id.progreswheel);
        mImageState = (ImageView) findViewById(R.id.image_state);

        mProgress.spin();
        animateOut();

        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);

//        requestFirmwareUpdate();

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = (String) mNext.getTag();
                LogUtil.i(TAG,"tag = " + tag);
                String wifiName = NetworkUtil.getWifiName(getApplicationContext());
                if (tag.equals("next")){
                    if (wifiName != null && (wifiName.contains("MobileTV_") ||
                            wifiName.contains("Mobile_TV_Box_"))){
                        DTVApplication.setWifiName(wifiName);
                        Intent intent = new Intent(HomePageActivity.this, EardatekVersion2Activity.class);
                        startActivity(intent);
                        HomePageActivity.this.overridePendingTransition(R.anim.photo_dialog_in_anim,
                                R.anim.photo_dialog_out_anim);
                        finish();
                    }
                }else {
                    NetworkUtil.openSetting(HomePageActivity.this);
                }
            }
        });

        String sofewareversion = PreferencesUtils.getString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION);
        //如果没有储存过版本号，直接跳过固件更新
        if(sofewareversion == null || sofewareversion.isEmpty()) {
            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
        }
        else {
//            initUpdaeHandler();
////            //开始固件版本检查
//            VersionReqThread versionReqThread = new VersionReqThread(mUpdateHandler);//开始请求
//            versionReqThread.start();
        }
    }

    private void initUpdaeHandler(){
        mUpdateHandler = new UpdateFirmwareHandler(HomePageActivity.this);
    }

    private void animateIn(){
        ObjectAnimator.ofFloat(mImageState,"alpha",0.0f,1.0f).setDuration(1000).start();
    }

    private void animateOut(){
        ObjectAnimator.ofFloat(mImageState,"alpha",1.0f,0.0f).setDuration(1000).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String wifiName = NetworkUtil.getWifiName(getApplicationContext());
        if(requestCode == Constants.WIFI_REQUEST_CODE){
            if (wifiName != null && wifiName.contains("MobileTV_")){
                DTVApplication.setWifiName(wifiName);
                mProgress.setProgress(100);
                Intent intent = new Intent(HomePageActivity.this, EardatekVersion2Activity.class);
                startActivity(intent);
                HomePageActivity.this.overridePendingTransition(R.anim.photo_dialog_in_anim,
                        R.anim.photo_dialog_out_anim);
                finish();
            }
        }else if(requestCode == Constants.OPEN_WIFI_CODE){
            if (wifiName != null && (wifiName.contains("MobileTV_")|| wifiName.contains("Mobile_TV_Box_"))){
                //成功连接到目标设备
                //start upload work
                mConnect.setText("已经连接到设备");
                mUpdateHandler.sendEmptyMessageDelayed(START_UPLOAD, 2500);

            }
            else {
                MessageHelper.sendMsg(mUpdateHandler,OPEN_WIFI, 1, 0);
            }
        }
    }

    private static class MyHandler extends Handler{

        private WeakReference<HomePageActivity> mActivity;

        public MyHandler(HomePageActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HomePageActivity activity = mActivity.get();
            if (activity != null){
                switch (msg.what){
                    case UPDATE_PROGRESS:
                        activity.mProgress.stopSpinning();
                        activity.mProgress.setVisibility(View.INVISIBLE);
                        String wifiName = NetworkUtil.getWifiName(activity);
                        if (wifiName != null && (wifiName.contains("MobileTV_") || wifiName.contains("Mobile_TV_Box_"))){
                            activity.mConnect.setText(activity.getText(R.string.connected));
                            activity.mNext.setVisibility(View.VISIBLE);
                            activity.mNext.setTag("next");
                            activity.mNext.setText(activity.getText(R.string.next));
                            activity.mImageState.setImageResource(R.drawable.ok);
                            activity.mImageState.setVisibility(View.VISIBLE);
                            activity.animateIn();
                        }else {
                            activity.mConnect.setText(activity.getText(R.string.disconnect));
                            activity.mNext.setVisibility(View.VISIBLE);
                            activity.mNext.setTag("connect");
                            activity.mNext.setText(activity.getText(R.string.connect));
                            activity.mImageState.setVisibility(View.VISIBLE);
                            activity.mImageState.setImageResource(R.drawable.cancel);
                            activity.animateIn();
                        }
                        break;
                    case ERROR_STATE:
                        activity.mImageState.setImageResource(R.drawable.cancel);
                        break;
                    case CHECK_STATE:

                        break;
                }
            }

        }
    }


    private final static int START_CONNECT = 32;
    private final static int START_CONNECT_T = 33;
    private final static int OPEN_WIFI = 34;
    private final static int START_UPLOAD = 35;
    public static final int SHOW_INFO = 36;

    private static class UpdateFirmwareHandler extends Handler{
        private WeakReference<HomePageActivity> mActivity;

        public UpdateFirmwareHandler(HomePageActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HomePageActivity activity = mActivity.get();
            long download_size = 0;
            if (activity != null){
                switch (msg.what){
                    //固件版本检查成功
                    case VersionReqThread.VERSIONREQ_FIN:
                        activity.versionInfo = (VersionInfo) msg.obj;
                        String sofewareversion = PreferencesUtils.getString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION);
                        LogUtil.i("Version",sofewareversion);
                        double Ver = Double.parseDouble(sofewareversion)  - 0.1;
                        if(activity.versionInfo.getVersion() > Ver){
                            AlertDialog.Builder builder = activity.getVersionAlert(sofewareversion);
                            builder.show();
                        }else {
                            activity.mConnect.setText("当前固件已经是最新版本");
                            sendEmptyMessageDelayed(START_CONNECT,2500);
                        }
                        break;
                    //固件版本检查
                    case VersionReqThread.VERSION_REQ_ERR:
//                        activity.mConnect.setText("访问服务器错误,将跳过更新");
                        this.sendEmptyMessageDelayed(START_CONNECT,2500);
                        break;
                    //开始下载固件
                    case DownloadReqThread.DOWNLOAD_START:
                        activity.mConnect.setText("正在下载最新版固件...");
                        download_size = 0;
                        break;
                    //固件下载完成，或者已经文件已经存在
                    case DownloadReqThread.DOWNLOAD_EXIST:
                    case DownloadReqThread.DOWNLOAD_FIN:
                        long t_size = msg.arg1 * 100000 + msg.arg2;
                        if(t_size == activity.versionInfo.getSize()){
                            activity.mConnect.setText("固件下载成功");
                            activity.mProgress.stopSpinning();
                            MessageHelper.sendMsg(this, OPEN_WIFI, 0, 0);
                        }else {
                            activity.mConnect.setText("校验失败，将跳过更新");
                            File file = new File((String)msg.obj);
                            if(file.exists()) file.delete();
                            this.sendEmptyMessageDelayed(START_CONNECT,2500);
                        }
                        break;
                    //固件下载进度
                    case DownloadReqThread.DOWNLOAD_COUNT:
                        download_size += (int)msg.obj;
                        double d_per = ((double)download_size / (double)activity.versionInfo.getSize() ) * 100.0;
                        int per = (int)d_per;
                        activity.mConnect.setText("正在下载最新版固件...(" + per + "%)" );
                        break;
                    //固件下载失败
                    case DownloadReqThread.DOWNLOAD_ERR:
                        activity.mConnect.setText("固件下载失败，将跳过更新");
                        String filepath = DownloadReqThread.DOWNLOAD_DIR + "/" + activity.versionInfo.getFilename();
                        File file = new File(filepath);
                        if(file.exists()) file.delete();
                        this.sendEmptyMessageDelayed(START_CONNECT,2500);
                        break;
                    //wifi检测超时或者wifi连接错误
                    case WifiConnectThread.WAIT_WIFI_OVERTIME:
                    case WifiConnectThread.CONNECT_ERR:
                        MessageHelper.sendMsg(this, OPEN_WIFI, 1, 0);
                        break;
                    //成功连接到目标设备
                    case WifiConnectThread.TARGET_CONNECT:
                        activity.mConnect.setText("已经连接到设备");
                        sendEmptyMessageDelayed(START_UPLOAD, 2500);
                        break;
                    //开始上传更新
                    case UploadThread.UPLOAD_START:
                        activity.mConnect.setText("开始连接更新服务器");
                        break;
                    //更新服务器连接错误
                    case UploadThread.UPLOAD_CONNECT_FAIL:
                        activity.mConnect.setText("连接更新服务器失败，将跳过更新");
                        sendEmptyMessageDelayed(START_CONNECT_T, 2500);
                        break;
                    //更新服务器验证失败
                    case UploadThread.UPLOAD_SERVER_FAIL:
                        activity.mConnect.setText("更新服务器验证失败，将跳过更新");
                        sendEmptyMessageDelayed(START_CONNECT_T, 2500);
                        break;
                    //更新服务器无法准备接收文件
                    case UploadThread.UPLOAD_NOT_READY:
                        activity.mConnect.setText("服务器无法接收固件，将跳过更新");
                        sendEmptyMessageDelayed(START_CONNECT_T, 2500);
                        break;
                    //开始上传固件
                    case UploadThread.UPLOAD_FILE_START:
                        activity.mConnect.setText("正在上传固件...");
                        download_size = 0;
                        break;
                    //上传进度
                    case UploadThread.UPLOAD_FILE_COUNT:
                        download_size += (int)msg.obj;
                        double d2_per = ((double)download_size / (double)activity.versionInfo.getSize() ) * 100.0;
                        int per2 = (int)d2_per;
                        activity.mConnect.setText("正在上传固件...(" + per2 + "%)" );
                        break;
                    //固件包校验失败
                    case UploadThread.UPDATE_CHECK_FAIL:
                        activity.mConnect.setText("固件校验失败，将跳过更新");
                        sendEmptyMessageDelayed(START_CONNECT_T, 2500);
                        break;
                    //上传错误
                    case UploadThread.UPLOAD_ERR:
                        activity.mConnect.setText("上传过程中发生未知错误，将跳过更新");
                        sendEmptyMessageDelayed(START_CONNECT_T, 2500);
                        break;
                    //开始固件更新
                    case UploadThread.UPDATE_START:
                        activity.mConnect.setText("固件校验成功，开始固件更新");
                        activity.mProgress.spin();
                        break;
                    //更新成功
                    case UploadThread.UPDATE_FIN:
                        activity.mProgress.stopSpinning();
                        activity.mConnect.setText("固件已经更新到最新版本");
                        sendEmptyMessageDelayed(START_CONNECT_T, 2500);
                        break;
                    //更新错误
                    case UploadThread.UPDATE_ERR:
                        activity.mProgress.stopSpinning();
                        activity.mConnect.setText("固件更新失败，错误码："  + msg.obj.toString() + "" );
                        sendEmptyMessageDelayed(START_CONNECT, 2500);
                        break;
                    //进入到连接设备状态
                    case START_CONNECT:
                        activity.mConnect.setText("正在连接...");
                    case START_CONNECT_T:
                        activity.mNext.setClickable(true);
                        activity.mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
                        break;
                    //显示请求打开wifi弹窗
                    case OPEN_WIFI:
                        AlertDialog.Builder builder = activity.getOpenWifiAlert(msg.arg1);
                        builder.show();
                        break;
                    //开始上传固件
                    case START_UPLOAD:
                        UploadThread uploadThread = new UploadThread(this, "192.168.1.1", 6667, activity.versionInfo);
                        uploadThread.start();
                        break;
                    case SHOW_INFO:
                        activity.info(msg.obj.toString());
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    private AlertDialog.Builder getVersionAlert(String ver){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setTitle("固件更新");
        String hw_size = "";
        if(versionInfo.getSize() > 1024 *1024){
            Double sizeMb = (double)versionInfo.getSize() / 1024.0 /1024.0;
            hw_size = String.format(Locale.ENGLISH,"%.2fMb",sizeMb);
        }else{
            Double sizeKb = (double)versionInfo.getSize() / 1024.0 ;
            hw_size = String.format(Locale.ENGLISH,"%.2fKb",sizeKb);
        }
        builder.setMessage("检查到可更新的固件" +
                "\n版本：" + ver + " --> " +  versionInfo.getVersion() +
                "\n大小：" + hw_size +
                "\n是否更新？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do download work
                mNext.setText("等待");
                mNext.setTag("wait");
                mNext.setClickable(false);
                mNext.setVisibility(View.VISIBLE);
                DownloadReqThread downloadReqThread = new DownloadReqThread(mUpdateHandler,versionInfo);
                downloadReqThread.start();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUpdateHandler.sendEmptyMessageDelayed(START_CONNECT,0);
            }
        });
        return  builder;
    }

    private AlertDialog.Builder getOpenWifiAlert(int status){
        //String last_wifiname = PreferencesUtils.getString(DTVApplication.getAppContext(),"WIFINAME");
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setTitle("准备固件更新");
        if(status == 0)
            builder.setMessage("更新固件需要连接到设备的wifi，是否连接？");
        else
            builder.setMessage("未能连接到设备wifi，是否重试？");
                /*
                if(last_wifiname == null || last_wifiname.isEmpty()){
                */
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                if (intent.resolveActivity(HomePageActivity.this.getPackageManager()) != null)
                    HomePageActivity.this.startActivityForResult(intent, Constants.OPEN_WIFI_CODE);
                else {
                    Toast.makeText(DTVApplication.getAppContext(),
                            R.string.wifi_err_tips,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mConnect.setText("将跳过更新");
                mUpdateHandler.sendEmptyMessageDelayed(START_CONNECT,2500);
            }
        });
        return builder;
    }

    private void info(String str){
        Toast.makeText(HomePageActivity.this, str, Toast.LENGTH_LONG).show();
    }

}
