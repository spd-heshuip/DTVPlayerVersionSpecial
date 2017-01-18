package com.eardatek.special.player.bean;

import android.text.TextUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ChannelInfo extends RealmObject{
    public final static String TAG = "DTV/LibDTV/Media";

    @Required
    private String mTitle;

    @PrimaryKey
    private String mLocation;

    private boolean isEncrypt;

    private String videoType;

    public ChannelInfo() {

    }

    public ChannelInfo(String location, String title) {
        mLocation = location;
        mTitle = title;
        isEncrypt = isEncrypt();
        videoType = prarseVideoType();
    }


    public String getTitle() {
        return mTitle;
    }
    public String getLocation() {
        return mLocation;
    }
    public boolean isProgramEncrypt(){
        return isEncrypt;
    }
    public String getVideoType(){
        return prarseVideoType();
    }


    private boolean isEncrypt(){
        if (!TextUtils.isEmpty(mLocation)){
            String params[] = mLocation.split("-");
            int isEncrypt = Integer.parseInt(params[5].substring(9));
            if (isEncrypt == 1)
                return true;
        }
        return false;
    }

    private String prarseVideoType(){
        if (!TextUtils.isEmpty(mLocation)){
            String params[] = mLocation.split("-");
            int type = Integer.parseInt(params[6].substring(9));
            switch (type){
                case 1:
                    return "MPEG-1 Video";
                case 2:
                    return "MPEG-2 Video";
                case 27:
                    return "H264";
                case 36:
                    return "H265";
                case 66:
                    return "AVS";
                default:
                    return "Unknow";
            }
        }

        return "Unknow";
    }


    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }


    public void setmLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    @Override
    public String toString() {
        return "ChannelInfo:" + mTitle +  "\n" + mLocation;
    }
}
