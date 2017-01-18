package com.eardatek.special.player.actitivy;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eardatek.dtmb.ConnectSwitch;
import com.eardatek.dtmb.UsbDTMBDevice;
import com.eardatek.dtmb.UsbHelper;
import com.eardatek.special.player.R;
import com.eardatek.special.player.system.Constants;
import com.eardatek.special.player.system.DTVApplication;
import com.eardatek.special.player.update.DownloadReqThread;
import com.eardatek.special.player.update.MessageHelper;
import com.eardatek.special.player.update.UploadThread;
import com.eardatek.special.player.update.VersionInfo;
import com.eardatek.special.player.update.VersionReqThread;
import com.eardatek.special.player.update.WifiConnectThread;
import com.eardatek.special.player.util.LogUtil;
import com.eardatek.special.player.util.NetTunerCtrl;
import com.eardatek.special.player.util.NetworkUtil;
import com.eardatek.special.player.util.PreferencesUtils;
import com.eardatek.special.player.widget.ProgressWheel;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.eardatek.special.player.update.DownloadReqThread.DOWNLOAD_START;
import static com.eardatek.special.player.update.UploadThread.UPDATE_CHECK_FAIL;
import static com.eardatek.special.player.util.NetTunerCtrl.isConnectToDevice;

/**
 * Created by Luke He on 16-7-31 上午10:16.
 * Email:spd_heshuip@163.com
 * Company:Eardatek
 */

public class HomePageActivity extends BaseActivity{

    private static final String TAG = HomePageActivity.class.getSimpleName();

    private static final int UPDATE_PROGRESS = 1;
    private static final int ERROR_STATE = 2;
    private static final int CHECK_STATE = 3;
    private static final int JUMP = 4;
    private static final int RECONNECT = 5;

    private MyHandler mHandler;
    private UpdateFirmwareHandler mUpdateHandler;
    private ProgressWheel mProgress;
    private TextView mConnect;
    private Button mNext;
    private ImageView mImageState;

    private VersionInfo versionInfo;

    private UsbHelper usbHelper = null;
    private UsbDevice usbDevice = null;
    private boolean   updatingFlag = true;
    private AlertDialog builder = null;
    private boolean isRestart = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mHandler = new MyHandler(HomePageActivity.this);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        usbHelper.startListen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usbHelper.stopListen();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            NetTunerCtrl.getInstance().close();
            finish();
            return true;
        }
        return super.onKeyDown( keyCode, event ) ;
    }

    private void initUsbHelper(){
        Bundle bundle = HomePageActivity.this.getIntent().getExtras();
        if(bundle != null){
            String restart = bundle.getString("RESTART");
            if(restart != null)
                isRestart = true;
        }
        usbHelper = new UsbHelper(HomePageActivity.this) {
            private void reUpdate(){//恢复update状态
                ConnectSwitch.setType(ConnectSwitch.REMOTE_MODE);//还原为远程连接模式
                mNext.setClickable(true);
                if(updatingFlag){//如果之前正在更新，则跳过更新
                    mConnect.setText("正在连接");
                    mUpdateHandler.sendEmptyMessageDelayed(START_CONNECT_T,2500);
                }else {//如果之前未检查固件，则恢复固件更新
                    if(PreferencesUtils.getString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION) != null){
                        //开始固件版本检查
                        mConnect.setText("正在检查固件版本");
                        VersionReqThread versionReqThread = new VersionReqThread(mUpdateHandler);//开始请求
                        versionReqThread.start();
                    }
                }
            }
            @Override
            public void usb_in() {
                UsbDevice device = usbHelper.getDeviceByPidVid(0x0131, 0x15f4);
                if(device != null){//如果找到目标设备，则尝试请求权限
                    usbDevice = device;//保存device
                    ConnectSwitch.setType(ConnectSwitch.LOCAL_MODE);//设置当前为本地连接模式
                    if(builder != null && builder.isShowing()) builder.dismiss(); //如果当前有更新提示AlertDialog显示则关闭

                    //显示提示框
                    final SweetAlertDialog dialog = new SweetAlertDialog(HomePageActivity.this,SweetAlertDialog.NORMAL_TYPE);
                    if(isRestart){
                        dialog.setTitleText("设备重新接入");
                        dialog.setContentText("将进行初始化");
                    }else {
                        dialog.setTitleText("有效设备接入");
                        dialog.setContentText("检测到可用的OTG设备接入，是否切换到OTG连接模式？");
                    }
                    dialog.setConfirmText("确认");
                    dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            usbHelperHandler.sendEmptyMessageDelayed(PERM_REQ, 100);//100ms后开始初始化otg设备
                            mConnect.setText("正在初始化设备");
                            mImageState.setVisibility(View.INVISIBLE);
                            mProgress.setVisibility(View.VISIBLE);
                            mProgress.spin();
                            mNext.setClickable(false);
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    dialog.setCancelText("取消");
                    dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            reUpdate();
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            }

            @Override
            public void usb_out() {
                if(usbHelper.getDeviceByPidVid(0x0131, 0x15f4) == null){
                    //确认目标设备被拔出
                    reUpdate();
                }
            }

            @Override
            public void usb_Permission_assert(UsbDevice device) {
                if(device.getProductId() == 0x0131 && device.getVendorId() == 0x15f4){
                    int fd = usbHelper.getDeviceFD(device); //获取usb设备文件描述符
                    info("get fd:" + fd);
                    UsbHelperThread thread = new UsbHelperThread(fd);
                    thread.start();
                }

            }

            @Override
            public void usb_Permission_fail(UsbDevice device) {
                info("获取USB权限失败");
                reUpdate();
            }

            private final int START_NEXT = 1;
            private final int PERM_REQ = 2;
            private final int INIT_FAIL = 3;
            private Handler usbHelperHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == START_NEXT){ //跳转到播放界面
                        Intent intent = new Intent(HomePageActivity.this, EardatekVersion2Activity.class);
                        startActivity(intent);
                        HomePageActivity.this.overridePendingTransition(android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right);
                        usbHelper.stopListen();
                        finish();
                    }else if(msg.what == PERM_REQ){//开始请求权限
                        usbHelper.startReq(usbDevice);
                    }
                    else if(msg.what == INIT_FAIL){//初始化失败
                        info("initHWCore Fail");
                    }
                    else {
                        super.handleMessage(msg);
                    }
                }
            };

            class UsbHelperThread extends Thread {
                private int fd;
                private UsbDTMBDevice device = UsbDTMBDevice.getInstance();
                UsbHelperThread(int fd){
                    this.fd = fd;
                }
                @Override
                public void run() {
                    if(device.initHWCore(fd)){
                        while(device.getHWCoreStatus() != 3){//等待服务端监听
                            try {
                                sleep(100);
                                if(device.getHWCoreStatus() > 6){
                                    usbHelperHandler.sendEmptyMessage(INIT_FAIL);
                                    return ;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        usbHelperHandler.sendEmptyMessage(START_NEXT);
                        Log.d("JNIMsg","HWCore Version:" + UsbDTMBDevice.getInstance().getVersion());
                    }
                    else{
                        usbHelperHandler.sendEmptyMessage(INIT_FAIL);
                    }

                }

            };

        };
    }

    private void init(){
        mNext = (Button) findViewById(R.id.next);
        mConnect = (TextView) findViewById(R.id.connecting);
        mProgress = (ProgressWheel) findViewById(R.id.progreswheel);
        mImageState = (ImageView) findViewById(R.id.image_state);
        TextView mVersionName = (TextView) findViewById(R.id.version_text);
        mVersionName.setText(DTVApplication.getVersionInfo());

        mProgress.spin();
        animateOut();

        initUpdateHandler();
        initUsbHelper();
        if(usbHelper.getDeviceByPidVid(0x0131, 0x15f4) != null){//未进行固件检查前USB设备插入
            usbHelper.usb_in();
            updatingFlag = false;
        }

        if(ConnectSwitch.isRemoteType()){

            String sofewareversion = PreferencesUtils.getString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION);
            String hardwareType = PreferencesUtils.getString(DTVApplication.getAppContext(), "HARDWARE_TYPE");
            //如果没有储存过版本号或者固件类型，直接跳过固件更新
            if((sofewareversion == null || sofewareversion.isEmpty())) {
                mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
            }else if( hardwareType == null || hardwareType.isEmpty()){
                mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
            } else {
                usbHelper.stopListen();//开始更新固件就不监听usb
                //开始固件版本检查
//                mConnect.setText("正在检查固件版本");
                VersionReqThread versionReqThread = new VersionReqThread(mUpdateHandler);//开始请求
                versionReqThread.start();
            }
        }

        if(ConnectSwitch.isRemoteType()){
            NetTunerCtrl.getInstance().start("192.168.1.1", 6000, "TCP", 8000);
            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
        }else {
            NetTunerCtrl.getInstance().start("127.0.0.1", 6000, "TCP", 8000);
            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
        }

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = (String) mNext.getTag();
                String wifiName = NetworkUtil.getWifiName(getApplicationContext());
                switch (tag) {
                    case "next":
                        if (NetTunerCtrl.isConnectToDevice(wifiName)) {
//                        DTVApplication.setWifiName(wifiName);
                            Intent intent = new Intent(HomePageActivity.this, EardatekVersion2Activity.class);
                            startActivity(intent);
                            HomePageActivity.this.overridePendingTransition(R.anim.photo_dialog_in_anim,
                                    R.anim.photo_dialog_out_anim);
                            finish();
                        }

                        break;
                    case "connect_wifi":
                        NetworkUtil.openSetting(HomePageActivity.this);
                        break;
                    default:
                        if (ConnectSwitch.isRemoteType()) {
                            NetTunerCtrl.getInstance().start("192.168.1.1", 6000, "TCP", 8000);
                            mHandler.sendEmptyMessage(RECONNECT);
                        } else {
                            NetTunerCtrl.getInstance().start("127.0.0.1", 6000, "TCP", 8000);
                            mHandler.sendEmptyMessage(RECONNECT);
                        }

                        break;
                }
            }
        });


    }

    private void initUpdateHandler(){
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
        NetTunerCtrl.getInstance().start("192.168.1.1", 6000, "TCP", 8000);
        String wifiName = NetworkUtil.getWifiName(getApplicationContext());
        mImageState.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.spin();
        mConnect.setText(getText(R.string.connecting));
        mNext.setVisibility(View.GONE);
        if(requestCode == Constants.WIFI_REQUEST_CODE){
            if (isConnectToDevice(wifiName))
                mHandler.sendEmptyMessageDelayed(JUMP,1000);
            else
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        }else if(requestCode == Constants.OPEN_WIFI_CODE){
            if (isConnectToDevice(wifiName)){
                //成功连接到目标设备
                //start upload work
                mConnect.setText(R.string.connected);
                if (isConnectToDevice(wifiName)){
                    mUpdateHandler.sendEmptyMessageDelayed(START_UPLOAD, 1500);
                }
                else
                    MessageHelper.sendMsg(mUpdateHandler,UPDATE_CHECK_FAIL, 1, 0);

            }
            else {
                MessageHelper.sendMsg(mUpdateHandler,OPEN_WIFI, 1, 0);
            }
        }
    }

    private static class MyHandler extends Handler{

        private WeakReference<HomePageActivity> mActivity;

        MyHandler(HomePageActivity activity) {
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
                        activity.mNext.setClickable(true);
                        boolean isLogin = NetTunerCtrl.getInstance().isConnectedToDevice();
                        if (isConnectToDevice(wifiName) && isLogin){
                            activity.mConnect.setText(activity.getText(R.string.connected));
                            activity.mNext.setVisibility(View.VISIBLE);
                            activity.mNext.setTag("next");
                            activity.mNext.setText(activity.getText(R.string.next));
                            activity.mImageState.setImageResource(R.drawable.ok);
                            activity.mImageState.setVisibility(View.VISIBLE);
                            activity.animateIn();
                        }else if(isConnectToDevice(wifiName) && !isLogin){
                            activity.mConnect.setText(activity.getText(R.string.disconnect));
                            activity.mNext.setVisibility(View.VISIBLE);
                            activity.mNext.setTag("connect");
                            activity.mNext.setText(activity.getText(R.string.connect));
                            activity.mImageState.setVisibility(View.VISIBLE);
                            activity.mImageState.setImageResource(R.drawable.cancel);
                            activity.animateIn();
                        }else if(!isConnectToDevice(wifiName)){
                            activity.mConnect.setText(activity.getText(R.string.disconnect));
                            activity.mNext.setVisibility(View.VISIBLE);
                            activity.mNext.setTag("connect_wifi");
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
                    case RECONNECT:
                        activity.mProgress.setVisibility(View.VISIBLE);
                        activity.mImageState.setVisibility(View.INVISIBLE);
                        activity.mProgress.spin();
                        activity.mNext.setVisibility(View.INVISIBLE);
                        activity.mConnect.setText(activity.getText(R.string.connecting));
                        sendEmptyMessageDelayed(UPDATE_PROGRESS,500);
                        break;
                    case JUMP:
                        if (NetTunerCtrl.getInstance().isConnectedToDevice()){
                            Intent intent = new Intent(activity, EardatekVersion2Activity.class);
                            activity.startActivity(intent);
                            activity.overridePendingTransition(R.anim.photo_dialog_in_anim,
                                    R.anim.photo_dialog_out_anim);
                            activity.finish();
                        }else
                            sendEmptyMessage(UPDATE_PROGRESS);
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

        UpdateFirmwareHandler(HomePageActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HomePageActivity activity = mActivity.get();
            long download_size = 0;
            if(ConnectSwitch.isLocalType()){
                if(activity.builder != null && activity.builder.isShowing())
                    activity.builder.dismiss();
                if(msg.what != START_CONNECT)
                    return ;
            }
            if (activity != null){
                switch (msg.what){
                    //固件版本检查成功
                    case VersionReqThread.VERSIONREQ_FIN:
                        activity.versionInfo = (VersionInfo) msg.obj;
                        String sofewareversion = PreferencesUtils.getString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION);
                        LogUtil.i("Version",sofewareversion);
                        double Ver = Double.parseDouble(sofewareversion);
                        if(activity.versionInfo.getVersion() > Ver){
                            activity.builder = activity.getVersionAlert(sofewareversion);
                            activity.builder.show();
                        }
                        break;
                    //固件版本检查
                    case VersionReqThread.VERSION_REQ_ERR:
//                        activity.mConnect.setText("访问服务器错误,将跳过更新");
//                        this.sendEmptyMessageDelayed(START_CONNECT,2500);
                        break;
                    //开始下载固件
                    case DOWNLOAD_START:
                        activity.mConnect.setText(R.string.downling_fr);
                        download_size = 0;
                        break;
                    //固件下载完成，或者已经文件已经存在
                    case DownloadReqThread.DOWNLOAD_EXIST:
                    case DownloadReqThread.DOWNLOAD_FIN:
                        long t_size = msg.arg1 * 100000 + msg.arg2;
                        if(t_size == activity.versionInfo.getSize()){
                            activity.mConnect.setText(R.string.downl_succ);
                            activity.mProgress.stopSpinning();
                            MessageHelper.sendMsg(this, OPEN_WIFI, 0, 0);
                        }else {
                            File file = new File((String)msg.obj);
                            if(file.exists()) file.delete();
                            if (msg.what == DownloadReqThread.DOWNLOAD_EXIST)
                                activity.startDownload();
                            else {
                                activity.mConnect.setText(R.string.verify_fail);
                                sendEmptyMessageDelayed(START_CONNECT_T, 500);
                            }

                        }
                        break;
                    //固件下载进度
                    case DownloadReqThread.DOWNLOAD_COUNT:
                        download_size += (long)msg.obj;
                        double d_per = ((double)download_size / (double)activity.versionInfo.getSize() ) * 100.0;
                        int per = (int)d_per;
                        activity.mConnect.setText(String.format(Locale.ENGLISH,"%d%%", per));
                        break;
                    //固件下载失败
                    case DownloadReqThread.DOWNLOAD_ERR:
                        activity.mConnect.setText(R.string.downl_fail);
                        String filepath = DownloadReqThread.DOWNLOAD_DIR + "/" + activity.versionInfo.getFilename();
                        File file = new File(filepath);
                        if(file.exists()) file.delete();
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //wifi检测超时或者wifi连接错误
                    case WifiConnectThread.WAIT_WIFI_OVERTIME:
                    case WifiConnectThread.CONNECT_ERR:
                        MessageHelper.sendMsg(this, OPEN_WIFI, 1, 0);
                        break;
                    //成功连接到目标设备
                    case WifiConnectThread.TARGET_CONNECT:
//                        activity.mConnect.setText("已经连接到设备");
                        sendEmptyMessageDelayed(START_UPLOAD, 2500);
                        break;
                    //开始上传更新
                    case UploadThread.UPLOAD_START:
//                        activity.mConnect.setText("开始连接更新服务器");
                        break;
                    //更新服务器连接错误
                    case UploadThread.UPLOAD_CONNECT_FAIL:
                        activity.mConnect.setText(R.string.connect_update_server_fail);
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //更新服务器验证失败
                    case UploadThread.UPLOAD_SERVER_FAIL:
                        activity.mConnect.setText(R.string.verify_server_fail);
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //更新服务器无法准备接收文件
                    case UploadThread.UPLOAD_NOT_READY:
                        activity.mConnect.setText(R.string.server_fail);
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //开始上传固件
                    case UploadThread.UPLOAD_FILE_START:
                        activity.mConnect.setText(R.string.uploading_fr);
                        download_size = 0;
                        break;
                    //上传进度
                    case UploadThread.UPLOAD_FILE_COUNT:
                        download_size += (int)msg.obj;
                        double d2_per = ((double)download_size / (double)activity.versionInfo.getSize() ) * 100.0;
                        int per2 = (int)d2_per;
                        activity.mConnect.setText(R.string.uploading_fr + "(" + per2 + "%)" );
                        break;
                    //固件包校验失败
                    case UPDATE_CHECK_FAIL:
                        activity.mConnect.setText(R.string.verify_fr);
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //上传错误
                    case UploadThread.UPLOAD_ERR:
                        activity.mConnect.setText(R.string.upload_fail);
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //开始固件更新
                    case UploadThread.UPDATE_START:
                        activity.mConnect.setText(R.string.verify_succ);
                        activity.mProgress.spin();
                        break;
                    //更新成功
                    case UploadThread.UPDATE_FIN:
//                        activity.mProgress.stopSpinning();
                        activity.mConnect.setText(R.string.update_succ);
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //更新错误
                    case UploadThread.UPDATE_ERR:
                        activity.mProgress.stopSpinning();
                        activity.mConnect.setText(activity.getString(R.string.update_fail)  + msg.obj.toString() + "" );
                        sendEmptyMessageDelayed(START_CONNECT_T, 500);
                        break;
                    //进入到连接设备状态
                    case START_CONNECT:
//                        activity.mConnect.setText("正在连接...");
                        activity.usbHelper.startListen();
                    case START_CONNECT_T:
                        NetTunerCtrl.getInstance().start("192.168.1.1", 6000, "TCP", 8000);
                        activity.mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1500);
                        activity.usbHelper.startListen();
                        break;
                    //显示请求打开wifi弹窗
                    case OPEN_WIFI:
                        activity.builder = activity.getOpenWifiAlert(msg.arg1);
                        activity.builder.show();
                        break;
                    //开始上传固件
                    case START_UPLOAD:
                        //判断当前设备的固件类型是否与新固件相同
                        if (NetTunerCtrl.getInstance().isConnectedToDevice() &&
                                PreferencesUtils.getString(DTVApplication.getAppContext(),
                                        "HARDWARE_TYPE")
                                        .equals(activity.versionInfo.getDeviceType())){
                            UploadThread uploadThread = new UploadThread(this, "192.168.1.1", 6667, activity.versionInfo);
                            uploadThread.start();
                        }else {
                            activity.mConnect.setText(R.string.device_uncorrect);
                            activity.mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,1000);
                            activity.mNext.setClickable(true);
                        }

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

    private void startDownload(){
        mNext.setText(R.string.wait);
        mNext.setTag("wait");
        mNext.setClickable(false);
        mNext.setVisibility(View.VISIBLE);
        DownloadReqThread downloadReqThread = new DownloadReqThread(mUpdateHandler,versionInfo);
        downloadReqThread.start();
    }

    private AlertDialog getVersionAlert(String ver){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setTitle(R.string.update_fm);
        String hw_size = "";
        if(versionInfo.getSize() > 1024 *1024){
            Double sizeMb = (double)versionInfo.getSize() / 1024.0 /1024.0;
            hw_size = String.format(Locale.ENGLISH,"%.2fMb",sizeMb);
        }else{
            Double sizeKb = (double)versionInfo.getSize() / 1024.0 ;
            hw_size = String.format(Locale.ENGLISH,"%.2fKb",sizeKb);
        }
        builder.setMessage(getString(R.string.check_upda_fm_su) +
                getString(R.string.fr_version) + ver + " --> " +  versionInfo.getVersion() +
                getString(R.string.fr_size) + hw_size +
                getString(R.string.fr_type) + versionInfo.getDeviceType() +
                getString(R.string.update_or_not));
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do download work
                startDownload();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mUpdateHandler.sendEmptyMessage(UPDATE_PROGRESS);
                usbHelper.startListen();
            }
        });
        return  builder.create();
    }

    private AlertDialog getOpenWifiAlert(int status){
        //String last_wifiname = PreferencesUtils.getString(DTVApplication.getAppContext(),"WIFINAME");
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
        builder.setTitle(R.string.ready_update);
        if(status == 0)
            builder.setMessage(R.string.update_wifi_reque);
        else
            builder.setMessage(R.string.wifi_requ_repeat);
                /*
                if(last_wifiname == null || last_wifiname.isEmpty()){
                */
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mConnect.setText(R.string.cancel_update);
                mUpdateHandler.sendEmptyMessageDelayed(START_CONNECT_T,500);
            }
        });
        return builder.create();
    }

    private void info(String str){
        Toast.makeText(HomePageActivity.this, str, Toast.LENGTH_LONG).show();
    }


}
