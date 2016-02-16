package com.sina.sinavideo.coreplayer.splayer;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.media.AudioManager;
import android.media.TimedText;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MediaPlayer {

    /*
     * log filter
     */
    static final int SPLAYER_TRACE_QUIET = -8;
    static final int SPLAYER_TRACE_PANIC = 0;
    static final int SPLAYER_TRACE_FATAL = 8;
    static final int SPLAYER_TRACE_ERROR = 16;
    static final int SPLAYER_TRACE_WARNING = 24;
    static final int SPLAYER_TRACE_INFO = 32;
    static final int SPLAYER_TRACE_VERBOSE = 40;
    static final int SPLAYER_TRACE_DEBUG = 48;

    static {
        try {
            String LIB_ROOT = SPlayer.getLibraryPath();
            Log.d("MediaPlayer", "loading library");
            System.load(LIB_ROOT + "libsplayer.so");
            // String logPath = "/sdcard/";
            native_init(SPlayer.getLogPath(), MediaPlayer.SPLAYER_TRACE_ERROR, 1, 1); // native_init(logPath,MediaPlayer.SPLAYER_TRACE_ERROR,
                                                                                      // 1,
                                                                                      // 1);
            Log.d("MediaPlayer", "loading library success");
        } catch (Exception ex) {
            Log.e("MediaPlayer", "load libsplayer.so fail");
        }
    }

    /*
     * logPath: where saving log filefilter: see log filterenableConsole: 1 log
     * to logcat ; 0 notenableFile: 1 log to file ; 0 not
     */
    private static native final void native_init(String logPath, int filter, int enableConsole, int enableFile);

    private final static String TAG = "MediaPlayer";

    private int mNativeContext; // accessed by native methods
    // private int mNativeSurfaceTexture; // accessed by native methods
    // private int mListenerContext; // accessed by native methods
    // private SurfaceHolder mSurfaceHolder;
    // private SurfaceView mSurfaceView;
    private EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    private int mWakeLockMode;
    private Context mContext;

    /**
     * Default constructor. Consider using one of the create() methods for
     * synchronously instantiating a MediaPlayer from a Uri or resource.
     * <p>
     * When done with the MediaPlayer, you should call {@link #release()}, to
     * free the resources. If not released, too many MediaPlayer instances may
     * result in an exception.
     * </p>
     */
    public MediaPlayer(Context contxt) {

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        mContext = contxt;

        /*
         * Native setup requires a weak reference to our object. It's easier to
         * create it here than in C++.
         */
        native_setup(new WeakReference<MediaPlayer>(this), 0);
    }

    public MediaPlayer(Context contxt, boolean hardDecodeMode) {

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        mContext = contxt;

        /*
         * Native setup requires a weak reference to our object. It's easier to
         * create it here than in C++.
         */
        int useHard = 0;
        if (hardDecodeMode == true && supportHardDecode()) {
            useHard = 1;
        }
        native_setup(new WeakReference<MediaPlayer>(this), useHard);
    }

    private native final void native_setup(Object mediaplayer_this, int hardDecodeMode);

    /*
     * hard coded mode only support api level 16 and above Platform Version API
     * Level Android 4.4 19 Android 4.3 18 Android 4.2 17 Android 4.1 16 Android
     * 4.0.3 15 Android 4.0 14 Android 3.2 13 Android 3.1 12 Android 3.0 11
     * Android 2.3.3 10 Android 2.3 9 Android 2.2 8 Android 2.1 7 Android 2.0.1
     * 6 Android 2.0 5 Android 1.6 4 Android 1.5 3 Android 1.1 2 Android 1.0 1
     */
    static public boolean supportHardDecode() {
        if (android.os.Build.VERSION.SDK_INT > 0) {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                return true;
            }
        } else {
            int level = Integer.valueOf(android.os.Build.VERSION.SDK);
            if (level >= 16) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method to create a MediaPlayer for a given Uri. On success,
     * {@link #prepare()} will already have been called and must not be called
     * again.
     * <p>
     * When done with the MediaPlayer, you should call {@link #release()}, to
     * free the resources. If not released, too many MediaPlayer instances will
     * result in an exception.
     * </p>
     * 
     * @param context
     *            the Context to use
     * @param uri
     *            the Uri from which to get the datasource
     * @param holder
     *            the SurfaceHolder to use for displaying the video
     * @return a MediaPlayer object, or null if creation failed
     */
    public static MediaPlayer create(Context context, String path, SurfaceView sv) {

        try {
            MediaPlayer mp = new MediaPlayer(context);
            mp.setDataSource(path, null, null);
            mp.setDisplay(sv);
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (IllegalStateException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        }

        return null;
    }

    public SurfaceView CreateRender(Context context) {
        return new VideoView(context);
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     * 
     * @param path
     *            the path of the file, or the http/rtsp URL of the stream you
     *            want to play
     * @param headers
     *            the headers associated with the http request for the stream
     *            you want to play
     * @throws IllegalStateException
     *             if it is called in an invalid state
     * @hide pending API council
     */
    public native void setDataSource(String path, String[] keys, String[] values) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException;

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException,
            IllegalStateException {
        setDataSource(path, null, null);
    }

    /**
     * Sets the SurfaceView to be used as the sink for the video portion of
     * should be com.sina.sinavideo.coreplayer.ffmpeg.GLSurfaceView
     * 
     * @param sv
     *            The SurfaceView to be used for the video portion of the media.
     */
    public native void setDisplay(SurfaceView sv) throws IllegalArgumentException, IllegalStateException;

    /**
     * Sets the Surface to be used as the sink for the video portion of should
     * be android.view.Surface
     * 
     * @param surface
     *            The Surface to be used for the video portion of the media.
     */
    public native void setSurface(Surface surface) throws IllegalArgumentException, IllegalStateException;

    /**
     * Prepares the player for playback, synchronously.
     * 
     * After setting the datasource and the display surface, you need to either
     * call prepare() or prepareAsync(). For files, it is OK to call prepare(),
     * which blocks until MediaPlayer is ready for playback.
     * 
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public native void prepare() throws IOException, IllegalStateException;

    /**
     * Prepares the player for playback, asynchronously.
     * 
     * After setting the datasource and the display surface, you need to either
     * call prepare() or prepareAsync(). For streams, you should call
     * prepareAsync(), which returns immediately, rather than blocking until
     * enough data has been buffered.
     * 
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public native void prepareAsync() throws IllegalStateException;

    /**
     * Starts or resumes playback. If playback had previously been paused,
     * playback will continue from where it was paused. If playback had been
     * stopped, or never started before, playback will start at the beginning.
     * 
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public void start() throws IllegalStateException {
        stayAwake(true);
        native_start();
    }

    private native void native_start() throws IllegalStateException;

    /**
     * Stops playback after playback has been stopped or paused.
     * 
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public void stop() throws IllegalStateException {
        stayAwake(false);
        native_stop();
    }

    private native void native_stop() throws IllegalStateException;

    /**
     * Pauses playback. Call start() to resume.
     * 
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public void pause() throws IllegalStateException {
        // /stayAwake(false); 小牛拉车也注释了
        native_pause();
    }

    private native void native_pause() throws IllegalStateException;

    /**
     * Set the low-level power management behavior for this MediaPlayer. This
     * can be used when the MediaPlayer is not playing through a SurfaceHolder
     * set with {@link #setDisplay(SurfaceHolder)} and thus can use the
     * high-level {@link #setScreenOnWhilePlaying(boolean)} feature.
     * 
     * <p>
     * This function has the MediaPlayer access the low-level power manager
     * service to control the device's power usage while playing is occurring.
     * The parameter is a combination of {@link PowerManager} wake
     * flags. Use of this method requires
     * {@link android.Manifest.permission#WAKE_LOCK} permission. By default, no
     * attempt is made to keep the device awake during playback.
     * 
     * @param context
     *            the Context to use
     * @param mode
     *            the power/wake mode to set
     * @see PowerManager
     */
    public void setWakeMode(Context context, int mode) {
        // boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                // washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE, MediaPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        // if (washeld) {
        mWakeLock.acquire();
        // }
        mWakeLockMode = mode;
    }

    /**
     * Control whether we should use the attached SurfaceHolder to keep the
     * screen on while video playback is occurring. This is the preferred method
     * over {@link #setWakeMode} where possible, since it doesn't require that
     * the application have permission for low-level wake lock access.
     * 
     * @param screenOn
     *            Supply true to keep the screen on, false to allow it to turn
     *            off.
     */

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            // if (screenOn && mSurfaceHolder == null) {
            // Log.w(TAG,
            // "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            // }
            mScreenOnWhilePlaying = screenOn;
            // updateSurfaceScreenOn();
        }
    }

    private void stayAwake(boolean awake) {
        // Log.d("MedialPlayer","stayAwake into,awake="+awake);

        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                // Log.d("MedialPlayer","acquire lock");
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                // Log.d("MedialPlayer","delete lock ");
                mWakeLock.release();
                mWakeLock = null;
            }
        } else if (mWakeLock == null && awake) {
            // Log.d("MedialPlayer","create lock again");
            setWakeMode(this.mContext, this.mWakeLockMode);
        }
        mStayAwake = awake;
        // updateSurfaceScreenOn();
    }

    // private void updateSurfaceScreenOn() {
    // if (mSurfaceView != null) {
    // mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
    // }
    // }

    /**
     * Returns the width of the video.
     * 
     * @return the width of the video, or 0 if there is no video, no display
     *         surface was set, or the width has not been determined yet. The
     *         OnVideoSizeChangedListener can be registered via
     *         {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
     *         to provide a notification when the width is available.
     */
    public native int getVideoWidth();

    /**
     * Returns the height of the video.
     * 
     * @return the height of the video, or 0 if there is no video, no display
     *         surface was set, or the height has not been determined yet. The
     *         OnVideoSizeChangedListener can be registered via
     *         {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
     *         to provide a notification when the height is available.
     */
    public native int getVideoHeight();

    /**
     * Returns the aspect ratio of the video.
     * 
     * @return the aspect ratio of the video, or 0 if there is no video, or the
     *         width and height is not available.
     * @see io.vov.vitamio.widget.VideoView#setVideoLayout(int, float)
     */
    public native float getVideoAspectRatio();

    /**
     * Checks whether the MediaPlayer is playing.
     * 
     * @return true if currently playing, false otherwise
     */
    public native boolean isPlaying();

    /**
     * Seeks to specified time position.
     * 
     * @param msec
     *            the offset in milliseconds from the start to seek to
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized
     */
    public native void seekTo(int msec) throws IllegalStateException;

    /**
     * Gets the current playback position.
     * 
     * @return the current position in milliseconds
     */
    public native int getCurrentPosition();

    /**
     * Gets the duration of the file.
     * 
     * @return the duration in milliseconds
     */
    public native int getDuration();

    /**
     * Releases resources associated with this MediaPlayer object. It is
     * considered good practice to call this method when you're done using the
     * MediaPlayer. In particular, whenever an Activity of an application is
     * paused (its onPause() method is called), or stopped (its onStop() method
     * is called), this method should be invoked to release the MediaPlayer
     * object, unless the application has a special need to keep the object
     * around. In addition to unnecessary resources (such as memory and
     * instances of codecs) being held, failure to call this method immediately
     * if a MediaPlayer object is no longer needed may also lead to continuous
     * battery consumption for mobile devices, and playback failure for other
     * applications if no multiple instances of the same codec are supported on
     * a device. Even if multiple instances of the same codec are supported,
     * some performance degradation may be expected when unnecessary multiple
     * instances are used at the same time.
     */
    public void release() {
        stayAwake(false);
        // updateSurfaceScreenOn();
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mOnVideoSizeChangedListener = null;
        mOnTimedTextListener = null;
        native_release();
    }

    private native void native_release();

    /**
     * Resets the MediaPlayer to its uninitialized state. After calling this
     * method, you will have to initialize it again by setting the data source
     * and calling prepare().
     */
    public void reset() {
        stayAwake(false);
        native_reset();
        // make sure none of the listeners get called anymore
        mEventHandler.removeCallbacksAndMessages(null);
    }

    private native void native_reset();

    /**
     * Sets the player to be looping or non-looping.
     * 
     * @param looping
     *            whether to loop or not
     */
    public native void setLooping(boolean looping);

    /**
     * Checks whether the MediaPlayer is looping or non-looping.
     * 
     * @return true if the MediaPlayer is currently looping, false otherwise
     */
    public native boolean isLooping();

    public native boolean isBuffering();

    /**
     * set Player buffer time.
     * 
     * @param bufSize
     *            msec total buffer time in mill second, default 5000 = 5
     *            seconds
     */
    public native void setBufferSize(int msec);

    /**
     * Sets the audio stream type for this MediaPlayer. See {@link AudioManager}
     * for a list of stream types. Must call this method before prepare() or
     * prepareAsync() in order for the target stream type to become effective
     * thereafter.
     * 
     * @param streamtype
     *            the audio stream type
     * @see AudioManager
     */
    public void setAudioStreamType(int streamtype) {

    }

    /**
     * Sets the volume on this player. This API is recommended for balancing the
     * output of audio streams within an application. Unless you are writing an
     * application to control user settings, this API should be used in
     * preference to {@link AudioManager#setStreamVolume(int, int, int)} which
     * sets the volume of ALL streams of a particular type. Note that the passed
     * volume values are raw scalars. UI controls should be scaled
     * logarithmically.
     * 
     * @param leftVolume
     *            left volume scalar [ -50000 - 0 ]
     * @param rightVolume
     *            right volume scalar
     */
    public native void setVolume(float leftVolume, float rightVolume);

    public native void config(String cfg, int val) throws IllegalStateException;

    @Override
    protected void finalize() {
        native_finalize();
    }

    private native final void native_finalize();

    /*
     * Do not change these values without updating their counterparts in
     * include/media/mediaplayer.h!
     */
    private static final int MEDIA_NOP = 0; // interface test message
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;

    private class EventHandler extends Handler {

        private MediaPlayer mMediaPlayer;

        public EventHandler(MediaPlayer mp, Looper looper) {
            super(looper);
            mMediaPlayer = mp;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mMediaPlayer.mNativeContext == 0) {
                Log.w(TAG, "mediaplayer went away with unhandled events");
                return;
            }
            switch (msg.what) {
                case MEDIA_PREPARED :
                    if (mOnPreparedListener != null)
                        mOnPreparedListener.onPrepared(mMediaPlayer);
                    return;

                case MEDIA_PLAYBACK_COMPLETE :
                    if (mOnCompletionListener != null)
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    stayAwake(false);
                    return;

                case MEDIA_BUFFERING_UPDATE :
                    if (mOnBufferingUpdateListener != null)
                        mOnBufferingUpdateListener.onBufferingUpdate(mMediaPlayer, msg.arg1);
                    return;

                case MEDIA_SEEK_COMPLETE :
                    if (mOnSeekCompleteListener != null)
                        mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
                    return;

                case MEDIA_SET_VIDEO_SIZE :
                    if (mOnVideoSizeChangedListener != null)
                        mOnVideoSizeChangedListener.onVideoSizeChanged(mMediaPlayer, msg.arg1, msg.arg2);
                    return;

                case MEDIA_ERROR :
                    // For PV specific error values (msg.arg2) look in
                    // opencore/pvmi/pvmf/include/pvmf_return_codes.h
                    // Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled = false;
                    if (mOnErrorListener != null) {
                        error_was_handled = mOnErrorListener.onError(mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    if (mOnCompletionListener != null && !error_was_handled) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                    stayAwake(false);
                    return;

                case MEDIA_INFO :
                    if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING) {
//                         Log.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 +
//                         ")");
                    }
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mMediaPlayer, msg.arg1, msg.arg2);
                    }
                    // No real default action so far.
                    return;
                case MEDIA_TIMED_TEXT :

                    return;

                case MEDIA_NOP : // interface test message - ignore
                    break;

                default :
                    Log.e(TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }

    /**
     * Called from native code when an interesting event happens. This method
     * just uses the EventHandler system to post the event back to the main app
     * thread. We use a weak reference to the original MediaPlayer object so
     * that the native code is safe from the object disappearing from underneath
     * it. (This is the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        MediaPlayer mp = (MediaPlayer) ((WeakReference) mediaplayer_ref).get();
        if (mp == null) {
            return;
        }

        if (mp.mEventHandler != null) {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Interface definition for a callback to be invoked when the media source
     * is ready for playback.
     */
    public interface OnPreparedListener {

        /**
         * Called when the media file is ready for playback.
         * 
         * @param mp
         *            the MediaPlayer that is ready for playback
         */
        void onPrepared(MediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when the media source is ready for
     * playback.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /**
     * Interface definition for a callback to be invoked when playback of a
     * media source has completed.
     */
    public interface OnCompletionListener {

        /**
         * Called when the end of a media source is reached during playback.
         * 
         * @param mp
         *            the MediaPlayer that reached the end of the file
         */
        void onCompletion(MediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when the end of a media source has been
     * reached during playback.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition of a callback to be invoked indicating buffering
     * status of a media resource being streamed over the network.
     */
    public interface OnBufferingUpdateListener {

        /**
         * Called to update status in buffering a media stream received through
         * progressive HTTP download. The received buffering percentage
         * indicates how much of the content has been buffered or played. For
         * example a buffering update of 80 percent when half the content has
         * already been played indicates that the next 30 percent of the content
         * to play has been buffered.
         * 
         * @param mp
         *            the MediaPlayer the update pertains to
         * @param percent
         *            the percentage (0-100) of the content that has been
         *            buffered or played thus far
         */
        void onBufferingUpdate(MediaPlayer mp, int percent);
    }

    /**
     * Register a callback to be invoked when the status of a network stream's
     * buffer has changed.
     * 
     * @param listener
     *            the callback that will be run.
     */
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener;

    /**
     * Interface definition of a callback to be invoked indicating the
     * completion of a seek operation.
     */
    public interface OnSeekCompleteListener {

        /**
         * Called to indicate the completion of a seek operation.
         * 
         * @param mp
         *            the MediaPlayer that issued the seek operation
         */
        public void onSeekComplete(MediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when a seek operation has been
     * completed.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;

    /**
     * Interface definition of a callback to be invoked when the video size is
     * first known or updated
     */
    public interface OnVideoSizeChangedListener {

        /**
         * Called to indicate the video size
         * 
         * @param mp
         *            the MediaPlayer associated with this callback
         * @param width
         *            the width of the video
         * @param height
         *            the height of the video
         */
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height);
    }

    /**
     * Register a callback to be invoked when the video size is known or
     * updated.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    /**
     * Interface definition of a callback to be invoked when a timed text is
     * available for display. {@hide}
     */
    public interface OnTimedTextListener {

        /**
         * Called to indicate an avaliable timed text
         * 
         * @param mp
         *            the MediaPlayer associated with this callback
         * @param text
         *            the timed text sample which contains the text needed to be
         *            displayed and the display format. {@hide}
         */
        public void onTimedText(MediaPlayer mp, TimedText text);
    }

    /**
     * Register a callback to be invoked when a timed text is available for
     * display.
     * 
     * @param listener
     *            the callback that will be run {@hide}
     */
    public void setOnTimedTextListener(OnTimedTextListener listener) {
        mOnTimedTextListener = listener;
    }

    private OnTimedTextListener mOnTimedTextListener;

    public interface OnVideoOpenedListener {

        void onVideoOpened(MediaPlayer mp);
    }

    public void setOnVideoOpenedListener(OnVideoOpenedListener listener) {
        mOnVideoOpenedListener = listener;
    }

    private OnVideoOpenedListener mOnVideoOpenedListener;
    /*
     * Do not change these values without updating their counterparts in
     * include/media/mediaplayer.h!
     */
    /**
     * Unspecified media player error.
     * 
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /**
     * Media server died. In this case, the application must release the
     * MediaPlayer object and instantiate a new one.
     * 
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_SERVER_DIED = 100;

    /**
     * The video is streamed and its container is not valid for progressive
     * playback i.e the video's index (e.g moov atom) is not at the start of the
     * file.
     * 
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;

    public static final int MEDIA_ERROR_IO = -1004; // File or network related
                                                    // operation errors.
    public static final int MEDIA_ERROR_MALFORMED = -1007; // Bitstream is not
                                                           // conforming to the
                                                           // related coding
                                                           // standard or file
                                                           // spec.
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010; // Bitstream is
                                                             // conforming to
                                                             // the related
                                                             // coding
                                                             // standard or
                                                             // file spec,
                                                             // but the media
                                                             // framework
                                                             // does not
                                                             // support the
                                                             // feature.
    public static final int MEDIA_ERROR_TIMED_OUT = -110; // Some operation
                                                          // takes too long to
                                                          // complete, usually
                                                          // more than 3-5
                                                          // seconds.
    public static final int MEDIA_ERROR_DECODER_FAIL = -2000; // hardware
                                                              // decoder error
    /**
     * Interface definition of a callback to be invoked when there has been an
     * error during an asynchronous operation (other errors will throw
     * exceptions at method call time).
     */
    public interface OnErrorListener {

        /**
         * Called to indicate an error.
         * 
         * @param mp
         *            the MediaPlayer the error pertains to
         * @param what
         *            the type of error that has occurred:
         *            <ul>
         *            <li>{@link #MEDIA_ERROR_UNKNOWN}
         *            <li>{@link #MEDIA_ERROR_SERVER_DIED}
         *            </ul>
         * @param extra
         *            an extra code, specific to the error. Typically
         *            implementation dependant.
         * @return True if the method handled the error, false if it didn't.
         *         Returning false, or not having an OnErrorListener at all,
         *         will cause the OnCompletionListener to be called.
         */
        boolean onError(MediaPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened during an
     * asynchronous operation.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;

    /*
     * Do not change these values without updating their counterparts in
     * include/media/mediaplayer.h!
     */
    /**
     * Unspecified media player info.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_UNKNOWN = 1;

    /**
     * The video is too complex for the decoder: it can't decode frames fast
     * enough. Possibly only the audio plays fine at this stage.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

    /**
     * MediaPlayer is temporarily pausing playback internally in order to buffer
     * more data.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_BUFFERING_START = 701;

    /**
     * MediaPlayer is resuming playback after filling buffers.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_BUFFERING_END = 702;

    /**
     * Bad interleaving means that a media has been improperly interleaved or
     * not interleaved at all, e.g has all the video samples first then all the
     * audio ones. Video is playing but a lot of disk seeks may be happening.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;

    /**
     * The media cannot be seeked (e.g live stream)
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;

    /**
     * A new set of metadata is available.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;

    /** onInfo 的 BUFFERING 的 extra 定义
     * 用于区分第一次加载视频引起的缓冲；播放拖动进度引起的缓冲；网络原因造成的缓冲
     */
    public static final int  MEDIA_INFO_BUFFERING_LOAD = 1;   //由于 第一次加载 引起的bufeering
    public static final int  MEDIA_INFO_BUFFERING_SEEKTO = 2;   //由于 拖动进度 引起的bufeering
    public static final int  MEDIA_INFO_BUFFERING_NETWORK = 4;   //由于 网络原因 引起的bufeering

    /**
     * Interface definition of a callback to be invoked to communicate some info
     * and/or warning about the media or its playback.
     */
    public interface OnInfoListener {

        /**
         * Called to indicate an info or a warning.
         * 
         * @param mp
         *            the MediaPlayer the info pertains to.
         * @param what
         *            the type of info or warning.
         *            <ul>
         *            <li>{@link #MEDIA_INFO_UNKNOWN}
         *            <li>{@link #MEDIA_INFO_VIDEO_TRACK_LAGGING}
         *            <li>{@link #MEDIA_INFO_BUFFERING_START}
         *            <li>{@link #MEDIA_INFO_BUFFERING_END}
         *            <li>{@link #MEDIA_INFO_BAD_INTERLEAVING}
         *            <li>{@link #MEDIA_INFO_NOT_SEEKABLE}
         *            <li>{@link #MEDIA_INFO_METADATA_UPDATE}
         *            </ul>
         * @param extra
         *            an extra code, specific to the info. Typically
         *            implementation dependant.
         * @return True if the method handled the info, false if it didn't.
         *         Returning false, or not having an OnErrorListener at all,
         *         will cause the info to be discarded.
         */
        boolean onInfo(MediaPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    private OnInfoListener mOnInfoListener;

    public interface onProgressUpdateListener {

        public void onProgressUpdate(MediaPlayer mp, long current, long duration);
    }

    public void setOnProgressUpdateListener(onProgressUpdateListener l) {
        mOnProgressUpdateListener = l;
    }

    private onProgressUpdateListener mOnProgressUpdateListener;

}
