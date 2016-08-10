package com.blazevideo.libdtv;

import android.view.Surface;

public interface IVideoPlayer {
    void setSurfaceLayout(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den);
    int configureSurface(Surface surface, int width, int height, int hal);
    public void eventHardwareAccelerationError();
}
