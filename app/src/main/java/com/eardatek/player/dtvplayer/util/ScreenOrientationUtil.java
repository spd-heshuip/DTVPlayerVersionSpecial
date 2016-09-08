package com.eardatek.player.dtvplayer.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;

public class ScreenOrientationUtil {

	private OrientationListenner mListenner;

	private int mOrientation =  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	private OrientationEventListener mOrEventListener;
	
	private int mOrientation1;
	private OrientationEventListener mOrEventListener1;
	
	private Activity mActivity;
	
	private static ScreenOrientationUtil instance = new ScreenOrientationUtil();
	
	public static ScreenOrientationUtil getInstance(){
		return instance;
	} 
	
	public void start(Activity activity,OrientationListenner listenner){
		this.mListenner = listenner;
		this.mActivity = activity;
		if(mOrEventListener == null){
			initListener();
		}
		mOrEventListener.enable();
	}
	
	public void stop(){
		if(mOrEventListener != null){
			mOrEventListener.disable();
		}
		if(mOrEventListener1 != null){
			mOrEventListener1.disable();
		}
	}
	
	private void initListener(){
		mOrEventListener = new OrientationEventListener(mActivity) {
	        @Override
	        public void onOrientationChanged(int rotation) {
	        	LogUtil.e("ScreenOrientationUtil", ""+rotation);
	            if (rotation == OrientationEventListener.ORIENTATION_UNKNOWN) {
					rotation = 10;
	            }
				//if (orientation == mOrientation) {
//	                return;
//	            }

	            mOrientation = convert2Orientation(rotation);
				if (mListenner != null){
					mListenner.onStart(mOrientation);
				}
//	            mActivity.setRequestedOrientation(mOrientation);
	            
	        }
	    };
	    
	    mOrEventListener1 = new OrientationEventListener(mActivity) {
	        @Override
	        public void onOrientationChanged(int rotation) {
	            if (rotation == OrientationEventListener.ORIENTATION_UNKNOWN) {
					rotation = 10;
//					return;
	            }
	            int orientation = convert2Orientation(rotation);

	            if (orientation == mOrientation1) {
	                return;
	            }
	            mOrientation1 = orientation;
	            if(mOrientation1 == mOrientation){
	            	mOrEventListener1.disable();
	            	mOrEventListener.enable();
	            }
	        }
	    };
	}
	
	public boolean isPortrait(){
		if(mOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || mOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT){
			return true;
		}
		return false;
	}

	public void toggleScreen(){
		mOrEventListener.disable();
		mOrEventListener1.enable();
		int orientation =  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		LogUtil.i("ScreenOrientationUtil","mOrientation:" + mOrientation);
		if(mOrientation ==  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		}else if(mOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}else if(mOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE){
			orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}else if(mOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT){
			orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		}
		mOrientation = orientation;
		LogUtil.i("ScreenOrientationUtil","orientation:" + mOrientation);
		mActivity.setRequestedOrientation(mOrientation);
	}
	
	public int getOrientation(){
		return mOrientation;
	}

	public void setmOrientation(int mOrientation) {
		this.mOrientation = mOrientation;
	}

	private int convert2Orientation(int rotation){
		int orientation;
        if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if ((rotation > 45) && (rotation <= 135)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if ((rotation > 135) && (rotation <= 225)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        } else if ((rotation > 225) && (rotation <= 315)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return orientation;
	}

	public interface OrientationListenner{
		void onStart(int orientation);
	}
}
