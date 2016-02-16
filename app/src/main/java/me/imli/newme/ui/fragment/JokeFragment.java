package me.imli.newme.ui.fragment;

import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import java.util.List;

import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.api.JokeApi;
import me.imli.newme.databinding.FragmentJokeBinding;
import me.imli.newme.model.Image;
import me.imli.newme.model.Joke;
import me.imli.newme.rx.RxEndlessRecyclerView;
import me.imli.newme.rx.RxList;
import me.imli.newme.rx.RxNetworking;
import me.imli.newme.ui.ViewerActivity;
import me.imli.newme.ui.adapter.JokeAdapter;
import me.imli.newme.ui.base.BaseChanneFragment;
import me.imli.newme.utils.LogUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/12/9.
 */
public class JokeFragment extends BaseChanneFragment<FragmentJokeBinding> implements JokeAdapter.OnItemClickListener {

    /**
     * TAG
      */
    public static final String TAG = "JackFragment";

    // API
    private JokeApi mJokeApi;

    // Data
    private int mCurrPage;
    private ObservableList<Joke> mJokes;

    // Observable
    private Observable<List<Joke>> observableJokeRefreshData;
    private Observable<List<Joke>> observableJokeLoadMoreData;

    public static JokeFragment newInstance() {
        JokeFragment fragment = new JokeFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int inflateLayout() {
        return R.layout.fragment_joke;
    }

    @Override
    protected void initialization() {
        this.initData();
        this.initDBData();
        this.initView();
        this.initObservable();
    }

    private void initData() {
        this.mCurrPage = 1;
        this.mJokes = new ObservableArrayList<>();
    }

    private void initDBData() {
        getApp().getDB().getList(Const.DB_JOKES_NAME, Joke.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RxList.appendTo(mJokes), e -> e.printStackTrace());
    }

    private void initView() {
        JokeAdapter adapter = new JokeAdapter(getContext(), mJokes, Glide.with(getContext()));
        adapter.setOnItemClickListener(this);
        getBinding().content.setAdapter(adapter);
    }

    private void initObservable() {
        Observable.Transformer<List<Joke>, List<Joke>> networkingIndicator = RxNetworking.bindRefreshing(getBinding().refresher);

        observableJokeRefreshData = Observable.defer(() -> mJokeApi.joke("latest", 1))
                .doOnUnsubscribe(() -> unsubcribe("observable refresh data"))
                .flatMap(data -> Observable.just(data.data))
                .flatMap(list -> getApp().getDB().putList(Const.DB_JOKES_NAME, list, Joke.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(networkingIndicator);

        observableJokeLoadMoreData = Observable.defer(() -> mJokeApi.joke("latest", mCurrPage + 1))
                .doOnUnsubscribe(() -> unsubcribe("observable loadmore data"))
                .map(data -> {
                    mCurrPage = data.page;
                    return data.data;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(networkingIndicator);

        RxSwipeRefreshLayout.refreshes(getBinding().refresher)
                .doOnUnsubscribe(() -> unsubcribe("swipe refresh"))
                .flatMap(avoid -> observableJokeRefreshData)
                .compose(bindToLifecycle())
                .subscribe(RxList.prependTo(mJokes, getBinding().content), this::showError);

        RxEndlessRecyclerView.reachesEnd(getBinding().content)
                .doOnUnsubscribe(() -> unsubcribe("load more"))
                .flatMap(avoid -> observableJokeLoadMoreData)
                .compose(bindToLifecycle())
                .subscribe(RxList.appendTo(mJokes), this::showError);

        // 首次进入
        observableJokeRefreshData
                .map(list -> {
                    mJokes.clear();
                    return list;
                })
                .compose(bindToLifecycle())
                .subscribe(RxList.prependTo(mJokes, getBinding().content), this::showError);

    }

    @Override
    protected void createApi(ImApp app) {
        mJokeApi = app.createJokeApi(JokeApi.class);
    }

    /**
     *
     * @param msg
     * @param <T>
     * @return
     */
    private <T> Observable.Transformer<T, T> unsubcribe(String msg) {
        return observable -> observable
                .doOnSubscribe(() -> LogUtils.d(TAG, "unsubscribe " + msg))
                .doOnSubscribe(() -> getBinding().refresher.setRefreshing(false));
    }

    /**
     *
     * @param error
     */
    private void showError(Throwable error) {
        LogUtils.e(TAG, getString(R.string.error), error);
        Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.error_loading), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(JokeAdapter.ViewHolder holder, ImageView iv, Image[] images) {
        Intent intent = new Intent(getContext(), ViewerActivity.class);
        intent.putExtra(ViewerActivity.INTENT_POSTTION, holder.getAdapterPosition());
        intent.putExtra(ViewerActivity.INTENT_INDEX, 0);
        intent.putExtra(ViewerActivity.INTENT_IMAGES, images);
        // 兼容旧版本
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), iv, String.format("%s.image", images[0].url));
        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }


    @Override
    protected void onStopRefresh() {
        getBinding().refresher.setRefreshing(false);
    }
}
