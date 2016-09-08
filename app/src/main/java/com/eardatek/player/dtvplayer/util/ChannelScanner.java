package com.eardatek.player.dtvplayer.util;

import android.os.Handler;
import android.os.Message;

import com.blazevideo.libdtv.ChannelInfo;
import com.blazevideo.libdtv.LibDTV;
import com.blazevideo.libdtv.LibDtvException;
import com.eardatek.player.dtvplayer.actitivy.ScanChannelActivity;
import com.eardatek.player.dtvplayer.bean.EpgItem;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.system.DTVInstance;

import java.lang.ref.WeakReference;
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

		public ScanThread(ScanChannelActivity activity) {
			mActivity = new WeakReference<>(activity);
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
			if (mLibDTV.isPlaying())
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
		public int receiveChannelInfo( int freq, int bandWidth, int plp ) {
			for( int times = 0 ; times < 15 && !mAborted; times++ ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return 0;
                }
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
		return true ;
	}

	private List<EpgItem> getEpgInfo(String servicename){
        List<EpgItem> mList = EpgUtil.loadEpg(servicename);

        return mList;
    }

	private static int RADIO = 1;

    /**
     * check the program is video or radio
     * @param serviceID
     * @param pidList
     * @return
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
		
		return RADIO ;
	}

    /**
     * parse the program information
     * @param xml
     * @param pidList
     * @param freq
     * @param bw
     * @param plp
     * @return
     */
	private List<ChannelInfo> parseProgramList(String xml, String pidList, int freq, int bw, int plp) {
		List<ChannelInfo> mediaList = new ArrayList<>();
		
		String prgs[] = xml.split(";") ;
		for (String prg : prgs) {
			int serviceID = 0;
			String serviceName = "";
			String providerName = "";
			int isRadio = 0;//no radio

			String params[] = prg.split(":");
			if (params.length > 0)
				serviceID = Integer.parseInt(params[0]);

			if (isVideoService(serviceID, pidList) == RADIO) {
				isRadio = 1;//radio
			} else
				isRadio = 0;

			if (params.length > 1) {
				String nameParams[] = params[1].split("\\[");
				if (nameParams.length > 0)
					serviceName = nameParams[0];

				if (nameParams.length > 1) {
					providerName = nameParams[1];
					if (providerName.length() > 1)
						providerName = providerName.substring(0, providerName.length() - 1);
				}
			}

			getEpgInfo(String.format(Locale.ENGLISH, "%s [Program %d]", serviceName.trim(), serviceID));

			String location = String.format(Locale.ENGLISH, "freq%d-bw%d-plp%d-prog%d-isradio%d", freq, bw, plp, serviceID, isRadio);

			if (serviceName.isEmpty())
				serviceName = String.format(Locale.ENGLISH, "service-%d-%d", freq, serviceID);

			ChannelInfo media = new ChannelInfo(location, serviceName);
			mediaList.add(media);
		}
		
		return mediaList ;
	}

    /**
     * save the program information to the database
     * @param channelList
     */
	private void saveChannelList( List<ChannelInfo> channelList) {
        if (channelList == null || channelList.size() == 0)
            return;
		ChannelInfoDB db = ChannelInfoDB.getInstance();
		
//		db.emptyDatabase();
		for( int i = 0 ; i < channelList.size(); i++)
			db.addChannelInfo(channelList.get(i));
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

	private int mIsdbFreqList[] = 	{
			473143,
			479143,
			485143,
			491143,
			497143,
			503143,
			509143,
			515143,
			521143,
			527143,
			533143,
			539143,
			545143,
			551143,
			557143,
			563143,
			569143,
			575143,
			581143,
			587143,
			593143,
			599143,
			605143,
			611143,
			617143,
			623143,
			629143,
			635143,
			641143,
			647143,
			653143,
			659143,
			665143,
			671143,
			677143,
			683143,
			689143,
			695143,
			701143,
			707143,
			713143,
			719143,
			725143,
			731143,
			737143,
			743143,
			749143,
			755143,
			761143,
			767143,
			773143,
			779143,
			785143,
			791143,
			797143,
			803143,
			809143,
			815143,
			821143,
			827143
		} ;
}
