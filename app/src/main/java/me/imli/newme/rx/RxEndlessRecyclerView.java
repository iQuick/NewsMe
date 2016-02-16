package me.imli.newme.rx;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import rx.Observable;

/**
 * Created by Em on 2015/11/27.
 */
public class RxEndlessRecyclerView {

    public static Observable<Integer> reachesEnd(RecyclerView view) {
        return RxRecyclerView.scrollEvents(view)
                .filter(i -> view.getLayoutManager() != null)
                .concatMap(event -> {
                    RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) { // also GridLayoutManager
                        return lastVisibleItemPosition((LinearLayoutManager) layoutManager);
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        return lastVisibleItemPositions((StaggeredGridLayoutManager) layoutManager);
                    } else {
                        return Observable.empty();
                    }
                })
                .filter(i -> i >= view.getLayoutManager().getItemCount() - 1)
                .distinctUntilChanged();
    }

    public static Observable<Integer> lastVisibleItemPosition(LinearLayoutManager lm) {
        return Observable.just(lm.findLastVisibleItemPosition());
    }

    public static Observable<Integer> lastVisibleItemPositions(StaggeredGridLayoutManager lm) {
        return Observable.create(subscriber -> {
            int[] positions = new int[lm.getSpanCount()];
            lm.findLastVisibleItemPositions(positions);

            for (int i : positions) {
                if (subscriber.isUnsubscribed()) {
                    break;
                } else {
                    subscriber.onNext(i);
                }
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
    }

}
