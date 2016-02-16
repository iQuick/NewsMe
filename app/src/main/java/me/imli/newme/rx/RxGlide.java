package me.imli.newme.rx;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import rx.Observable;

/**
 * Created by Em on 2015/11/27.
 */
public class RxGlide {

    public static Observable<File> download(RequestManager rm, String url) {
        return download(rm, url, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public static Observable<File> download(RequestManager rm, String url, int width, int height) {
        return Observable.defer(() -> {
            try {
                return Observable.just(rm.load(url).downloadOnly(width, height).get());
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

}
