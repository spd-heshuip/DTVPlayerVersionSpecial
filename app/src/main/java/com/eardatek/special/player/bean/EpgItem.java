package com.eardatek.special.player.bean;

/**
 * 作者：Create By Administrator on 16-3-22 in com.eardatek.player.dtvplayer.bean.
 * 邮箱：spd_heshuip@163.com;
 */
public class EpgItem {

    public int 		mType ;
    public String 	mText ;
    public String	mTime ;
    public String   mDuration ;

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    @Override
    public String toString() {
        return "Type:" + mType + "\n" + "Text:" +  mText +  "\n"
        + "Time:" + mTime + "\n" + "Duration:" + mDuration;
    }
}
