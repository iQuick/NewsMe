package me.imli.newme.rx;

import android.support.v4.widget.SwipeRefreshLayout;

import rx.Observable;

/**
 * Created by Em on 2015/11/27.
 */
public class RxNetworking {

    public static <T> Observable.Transformer<T, T> bindRefreshing(SwipeRefreshLayout indicator) {
        return observable -> observable
                .doOnSubscribe(() -> indicator.post(() -> indicator.setRefreshing(true)))
                .doOnError(e -> indicator.post(() -> indicator.setRefreshing(false)))
                .doOnCompleted(() -> indicator.post(() -> indicator.setRefreshing(false)));
    }


}
