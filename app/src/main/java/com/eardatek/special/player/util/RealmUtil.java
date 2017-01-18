package com.eardatek.special.player.util;

import android.content.Context;

import io.realm.Realm;

/**
 * Created by Administrator on 16-9-13.
 */
public class RealmUtil {

    private Context context;
    private static RealmUtil mInstance;
    private String realmName = "channelInfo.realm";

    private RealmUtil(Context context){
        this.context = context;
    }

    public static RealmUtil getInstance(Context context){
        if (mInstance == null){
            synchronized (RealmUtil.class){
                if (mInstance == null){
                    mInstance = new RealmUtil(context);
                }
            }
        }

        return mInstance;
    }

    public Realm getRealm(){
        return Realm.getDefaultInstance();
    }
}
