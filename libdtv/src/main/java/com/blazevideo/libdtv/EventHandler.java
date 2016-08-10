package com.blazevideo.libdtv;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

public class EventHandler {

    public static final int MediaMetaChanged                  = 0;
    public static final int MediaParsedChanged                = 3;
    public static final int MediaPlayerPlaying                = 0x104;
    public static final int MediaPlayerPaused                 = 0x105;
    public static final int MediaPlayerStopped                = 0x106;
    public static final int MediaPlayerEndReached             = 0x109;
    public static final int MediaPlayerEncounteredError       = 0x10a;
    public static final int MediaPlayerTimeChanged            = 0x10b;
    public static final int MediaPlayerPositionChanged        = 0x10c;
    public static final int MediaPlayerVout                   = 0x112;
    public static final int CustomMediaListExpanding          = 0x2000;
    public static final int CustomMediaListExpandingEnd       = 0x2001;
    public static final int CustomMediaListItemAdded          = 0x2002;
    public static final int CustomMediaListItemDeleted        = 0x2003;
    public static final int CustomMediaListItemMoved          = 0x2004;

    public static final int HardwareAccelerationError         = 0x3000;

    private ArrayList<Handler> mEventHandler;
    private static EventHandler mInstance;

    EventHandler() {
        mEventHandler = new ArrayList<Handler>();
    }

    public static EventHandler getInstance() {
        if (mInstance == null) {
            mInstance = new EventHandler();
        }
        return mInstance;
    }

    public void addHandler(Handler handler) {
        if (!mEventHandler.contains(handler))
            mEventHandler.add(handler);
    }

    public void removeHandler(Handler handler) {
        mEventHandler.remove(handler);
    }

    public void callback(int event, Bundle b) {
        b.putInt("event", event);
        for (int i = 0; i < mEventHandler.size(); i++) {
            Message msg = Message.obtain();
            msg.setData(b);
            mEventHandler.get(i).sendMessage(msg);
        }
    }
}
