package com.sina.sinavideo.coreplayer;

public interface ISinaMediaPlayer {

    public interface OnPreparedListener {

        void onPrepared();
    }

    public interface OnVideoSizeChangedListener {

        public void onVideoSizeChanged(int width, int height);
    }

    public interface OnBufferingUpdateListener {

        void onBufferingUpdate(int percent);
    }

    public interface OnCompletionListener {

        void onCompletion();
    }

    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_DECODER_FAIL = -2000; // hardware
                                                              // decoder error

    public interface OnErrorListener {

        boolean onError(int what, int extra);
    }

    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_DOWNLOAD_RATE_CHANGED = 901;

    public static final int VIDEOQUALITY_LOW = -16;
    public static final int VIDEOQUALITY_MEDIUM = 0;
    public static final int VIDEOQUALITY_HIGH = 16;

    public interface OnInfoListener {

        boolean onInfo(int what, int extra);
    }

    public interface OnSeekCompleteListener {

        public void onSeekComplete();
    }

    public interface OnTimedTextListener {

        public void onTimedText(String text);

        public void onTimedTextUpdate(byte[] pixels, int width, int height);
    }

    public interface onProgressUpdateListener {

        public void onProgressUpdate(long current, long duration);
    }

    public interface OnVideoOpenedListener {

        void onVideoOpened();
    }

    // // add media player interface here
    // public void setDataSource(Context context, Uri uri)
    // throws IOException, IllegalArgumentException, SecurityException,
    // IllegalStateException;
    //
    // public void setDataSource(Context context, Uri uri, Map<String, String>
    // headers)
    // throws IOException, IllegalArgumentException, SecurityException,
    // IllegalStateException;
    //
    // public void setDataSource(String path)
    // throws IOException, IllegalArgumentException, SecurityException,
    // IllegalStateException;
    //
    // public void start() throws IllegalStateException;
    //
    // public void stop() throws IllegalStateException;
    //
    // public void prepare() throws IOException, IllegalStateException;
    //
    // public void prepareAsync() throws IllegalStateException;
    //
    // public void reset() throws IllegalStateException;
    //
    // public void pause() throws IllegalStateException;
    //
    // public void release() throws IllegalStateException;
    //
    // public boolean isPlaying();
    //
    // public int getDuration();
    //
    // public int getCurrentPosition();
    //
    // public void seekTo(int msec) throws IllegalStateException;
    //
    // public int getVideoWidth();
    //
    // public int getVideoHeight();
    //
    // // used by subclass
    // public void setOnPreparedListener(OnPreparedListener listener);
    // public void setOnCompletionListener(OnCompletionListener listener);
    // public void setOnBufferingUpdateListener(OnBufferingUpdateListener
    // listener);
    // public void setOnSeekCompleteListener(OnSeekCompleteListener listener);
    // public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener
    // listener);
    // public void setOnErrorListener(OnErrorListener listener);
    // public void setOnInfoListener(OnInfoListener listener);
}