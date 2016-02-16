package me.imli.newme.rx;

import android.app.WallpaperManager;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;

/**
 * Created by Em on 2015/12/2.
 */
public class RxWallpaperManager {

    public static Observable<Void> setStream(Context context, InputStream stream) {
        return Observable.defer(() -> {
            try {
                WallpaperManager.getInstance(context).setStream(stream);
                return Observable.just(null);
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

}
