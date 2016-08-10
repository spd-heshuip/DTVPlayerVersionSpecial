package com.eardatek.player.dtvplayer.system;

import android.content.Context;

import com.blazevideo.libdtv.LibDTV;
import com.blazevideo.libdtv.LibDtvException;

public class DTVInstance {
    public final static String TAG = "DTV/DTVInstance";
    
    public static LibDTV getLibDtvInstance() throws LibDtvException {
        LibDTV instance = LibDTV.getExistingInstance();
        if (instance == null) {
            instance = LibDTV.getInstance();
            final Context context = DTVApplication.getAppContext();
            instance.init(context);
        }
        return instance;
    }
}
