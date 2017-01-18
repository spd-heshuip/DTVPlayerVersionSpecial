package com.eardatek.special.player.update;

import android.os.Handler;
import android.os.Message;

/**
 * Created by tomato on 2016/8/19.
 */
public class MessageHelper {
    public static void sendMsg(Handler handler,int what){
        Message msg = handler.obtainMessage(what);
        msg.sendToTarget();
    }
    public static void sendMsg(Handler handler,int what, Object obj){
        Message msg = handler.obtainMessage(what,obj);
        msg.sendToTarget();
    }
    public static void sendMsg(Handler handler,int what, int arg1, int arg2){
        Message msg = handler.obtainMessage(what,arg1, arg2);
        msg.sendToTarget();
    }
    public static void sendMsg(Handler handler,int what, int arg1, int arg2 ,Object obj){
        Message msg = handler.obtainMessage(what, arg1, arg2, obj);
        msg.sendToTarget();
    }
}
