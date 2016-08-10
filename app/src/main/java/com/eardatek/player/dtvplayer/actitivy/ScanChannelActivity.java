package com.eardatek.player.dtvplayer.actitivy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.callback.MyEvents;
import com.eardatek.player.dtvplayer.system.DTVApplication;
import com.eardatek.player.dtvplayer.util.ChannelScanner;
import com.eardatek.player.dtvplayer.util.WeakHandler;
import com.eardatek.player.dtvplayer.widget.CustomToolbar;
import com.eardatek.player.dtvplayer.widget.RadarSearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class ScanChannelActivity extends AppCompatActivity {
	public final static String TAG = ScanChannelActivity.class.getSimpleName();
	
    public static final int SCAN_CHANNEL_PER_TP = 10;
    public static final int SCAN_CHANNEL_FINISHED = 11;
    public static final int SCAN_CHANNEL_CLOSED = 12;
    
    private ChannelScanner mChannelScanner ;

    private TextView mShowInfoTextView1 ;
    private TextView mShowInfoTextView2 ;
    private ProgressBar mProgressBar ;
    private int freq = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        int keepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setFlags(keepScreenOn, keepScreenOn);

		setContentView(R.layout.activity_scan_channel);
		initToolbar();
		mShowInfoTextView1 = (TextView)findViewById(R.id.tv_show_info1);
		mShowInfoTextView2 = (TextView)findViewById(R.id.tv_show_info2);
		mProgressBar = (ProgressBar)findViewById(R.id.progressbar);


        RadarSearchView search_device_view = (RadarSearchView) findViewById(R.id.radar_search_view);
		search_device_view.setWillNotDraw(false);	
		search_device_view.setSearching(true);

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

    public void showInfo( String info1, String info2 ) {
        if (info1 != null)
    	    mShowInfoTextView1.setText(info1);
    	if(info2 != null )
    		mShowInfoTextView2.setText(info2);
    }
    
    public void setScanProgress( int progress ) {
    	mProgressBar.setProgress(mProgressBar.getMax() / 100 * progress);
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
                	String info1 = String.format(Locale.ENGLISH," %dMHz  %d%% ", msg.arg1/1000, msg.arg2/10000 ) ;
                	String info2 = String.format(Locale.ENGLISH,"Found channels: %d ", msg.arg2%10000) ;
                	activity.setScanProgress( msg.arg2/10000 ) ;
                	activity.showInfo( info1, info2) ;
                	break ;

                case SCAN_CHANNEL_FINISHED:
                	activity.setScanProgress(100) ;
                    if (msg.arg1 == 0){
                        Toast.makeText(DTVApplication.getAppContext(),"No Chanel Found!",Toast.LENGTH_SHORT).show();
                    }
                    String chanelCount = Integer.toString(msg.arg1);
                	activity.showInfo("100%", "Found channels:" + chanelCount) ;
                	break ;

                case SCAN_CHANNEL_CLOSED:
                    activity.setResult(0);
                    activity.mHandler.removeCallbacksAndMessages(null);
                    activity.finish();
                    break ;
            }
        }
    };   
    
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
