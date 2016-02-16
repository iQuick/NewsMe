package me.imli.newme.rx;

import android.support.v7.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/11/27.
 */
public class RxList {

    public static <E> Action1<List<E>> appendTo(List<E> list) {
        return list::addAll;
    }

//    public static <E> Action1<List<E>> prependTo(List<E> list, RecyclerView rv) {
//        return items -> Observable.just(items)
////                .flatMap(is -> {
////                    Collections.reverse(is);
////                    return Observable.just(is);})
//                .flatMap(is -> Observable.from(is))
////                .filter(i -> !list.contains(i))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(i -> list.add(0, i), e -> e.printStackTrace(), () -> rv.scrollTo(0, 0));
//    }

    public static <E> Action1<List<E>> prependTo(List<E> list, RecyclerView rx) {
        return items -> Observable.just(items)
                .flatMap(is -> {
                    Collections.reverse(is);
                    return Observable.just(is);
                })
                .flatMap(is -> Observable.from(is))
                .filter(i -> !list.contains(i))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> rx.smoothScrollToPosition(0))
                .subscribe(i -> list.add(0, i), e -> e.printStackTrace());
    }

    public static <T> Observable.Transformer<T, T> clear(List<?> list) {
        return observable -> observable
                .doOnSubscribe(() -> list.clear());
    }

}
