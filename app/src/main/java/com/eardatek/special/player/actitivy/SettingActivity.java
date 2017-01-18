package com.eardatek.special.player.actitivy;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.eardatek.special.player.R;
import com.eardatek.special.player.system.Constants;
import com.eardatek.special.player.util.LogUtil;
import com.eardatek.special.player.util.NetTunerCtrl;
import com.eardatek.special.player.util.PreferencesUtils;
import com.eardatek.special.player.util.WeakHandler;
import com.eardatek.special.player.widget.CustomToolbar;
import com.eardatek.special.player.widget.ProgressWheel;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Created by Luke He on 16-9-18 上午10:17.
 * Email:spd_heshuip@163.com
 * Company:Eardatek
 */
public class SettingActivity extends SwipeBackBaseActicity{

    private static final int UPDATE_SIGNAL_STATUS = 1;
    private static final int UPDATE_LANGUAGE = 2;
    private static final String TAG = SettingActivity.class.getSimpleName();

    private SwitchButton mChinaBtn;
    private SwitchButton mEnBtn;

    private TextView mModulation;

    private ProgressWheel mSignalStrengthPro;
    private ProgressWheel mSignalSQuaPro;

    private TextView mSignalStrength;
    private TextView mSignalQuality;

    private int mFreq;

    private SignalHandler mHandler;
    private Thread mSignalStatusThread;

    public static void startSettingActivity(Context context,int freq){
        Intent intent = new Intent(context,SettingActivity.class);
        intent.putExtra(Constants.CURRENT_FREQ,freq);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if (getIntent() != null){
            mFreq = getIntent().getIntExtra(Constants.CURRENT_FREQ,0);
        }
        init();
    }

    private void init(){
        CustomToolbar mToolbar = (CustomToolbar) findViewById(R.id.toolbar_setting);
        mChinaBtn = (SwitchButton) findViewById(R.id.def_switchbtn);
        mEnBtn = (SwitchButton) findViewById(R.id.english_switchbtn);

        TextView mEngText = (TextView) findViewById(R.id.english_text);
        TextView mChiText = (TextView) findViewById(R.id.china_text);

        TextView mTvType = (TextView) findViewById(R.id.tv_type_text);

        TextView mCurrentFre = (TextView) findViewById(R.id.current_freq);
        mModulation = (TextView) findViewById(R.id.text_qam);

        mSignalStrengthPro = (ProgressWheel) findViewById(R.id.signal_strength_progreswheel);
        mSignalSQuaPro = (ProgressWheel) findViewById(R.id.freq_quality_progressWheel);

        mSignalStrength = (TextView) findViewById(R.id.text_signal_strength);
        mSignalQuality = (TextView) findViewById(R.id.text_quality);

        mTvType.setText(PreferencesUtils.getString(this,Constants.DEVICE_TYPE));

        mChinaBtn.setAnimationDuration(500);
        mEnBtn.setAnimationDuration(500);

        LogUtil.i(TAG,"current locale:" + getResources().getConfiguration().locale.toString());
        if (getResources().getConfiguration().locale.equals(Locale.US)){
            mEngText.setText(getString(R.string.english));
            mEngText.setTextColor(getResources().getColor(R.color.earda_background));

            mChiText.setTextColor(getResources().getColor(R.color.white));

            mEnBtn.setCheckedImmediatelyNoEvent(true);
            mEnBtn.setBackColorRes(R.color.earda_background);
            mEnBtn.setClickable(false);


            mChinaBtn.setCheckedImmediatelyNoEvent(false);
            mChinaBtn.setBackColorRes(R.color.grey700);
        }else {
            mChiText.setText(getString(R.string.chinese));
            mChiText.setTextColor(getResources().getColor(R.color.earda_background));

            mEngText.setTextColor(getResources().getColor(R.color.white));

            mChinaBtn.setCheckedImmediatelyNoEvent(true);
            mChinaBtn.setBackColorRes(R.color.earda_background);
            mChinaBtn.setClickable(false);

            mEnBtn.setCheckedImmediatelyNoEvent(false);
            mEnBtn.setBackColorRes(R.color.grey700);
        }

        mChinaBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (mEnBtn.isChecked()){
                        mEnBtn.setChecked(false);
                        mEnBtn.setBackColorRes(R.color.grey700);
                        mEnBtn.setClickable(true);
                    }
                    mChinaBtn.setBackColorRes(R.color.earda_background);
                    Message msg = mHandler.obtainMessage(UPDATE_LANGUAGE);
                    msg.arg1 = Constants.LAN_CHINESE_SIMPLE;
                    mHandler.sendMessageDelayed(msg,1500);
                }else {
                    if (!mEnBtn.isChecked())
                        mEnBtn.setChecked(true);
                }
            }
        });

        mEnBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (mChinaBtn.isChecked()){
                        mChinaBtn.setChecked(false);
                        mChinaBtn.setBackColorRes(R.color.grey700);
                        mChinaBtn.setClickable(true);
                    }
                    mEnBtn.setBackColorRes(R.color.earda_background);
                    Message msg = mHandler.obtainMessage(UPDATE_LANGUAGE);
                    msg.arg1 = Constants.LAN_ENGLISH;
                    LogUtil.i(TAG,"change to locale:" + msg.arg1);
                    mHandler.sendMessageDelayed(msg,1000);
                }else {
                    if (!mChinaBtn.isChecked())
                        mChinaBtn.setChecked(true);
                }
            }
        });


        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doClean();
            }
        });
        mHandler = new SignalHandler(SettingActivity.this);

        if (mFreq > 0)
            mCurrentFre.setText(String.format(Locale.ENGLISH,"%s:%dMHz", getString(R.string.currfreq), mFreq/1000));
        else{
            mCurrentFre.setText(getString(R.string.nolockfreq));
            mSignalQuality.setText(getString(R.string.nolock));
            mSignalStrength.setText(getString(R.string.nolock));
        }

        if (mFreq > 0){
            mSignalStatusThread = new Thread(new SignalStatusRunable(SettingActivity.this));
            mSignalStatusThread.start();
        }


    }

    private void doClean(){
        if (mSignalStatusThread != null && mSignalStatusThread.isAlive())
            mSignalStatusThread.interrupt();
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
//            mHandler = null;
        }

        finish();
    }

    @SuppressWarnings("deprecation")
    private void switchLanguage(int lan){
        Resources resources = getApplication().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        switch (lan){
            case Constants.LAN_DEFAULT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    configuration.setLocale(Locale.getDefault());
                }else
                    configuration.locale = Locale.getDefault();
                break;
            case Constants.LAN_CHINESE_SIMPLE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
                }else
                    configuration.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case Constants.LAN_ENGLISH:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    configuration.setLocale(Locale.US);
                }else
                    configuration.locale = Locale.US;
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    configuration.setLocale(Locale.getDefault());
                }else
                    configuration.locale = Locale.getDefault();
                break;
        }

        LogUtil.i(TAG,"Locale:" + configuration.locale.toString());
        PreferencesUtils.putInt(this,Constants.LAN_KEY,lan);
        resources.updateConfiguration(configuration,displayMetrics);

        // 杀掉进程
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void updateSignalStatus(int strength,int quality,String modulation){
        LogUtil.i(TAG,"updateSignalStatus");
        mSignalQuality.setText(String.format(Locale.ENGLISH,"%d%%", quality));
        mSignalStrength.setText(String.format(Locale.ENGLISH,"%ddbm", strength));
        if (!TextUtils.isEmpty(modulation)){
            mModulation.setText(String.format(Locale.ENGLISH,"%s%s",getString(R.string.qam),modulation));
        }

        int progress = (int) (quality * 3.6);
        mSignalSQuaPro.setProgress(progress);
        if (strength < -92)
            strength = -92;

        if (strength < 0)
            progress = (int) ((strength + 100) * 3.6);
        else if (strength == 0)
            progress = (int) (90 * 3.6);
        else
            progress = (int) ((100 - strength) * 3.6);

        mSignalStrengthPro.setProgress(progress);
    }

    private static final class SignalHandler extends WeakHandler<SettingActivity> {

        public SignalHandler(SettingActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            SettingActivity activity = getOwner();
            if (activity == null)
                return;

            switch (msg.what){
                case UPDATE_SIGNAL_STATUS:
                    activity.updateSignalStatus(msg.arg1,msg.arg2, (String) msg.obj);
                    break;
                case UPDATE_LANGUAGE:
                    activity.switchLanguage(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    private static final class SignalStatusRunable implements Runnable{

        private WeakReference<SettingActivity> mActivity;

        public SignalStatusRunable(SettingActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            SettingActivity activity = mActivity.get();
            if (activity == null)
                return;
            while (!activity.mSignalStatusThread.isInterrupted()){
                boolean isSuccess = NetTunerCtrl.getInstance().getSignalStatus();
                if (!isSuccess)
                    return;
                int signalStrength = NetTunerCtrl.getInstance().getSignalStrength();
                Message message = activity.mHandler.obtainMessage(UPDATE_SIGNAL_STATUS);

                message.arg1 = signalStrength;
                message.arg2 = NetTunerCtrl.getInstance().getQuality();
                message.obj = NetTunerCtrl.getInstance().getQam();
                activity.mHandler.sendMessage(message);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doClean();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            doClean();
            return true;
        }
            return super.onKeyDown(keyCode, event);
    }
}


