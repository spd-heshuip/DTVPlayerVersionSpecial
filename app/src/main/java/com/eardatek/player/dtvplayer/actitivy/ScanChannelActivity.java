package com.eardatek.player.dtvplayer.actitivy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.system.DTVApplication;
import com.eardatek.player.dtvplayer.util.ChannelScanner;
import com.eardatek.player.dtvplayer.util.LogUtil;
import com.eardatek.player.dtvplayer.util.WeakHandler;
import com.eardatek.player.dtvplayer.widget.CustomToolbar;
import com.eardatek.player.dtvplayer.widget.ProgressWheel;

import java.util.Locale;

public class ScanChannelActivity extends AppCompatActivity {
	public final static String TAG = ScanChannelActivity.class.getSimpleName();
	
    public static final int SCAN_CHANNEL_PER_TP = 10;
    public static final int SCAN_CHANNEL_FINISHED = 11;
    public static final int SCAN_CHANNEL_CLOSED = 12;
    
    private ChannelScanner mChannelScanner ;

    private TextView mFoundChannels;
    private TextView mScanFreq;
    private ProgressWheel mProgressBar ;
    private int freq = 0;
    private int mScaningFreq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        int keepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setFlags(keepScreenOn, keepScreenOn);

		setContentView(R.layout.activity_scan);
		initToolbar();

		mFoundChannels = (TextView)findViewById(R.id.found_channels);
		mScanFreq = (TextView)findViewById(R.id.scan_freq);
		mProgressBar = (ProgressWheel) findViewById(R.id.scan_progreswheel);

        freq = getIntent().getIntExtra("advance_search",0);
//        Log.i(TAG,"freq="+ freq);

        startScan() ;
	}

    public  int getFreq() {
        return freq;
    }

    private void initToolbar(){
        CustomToolbar mToolbar = (CustomToolbar) findViewById(R.id.toolbar_scan);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mChannelScanner.stopScan();
            }
        });
    }

    //Scan all the frequency point,if the freq have signal,get the program infomation(etc:freq,bandwidth,program name,video or radio)
    // save to the database. See the ChannelScanner for the detail;
    public void startScan() {
    	mChannelScanner = new ChannelScanner( mHandler ,this);
        mChannelScanner.startScan();
    }

    public void showInfo( String foundChannels, String scanFreq ) {
        if (foundChannels != null)
    	    mFoundChannels.setText(foundChannels);
    	if(scanFreq != null )
    		mScanFreq.setText(scanFreq);
    }
    
    public void setScanProgress( int progress ) {
    	mProgressBar.setProgress(progress);
    }
    
    private  Handler mHandler = new ScanChannelHandler(this);

    private static class ScanChannelHandler extends WeakHandler<ScanChannelActivity> {
        public ScanChannelHandler(ScanChannelActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
        	ScanChannelActivity activity = getOwner();
            if(activity == null) // WeakReference could be GC'ed early
                return;

            switch (msg.what) {
                case SCAN_CHANNEL_PER_TP:
                    activity.mScaningFreq = msg.arg1/1000;
                    String scanFreq = String.format(Locale.ENGLISH,"%dMHz", msg.arg1/1000) ;
                    String foundChannels = String.format(Locale.ENGLISH,"%d", msg.arg2%10000) ;
                    int progress = (int) ((msg.arg2/10000)  * 3.6);
                    LogUtil.i(TAG,"progress:" + progress);
                    activity.showInfo( foundChannels, scanFreq) ;
                    activity.setScanProgress(progress) ;
                	break ;

                case SCAN_CHANNEL_FINISHED:
                    if (msg.arg1 == 0){
                        Toast.makeText(DTVApplication.getAppContext(),DTVApplication.getAppResources().
                                getString(R.string.nochannletips),Toast.LENGTH_SHORT).show();
                    }
                    String freq = String.format(Locale.ENGLISH,"%dMHz", activity.mScaningFreq) ;
                    String count = String.valueOf(msg.arg1);
                    activity.showInfo( count, freq) ;
                    activity.setScanProgress(360) ;
                    break ;

                case SCAN_CHANNEL_CLOSED:
                    activity.setResult(0);
                    if (activity.mHandler != null)
                        activity.mHandler.removeCallbacksAndMessages(null);
                    activity.finish();
                    break ;
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    private long timeMillis = 0;
    @Override    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
       {
           if ((System.currentTimeMillis() - timeMillis) > 3000){
               Toast.makeText(getApplicationContext(), R.string.scantips, Toast.LENGTH_SHORT).show();
               timeMillis = System.currentTimeMillis();
           }else {
               mChannelScanner.stopScan();
               mHandler.removeCallbacksAndMessages(null);
               finish();
           }
           return true ;
       }
       return super.onKeyDown( keyCode, event ) ;
    }
}
