package me.imli.newme.rx;

import android.app.Dialog;

import rx.Observable;

/**
 * Created by Em on 2015/12/22.
 */
public class RxWaiting {

//    public static <T> Observable.Transformer<T, T> bindRefreshing(SwipeRefreshLayout indicator) {
//        return observable -> observable
//                .doOnSubscribe(() -> indicator.post(() -> indicator.setRefreshing(true)))
//                .doOnError(e -> indicator.post(() -> indicator.setRefreshing(false)))
//                .doOnCompleted(() -> indicator.post(() -> indicator.setRefreshing(false)));
//    }

    public static <T> Observable.Transformer<T, T>bindWaiting(Dialog dialog) {
        return observable -> observable
                .doOnSubscribe(() -> dialog.show())
                .doOnCompleted(() -> dialog.dismiss());
    }

}
