package com.sina.sinavideo.coreplayer;

import android.net.Uri;
import android.view.View;

public interface ISinaMediaController {

    public interface ISinaMediaPlayerControl {

        void start();

        void pause();

        long getDuration();

        long getCurrentPosition();

        void seekTo(long pos);

        boolean isPlaying();

        boolean isBuffering();

        void setBufferSize(int bufSize);

        int getBufferPercentage();

        void setVolume(float leftVolume, float rightVolume);

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        boolean isPlayEnd();

        public void setVideoPath(String path);

        public void setVideoURI(Uri uri);

        public void stopPlayback();

        public void suspend();

        public void resume();
    }

    public void setMediaPlayer(ISinaMediaPlayerControl player);

    public void setAnchorView(View view);

    public void setEnabled(boolean enabled);

    public void setFileName(String name);

    public boolean isShowing();

    public void show(int timeout);

    public void show();

    public void hide();
}