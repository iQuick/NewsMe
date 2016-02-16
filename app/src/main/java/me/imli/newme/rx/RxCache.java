package me.imli.newme.rx;

import android.content.Context;

import me.imli.newme.utils.CacheUtils;
import me.imli.newme.utils.FileSizeUtil;
import rx.Observable;

/**
 * Created by Em on 2015/12/22.
 */
public class RxCache {

    public static Observable<String> getCacheSize(Context context) {
        return Observable.defer(() -> {
            try {
                return Observable.just(FileSizeUtil.formetFileSize(CacheUtils.getCacheSize(context)));
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    public static Observable<String> clearCache(Context context) {
        return Observable.defer(() -> {
            try {
               return Observable.just(FileSizeUtil.formetFileSize(CacheUtils.clearCache(context)));
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

}
