package com.eardatek.special.player.util;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.blazevideo.libdtv.LibDTV;
import com.blazevideo.libdtv.LibDtvException;
import com.eardatek.special.player.R;
import com.eardatek.special.player.actitivy.ScanChannelActivity;
import com.eardatek.special.player.bean.ChannelInfo;
import com.eardatek.special.player.bean.EpgItem;
import com.eardatek.special.player.impl.ChannelInfoDaoImpl;
import com.eardatek.special.player.system.DTVApplication;
import com.eardatek.special.player.system.DTVInstance;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChannelScanner 
{
	private Handler mHandler = null ;
	private NetTunerCtrl mTuner = null;
	private boolean mAborted = false ;
	private ScanChannelActivity mActivity;
	private Thread mScanThread;
	private LibDTV mLibDTV;


	public ChannelScanner(Handler handler,ScanChannelActivity activity) {
		mHandler = handler ;
        mActivity = activity;
		mTuner = NetTunerCtrl.getInstance();
	}

    /**
     * scan thread
     */
	private class ScanThread implements Runnable{
		private List<ChannelInfo> mFoundChannelList = new ArrayList<>();
		private WeakReference<ScanChannelActivity> mActivity;

		ScanThread(ScanChannelActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		/**
		 * save the program information to the database
		 * @param channelList channel list
		 */
		private void saveChannelList( List<ChannelInfo> channelList) {
			if (channelList == null || channelList.size() == 0)
				return;

			ChannelInfoDaoImpl channelInfoDao = new ChannelInfoDaoImpl(DTVApplication.getAppContext());
			for( int i = 0 ; i < channelList.size(); i++){
				try {
					channelInfoDao.updateOrInsertChannel(channelList.get(i));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				channelInfoDao.closeRealm();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			ScanChannelActivity activity = mActivity.get();
			if (activity == null)
				return;

			try {
				mLibDTV = DTVInstance.getLibDtvInstance();
			} catch (LibDtvException e) {
				return;
			}

			String opt[]={":no-video",":no-audio"};

			String tvType = mTuner.getTvType() ;
			int[] mFreqList;
			if (activity.getFreq() > 0){
				mAdvance_SearchList[0] = activity.getFreq();
				mFreqList = mAdvance_SearchList;
			}else {
				if( tvType.indexOf("DVB") > 0 || tvType.indexOf("DMB") > 0 )// Europe standard and chines standard
					mFreqList = mDvbtFreqList ;
				else if( tvType.indexOf("ISDB") > 0 )
					mFreqList = mIsdbFreqList ;
				else if(tvType.contains("ATSC"))
					mFreqList = mATSCFreqList;
				else
					mFreqList = mDvbtFreqList ;
			}

			int count = mFreqList.length ;
			int bandWidth = 8000 ; // DVB-T: most of country 8000,except Australia(7000),Taiwan is 6000,south America use isdbt standdard

			int found = 0;
			for( int fp = 0; fp < count && !mAborted && !mScanThread.isInterrupted(); fp++ ) {
				int freq = mFreqList[fp];

				Message msg = mHandler.obtainMessage(10);
				msg.what = ScanChannelActivity.SCAN_CHANNEL_PER_TP ;
				msg.arg1 = freq ;
				int percent = fp * 100/count;
				msg.arg2 = percent*10000 + found ;// 百分比和发现频道数共用一个变量
				mHandler.sendMessage(msg);
				for( int plp = 0 ;!mAborted && !mScanThread.isInterrupted(); plp++ ) {
					boolean bLocked = mTuner.lockFreqPoint( freq,  bandWidth,  plp ,0);
					if( !bLocked )
						break ;

					String loc = NetTunerCtrl.getInstance().getMediaLocation();
					mLibDTV.playMRL( loc, opt);

					receiveChannelInfo( freq, bandWidth, plp ) ;

					mTuner.stopStream();

					mLibDTV.stop();

					int plpCount = mTuner.getPLPCount() ;
					if( plp+1 >= plpCount )
						break ;
				}
				saveChannelList(mFoundChannelList);
				found = found + mFoundChannelList.size();
				mFoundChannelList.clear();
			}
			mLibDTV.stop();

			Message msg = mHandler.obtainMessage(ScanChannelActivity.SCAN_CHANNEL_FINISHED);
			msg.arg1 = found;
			mHandler.sendMessage(msg);

			msg = mHandler.obtainMessage(ScanChannelActivity.SCAN_CHANNEL_CLOSED);
			msg.arg1 = 0 ;
			mHandler.sendMessageDelayed(msg, 1000);
		}

        /**
         * get program list
         * @param freq frequency
         * @param bandWidth bandwidth
         * @param plp plpcount
         * @return ret
         */
		int receiveChannelInfo(int freq, int bandWidth, int plp) {
			for( int times = 0 ; times < 15 && !mAborted; times++ ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return 0;
                }
				if (!mLibDTV.isPlaying())
					return 0;
                String xml = mLibDTV.getProgramList();
				String pidList = mLibDTV.getPidList();
				if( !xml.isEmpty() && !pidList.isEmpty() ) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return 0;
                    }
					String xml2 = mLibDTV.getProgramList();
					String pidList2 = mLibDTV.getPidList();
					if( xml.equals(xml2) && pidList.equals(pidList2) )
					{
						LogUtil.i("ScanChannelActivity","pidlist:" + pidList);
						List<ChannelInfo> list = parseProgramList(xml, pidList, freq, bandWidth, plp ) ;
						if( list.size() > 0 && times < 14 )
						{
							String sTitle = list.get(0).getTitle() ;
							if( sTitle.length() > 8 && sTitle.substring(0, 8 ).equals( "service-") )
								continue ;
						}

						mFoundChannelList.addAll(list);
						break ;
					}
				}
			}

			return 0  ;
		}
	}
	
	public boolean  startScan( ) {
		mScanThread = new Thread(new ScanThread(mActivity));
		mScanThread.start();
		return true ;
	}

    /**
     * stop scan
     * @return true
     */
	public boolean stopScan() {
		mAborted = true ;
		mScanThread.interrupt();
		mTuner.abortOperation();
		if (mLibDTV != null && mLibDTV.isPlaying())
			mLibDTV.stop();
		return true ;
	}

	private List<EpgItem> getEpgInfo(String servicename){

		return EpgUtil.loadEpg(servicename);
    }

	private ProgramInfo getProgramInfo(int serviceID,String pidList){
		String prgPidList[] = pidList.split(";");
		for (String aPrgPidList : prgPidList) {
			String prgParams[] = aPrgPidList.split(":");
			if (prgParams.length >= 3){
				if ((Integer.parseInt(prgParams[0]) == serviceID)){
					String typeParams[] = prgParams[3].split(",");
					int videoType = Integer.parseInt(typeParams[0].substring(10));
					boolean isVideo = prgParams[1].equals("3");
					boolean hasCa = typeParams[2].equals("hasCA=1");

					return new ProgramInfo(isVideo,videoType,hasCa);
				}
			}
		}
		return new ProgramInfo(false,0,false);
	}

	/**
     * check the program is video or radio
     * @param serviceID program number
     * @param pidList pid list
     * @return 0 is video
     */
	private int isVideoService( int serviceID, String pidList ) {
		String prgPidList[] = pidList.split(";");
		for (String aPrgPidList : prgPidList) {
			String prgParams[] = aPrgPidList.split(":");
			int VIDEO = 0;
			if ((Integer.parseInt(prgParams[0]) == serviceID) &&
					(prgParams[1].equals("3")))
				return VIDEO;
		}

		return 1;
	}

	private int getVideoType(int serviceID,String pidList){
		String prgPidList[] = pidList.split(";");
		for (String aPrgPidList : prgPidList) {
			String prgParams[] = aPrgPidList.split(":");
			String typeParams[] = prgParams[3].split(",");
			int videoType = Integer.parseInt(typeParams[0].substring(10));
			if ((Integer.parseInt(prgParams[0]) == serviceID))
				return videoType;
		}

		return 0 ;
	}

	private boolean isEncrypt(int serviceID,String pidList){
		String prgPidList[] = pidList.split(";");
		for(String aPrgPidList : prgPidList){
			String prgParams[] = aPrgPidList.split(":");
			String typeParams[] = prgParams[3].split(",");
			String hasCa = typeParams[2];
			if ((Integer.parseInt(prgParams[0]) == serviceID)
					&& (hasCa.equals("hasCA=1"))){
				LogUtil.i("ScanChannelActivity","hasCa:" + hasCa);
				return true;
			}
		}
		return false;
	}

    /**
     * parse the program information
     * @param xml xml
     * @param pidList pid list
     * @param freq frequency
     * @param bw bandwidth
     * @param plp PLP
     * @return program list
     */
	private List<ChannelInfo> parseProgramList(String xml, String pidList, int freq, int bw, int plp) {
		List<ChannelInfo> mediaList = new ArrayList<>();
		
		String prgs[] = xml.split(";") ;
		int i = 0;
		for (String prg : prgs) {
			int serviceID = 0;
			String serviceName = "";

			String params[] = prg.split(":");
			if (params.length > 0)
				serviceID = Integer.parseInt(params[0]);

			ProgramInfo programInfo = getProgramInfo(serviceID,pidList);
			LogUtil.i("ScanChannelActivity","videoType:" + programInfo.videoType);
			if(!programInfo.isVideo)
				continue;

			int isEnCrypt = programInfo.isEncrypt ? 1 : 0;

			if (params.length > 1) {
				String nameParams[] = params[1].split("\\[");
				if (nameParams.length > 0)
					serviceName = nameParams[0];

//				if (nameParams.length > 1) {
//					providerName = nameParams[1];
//					if (providerName.length() > 1)
//						providerName = providerName.substring(0, providerName.length() - 1);
//				}
			}

//			getEpgInfo(String.format(Locale.ENGLISH, "%s [Program %d]", serviceName.trim(), serviceID));

			String location = String.format(Locale.ENGLISH, "freq%d-bw%d-plp%d-prog%d-isradio%d-isEncrypt%d-videoType%d",
					freq, bw, plp, serviceID, 0,isEnCrypt,programInfo.videoType);

			if (serviceName.isEmpty())
				serviceName = String.format(Locale.ENGLISH, "Program %d", serviceID);

			ChannelInfo media = new ChannelInfo(location, serviceName);
			mediaList.add(media);
			i++;
		}
		
		return mediaList ;
	}

	private static class ProgramInfo{
		boolean isVideo;
		int videoType;
		boolean isEncrypt;

		ProgramInfo(boolean isVideo, int videoType, boolean isEncrypt) {
			this.isVideo = isVideo;
			this.videoType = videoType;
			this.isEncrypt = isEncrypt;
		}
	}

	public static boolean isValidFreq(String tvType,int freq){
		if ((tvType.contains("DVB") || tvType.contains("DTMB")) &&
				(freq > 858 || freq < 100) ){
			Toast.makeText(DTVApplication.getAppContext(), R.string.freqtips, Toast.LENGTH_SHORT).show();
			return false;
		}else if((tvType.contains("ATSC")) && (freq > 887 || freq < 57)){
			Toast.makeText(DTVApplication.getAppContext(), R.string.freqtips, Toast.LENGTH_SHORT).show();
			return false;
		}else if (tvType.contains("ISDB") && (freq > 827 || freq < 93)){
			Toast.makeText(DTVApplication.getAppContext(), R.string.freqtips, Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

    private int mAdvance_SearchList[] = {0};

	private int mDvbtFreqList[] = { 
			474000,
			482000,
			490000,
			498000,
			506000,
			514000,
			522000,
			530000,
			538000,
			546000,
			554000,
			562000,
			570000,
			578000,
			586000,
			594000,
			594000,
			602000,
			610000,
			618000,
			626000,
			634000,
			642000,
			650000,
			658000,
			666000,
			674000,
			682000,
			690000,
			698000,
			706000,
			714000,
			722000,
			730000,
			738000,
			746000,
			754000,
			762000,
			770000,
			778000,
			786000,
			794000,
			802000,
			810000,
			818000,
			826000,
			834000,
			842000,
			850000,
			858000
		};	

	private int mATSCFreqList[] = {57000,63000,69000,79000,85000,
			177000,183000,189000,195000,201000,207000,213000,
			473000,479000,485000,491000,497000,
			503000,509000,515000,521000,527000,533000,539000,545000,551000,557000,563000,
			569000,575000,581000,587000,593000,599000,
			605000,611000,617000,623000,629000,635000,641000,647000,653000,659000,
			665000,671000,677000,683000,689000,695000,
			701000,707000,713000,719000,725000,731000,737000,743000,
			749000,755000,761000,767000,773000,779000,785000,791000,797000,
			803000,809000,815000,821000,827000,833000,839000,845000,851000,
			857000,863000,869000,875000,881000,887000};

	private int mIsdbFreqList[] = 	{
			93000,99000,
			105000,111000,117000,123000,129000,135000,141000,147000,153000,159000,
			167000,173000,179000,185000,191000, 195000,
			201000,207000,213000,219000,225000,231000,237000,243000,249000,255000,261000,
			267000,273000,279000,285000,291000,297000,303000,309000,315000,321000,327000,333000,
			339000,345000,351000,357000,363000,369000,375000,381000,387000,393000,399000,
			405000, 411000,417000,423000,429000,435000,441000,447000,453000,459000,465000,
			473000, 479000, 485000, 491000, 497000,
			503000, 509000, 515000, 521000, 527000, 533000, 539000, 545000,
			551000, 557000, 563000, 569000, 575000, 581000, 587000, 593000, 599000,
			605000, 611000, 617000, 623000, 629000, 635000, 641000, 647000, 653000, 659000, 665000,
			671000, 677000, 683000, 689000, 695000,
			701000, 707000, 713000, 719000, 725000, 731000, 737000, 743000, 749000, 755000,
			761000, 767000, 773000, 779000, 785000, 791000, 797000,
			803000, 809000, 815000, 821000, 827000
		} ;
}
