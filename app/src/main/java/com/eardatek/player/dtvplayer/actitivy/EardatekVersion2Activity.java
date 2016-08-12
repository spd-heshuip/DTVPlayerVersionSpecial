package com.eardatek.player.dtvplayer.actitivy;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.blazevideo.libdtv.ChannelInfo;
import com.blazevideo.libdtv.EventHandler;
import com.blazevideo.libdtv.IVideoPlayer;
import com.blazevideo.libdtv.LibDTV;
import com.blazevideo.libdtv.LibDtvException;
import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.adapter.ChanelListAdaptar;
import com.eardatek.player.dtvplayer.adapter.TabFragmentPagerAdapter;
import com.eardatek.player.dtvplayer.bean.Tab;
import com.eardatek.player.dtvplayer.callback.MyEvents;
import com.eardatek.player.dtvplayer.callback.OnTvChanelItemClickListener;
import com.eardatek.player.dtvplayer.callback.ScreenListener;
import com.eardatek.player.dtvplayer.data.AbstractDataProvider;
import com.eardatek.player.dtvplayer.data.TvDataProvider;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.fragment.SwipeWithButtonFragment;
import com.eardatek.player.dtvplayer.layoutmanager.FullyLinearLayoutManager;
import com.eardatek.player.dtvplayer.system.DTVApplication;
import com.eardatek.player.dtvplayer.system.DTVInstance;
import com.eardatek.player.dtvplayer.util.ListUtil;
import com.eardatek.player.dtvplayer.util.LogUtil;
import com.eardatek.player.dtvplayer.util.NetTunerCtrl;
import com.eardatek.player.dtvplayer.util.StringUtil;
import com.eardatek.player.dtvplayer.util.WeakHandler;
import com.eardatek.player.dtvplayer.util.WindowUtil;
import com.eardatek.player.dtvplayer.widget.CustomEditText;
import com.eardatek.player.dtvplayer.widget.CustomToast;
import com.eardatek.player.dtvplayer.widget.ProgressWheel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.eardatek.player.dtvplayer.R.id.indicator_layout;

/**
 * Created by Administrator on 16-3-30.
 */
public class EardatekVersion2Activity extends BaseActivity implements TabHost.OnTabChangeListener,
        ViewPager.OnPageChangeListener,IVideoPlayer,OnTvChanelItemClickListener,SensorEventListener
{

    private static final String TAG = "EardatekVersion2";

    // aspect ratio
    private static final int SURFACE_AUTO = 0;
    private static final int SURFACE_16_9 = 1;
    private static final int SURFACE_4_3 = 2;
    private int mCurrentSize = SURFACE_AUTO;

    private static final int SURFACE_LAYOUT = 3;
    private static final int FADE_OUT = 4;
    private static final int SET_TITLE = 5;
    private static final int START_PROGRESS_WHEEL = 6;
    private static final int STOP_PROGRESS_WHEEL = 7;
    private static final int SET_FREQ = 8;
    private static final int NO_SIGNAL = 9;
    private static final int HIDE_PLAY_ICON = 10;
    private static final int VOLUMBRIGHT_FADE_OUT = 11;
    private static final int FINISH_ANIMATION = 12;
    private static final int LOAD_EPG_INFO = 13;
    private static final int EMPTY_DATA_BASE = 15;
    private static final int PLAY_OR_PAUSE_OPTION = 17;
    private static final int UPDATE_SIGNAL_ICON = 18;
    private static final int SEND_DATA_FAIL_OPTION = 19;
    private static final int PARSE_PID_FAIL = 20;

    /**
     * max volume
     */
    private int mMaxVolume;
    /**
     * the current volume
     */
    private int mVolume = -1;
    /**
     * the current brightness
     */
    private float mTouchY, mTouchX;

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;

    private String mVideoSize = "Auto";

    private volatile boolean mPlaying = false;
    private volatile boolean mReplay = false;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    private LibDTV mLibDTV;

    private SurfaceView mSurfaceView;
    private SurfaceView mSubtitlesSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder mSubtitlesSurfaceHolder;
    private Surface mSurface = null;
    private Surface mSubtitleSurface = null;
    private FrameLayout mSurfaceFrame;
    private View.OnLayoutChangeListener mOnLayoutChangeListener;

    private ChanelListAdaptar mChanelAdapter;


    private Toolbar mToolBar;

    private String mLocation;
    private String mServiceName;
    private int mServiceID;
    private boolean isGetPidList;

    private boolean mLocked;
    private static boolean isSwitching = false;

    private boolean mIsRunning = true;

    private FrameLayout mTitleBar;
    private TextView mTitle;
    private ImageView mGoBackButton;
    private ImageView mTvSignalStrengthButton;
    private TextView mVideoSizeButton;
    private TextView mSWorHW;
    private TextView mCurTime;
    private TextView mBatteryLevelView;
    private ProgressWheel mLoadingProgress;
    private ImageView mPlay;
    private ImageView mIsLock;

    private RelativeLayout mBottomTitleLayout;
    private TextView mFreqText;
    private RecyclerView mChanelList;
    private ImageView mFullScreen;

    private FrameLayout mVolumnOrBrightLayout;
    private TextView mPercentText;
    private ImageView mVolumOrBrightness;
    private TextView mProgramName;

    private FloatingActionButton mFloatButton;

    private CustomEditText mEditFreq;

    private BroadcastReceiver mBatteryLevelReceiver;
    private int mBatteryLevel = 0;

    private int mFreq = 0;
    private boolean isFirstPlay = true;

    private boolean isConnectToDevice = false;

    private int mTvGridAdapterPosition;
    private boolean isChangedProgram;

    public boolean isRadioPlaying = false;
    public boolean isVideoPlaying = false;

    private FragmentTabHost mTabHost;
    private ViewPager mViewPager;

    private List<Tab> mTabs = new ArrayList<>(3);

    private LayoutInflater mInflater;
    private final Handler mHandler = new LiveTvPlayerHandler(this);

    private Thread mPlayThread;
    private  Thread mSignalStatusThread = null;
    private  Thread checkSignalThread = null;

    private int mScreenOrientation = Configuration.ORIENTATION_PORTRAIT;

    private boolean mIsFirstBrightnessGesture = true;

    private int mSurfaceDisplayRange = 0;

    private ChannelInfoDB mDbInstance;
    private ScreenListener mScreenListener;

    private TvDataProvider mDataProvider;

    private boolean isLockScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int keepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setFlags(keepScreenOn, keepScreenOn);

        setContentView(R.layout.activity_eardatek_version2_main);
        mDataProvider = new TvDataProvider();
        initToolbar();
        initSurface();
        initTitlebar();
        initTab();
        initViewPager();
        initFloatButton();

        EventBus.getDefault().register(this);

        startListener();

        startMonitorBatteryState();
        showHideTitleBar(false);

        mScreenListener = new ScreenListener(this);
        mScreenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                LogUtil.i(TAG,"onScreenOn");
            }

            @Override
            public void onScreenOff() {
                LogUtil.i(TAG,"onScreenOff");
                stopPlayAction();
            }

            @Override
            public void onUserPresent() {
                LogUtil.i(TAG,"onUserPresent");
//                isLockScreen = false;
                if (!mPlaying && mScreenOrientation ==
                        Configuration.ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    changeToLandscape();
                }else if(!mPlaying && mScreenOrientation ==
                        Configuration.ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    changeToPortRait();
                }
            }
        });

        mPlay.setVisibility(View.VISIBLE);

        mDbInstance = ChannelInfoDB.getInstance();

        try {
            mLibDTV = DTVInstance.getLibDtvInstance();
        } catch (LibDtvException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "LibDTV initialisation failed");
        }
        //improve decode performance,skip the incorrent frame
//        mLibDTV.setFrameSkip(true);
        //notice this!if the value you set is bigger may be cause decoding very slow,it depend on phone's cpu performance
//        mLibDTV.setDeblocking(100);

        EventHandler em = EventHandler.getInstance();
        em.addHandler(eventHandler);

        mSurfaceHolder.addCallback(mSurfaceCallback);
        mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);

        final MyHandler mSystemHandler = new MyHandler(EardatekVersion2Activity.this);
        //connect to the device....

        NetTunerCtrl.getInstance().start("192.168.1.1", 6000, "TCP", 8000, mSystemHandler, EardatekVersion2Activity.this) ;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom){
                        LogUtil.i(TAG,"setSurfaceLayout");
//                        setSurfaceLayout(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
                        changeSurfaceLayout();
                    }
                }
            };
        }
        mSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
        //set surface size
        setSurfaceLayout(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        int mCurrentOrientation = getResources().getConfiguration().orientation;
//        if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE && mLibDTV.isPlaying()){
//            LogUtil.i(TAG,"onConfigurationChanged ORIENTATION_LANDSCAPE");
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            changeToLandscape();
//        }
//        else if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT){
//            LogUtil.i(TAG,"onConfigurationChanged ORIENTATION_PORTRAIT");
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            changeToPortRait();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mLibDTV.isPlaying() && mPlaying){
            LogUtil.i(TAG,"onStop call stop playing");
            stopPlayAction();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEventBus(MyEvents events){
        switch (events.getEventType()){
            case MyEvents.SCAN_CHANNEL:
                showDialog();
                break;
            case MyEvents.DELETE_PLAYING_ITEM:
                stopPlayAction();
                break;
            case MyEvents.CHANGE_LANDSCAPE_LIST_POSITON:
                mTvGridAdapterPosition = (int) events.getData();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mChanelAdapter.clearData();
        mChanelAdapter.loadData();
        mChanelList.setAdapter(mChanelAdapter);

        mDataProvider = new TvDataProvider();
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> list = fragmentManager.getFragments();
        for (Fragment fragment : list){
            fragment.onActivityResult(requestCode,resultCode,data);
        }
    }

    public void stopPlaying() {
        if (!mPlaying)
            return;
        if (mLoadingProgress.isSpinning())
            mHandler.sendEmptyMessage(STOP_PROGRESS_WHEEL);
        mPlaying = false;
        mReplay = true;
        LogUtil.i(TAG,"stopplaying set replay");
        if(mPlayThread != null && mPlayThread.isAlive())
            mPlayThread.interrupt();
        mLocked = false;
        NetTunerCtrl tuner = NetTunerCtrl.getInstance();
        tuner.abortOperation();
    }

    private void doClean(){
        LogUtil.i(TAG,"do clean call stop playing");
        stopPlaying();

        EventHandler em = EventHandler.getInstance();
        em.removeHandler(eventHandler);

        mLibDTV.eventVideoPlayerActivityCreated(false);

        mIsRunning = false ;
        NetTunerCtrl.getInstance().stop();
        mHandler.removeCallbacksAndMessages(null);

        unregisterReceiver(mBatteryLevelReceiver);
        mScreenListener.unRegisterListener();
        mOrientationListener.disable();
        finish();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        doClean();
    }

    private void changeToLandscape(){
        mFullScreen.setImageResource(R.drawable.icon_quit_fullscreen);
        mToolBar.setVisibility(View.GONE);
        mTabHost.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
        mFloatButton.setVisibility(View.GONE);

        mGoBackButton.setVisibility(View.VISIBLE);
        mCurTime.setVisibility(View.VISIBLE);
        mChanelList.setVisibility(View.VISIBLE);

        mScreenOrientation = Configuration.ORIENTATION_LANDSCAPE;
        WindowUtil.fullScreen(true, this);
        updateChanelList();
    }

    private void animateIn(FloatingActionButton button) {
        button.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(button).translationY(0)
                    .setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener(null)
                    .start();
        }
    }

    private void changeToPortRait(){
        mFullScreen.setImageResource(R.drawable.icon_fullscreen);
        mToolBar.setVisibility(View.VISIBLE);
        mTabHost.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
        if(mFloatButton.getVisibility() == View.GONE)
            animateIn(mFloatButton);

        mIsLock.setVisibility(View.GONE);
        mIsLock.setImageResource(R.drawable.unlock);
        mIsLock.setTag("unlock");

        mGoBackButton.setVisibility(View.GONE);
        mCurTime.setVisibility(View.GONE);
        mChanelList.setVisibility(View.GONE);
        mScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
        if (isChangedProgram){
            isChangedProgram = false;
        }

        isLockScreen = false;
        WindowUtil.fullScreen(false, this);
    }


    public void updateChanelList(){
        mChanelAdapter.clearData();
        mChanelAdapter.loadData();
        mChanelAdapter.setSelectProgram(mLocation);
        mChanelList.setAdapter(mChanelAdapter);
    }

    public void switchSWorHW() {
        String tag = (String) mSWorHW.getTag();
        if (tag.equalsIgnoreCase("SW")) {
            mSWorHW.setText(R.string.software);
            mSWorHW.setTag("HW");
            mLibDTV.setHardwareAcceleration(LibDTV.HW_ACCELERATION_FULL);
            mLibDTV.setDevHardwareDecoder(LibDTV.DEV_HW_DECODER_MEDIACODEC);
            showInfo("硬件解码", 1000);
        } else {
            mSWorHW.setText(R.string.hardware);
            mSWorHW.setTag("SW");
            mLibDTV.setHardwareAcceleration(LibDTV.HW_ACCELERATION_DISABLED);
            showInfo("软件解码", 1000);
        }
        mReplay = true;
        mLibDTV.stop();
        clearSurfaveView();
        LogUtil.i(TAG,"switchSWorHW set replay");
    }

    private void initFloatButton(){
        mFloatButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void initTitlebar(){
        mTitleBar = (FrameLayout) findViewById(R.id.include);
        mTitle = (TextView) findViewById(R.id.textTitle);
        mVideoSizeButton = (TextView) findViewById(R.id.aspect_ratio);
        mTvSignalStrengthButton = (ImageView) findViewById(R.id.tv_signal_strength);
        mSWorHW = (TextView) findViewById(R.id.textSWorHW);
        mGoBackButton = (ImageView) findViewById(R.id.go_back);
        mCurTime = (TextView) findViewById(R.id.tv_cur_time);
        mBatteryLevelView = (TextView) findViewById(R.id.tv_battery_level);
        mPlay = (ImageView) findViewById(R.id.play);
        mVolumnOrBrightLayout = (FrameLayout) findViewById(R.id.volumeorbright_layout);
        mPercentText = (TextView) findViewById(R.id.text_percent);
        mVolumOrBrightness = (ImageView) findViewById(R.id.operation_bg);
        mProgramName = (TextView)findViewById(R.id.program_name);
        mIsLock = (ImageView) findViewById(R.id.lock_unlock);


        mVolumnOrBrightLayout.setAlpha(0.6f);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mPercentText.setText(String.format(Locale.ENGLISH,"%d%%",(mVolume / mMaxVolume * 100)));
//        mPercentText.setText((mVolume / mMaxVolume * 100) + "%");
        mGestureDetector = new GestureDetector(EardatekVersion2Activity.this,new MyGestureListener());


        mBottomTitleLayout = (RelativeLayout) findViewById(R.id.listlayout);
        mBottomTitleLayout.setAlpha(0.9f);
        mFreqText = (TextView) findViewById(R.id.program_freq);
        mChanelList = (RecyclerView) findViewById(R.id.channel_list);
        mFullScreen = (ImageView) findViewById(R.id.fullscreen);
        mChanelList.setVisibility(View.INVISIBLE);
        mChanelAdapter = new ChanelListAdaptar(EardatekVersion2Activity.this, new ChanelListAdaptar.OnItemClickListener() {
            @Override
            public void onClick(String location, boolean isReplay,int position) {
                if (!location.equals(mLocation)){
                    isSwitching = true;
                    mLocation = location;
                    if (mLibDTV.isPlaying() && mPlaying)
                        mLibDTV.stop();
                    clearSurfaveView();
                    mReplay = true;
                    LogUtil.i(TAG,"mChanelAdapter set replay");
//                    startTranslationXAnimation(mSurfaceView, mDx);
                    mTvGridAdapterPosition = position;
                    isChangedProgram = true;
                    MyEvents.postEvent(MyEvents.CHANGE_PROGRAM,position);
                }
            }
        });
        FullyLinearLayoutManager linearLayoutManager = new FullyLinearLayoutManager(EardatekVersion2Activity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mChanelList.setLayoutManager(linearLayoutManager);
        mChanelList.setAdapter(mChanelAdapter);

        mFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScreenOrientation = getResources().getConfiguration().orientation;
                mClick = true;
                if (!mIsLand && mPlaying) {
                    changeToLandscape();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mIsLand = true;
                    mClickLand = false;
                } else {
                    changeToPortRait();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mIsLand = false;
                    mClickPort = false;
                }
            }
        });

        mGoBackButton.setVisibility(View.GONE);
        mCurTime.setVisibility(View.GONE);
        mTitleBar.setAlpha(0.9f);


        mLoadingProgress = (ProgressWheel) findViewById(R.id.pw_progress);
        mLoadingProgress.setProgress(180);
        mLoadingProgress.incrementProgress();

        mVideoSizeButton.setText(mVideoSize);
        mVideoSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentSize++;
                mCurrentSize %= 3;
                String[] str = {"Auto","16:9","4:3"};
                mVideoSize = str[mCurrentSize];
                mVideoSizeButton.setText(mVideoSize);

                showInfo("长宽比例: " + mVideoSize, 1000);
                changeSurfaceLayout();
            }
        });

        mTvSignalStrengthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetTunerCtrl tuner = NetTunerCtrl.getInstance();
                showInfo(String.format(Locale.ENGLISH,"信号强度: %d dBm", tuner.getSignalStrength()), 500);
            }
        });

        mSWorHW.setClickable(true);
        mSWorHW.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchSWorHW();
            }
        });

        mGoBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                changeToPortRait();
                mIsLand = false;
                mClickPort = false;
            }
        });


        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayOrPauseOptionEvent();
            }
        });

        mIsLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsLock.getTag().toString().equals("unlock")){
                    isLockScreen = true;
                    CustomToast.showToast(DTVApplication.getAppContext(),"已锁定屏幕",3000);
                    mIsLock.setImageResource(R.drawable.fitness_lock);
                    mIsLock.setTag("lock");
                }else if (mIsLock.getTag().toString().equals("lock")){
                    isLockScreen = false;
                    CustomToast.showToast(DTVApplication.getAppContext(),"已解锁屏幕",3000);
                    mIsLock.setImageResource(R.drawable.unlock);
                    mIsLock.setTag("unlock");
                }
            }
        });
    }

    private void clearSurfaveView(){
        Canvas canvas = mSurfaceView.getHolder().lockCanvas();
        if (canvas != null){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mSurfaceView.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void PlayOrPauseOptionEvent(){
        if (mPlay.getTag().equals("play")){
            if (isFirstPlay)
                return;
            startPlay(mLocation);
            mHandler.sendEmptyMessageDelayed(HIDE_PLAY_ICON, 3000);
        }else if (mPlay.getTag().equals("pause")){
            stopPlayAction();
            LogUtil.i(TAG,"PlayOrPauseOptionEvent set replay");
            mHandler.sendEmptyMessageDelayed(HIDE_PLAY_ICON,3000);
        }
    }

    private void initSurface(){
        mSurfaceView = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                surfaceViewTouchEvent(event);
                return true;
            }
        });
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);


        mSubtitlesSurfaceView = (SurfaceView) findViewById(R.id.subtitles_surface);
        mSubtitlesSurfaceHolder = mSubtitlesSurfaceView.getHolder();
        mSubtitlesSurfaceView.setZOrderMediaOverlay(true);
        mSubtitlesSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        mSubtitlesSurfaceView.setVisibility(View.GONE);
    }


    private void initToolbar(){
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitle(R.string.toolbar_title);
        mToolBar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        setSupportActionBar(mToolBar);

        mToolBar.setOnMenuItemClickListener(onMenuItemClickListener);

        if (getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

    }

    private void stopPlayAction(){
        mPlay.setImageResource(R.drawable.icon_play_64);
        mPlay.setVisibility(View.VISIBLE);
        mPlay.setTag("play");
        mPlaying = false;
        mReplay = true;
        mLibDTV.stop();
        mPlayThread.interrupt();
        stopSignalAndCheckLockThread();
        clearSurfaveView();
        isLockScreen = false;
    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.search_layout,
                null);
        final Dialog dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        Button search_all = (Button) view.findViewById(R.id.search_all_btn);
        Button search_ad = (Button) view.findViewById(R.id.advance_search_btn);
        mEditFreq = (CustomEditText) view.findViewById(R.id.freq_edit);

        search_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();
                if (mPlaying) {
                    stopPlayAction();
                    LogUtil.i(TAG,"mSearchAll set replay");
                }
                mDbInstance.emptyDatabase();
                ListUtil.clearFile("Channel.txt");
                Intent intent = new Intent(EardatekVersion2Activity.this, ScanChannelActivity.class);
                intent.putExtra("itemLocation", "scan channels country xxx");
                startActivityForResult(intent, 10);
                EardatekVersion2Activity.this.overridePendingTransition(R.anim.photo_dialog_in_anim,
                        R.anim.photo_dialog_out_anim);
            }
        });

        search_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditFreq.getText().toString();
                boolean isNumber = StringUtil.isNumber(text);
                if (!TextUtils.isEmpty(text) && isNumber){
                    int freq = Integer.parseInt(mEditFreq.getText().toString());
                    if (freq > 858 || freq < 100 ){
                        Toast.makeText(getApplicationContext(), R.string.freqtips, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (dialog.isShowing())
                        dialog.dismiss();
                    if (mPlaying) {
                        mPlay.setImageResource(R.drawable.icon_play_64);
                        mPlay.setTag("play");
                        mPlay.setVisibility(View.VISIBLE);
                        mPlaying = false;
                        mReplay = true;
                        mLibDTV.stop();
                        stopSignalAndCheckLockThread();
                        LogUtil.i(TAG,"mAdvanceSearch set replay");
                    }

                    Intent intent = new Intent(DTVApplication.getAppContext(), ScanChannelActivity.class);
                    intent.putExtra("advance_search", freq*1000) ;
                    EardatekVersion2Activity.this.startActivityForResult(intent, 9);
                    EardatekVersion2Activity.this.overridePendingTransition(R.anim.photo_dialog_in_anim, R.anim.photo_dialog_out_anim);
                }else
                    Toast.makeText(getApplicationContext(),R.string.freqtips,Toast.LENGTH_SHORT).show();

            }
        });

        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        //设置窗口显示位置
        window.setGravity(Gravity.CENTER);
        // 设置窗口动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //设置窗口显示位置偏移量
        wl.x = 0;
        wl.y = 0;
        // 使窗口宽度铺满手机屏幕
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 窗口属性改变时，显示窗口
        dialog.onWindowAttributesChanged(wl);
        // 点击外围dimiss dialog
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


    private View buildIndicactor(Tab tab){
        View view = mInflater.inflate(R.layout.tab_indicator,null,false);
        TextView textView = (TextView) view.findViewById(R.id.txt_indicator);
        LinearLayout layout = (LinearLayout) view.findViewById(indicator_layout);
        textView.setText(tab.getTitle());
        layout.setBackgroundResource(R.drawable.selector_tab_indicator_background);
        return view;
    }

    private void initTab(){
        mInflater = LayoutInflater.from(this);
        Tab tab = new Tab(R.string.program, SwipeWithButtonFragment.class);
//        Tab tab1 = new Tab(R.string.epg, EpgFragment.class);

        mTabs.add(tab);
//        mTabs.add(tab1);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        for (Tab tab0 : mTabs){
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(tab0.getTitle()));
            tabSpec.setIndicator(buildIndicactor(tab0));
            mTabHost.addTab(tabSpec,tab0.getFragment(),null);
        }

        mTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mTabHost.setCurrentTab(0);
        mTabHost.setOnTabChangedListener(this);
    }

    private void initViewPager(){
        mViewPager = (ViewPager) findViewById(R.id.pager);
        TabFragmentPagerAdapter mPagerAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(), mTabs);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onTabChanged(String tabId) {
        int position = mTabHost.getCurrentTab();
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int curItem = mViewPager.getCurrentItem();
        mTabHost.setCurrentTab(curItem);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private void startPlay(String location){
        mPlayThread = new Thread(new MyRunable(EardatekVersion2Activity.this));
        mPlayThread.start();
    }


    public void _lockChannel(String location) {
        if (TextUtils.isEmpty(location))
            return;
        String params[] = location.split("-");
        if (params.length != 5)
            return;

        NetTunerCtrl tuner = NetTunerCtrl.getInstance();
        mFreq = Integer.parseInt(params[0].substring(4));
        mHandler.sendEmptyMessage(SET_FREQ);

        int mBandWidth = Integer.parseInt(params[1].substring(2));
        int mPlp = Integer.parseInt(params[2].substring(3));
        int serviceID = Integer.parseInt(params[3].substring(4));
        mServiceID = serviceID;
        int isradio = Integer.parseInt(params[4].substring(7));

        ChannelInfo media = mDbInstance.getChannelInfo(location);

        if (media == null){
            LogUtil.i(TAG,"invalis media");
            mLocked = false;
            return;
        }
        mServiceName = media.getTitle().trim();
        mHandler.sendEmptyMessage(SET_TITLE);

        isSwitching = true;
        mLocked = tuner.lockFreqPoint(mFreq, mBandWidth, mPlp,serviceID);
        if (!mLocked || !mPlaying) {
            // 已终止
            mLocked = false;
            if (mLibDTV.isPlaying())
                mLibDTV.stop();
            return;
        }

        String loc = tuner.getMediaLocation();
        String codec = "";
        if( mSWorHW.getTag().equals("HW") )
            codec = ":codec=mediacodec,all" ;
        String isRadio = "";
//        if (isradio == 1)
//            isRadio = ":no-video";
        String opt[] = {
                ":network-caching=1500",
                codec,
                String.format(Locale.ENGLISH,":program=%d", serviceID),
                isRadio
        };

        isGetPidList = false;
        mLibDTV.playMRL(loc, opt);

        mLibDTV.setProgram(serviceID);
        isGetPidList = true;
    }

    @Override
    public void setSurfaceLayout(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        if (width * height == 0)
            return;

        // store video size
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;
        Message msg = mHandler.obtainMessage(SURFACE_LAYOUT);
        mHandler.sendMessage(msg);
    }


    @Override
    public int configureSurface(final Surface surface, final int width, final int height, final int hal) {
        //Only use before android 2.3,do not modity this method
        if (true || surface == null)
            return -1;
        if (width * height == 0)
            return 0;
        LogUtil.d(TAG, "configureSurface: " + width + "x" + height);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mSurface == surface && mSurfaceHolder != null) {
                    if (hal != 0)
                        mSurfaceHolder.setFormat(hal);
                    mSurfaceHolder.setFixedSize(width, height);
                } else if (mSubtitleSurface == surface && mSubtitlesSurfaceHolder != null) {
                    if (hal != 0)
                        mSubtitlesSurfaceHolder.setFormat(hal);
                    mSubtitlesSurfaceHolder.setFixedSize(width, height);
                }

                synchronized (surface) {
                    surface.notifyAll();
                }
            }
        });

        try {
            synchronized (surface) {
                surface.wait();
            }
        } catch (InterruptedException e) {
            return 0;
        }
        return 1;
    }

    public void eventHardwareAccelerationError() {
        EventHandler em = EventHandler.getInstance();
        em.callback(EventHandler.HardwareAccelerationError, new Bundle());
    }

    @Override
    public void onItemClick(String location,int positon) {
        if ((location.equals(mLocation) && mPlaying) ||
                !NetTunerCtrl.getInstance().isConnectedToDevice())
            return;
        mLocation = location;
        mTvGridAdapterPosition = positon;
        isFirstPlay = false;
        isVideoPlaying = true;
        isRadioPlaying = false;
        if (!mPlaying){
            startPlay(mLocation);
        }else {
            mLibDTV.stop();
            clearSurfaveView();
            mReplay = true;
            LogUtil.i(TAG,"onItemClick set replay");
            mPlayThread.interrupt();
            stopSignalAndCheckLockThread();
        }

    }

    private final Handler eventHandler = new VideoPlayerEventHandler(this);

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        LogUtil.i(TAG,"onSensorChanged");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        LogUtil.i(TAG,"onAccuracyChanged");
    }

    private static class VideoPlayerEventHandler extends WeakHandler<EardatekVersion2Activity> {
        public VideoPlayerEventHandler(EardatekVersion2Activity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            EardatekVersion2Activity activity = getOwner();
            if (activity == null) return;
            // Do not handle events if we are leaving the VideoPlayerActivity

            int event = msg.getData().getInt("event");
            switch (event) {
                case EventHandler.MediaParsedChanged:
                case EventHandler.MediaPlayerPlaying:
                case EventHandler.MediaPlayerPaused:
                case EventHandler.MediaPlayerStopped:
                case EventHandler.MediaPlayerTimeChanged:
                case EventHandler.MediaPlayerVout:
                case EventHandler.MediaPlayerPositionChanged:
                    break;
                case EventHandler.MediaPlayerEndReached:
                    LogUtil.i(TAG, "MediaPlayerEndReached");
//                    activity.playbackEndReached();
                    break;
                case EventHandler.MediaPlayerEncounteredError:
                    LogUtil.i(TAG, "MediaPlayerEncounteredError");
                    if (activity.mLibDTV.isPlaying())
                        activity.mLibDTV.stop();
                    activity.mReplay = true;
                    activity.mPlaying = false;
                    activity.mPlay.setVisibility(View.VISIBLE);
                    activity.mPlay.setImageResource(R.drawable.icon_play_64);
                    activity.mPlay.setTag("play");
                    activity.mPlayThread.interrupt();
                    activity.stopSignalAndCheckLockThread();
                    activity.showInfo("MediaPlayerEncounteredError",500);
                    break;
                case EventHandler.HardwareAccelerationError:
                    LogUtil.i(TAG, "HardwareAccelerationError");
                    activity.showInfo("HardwareAccelerationError", 500);
                    activity.stopPlayAction();
                    break;
            }
        }
    }

    private static final class MyRunable implements Runnable{
        private WeakReference<EardatekVersion2Activity> mActivity;

        public MyRunable(EardatekVersion2Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            EardatekVersion2Activity activity = mActivity.get();
            if (activity == null)
                return;
            int lockFailedCount = 0;
            activity.mPlaying = true;
            while (activity.mPlaying) {
                if (activity.mLibDTV.isPlaying())
                    activity.mLibDTV.stop();

                if (!activity.mLoadingProgress.isSpinning())
                    activity.mHandler.sendEmptyMessage(START_PROGRESS_WHEEL);

                while ((!activity.mLocked && lockFailedCount < 2) || activity.mReplay
                        /*|| (activity.mLocked && lockFailedCount < 2 && !activity.isGetPidList)*/){
//                    LogUtil.i(TAG,activity.mLocked + "" + lockFailedCount  + "" + activity.mReplay + activity.isGetPidList);
                    activity.mReplay = false;
                    LogUtil.i(TAG,"MyRunable set replay");
                    activity._lockChannel(activity.mLocation);
                    lockFailedCount++;
                    if (activity.mLocked && !activity.isGetPidList && lockFailedCount < 2){
                        activity._lockChannel(activity.mLocation);
                        lockFailedCount++;
                    }
                }

                if (!activity.mLocked || !activity.isGetPidList){
                    isSwitching = false;
                    activity.mHandler.sendEmptyMessage(STOP_PROGRESS_WHEEL);
                    activity.mHandler.sendEmptyMessage(NO_SIGNAL);
                    activity.mPlaying = false;
                    return;
                }

                lockFailedCount = 0;

                activity.mHandler.sendEmptyMessage(STOP_PROGRESS_WHEEL);
                activity.mHandler.sendEmptyMessage(LOAD_EPG_INFO);
                if (isSwitching && activity.mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    activity.mHandler.sendEmptyMessage(FINISH_ANIMATION);

                isSwitching = false;

                if (activity.mSignalStatusThread == null || !activity.mSignalStatusThread.isAlive()){
                    activity.mSignalStatusThread = new Thread(new SignalStatusRunnable(activity));
                    activity.mSignalStatusThread.start();
                }
                for (;activity.mPlaying || !activity.mPlayThread.isInterrupted();) {
                    if (activity.mReplay) {
                        LogUtil.i(TAG, "stop playing");
                        break;
                    }
                }

                NetTunerCtrl.getInstance().stopStream();
                activity.stopSignalAndCheckLockThread();
            }
        }
    }

    private void stopSignalAndCheckLockThread(){
        if (mSignalStatusThread != null && mSignalStatusThread.isAlive())
            mSignalStatusThread.interrupt();
    }

    private static final class SignalStatusRunnable implements Runnable {

        private WeakReference<EardatekVersion2Activity> mActivity;

        public SignalStatusRunnable(EardatekVersion2Activity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            EardatekVersion2Activity activity = mActivity.get();
            if (activity == null)
                return;
            boolean isSuccess;
            if(activity.mPlaying || !activity.mSignalStatusThread.isInterrupted()){
                isSuccess = NetTunerCtrl.getInstance().getSignalStatus();
                if (!isSuccess)
                    return;
                int signalStrength = NetTunerCtrl.getInstance().getSignalStrength();
                Message message = activity.mHandler.obtainMessage(UPDATE_SIGNAL_ICON);
                message.arg1 = signalStrength;
                message.arg2 = NetTunerCtrl.getInstance().getQuality();
                message.obj = NetTunerCtrl.getInstance().getQam();
                activity.mHandler.sendMessage(message);

            }
        }
    }

    private void showInfo(String info, int timeout)
    {
        Toast.makeText(getApplicationContext(), info, timeout).show();
    }

    private void playOrPause(boolean isplay){
        if (isplay){
            mPlay.setImageResource(R.drawable.icon_play_64);
            mPlay.setVisibility(View.VISIBLE);
            mPlay.setTag("play");
            isFirstPlay = false;
            mHandler.sendEmptyMessageDelayed(HIDE_PLAY_ICON, 3000);
        }else {
            mPlay.setImageResource(R.drawable.icon_pause_64);
            mPlay.setVisibility(View.VISIBLE);
            mPlay.setTag("pause");
            isFirstPlay = false;
            mHandler.sendEmptyMessageDelayed(HIDE_PLAY_ICON, 3000);
        }
    }

    public final static class MyHandler extends Handler {
        private WeakReference<EardatekVersion2Activity> mActivity;

        public MyHandler(EardatekVersion2Activity activity) {
            this.mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            EardatekVersion2Activity mainActivity = mActivity.get();
            if (mainActivity != null){
                switch (msg.what) {
                    case 0:
                        if( mainActivity.mIsRunning ){
                            CustomToast.showToast(mainActivity.getApplicationContext(),
                                    "成功连接到设备！",3000);
                            mainActivity.isConnectToDevice = true;
                        }
                        break ;
                    case 1:
                        if( mainActivity.mIsRunning ){
                            CustomToast.showToast(mainActivity.getApplicationContext(),
                                    "连接设备失败，请检查手机是否连接到设备wifi！",3000);
                            mainActivity.isConnectToDevice = false;
                            if (mainActivity.mLibDTV.isPlaying())
                                mainActivity.mLibDTV.stop();
                        }
                        break ;
                    case 2:
                        if( mainActivity.mIsRunning ){
                            CustomToast.showToast(mainActivity.getApplicationContext(),
                                    "正在连接设备..",3000);
                        }
                        break ;
                    case 3:
                        mainActivity.stopPlayAction();
                        break;
                    case 4:
                        if (mainActivity.mPlaying){
                            CustomToast.showToast(DTVApplication.getAppContext(),
                                    "设备已断开连接，请检查是否已连接到设备wifi！",5000);
                            mainActivity.stopPlayAction();
                        }
                        break;
                }
            }
        }
    }

    public void startProgressWheel(boolean start) {
        if (start) {
            mLoadingProgress.setVisibility(View.VISIBLE);
            mLoadingProgress.spin();//使控件开始旋转
        } else {
            mLoadingProgress.setVisibility(View.INVISIBLE);
            mLoadingProgress.stopSpinning();//使控件停止旋转
        }
    }

    private void updateSignalIcon(int signalStrength,int signalQuality){
        if (signalStrength > -50)
            mTvSignalStrengthButton.setImageResource(R.drawable.tv_signal_strength_5);
        else if (signalStrength > -60)
            mTvSignalStrengthButton.setImageResource(R.drawable.tv_signal_strength_4);
        else if (signalStrength > -70)
            mTvSignalStrengthButton.setImageResource(R.drawable.tv_signal_strength_3);
        else if (signalStrength > -80)
            mTvSignalStrengthButton.setImageResource(R.drawable.tv_signal_strength_2);
        else if (signalStrength > -100)
            mTvSignalStrengthButton.setImageResource(R.drawable.tv_signal_strength_1);
        else
            mTvSignalStrengthButton.setImageResource(R.drawable.tv_signal_strength_0);

    }

    private void showHideTitleBar(boolean bShow) {
        mHandler.removeMessages(FADE_OUT);
        if (bShow) {
            if (!mPlaying && mScreenOrientation == Configuration.ORIENTATION_PORTRAIT)
                return;

            if (mSignalStatusThread == null || !mSignalStatusThread.isAlive()){
                mSignalStatusThread = new Thread(new SignalStatusRunnable(EardatekVersion2Activity.this));
                mSignalStatusThread.start();
            }

            SimpleDateFormat df = new SimpleDateFormat("HH:mm",Locale.CHINA);
            long time = System.currentTimeMillis();
            String now = df.format(new Date(time));
            mCurTime.setText(now);

            mBatteryLevelView.setText(String.format(Locale.ENGLISH,"%3d%%", mBatteryLevel));

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                mIsLock.setVisibility(View.INVISIBLE);
            else
                mIsLock.setVisibility(View.VISIBLE);

            mTitleBar.setVisibility(View.VISIBLE);
            mBottomTitleLayout.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(mTitleBar,"alpha",0.0f,0.5f).setDuration(1000).start();
            ObjectAnimator.ofFloat(mBottomTitleLayout,"alpha",0.0f,0.5f).setDuration(1000).start();
            if (!mLoadingProgress.isSpinning())
                mPlay.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(FADE_OUT, 5000);
        } else {
            mIsLock.setVisibility(View.INVISIBLE);
            ObjectAnimator.ofFloat(mTitleBar,"alpha",0.5f,0.0f).setDuration(1000).start();
            mTitleBar.setVisibility(View.INVISIBLE);
            if (mPlaying)
                mPlay.setVisibility(View.INVISIBLE);
            if(mChanelList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                ObjectAnimator.ofFloat(mBottomTitleLayout,"alpha",0.5f,0.0f).setDuration(1000).start();
                mBottomTitleLayout.setVisibility(View.INVISIBLE);
            }
            else
                mHandler.sendEmptyMessageDelayed(FADE_OUT, 5000);
        }
    }


    private static class LiveTvPlayerHandler extends WeakHandler<EardatekVersion2Activity> {
        public LiveTvPlayerHandler(EardatekVersion2Activity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            EardatekVersion2Activity activity = getOwner();
            if (activity == null)
                return;

            switch (msg.what) {
                case FADE_OUT:
                    activity.showHideTitleBar(false);
                    break;
                case SURFACE_LAYOUT:
                    activity.changeSurfaceLayout();
                    break;
                case SET_TITLE:
                    activity.mTitle.setText(activity.mServiceName);
                    activity.mProgramName.setVisibility(View.VISIBLE);
                    activity.mProgramName.setText(activity.mServiceName);
                    break;
                case START_PROGRESS_WHEEL:
                    activity.mPlay.setVisibility(View.INVISIBLE);
                    activity.startProgressWheel(true);
                    break;
                case STOP_PROGRESS_WHEEL:
                    activity.startProgressWheel(false);
                    activity.mProgramName.setVisibility(View.INVISIBLE);
                    if (activity.mLocked && activity.mPlaying){
                        activity.playOrPause(false);
                    }else {
                        activity.playOrPause(true);
                    }
                    break;
                case SET_FREQ:
                    activity.mFreqText.setText(String.format(Locale.ENGLISH,"Freq:%d" + "KHz",activity.mFreq));
                    break;
                case NO_SIGNAL:
                    activity.mLibDTV.stop();
                    activity.mPlaying = false;
                    activity.mReplay = true;
                    activity.mPlay.setImageResource(R.drawable.icon_play_64);
                    activity.mPlay.setTag("play");
                    activity.mPlay.setVisibility(View.VISIBLE);
                    if (NetTunerCtrl.getInstance().isConnectedToDevice())
                        activity.showInfo("没有信号哟！",1000);
                    break;
                case HIDE_PLAY_ICON:
                    if (activity.mPlaying)
                        activity.mPlay.setVisibility(View.INVISIBLE);
                    break;
                case VOLUMBRIGHT_FADE_OUT:
                    if (!activity.mLoadingProgress.isSpinning()){
                        activity.mVolumnOrBrightLayout.setVisibility(View.INVISIBLE);
                        sendEmptyMessageDelayed(VOLUMBRIGHT_FADE_OUT,1000);
                    }
                    break;
                case FINISH_ANIMATION:
//                    activity.finishTranslationXAnimation(activity.mSurfaceView,activity.mDx);
                    break;
                case LOAD_EPG_INFO:
                    break;
                case EMPTY_DATA_BASE:
                    activity.mDbInstance.emptyDatabase();
                    break;
                case PARSE_PID_FAIL:
                    activity.mLibDTV.stop();
                    activity.mPlaying = false;
                    activity.mReplay = true;
                    NetTunerCtrl.getInstance().stopStream();
                    activity.startProgressWheel(false);
                    activity.mPlay.setImageResource(R.drawable.icon_play_64);
                    activity.mPlay.setTag("play");
                    activity.mPlay.setVisibility(View.VISIBLE);
                    if (NetTunerCtrl.getInstance().isConnectedToDevice())
                        activity.showInfo("parse pid error,please try to play again!",1000);
                    break;
                case PLAY_OR_PAUSE_OPTION:
//                    activity.PlayOrPauseOptionEvent();
                    break;
                case UPDATE_SIGNAL_ICON:
                    activity.updateSignalIcon(msg.arg1,msg.arg2);
                    break;
                case SEND_DATA_FAIL_OPTION:
                    if (!activity.mPlaying)
                        return;
                    activity.startProgressWheel(false);
                    activity.mPlay.setImageResource(R.drawable.icon_play_64);
                    activity.mPlay.setTag("play");
                    activity.mPlay.setVisibility(View.VISIBLE);
                    activity.mPlaying = false;
                    activity.mReplay = true;
                    isSwitching = false;
                    LogUtil.i(TAG,"handler SEND_DATA_FAIL_OPTION message");
                    if (activity.mPlayThread != null)
                        activity.mPlayThread.interrupt();
                    activity.stopSignalAndCheckLockThread();
                    activity.showInfo("No Signal",1000);
                    break;
            }
        }
    }

    private void startTranslationXAnimation(View view,float dx){
        int width = getWindow().getDecorView().getWidth();
        if (dx > 0){
            ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationX",0.0f,width);
            animator.setDuration(700);
            animator.start();
        }else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationX",0.0f,-width);
            animator.setDuration(700);
            animator.start();
        }
    }

    private void finishTranslationXAnimation(View view,float dx){
        int width = getWindow().getDecorView().getWidth();
        if (dx > 0){
            ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationX",-width,0.0f);
            animator.setDuration(700);
            animator.start();
        }else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationX",width,0.0f);
            animator.setDuration(700);
            animator.start();
        }

    }

    private void showDleteDialog(){
        final SweetAlertDialog alertDialog = new SweetAlertDialog(EardatekVersion2Activity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("您确定吗？")
                .setContentText("删除节目列表将不可恢复！")
                .setCancelText("否")
                .setConfirmText("是")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (mPlaying) {
                            stopPlayAction();
                            LogUtil.i(TAG,"showDleteDialog set replay");
                        }
                        mDbInstance.emptyDatabase();
                        ListUtil.clearFile("Channel.txt");
                        MyEvents.postEvent(MyEvents.DATA_BASE_EMPTY,null);
                        sweetAlertDialog.setTitleText("删除!")
                                .setContentText("节目列表已被删除！")
                                .setConfirmText("是")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                });
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.edit_list:
                    if (ChannelInfoDB.getInstance().getAllVideoProgram().size() > 0)
                        showDleteDialog();
                    else
                        CustomToast.showToast(DTVApplication.getAppContext(),"当前没有节目可删除！",3000);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public  void initBrightnessTouch() {
        float value = 0;
        try {
            value = android.provider.Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = value;
        getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }
    /**
     * 滑动改变声音大小
     *
     * @param percent percent
     */
    private void onVolumeSlide(float percent) {
        if (mScreenOrientation != Configuration.ORIENTATION_LANDSCAPE)
            return;
        int delta = -(int)((percent / mSurfaceDisplayRange) * mMaxVolume);
        int vol = Math.min(Math.max(mVolume + delta,0),mMaxVolume);
        if (delta != 0){
            //        // 变更声音
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,vol,0);
        }
        // 显示
        mPlay.setVisibility(View.INVISIBLE);
        mVolumnOrBrightLayout.setVisibility(View.VISIBLE);
        mVolumOrBrightness.setImageResource(R.drawable.video_volumn_bg);

//        mPercentText.setText((vol * 10 / mMaxVolume) * 10 + "%");
        mPercentText.setText(String.format(Locale.ENGLISH,"%d%%",(vol * 10 / mMaxVolume) * 10));

    }

    /**
     * 滑动改变亮度
     *
     * @param percent percent
     */
    private void onBrightnessSlide(float percent) {
        if (mScreenOrientation != Configuration.ORIENTATION_LANDSCAPE)
            return;
        if (mIsFirstBrightnessGesture)
            initBrightnessTouch();
        float delta = -percent /  mSurfaceDisplayRange * 0.07f;

        // 显示
        mPlay.setVisibility(View.INVISIBLE);
        mVolumnOrBrightLayout.setVisibility(View.VISIBLE);
        mVolumOrBrightness.setImageResource(R.drawable.video_brightness_bg);

        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = Math.min(Math.max(lpa.screenBrightness + delta,0.01f),1);
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.03f;
        getWindow().setAttributes(lpa);

        // 变更百分比
//        mPercentText.setText((int) (lpa.screenBrightness * 100) + "%");
        mPercentText.setText(String.format(Locale.ENGLISH,"%d%%",(int) (lpa.screenBrightness * 100)));

    }
    /**
     * 手势结束
     */
    private void endGesture() {
        mVolume = -1;

        // 隐藏
        mHandler.removeMessages(VOLUMBRIGHT_FADE_OUT);
        mHandler.sendEmptyMessageDelayed(VOLUMBRIGHT_FADE_OUT, 5000);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            showHideTitleBar(false);
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            int x = (int) e2.getRawX();

            float y_changed = e2.getRawY() - mTouchY;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int windowWidth = displayMetrics.widthPixels;
            int windowHeight = displayMetrics.heightPixels;
            if (mSurfaceDisplayRange == 0)
                mSurfaceDisplayRange = Math.min(windowHeight,windowWidth);

            if (mOldX > windowWidth / 2.0 && Math.abs(mOldY - y) > Math.abs((mOldX - x)) && Math.abs(mOldY - y) > 25)// 右边滑动
                onVolumeSlide(y_changed);
            else if (mOldX < windowWidth / 2.0 && Math.abs(mOldY - y) > Math.abs(mOldX - x) && Math.abs(mOldY - y) > 50)// 左边滑动
                onBrightnessSlide(y_changed);
            return true;
        }
    }

    private void changeSurfaceLayout() {
        int sw;
        int sh;

        // get screen size
        sw = getWindow().getDecorView().getWidth();
        sh = getWindow().getDecorView().getHeight();
        if (mLibDTV != null)
            mLibDTV.setWindowSize(sw, sh);

        double dw = sw, dh = sh;
        boolean isPortrait;

        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            LogUtil.e(TAG, "Invalid surface size");
            return;
        }

        // 计算长宽比
        double ar, vw;
        if (mSarDen == mSarNum) {
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // 计算显示长宽比
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_AUTO:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
        }

        SurfaceView surface;
        SurfaceView subtitlesSurface;
        FrameLayout surfaceFrame;

        surface = mSurfaceView;
        subtitlesSurface = mSubtitlesSurfaceView;
        surfaceFrame = mSurfaceFrame;

        // 设置显示尺寸
        ViewGroup.LayoutParams lp = surface.getLayoutParams();
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);

        surface.setLayoutParams(lp);
        subtitlesSurface.setLayoutParams(lp);

        //    设置帧尺寸
        lp = surfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surface.invalidate();
        subtitlesSurface.invalidate();
    }
    /**
     * attach and disattach surface to the lib
     */
    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mLibDTV != null) {
                final Surface newSurface = holder.getSurface();
                if (mSurface != newSurface) {
                    if (mSurface != null) {
                        synchronized (mSurface) {
                            mSurface.notifyAll();
                        }
                    }
                    mSurface = newSurface;
                    LogUtil.i(TAG, "surfaceChanged: " + mSurface);
                    mLibDTV.attachSurface(mSurface, EardatekVersion2Activity.this);
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.i(TAG, "surfaceCreated");
            if(mPlaying){
                mReplay = true;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.d(TAG, "surfaceDestroyed");
            if (mLibDTV != null) {
                synchronized (mSurface) {
                    mSurface.notifyAll();
                }
                mSurface = null;
                mLibDTV.detachSurface();
            }
        }
    };

    private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogUtil.i(TAG,"mSubtitlesSurfaceCallback surfaceChanged");
            if (mLibDTV != null) {
                final Surface newSurface = holder.getSurface();
                if (mSubtitleSurface != newSurface) {
                    if (mSubtitleSurface != null) {
                        synchronized (mSubtitleSurface) {
                            mSubtitleSurface.notifyAll();
                        }
                    }
                    mSubtitleSurface = newSurface;
                    mLibDTV.attachSubtitlesSurface(mSubtitleSurface);
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mLibDTV != null) {
                synchronized (mSubtitleSurface) {
                    mSubtitleSurface.notifyAll();
                }
                mSubtitleSurface = null;
                mLibDTV.detachSubtitlesSurface();
            }
        }
    };

    private void changeProgram(float dx){
        if (mChanelAdapter.getChanelList() == null)
            return;
        for (int i = 0; i < mChanelAdapter.getChanelList().size(); i++) {
            ChannelInfo ci = ChannelInfoDB.getInstance().getChannelInfo(mChanelAdapter.getChanelList().get(i).getText());
            if (ci.getLocation().equals(mLocation)) {
                if (dx < 0) {
                    if (i + 1 < mChanelAdapter.getChanelList().size()){
                        ci = ChannelInfoDB.getInstance().getChannelInfo(mChanelAdapter.getChanelList().get(i + 1).getText());
                        mTvGridAdapterPosition = i + 1;
                    }
                    else{
                        ci = ChannelInfoDB.getInstance().getChannelInfo(mChanelAdapter.getChanelList().get(0).getText());
                        mTvGridAdapterPosition = 0;
                    }
                } else if (dx > 0) {
                    if (i > 0){
                        ci = ChannelInfoDB.getInstance().getChannelInfo(mChanelAdapter.getChanelList().get(i-1).getText());
                        mTvGridAdapterPosition = i - 1;
                    }
                    else{
                        ci = ChannelInfoDB.getInstance().getChannelInfo(
                                mChanelAdapter.getChanelList().get(mChanelAdapter.getChanelList().size() -
                                1).getText());
                        mTvGridAdapterPosition = mChanelAdapter.getItemCount() - 1;
                    }
                }

                mLocation = ci.getLocation();
                mChanelAdapter.setSelectProgram(ci.getLocation());
                mLibDTV.stop();
                clearSurfaveView();
                mReplay = true;
                LogUtil.i(TAG,"changeProgram set replay");
                isChangedProgram = true;
                showHideTitleBar(true);
                break;
            }
        }
    }

    private boolean surfaceViewTouchEvent(MotionEvent event){
        if (!mPlaying)
            return false;
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getRawX();
                mTouchY = event.getRawY();
                mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (mVolumnOrBrightLayout.isShown())
                    break;
                float dx = event.getRawX() - mTouchX;
                float dy = event.getRawY() - mTouchY;
                if (Math.abs(dx) > 150 && Math.abs(dx) > Math.abs(dy * 2) && !isSwitching && mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    isSwitching = true;
                    changeProgram(dx);
                    mChanelList.scrollToPosition(mTvGridAdapterPosition);
                    MyEvents events = new MyEvents();
                    events.setEventType(MyEvents.CHANGE_PROGRAM);
                    events.setData(mTvGridAdapterPosition);
                    EventBus.getDefault().post(events);
                }else {
                    if (mTitleBar.getVisibility() == View.INVISIBLE)
                        showHideTitleBar(true);
                    else
                        showHideTitleBar(false);
                }
                endGesture();
                break;
        }

        return true;
    }

    private void startMonitorBatteryState() {
        mBatteryLevelReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                StringBuilder sb = new StringBuilder();
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                int status = intent.getIntExtra("status", -1);
                int health = intent.getIntExtra("health", -1);
                int level = -1; // percentage, or -1 for unknown
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

                mBatteryLevel = level;

                sb.append("The phone");
                if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {
                    sb.append("'s battery feels very hot!");
                } else {
                    switch (status) {
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            sb.append("no battery.");
                            break;
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            sb.append("'s battery");
                            if (level <= 33)
                                sb.append(" is charging, battery level is low"
                                        + "[" + level + "]");
                            else if (level <= 84)
                                sb.append(" is charging." + "[" + level + "]");
                            else
                                sb.append(" will be fully charged.");
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            if (level == 0)
                                sb.append(" needs charging right away.");
                            else if (level > 0 && level <= 33)
                                sb.append(" is about ready to be recharged, battery level is low"
                                        + "[" + level + "]");
                            else
                                sb.append("'s battery level is" + "[" + level + "]");
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            sb.append(" is fully charged.");
                            break;
                        default:
                            sb.append("'s battery is indescribable!");
                            break;
                    }
                }
                sb.append(' ');
                //batterLevel.setText(sb.toString());
            }
        };
        IntentFilter mBatteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryLevelReceiver, mBatteryLevelFilter);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isSwitching){
            CustomToast.showToast(this,"正在切换节目，请稍等！",1000);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eardatek, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                changeToPortRait();
                mIsLand = false;
                mClickPort = false;
            }else
                showQuitDialog();
            return true;
        }
        return super.onKeyDown( keyCode, event ) ;
    }

    public String getServiceName() {
        return mServiceName;
    }

    public int getServiceID() {
        return mServiceID;
    }

    public int getmTvGridAdapterPosition() {
        return mTvGridAdapterPosition;
    }

    public boolean isChangedProgram() {
        return isChangedProgram;
    }

    public void setIsChangedProgram(boolean isChangedProgram) {
        this.isChangedProgram = isChangedProgram;
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }



    private void showQuitDialog(){
        final SweetAlertDialog alertDialog = new SweetAlertDialog(EardatekVersion2Activity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("退出播放器？")
                .setContentText("退出后，记得关闭电视宝设备的电源哟！")
                .setCancelText("否")
                .setConfirmText("退出")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        doClean();
                    }
                });
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    private OrientationEventListener mOrientationListener; // 屏幕方向改变监听器
    private boolean mIsLand = false; // 是否是横屏
    private boolean mClick = false; // 是否点击
    private boolean mClickLand = true; // 点击进入横屏
    private boolean mClickPort = true; // 点击进入竖屏

    private final void startListener() {
        mOrientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (isLockScreen)
                    return;
                if (!mPlaying && (rotation != Configuration.ORIENTATION_PORTRAIT)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    changeToPortRait();
                }
                // 设置竖屏
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    if (mClick) {
                        if (mIsLand && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClick = false;
                            mIsLand = false;
                        }
                    } else {
                        if (mIsLand) {
                            LogUtil.i(TAG,"startListener SCREEN_ORIENTATION_PORTRAIT");
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            changeToPortRait();
                            mIsLand = false;
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if (((rotation >= 230) && (rotation <= 310))) {
                    if (mClick) {
                        if (!mIsLand && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = true;
                        }
                    } else {
                        if (!mIsLand && mPlaying) {
                            LogUtil.i(TAG,"startListener SCREEN_ORIENTATION_LANDSCAPE");
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            changeToLandscape();
                            mIsLand = true;
                            mClick = false;
                        }
                    }
                }
            }
        };
        mOrientationListener.enable();
    }

}