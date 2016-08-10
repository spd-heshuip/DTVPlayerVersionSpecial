package com.blazevideo.libdtv;

public class ChannelInfo {
    public final static String TAG = "DTV/LibDTV/Media";

    private String mTitle;
    private final String mLocation;

    public ChannelInfo(String location, String title) {
        mLocation = location;
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }
    public String getLocation() {
        return mLocation;
    }

}
