package com.sina.sinavideo.coreplayer;

import com.sina.sinavideo.coreplayer.ISinaMediaController.ISinaMediaPlayerControl;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnBufferingUpdateListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnCompletionListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnErrorListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnInfoListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnPreparedListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnSeekCompleteListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnTimedTextListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnVideoOpenedListener;
import com.sina.sinavideo.coreplayer.splayer.MediaPlayer.OnVideoSizeChangedListener;

public interface ISinaVideoView extends ISinaMediaPlayerControl {

    public static final int VIDEO_SCALE_CENTER_INSIDE = 0;
    public static final int VIDEO_SCALE_CENTER_CROP = 1;
    public static final int VIDEO_SCALE_FIT_STRENTH = 2;
    public static final int VIDEO_SCALE_ORIGIN = 3;

    public void configScaleType(int type);

    public void setMediaController(ISinaMediaController controller);

    public void requestVideoLayout();

    public void beginChangeParentView();

    public void endChangeParentView();

    // set listeners
    public void setOnPreparedListener(OnPreparedListener listener);

    public void setOnCompletionListener(OnCompletionListener listener);

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    public void setOnErrorListener(OnErrorListener listener);

    public void setOnInfoListener(OnInfoListener listener);

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener);

    public void setOnTimedTextListener(OnTimedTextListener listener);

    public void setOnVideoOpenedListener(OnVideoOpenedListener listener);

    @Override
    public long getCurrentPosition();
}