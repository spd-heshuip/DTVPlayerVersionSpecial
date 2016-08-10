package com.blazevideo.libdtv;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class LibDTV {
    private static final String TAG = "DTV/LibDTV";
    public static final int AOUT_AUDIOTRACK_JAVA = 0;
    public static final int AOUT_AUDIOTRACK = 1;
    public static final int AOUT_OPENSLES = 2;

    public static final int VOUT_ANDROID_SURFACE = 0;
    public static final int VOUT_OPEGLES2 = 1;
    public static final int VOUT_ANDROID_WINDOW = 2;

    public static final int HW_ACCELERATION_AUTOMATIC = -1;
    public static final int HW_ACCELERATION_DISABLED = 0;
    public static final int HW_ACCELERATION_DECODING = 1;
    public static final int HW_ACCELERATION_FULL = 2;

    public static final int DEV_HW_DECODER_AUTOMATIC = -1;
    public static final int DEV_HW_DECODER_OMX = 0;
    public static final int DEV_HW_DECODER_OMX_DR = 1;
    public static final int DEV_HW_DECODER_MEDIACODEC = 2;
    public static final int DEV_HW_DECODER_MEDIACODEC_DR = 3;

    public static final int INPUT_NAV_ACTIVATE = 0;
    public static final int INPUT_NAV_UP = 1;
    public static final int INPUT_NAV_DOWN = 2;
    public static final int INPUT_NAV_LEFT = 3;
    public static final int INPUT_NAV_RIGHT = 4;

    private static final String DEFAULT_CODEC_LIST = "mediacodec,iomx,all";
    private static final boolean HAS_WINDOW_VOUT = true ;

    private static LibDTV sInstance;

    private long mLibDtvInstance = 0; // Read-only, reserved for JNI
    private int mInternalMediaPlayerIndex = 0; // Read-only, reserved for JNI
    private long mInternalMediaPlayerInstance = 0; // Read-only, reserved for JNI

    private StringBuffer mDebugLogBuffer;
    private boolean mIsBufferingLog = false;

    //private WakeLock mWakeLock;

    private int hardwareAcceleration = HW_ACCELERATION_AUTOMATIC;
    private int devHardwareDecoder = DEV_HW_DECODER_AUTOMATIC;
    private String codecList = DEFAULT_CODEC_LIST;
    private String devCodecList = null;
    private String subtitlesEncoding = "";
    private int aout = AOUT_OPENSLES ;
    private int vout = VOUT_ANDROID_SURFACE;
    private boolean timeStretching = false;
    private int deblocking = -1;
    private String chroma = "";
    private boolean verboseMode = true;
    private float[] equalizer = null;
    private boolean frameSkip = false;
    private int networkCaching = 0;
    private boolean httpReconnect = false;

    private String mCachePath = "";

    private OnNativeCrashListener mOnNativeCrashListener;

    private boolean mIsInitialized = false;
    public native void attachSurface(Surface surface, IVideoPlayer player);

    public native void detachSurface();

    public native void attachSubtitlesSurface(Surface surface);
    public native void detachSubtitlesSurface();

    public native void eventVideoPlayerActivityCreated(boolean created);

    static {
        try {
            System.loadLibrary("dtvjni");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Can't load dtvjni library: " + ule);
            System.exit(1);
        } catch (SecurityException se) {
            Log.e(TAG, "Encountered a security issue when loading dtvjni library: " + se);
            System.exit(1);
        }
    }

    public static LibDTV getInstance() throws LibDtvException {
        synchronized (LibDTV.class) {
            if (sInstance == null) {
                sInstance = new LibDTV();
                sInstance.setVout( VOUT_ANDROID_SURFACE ) ;
            }
        }

        return sInstance;
    }

    /**
     * Return an existing instance of libDTV Call it when it is NOT important
     * that this fails
     *
     * @return libDTV instance OR null
     */
    public static LibDTV getExistingInstance() {
        synchronized (LibDTV.class) {
            return sInstance;
        }
    }

    /**
     * Constructor
     * It is private because this class is a singleton.
     */
    private LibDTV() {
        
    }

    /**
     * Destructor:
     * It is bad practice to rely on them, so please don't forget to call
     * destroy() before exiting.
     */
    @Override
    protected void finalize() {
        if (mLibDtvInstance != 0) {
            Log.d(TAG, "LibDTV is was destroyed yet before finalize()");
            destroy();
        }
    }


    /**
     * Give to LibDTV the surface to draw the video.
     * @param f the surface to draw
     */
    public native void setSurface(Surface f);

    public static synchronized void restart(Context context) {
        if (sInstance != null) {
            try {
                sInstance.destroy();
                sInstance.init(context);
            } catch (LibDtvException lve) {
                Log.e(TAG, "Unable to reinit libdtv: " + lve);
            }
        }
    }

     public int getHardwareAcceleration() {
    	return 1 ;
    }

    public void setHardwareAcceleration(int hardwareAcceleration) {
        if (hardwareAcceleration == HW_ACCELERATION_DISABLED) {
            Log.d(TAG, "HWDec disabled: by user");
            this.hardwareAcceleration = HW_ACCELERATION_DISABLED;
            this.codecList = "all";
        } else {
            // Automatic or forced
            HWDecoderUtil.Decoder decoder = HWDecoderUtil.getDecoderFromDevice();

            if (decoder == HWDecoderUtil.Decoder.NONE) {
                // NONE
                this.hardwareAcceleration = HW_ACCELERATION_DISABLED;
                this.codecList = "all";
                Log.d(TAG, "HWDec disabled: device not working with mediacodec,iomx");
            } else if (decoder == HWDecoderUtil.Decoder.UNKNOWN) {
                // UNKNOWN
                if (hardwareAcceleration < 0) {
                    this.hardwareAcceleration = HW_ACCELERATION_DISABLED;
                    this.codecList = "all";
                    Log.d(TAG, "HWDec disabled: automatic and (unknown device or android version < 4.3)");
                } else {
                    this.hardwareAcceleration = hardwareAcceleration;
                    this.codecList = DEFAULT_CODEC_LIST;
                    Log.d(TAG, "HWDec enabled: forced by user and unknown device");
                }
            } else {
                // OMX, MEDIACODEC or ALL
                this.hardwareAcceleration = hardwareAcceleration < 0 ?
                        HW_ACCELERATION_FULL : hardwareAcceleration;
                if (decoder == HWDecoderUtil.Decoder.ALL)
                    this.codecList = DEFAULT_CODEC_LIST;
                else {
                    final StringBuilder sb = new StringBuilder();
                    if (decoder == HWDecoderUtil.Decoder.MEDIACODEC)
                        sb.append("mediacodec,");
                    else if (decoder == HWDecoderUtil.Decoder.OMX)
                        sb.append("iomx,");
                    sb.append("all");
                    this.codecList = sb.toString();
                }
                Log.d(TAG, "HWDec enabled: device working with: " + this.codecList);
            }
        }
    }

    public int getDevHardwareDecoder() {
    	return DEV_HW_DECODER_AUTOMATIC;
    }

    public void setDevHardwareDecoder(int devHardwareDecoder) {
        if (devHardwareDecoder != DEV_HW_DECODER_AUTOMATIC) {
            this.devHardwareDecoder = devHardwareDecoder;
            if (this.devHardwareDecoder == DEV_HW_DECODER_OMX ||
                    this.devHardwareDecoder == DEV_HW_DECODER_OMX_DR)
                this.devCodecList = "iomx";
            else
                this.devCodecList = "mediacodec";

            Log.d(TAG, "HWDec forced: " + this.devCodecList +
                (isDirectRendering() ? "-dr" : ""));
            this.devCodecList += ",none";
        } else {
            this.devHardwareDecoder = DEV_HW_DECODER_AUTOMATIC;
            this.devCodecList = null;
        }
    }

    public boolean isDirectRendering() {
    	return true ;
    }

    public String getSubtitlesEncoding() {
        return subtitlesEncoding;
    }

    public void setSubtitlesEncoding(String subtitlesEncoding) {
        this.subtitlesEncoding = subtitlesEncoding;
    }

    public int getAout() {
        return aout;
    }

    public void setAout(int aout) {
        if (aout < 0)
            this.aout = AOUT_OPENSLES ;
        else
            this.aout = aout;
    }

    public int getVout() {
        return vout;
    }

    public void setVout(int vout) {
        if (vout < 0)
            this.vout = VOUT_ANDROID_SURFACE;
        else
            this.vout = vout;
        if (this.vout == VOUT_ANDROID_SURFACE && HAS_WINDOW_VOUT)
            this.vout = VOUT_ANDROID_WINDOW;
    }

    public boolean useCompatSurface() {
        return this.vout != VOUT_ANDROID_WINDOW;
    }

    public boolean timeStretchingEnabled() {
        return timeStretching;
    }

    public void setTimeStretching(boolean timeStretching) {
        this.timeStretching = timeStretching;
    }

    public int getDeblocking() {
    	return 4 ;
    }

    public void setDeblocking(int deblocking) {
        this.deblocking = deblocking;
    }

    public String getChroma() {
        return chroma;
    }

    public boolean isVerboseMode() {
        return verboseMode;
    }

    public void setVerboseMode(boolean verboseMode) {
        this.verboseMode = verboseMode;
    }

    public float[] getEqualizer()
    {
        return equalizer;
    }

    public void setEqualizer(float[] equalizer)
    {
        this.equalizer = equalizer;
        applyEqualizer();
    }

    private void applyEqualizer()
    {
        setNativeEqualizer(mInternalMediaPlayerInstance, this.equalizer);
    }
    private native int setNativeEqualizer(long mediaPlayer, float[] bands);

    public boolean frameSkipEnabled() {
        return frameSkip;
    }

    public void setFrameSkip(boolean frameskip) {
        this.frameSkip = frameskip;
    }

    public int getNetworkCaching() {
        return this.networkCaching;
    }

    public void setNetworkCaching(int networkcaching) {
        this.networkCaching = networkcaching;
    }

    public boolean getHttpReconnect() {
        return httpReconnect;
    }

    public void setHttpReconnect(boolean httpReconnect) {
        this.httpReconnect = httpReconnect;
    }

    public void init(Context context) throws LibDtvException {
        Log.v(TAG, "Initializing LibDTV");
        mDebugLogBuffer = new StringBuffer();
        if (!mIsInitialized) {

            File cacheDir = context.getCacheDir();
            mCachePath = (cacheDir != null) ? cacheDir.getAbsolutePath() : null;
            nativeInit();
            setEventHandler(EventHandler.getInstance());
            mIsInitialized = true;
        }
    }

    public void destroy() {
        nativeDestroy();
        detachEventHandler();
        mIsInitialized = false;
    }

    private native void nativeInit() throws LibDtvException;
    private native void nativeDestroy();

    public native void startDebugBuffer();
    public native void stopDebugBuffer();
    public String getBufferContent() {
        return mDebugLogBuffer.toString();
    }

    public void clearBuffer() {
        mDebugLogBuffer.setLength(0);
    }

    public boolean isDebugBuffering() {
        return mIsBufferingLog;
    }

    public native void playMRL(String mrl, String[] mediaOptions);
    public native boolean isPlaying();
    public native boolean isSeekable();
    public native void play();

    public native void pause();
    public native void stop();

    public native int getPlayerState();

    public native int getVolume();
    public native int setVolume(int volume);

    public native long getTime();

    public native long setTime(long time);
    public native float getPosition();
    public native void setPosition(float pos);

    public native String changeset();

    public native boolean hasVideoTrack(String mrl) throws java.io.IOException;
    public native int getAudioTracksCount();
    public native Map<Integer,String> getAudioTrackDescription();
    public native Map<String, Object> getStats();
    public native int getAudioTrack();
    public native int setAudioTrack(int index);

    public native int getVideoTracksCount();

    public native int addSubtitleTrack(String path);
    public native Map<Integer,String> getSpuTrackDescription();

    public native int getSpuTrack();
    public native int setSpuTrack(int index);
    public native int getSpuTracksCount();

    public static native String nativeToURI(String path);
    public native static void sendMouseEvent( int action, int button, int x, int y);

    public static String PathToURI(String path) {
        if(path == null) {
            throw new NullPointerException("Cannot convert null path!");
        }
        return LibDTV.nativeToURI(path);
    }

    public static native void nativeReadDirectory(String path, ArrayList<String> res);
    public native static boolean nativeIsPathDirectory(String path);

    private native void setEventHandler(EventHandler eventHandler);
    private native void detachEventHandler();

    public static interface OnNativeCrashListener {
        public void onNativeCrash();
    }

    public void setOnNativeCrashListener(OnNativeCrashListener l) {
        mOnNativeCrashListener = l;
    }

    private void onNativeCrash() {
        if (mOnNativeCrashListener != null)
            mOnNativeCrashListener.onNativeCrash();
    }

    public String getCachePath() {
        return mCachePath;
    }

    public native int getTitle();
    public native void setTitle(int title);
    public native int getChapterCountForTitle(int title);
    public native int getTitleCount();
    public native void playerNavigate(int navigate);

    public native String getMeta(int meta);

    public native int setWindowSize(int width, int height);
    
    public native int getProgram();
    public native int setProgram(int program);
    public native String getProgramList();
    public native String getEpgList(String serviceName);
    public native String getPidList();
}
