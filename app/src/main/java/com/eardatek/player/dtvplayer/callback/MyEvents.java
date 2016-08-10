package com.eardatek.player.dtvplayer.callback;


import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 16-4-8.
 */
public class MyEvents {

    public static final int RADIO_PLAY = 1;
    public static final int DATA_BASE_EMPTY = 2;
    public static final int UPDATE_EPG_FRAGMENT = 3;
    public static final int VIDEO_PLAY = 4;
    public static final int CHANGE_PROGRAM = 5;
    public static final int FIRST_PLAY = 6;
    public static final int SCAN_CHANNEL = 7;
    public static final int DELETE_PLAYING_ITEM = 8;
    public static final int CHANGE_LANDSCAPE_LIST_POSITON = 9;
    public static final int STOP_SCAN_HALFWAY = 10;

    private int EventType;
    private Object data;

    public int getEventType() {
        return EventType;
    }

    public void setEventType(int eventType) {
        EventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static void postEvent(int eventType,Object data){
        MyEvents events = new MyEvents();
        events.setData(data);
        events.setEventType(eventType);
        EventBus.getDefault().post(events);
    }
}
