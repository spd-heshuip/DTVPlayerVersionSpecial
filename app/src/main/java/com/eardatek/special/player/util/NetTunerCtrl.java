package com.eardatek.special.player.util;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;

import com.eardatek.dtmb.ConnectSwitch;
import com.eardatek.special.player.system.Constants;
import com.eardatek.special.player.system.DTVApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Locale;

/**
 * use this class communicate with DVB device
 */
public final class NetTunerCtrl {

	//the Ip address of Server which in DVB device(192.168.1.1)
	private String mServerIP;

	//server port(6000)
	private int mServerPort;

    //data transport protocol(TCP Or UDP)
	private String mDataProtocol;

    //local transport port(8000)
	private int mLocalDataPort ;

    private boolean isFirstLogin = true;

    private boolean isConnectedDevice = false;

    private static Socket mSocket = null;
    private String mTvType = "DVB-T" ;

	//plp count
	private int mPLPCount = 1 ;

	private volatile static boolean mStopped ;
	private volatile boolean mAborted ;
	private static NetTunerCtrl mNetTunerCtrl = null ;

    //Tv signal strength
	private int mSignalStrength = 0 ;
    //ber
	private int  mQuality = 0;

	private int mQam = 0;
	//per
    private double mPer = 0;

    //Heartbeat Thread,check if the app connnectted to the device at any time;

	private static MyThread mThreadHeartBeat;

    //singleTon
	public static NetTunerCtrl getInstance() {
		if( mNetTunerCtrl == null )
			mNetTunerCtrl = new NetTunerCtrl();
		return mNetTunerCtrl ;
	}

    /**
     * connect to the Server which on the Device
     * @return it is success or not
     */
	private  boolean connectToServer() {
		try {
			mSocket = new Socket();
			SocketAddress sa = new InetSocketAddress(mServerIP, mServerPort);
			if (mSocket != null && !mSocket.isConnected() && !mSocket.isClosed())
				mSocket.connect(sa, 3000) ;
			else{
				return false;
			}
			if( mSocket == null || !mSocket.isConnected()) {
				if (mSocket != null)
					mSocket.close() ;
				mSocket = null ;
				return false ;
			}
			mSocket.setKeepAlive(true);
			mSocket.setSoTimeout(100);
		}
		catch (SocketException e){
//			e.printStackTrace();
			mSocket = null ;
			return false ;
		} catch (IOException e) {
//			e.printStackTrace();
			return false;
		}
		return true ;
	}

    //communicate  with the device
	private synchronized String exchangeMessage( String request ,boolean islock) {
		String resp = "";
        int tryCount = 100;
		mAborted = false ;
        if (!islock)
            tryCount = 30;

		if( mSocket == null )
			return "" ;
		try {
			int len = 0 ;
			byte [] response = new byte[1024];

			try{
				mSocket.getInputStream().read( response );
			}catch( SocketTimeoutException e){
				e.printStackTrace();
            }
			
			byte [] req = request.getBytes();
			mSocket.getOutputStream().write( req );
			
//			Log.i("ScanChannelActivity", request ) ;

			for( int tryTimes=0; tryTimes<tryCount && !mAborted ; tryTimes++){
				try{
					len = mSocket.getInputStream().read( response );
				}catch( SocketTimeoutException e){
//					Log.i("ScanChannelActivity", "wait reply" ) ;
					continue ;
				}
				break ;
			}
			if( len <= 0 ) {
//				Log.i("ScanChannelActivity", "No response!" ) ;
				return "";
			}
			resp = new String( response, 0, len );
//			Log.i("ScanChannelActivity", resp ) ;
		}
		catch( SocketException se ) {
			try{
				mSocket.close();
			}catch(Exception e1){
				e1.printStackTrace();
			}
			mSocket = null ;			
		}
		catch( IOException e) {
			return "";
		}
		catch( Exception e) {
			return "";
		}
		return resp ;
	}

    /**
     *
     * @param xml parse string
     * @param paramName param name
     * @param start start flag
     * @param end end flag
     * @return return value
     */
	private String getParamValue( String xml, String paramName, String start, String end ) {
		int posParam = xml.indexOf(paramName);
		if( posParam < 0 )
			return "";
		
		int posStart = xml.indexOf( start, posParam+paramName.length() );
		if( posStart < 0 )
			return "";
		
		int posValue = posStart + start.length();
		
		int posEnd = xml.indexOf( end, posValue );
		if( posEnd < 0 )
			return "";
		
		int len = posEnd - posValue;

		return xml.substring( posValue, posValue+len );
	}
	
	private int getRetValue( String xml ) {
		String val = getParamValue( xml, "ret", "\"", "\"" ) ;
		if( val.equals(""))
			return -1 ;
		
		return Integer.parseInt( val ) ;
	}

	private String mMediaLocation = "";

	private Handler mHandler ;

    /**
     *
     * @param serverIP the dvb device ip address
     * @param serverPort server port
     * @param protocol translate protocol
     * @param localDataPort local data port 8000
     * @param handler handle
     * @param activity activity
     * @return return value
     */
	public boolean start( String serverIP, int serverPort, String protocol, int localDataPort, Handler handler ,Activity activity) {
		mServerIP = serverIP ;
		mServerPort = serverPort ;
		mDataProtocol = protocol ;
		mLocalDataPort = localDataPort ;
		mHandler = handler ;
		mStopped = false ;
		mAborted = false ;

		mThreadHeartBeat = new MyThread(activity) ;
		mThreadHeartBeat.start();

		return true ;
	}

	public boolean start(String serverIP, int serverPort, String protocol, int localDataPort){
		mServerIP = serverIP ;
		mServerPort = serverPort ;
		mDataProtocol = protocol ;
		mLocalDataPort = localDataPort ;
		mStopped = false ;
		mAborted = false ;

		new HomePageThread().start();

		return isConnectedDevice;
	}

	public void reconnect() {
        mSocket = null;
	}

	public void close(){
		try {
			if (mSocket != null){
				mSocket.close();
				mSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		mStopped = true ;
	}
	
	public void abortOperation() {
		mAborted = true ;
	}

    /**
     * call this function to login to the DVB device and then you can send the command to the device;
     * @return login success or not
     */
	private boolean login() {
		String req = String.format(Locale.ENGLISH,"<msg type='login_req'><params protocol='%s' port='%d'/></msg>", mDataProtocol, mLocalDataPort ) ;
		req = req.replace('\'', '\"' );
		String ack = exchangeMessage( req ,false);
		int ret = getRetValue( ack ) ;
		if( ret != 0 )
			return false ;

        String mDeviceInfo = getParamValue(ack, "deviceinfo", " ", "/>");

		// 解释得到电视制式
		 if( mDeviceInfo.isEmpty() )
			 mTvType = "Unkown" ;
		 else if( mDeviceInfo.contains("DVB"))
			 mTvType = "DVB-T/T2" ;
		 else if( mDeviceInfo.contains("ISDBT"))
			 mTvType = "ISDBT" ;
		 else if( mDeviceInfo.contains("DTMB"))
			 mTvType = "DTMB" ;
		 else if( mDeviceInfo.contains("ATSC"))
			 mTvType = "ATSC";


		String mSoftwareVersion = getParamValue(ack, "softwareversion", "\"", "\"");
//		if (!TextUtils.isEmpty(mSoftwareVersion)){
//			PreferencesUtils.putString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION, mSoftwareVersion);
//		}

		if(ConnectSwitch.isRemoteType()){
			PreferencesUtils.putString(DTVApplication.getAppContext(), "HARDWARE_TYPE",mTvType);
//			mSoftwareVersion = getParamValue(ack,"softwareversion","\"","\"");
			if (!TextUtils.isEmpty(mSoftwareVersion)){
				PreferencesUtils.putString(DTVApplication.getAppContext(), Constants.SOFWARE_VERSION, mSoftwareVersion);
			}
		}else {
			PreferencesUtils.putString(DTVApplication.getAppContext(), "HARDWARE_TYPE_OTG",mTvType);
//			mSoftwareVersion = getParamValue(ack,"softwareversion","\"","\"");
			if (!TextUtils.isEmpty(mSoftwareVersion)){
				PreferencesUtils.putString(DTVApplication.getAppContext(),"SOFWARE_VERSION_OTG",mSoftwareVersion);
			}
		}

		if (!TextUtils.isEmpty(mTvType)){
			PreferencesUtils.putString(DTVApplication.getAppContext(),Constants.DEVICE_TYPE,mTvType);
		}

		return true ;
	}

	private String getSoftwareVersion() {
//		return PreferencesUtils.getString(DTVApplication.getAppContext(),Constants.SOFWARE_VERSION);
		if(ConnectSwitch.isRemoteType()){
			return PreferencesUtils.getString(DTVApplication.getAppContext(),Constants.SOFWARE_VERSION);
		}else {
			return PreferencesUtils.getString(DTVApplication.getAppContext(),"SOFWARE_VERSION_OTG");
		}
	}

	//Tv type DVB-T/T2 or ISDBT or DTMB or ATSC
	public String getTvType() {
		return mTvType ;
	}

    //logout form the device
	private boolean logout() {
		String req = "<msg type='logout_req'></msg>";
		req = req.replace('\'', '\"' );
//		String ack = exchangeMessage( req ,false);
//		int ret = getRetValue( ack ) ;
//		if( ret != 0 )
//			return false ;
        sendLogOutMessage(req);
		
		return true ;
	}

    private void sendLogOutMessage(String req){
        byte [] string = req.getBytes();
        try {
            if (mSocket != null)
                mSocket.getOutputStream().write( string );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void sleep( int ms) {
		try {
			Thread.sleep(ms);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

    /**
     * make the device lock the frequency point
     * @param freq unit KHz
     * @param bandwidth bandwidth
     * @param plp plp
     * @return lock or not
     */
	public boolean lockFreqPoint( int freq, int bandwidth, int plp,int serviceId) {
		String req = String.format(Locale.ENGLISH,"<msg type='tune_req'><params tv_type='%s' freq='%d' bandwidth='%d' plp='%d' programNumber='%d'/></msg>",
				mTvType, freq, bandwidth, plp,serviceId) ;
		req = req.replace('\'', '\"' );

		if( this.mDataProtocol.equalsIgnoreCase("udp") )
			mMediaLocation = "udp://@:8000" ;
		else
			mMediaLocation = String.format( "tcp://%s:8000", mServerIP ) ;

		if( !this.isServerAvaible()) {
			sleep(100);
			return false ;
		}
		
		String ack = exchangeMessage( req ,true);
		int ret = getRetValue( ack ) ;
		if( ret != 0 ){
			return false ;
		}

		// 如果是DVB-T2解释得到Plp个数
		if( mTvType.indexOf("T2") > 0 ) {
			String plpCount = getParamValue( ack, "plpCount", "\"", "\"" ) ;
			if( !plpCount.isEmpty() )
				mPLPCount = Integer.parseInt(plpCount) ;
			else				
				mPLPCount = 1 ;
		}
	
		return true ;
	}

	int getPLPCount() {
		if( mTvType.indexOf("T2") > 0 )
			return mPLPCount ;
		else
			return 1 ;
	}


    /**
     * make the DVB device to stop translate Ts stream
     * @return success or not
     */
	public boolean stopStream() {
		String req = "<msg type='stop_stream_req'></msg>";
		req = req.replace('\'', '\"' );
		sendLogOutMessage(req);
		
		return true ;
	}

    /**
     * get Tv signal strength
     * @return successs or not
     */
	public boolean getSignalStatus() {
		String req = "<msg type='signal_status_req'></msg>";
		req = req.replace('\'', '\"' );
		String ack = exchangeMessage( req ,false);
		int ret = getRetValue( ack ) ;
		if( ret != 0 )
			return false ;
		
		String signalStrength = getParamValue( ack, "strength", "\"", "\"" ) ;
		if( !TextUtils.isEmpty(signalStrength))
			mSignalStrength = Integer.parseInt(signalStrength) ;

		String quality = getParamValue(ack,"quality","\"","\"");
		if (!TextUtils.isEmpty(quality))
			mQuality = Integer.parseInt(quality);

        String qam = getParamValue(ack,"qam","\"","\"");
        if (!TextUtils.isEmpty(qam))
            mQam = Integer.parseInt(qam);
		return true ;
	}

    public int getSignalStrength() {
		return mSignalStrength ;
	}

	public int getQuality(){
		return mQuality;
	}

    public String getQam() {
        String modulationType;
        switch (mQam){
            case 0:
                modulationType = "unknow";
                break;
            case 1:
                modulationType = "4QAM_NR";
                break;
            case 2:
                modulationType = "4QAM";
                break;
            case 3:
                modulationType = "16QAM";
                break;
            case 4:
                modulationType = "32QAM";
                break;
            case 5:
                modulationType = "64QAM";
                break;
            default:
                modulationType = "unknow";
                break;
        }
        return modulationType;
    }

    /**
     * send the stream's pid list which you need to the DVB device,and the device will shiled other
     * @param strPids the pid list you need
     * @return success or not
     */
	public boolean setPidFilter(String strPids) {
		String req = String.format(Locale.ENGLISH,"<msg type='Pid_Filter_req'><params pid_list='%s'/></msg>", strPids ) ;
		req = req.replace('\'', '\"' );
		String ack = exchangeMessage( req ,false);
		int ret = getRetValue( ack ) ;
		return ret == 0;

	}

	/**
	 * check if device lockked the some frequency point
	 * @return success or not
	 */
	public int isSignalLockAndStreamServerAlive(){
		String req = "<msg type='Check_Lock_req'></msg>";
		req = req.replace('\'', '\"' );
		String ack = exchangeMessage( req ,false);
		return getRetValue( ack ) ;
	}

	private boolean isServerAvaible() {
		if( mSocket == null ){
            isFirstLogin = true;
            return false ;
        }

		return true ;
	}

	public boolean isServerAlive(){
		String req = "isalive";
		byte [] string = req.getBytes();
		try {
			if (mSocket != null)
				mSocket.getOutputStream().write( string );
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    public boolean isConnectedToDevice(){
        if (!isConnectedDevice || mSocket == null || !mSocket.isConnected())
            isFirstLogin = true;
        return isConnectedDevice;
    }

	public static boolean isConnectToDevice(String wifiName) {
		if(!TextUtils.isEmpty(wifiName)){
			if (wifiName.contains("MobileTV_") || wifiName.contains("Mobile_TV_box")
					|| wifiName.contains("@Tevemo_"))
				return true;
		}
		return false;
	}

	public String getMediaLocation() {
		return	mMediaLocation ;
	}

	private final class MyThread extends Thread{

		private WeakReference<Activity> mActivity;

		MyThread(Activity activity) {
			this.mActivity = new WeakReference<>(activity);
		}

		@Override
		public void run() {
			while( !mStopped || !mThreadHeartBeat.isInterrupted())
			{
				Activity activity = mActivity.get();
				if (activity == null)
					return;
				try {
						if( mSocket == null || !mSocket.isConnected() ) {// 未连接则主动连接
                            if(isFirstLogin)
							    mHandler.sendEmptyMessage(2); // 正在连接...
							mHandler.sendEmptyMessage(4);
							boolean bOK = connectToServer();
//							LogUtil.i("EardatekVersion2","connectToServer" + bOK + "");
							if( !bOK ) {
                                if (isFirstLogin)
								    mHandler.sendEmptyMessage(1); // 连接失败
                                isFirstLogin = false;
                                isConnectedDevice = false;
							}
							else {
                                bOK = login();
//								LogUtil.i("EardatekVersion2","login" + bOK + "");
								if( !bOK ) {
									if (mSocket != null)
										mSocket.close();
									mSocket = null ;
                                    if (isFirstLogin)
							    		mHandler.sendEmptyMessage(1); // // 连接失败
                                    isFirstLogin = false;
                                    isConnectedDevice = false;
								}
								else {
									mHandler.sendEmptyMessage(0);// 成功
                                    isConnectedDevice = true;
                                    isFirstLogin = false;
                                }
							}
						}
						else {
//							LogUtil.i("EardatekVersion2","sofe ware version:" + Float.parseFloat(getSoftwareVersion()));
							if (Float.parseFloat(getSoftwareVersion()) > 2.3f){
									try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									e.printStackTrace();
//									return;
								}
							}else {
								Socket hearBeatSocket = new Socket();
								SocketAddress sa = new InetSocketAddress(mServerIP, mServerPort);
								hearBeatSocket.connect(sa, 3000) ;
								hearBeatSocket.setSoTimeout(5000);
								InputStream is = hearBeatSocket.getInputStream() ;
								OutputStream os = hearBeatSocket.getOutputStream();
								byte[] buffer = new byte[1024];
								while( !mStopped && !mThreadHeartBeat.isInterrupted() && hearBeatSocket.isConnected()
										&& mSocket != null && mSocket.isConnected() ) {
									try{
										int len = is.read(buffer);
										if (len < 0){
											len = 0;
//										LogUtil.i("EardatekVersion2","receive the heartbeat fail!");
											mSocket.close();
											mSocket = null;
											mHandler.sendEmptyMessage(4);
										}
										String isFail = new String(buffer,0,len);
										if (isFail.contains("send data fail"))
											mHandler.sendEmptyMessage(3);
										os.write(buffer) ;
									}catch (IOException e){
										e.printStackTrace();
									}

								}
								hearBeatSocket.close() ;
								isConnectedDevice = false;
							}
						}
				}
				catch (SocketException e1){
                    e1.printStackTrace();
					if (mSocket == null || !mSocket.isConnected()){
						isConnectedDevice = false;
						mHandler.sendEmptyMessage(4);
						try {
							if (mSocket != null){
								mSocket.close();
								mSocket = null;
							}
						} catch (IOException e2) {
							e1.printStackTrace();
						}
                    }
                }
				catch( IOException e) {
					e.printStackTrace() ;
                    LogUtil.i("EardatekVersion2","lose connect to server!");
					if(!mSocket.isConnected() || mSocket == null){
						isConnectedDevice = false;
						try {
							if (mSocket != null){
								mSocket.close();
								mSocket = null;
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}

			if( mSocket != null )
				logout();
			try {
				if( mSocket != null )
					mSocket.close();
				mSocket = null ;
			}
			catch( IOException e) {
				e.printStackTrace();
			}
		}
	}

	private final class HomePageThread extends Thread{

		private void start_login(){
			boolean bOK = login();
			if( !bOK ) {
				if (mSocket != null)
					try {
						mSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				mSocket = null ;
				isFirstLogin = false;
				isConnectedDevice = false;
			}
			else {
				isConnectedDevice = true;
				isFirstLogin = false;
			}
		}

		@Override
		public void run() {
			boolean bOK;
			if( mSocket == null || !mSocket.isConnected() ) {// 未连接则主动连接
				bOK = connectToServer();
				if( !bOK ) {
					isFirstLogin = false;
					isConnectedDevice = false;
				}
				else {
					start_login();
				}
			}else {
				start_login();
			}
		}
	}
}