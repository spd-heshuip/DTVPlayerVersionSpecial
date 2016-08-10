package com.eardatek.player.dtvplayer.util;

import android.util.Log;

import com.blazevideo.libdtv.LibDTV;
import com.blazevideo.libdtv.LibDtvException;
import com.eardatek.player.dtvplayer.bean.EpgItem;
import com.eardatek.player.dtvplayer.system.DTVInstance;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 作者：Create By Administrator on 16-3-22 in com.eardatek.player.dtvplayer.util.
 * 邮箱：spd_heshuip@163.com;
 */
public class EpgUtil {

    private static final String TAG = EpgUtil.class.getSimpleName();

    /**
     * get Epg information
     * @param serviceName Program's name
     */
    public static List<EpgItem> loadEpg(String serviceName){
        List<EpgItem> mEpgList = new ArrayList<>();
        LibDTV mLibDTV;

        try {
            mLibDTV = DTVInstance.getLibDtvInstance();
        } catch (LibDtvException e) {
            Log.d(TAG, "LibDTV initialisation failed");
            return null;
        }
        if (!mLibDTV.isPlaying())
            return null;
        String epg = mLibDTV.getEpgList( serviceName );
        if( epg == null || epg.isEmpty() )
            return null;

        String [] epgList = epg.split("\\|");

        String groupDate = "" ;
        for( int i = 0 ; i < epgList.length && mLibDTV.isPlaying(); i++ )
        {
            int len = epgList[i].length();
            if( len < 23 )
                continue ;

            String date = epgList[i].substring(1,1+10) ;
            EpgItem item = new EpgItem() ;
            if( date.compareTo(groupDate) != 0 ) {
                item.mType 	= 0 ;
                item.mText	= date ;
                groupDate = date ;
                mEpgList.add(item);
            }

            item = new EpgItem() ;
            item.mType 	= 1 ;
            item.mText	= epgList[i].substring(21, len-7-1 );
            item.mTime	= epgList[i].substring(12, 12+8);
            item.mDuration	= epgList[i].substring( len-6, len-6+5 );
            mEpgList.add(item) ;
        }

        return mEpgList;
    }
}
