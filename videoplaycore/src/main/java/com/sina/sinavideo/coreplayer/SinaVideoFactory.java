package com.sina.sinavideo.coreplayer;

import android.content.Context;

import com.sina.sinavideo.coreplayer.splayer.VideoView;

public class SinaVideoFactory {

    private static SinaVideoFactory mInstance = null;

    public static SinaVideoFactory getInstance() {
        if (mInstance == null) {
            mInstance = new SinaVideoFactory();
        }
        return mInstance;
    }

    public ISinaVideoView createSinaVideoView(Context context) {
        // /return new VitamioVideoView(context);
        return new VideoView(context);
        // /return new VideoViewHard(context);
    }
}