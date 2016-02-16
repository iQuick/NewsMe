package me.imli.newme.ui.fragment;

import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import java.net.UnknownHostException;
import java.util.List;

import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.api.NewsApi;
import me.imli.newme.databinding.FragmentChannelBinding;
import me.imli.newme.model.Image;
import me.imli.newme.model.News;
import me.imli.newme.rx.RxEndlessRecyclerView;
import me.imli.newme.rx.RxList;
import me.imli.newme.rx.RxNetworking;
import me.imli.newme.ui.MainActivity;
import me.imli.newme.ui.ViewerActivity;
import me.imli.newme.ui.WebActivity;
import me.imli.newme.ui.adapter.NewsAdapter;
import me.imli.newme.ui.base.BaseFragment;
import me.imli.newme.utils.LogUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/11/27.
 */
public class NewsChannelFragment extends BaseFragment<FragmentChannelBinding> implements NewsAdapter.OnItemClickListener {

    private static final String TAG = "ChannelFragment";
    private static final String BUNDLE_ID = "id";
    private static final String BUNDLE_NAME = "name";

    // Api
    private int mCurrPage;
    private NewsApi mNewApi;

    // Data
    private ObservableList<News> mNews;
    private Observable<List<News>> observableRefresherNewsData;
    private Observable<List<News>> observableLoadMoreNewsData;

    public static NewsChannelFragment newInstance(String id, String name) {
        NewsChannelFragment fragment = new NewsChannelFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_ID, id);
        bundle.putString(BUNDLE_NAME, name);
        fragment.setArguments(bundle);
        return fragment;
//        return newInstance(context, ChannelFragment.class.getName(), new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int inflateLayout() {
        return R.layout.fragment_channel;
    }

    @Override
    protected void initialization() {
        this.initData();
        this.initDBData();
        this.initObservables();
        this.initView();
        this.setExitSharedElementCallback();
    }

    private void initData() {
        mCurrPage = 1;
        mNews = new ObservableArrayList<>();
    }

    private void initDBData() {
        getApp().getDB().getList(Const.DB_NEWS_NAME, News.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RxList.appendTo(mNews), e -> e.printStackTrace());
    }

    private void initView() {
        NewsAdapter adapter = new NewsAdapter(getActivity(), mNews, Glide.with(this));
        adapter.setOnItemClickListener(this);
        getBinding().content.setAdapter(adapter);
    }

    private void initObservables() {
        Observable.Transformer<List<News>, List<News>> networkingIndicator = RxNetworking.bindRefreshing(getBinding().refresher);

        observableRefresherNewsData = Observable.defer(() -> mNewApi.queryNewsByCName(getArguments().getString(BUNDLE_NAME), 1))
                .doOnUnsubscribe(() -> this.unsubcribe("observableNewsData"))
                .flatMap(data -> Observable.just(data.contentlist))
                .flatMap(list -> getApp().getDB().putList(Const.DB_NEWS_NAME, list, News.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(networkingIndicator);

        observableLoadMoreNewsData = Observable.defer(() -> mNewApi.queryNewsByCName(getArguments().getString(BUNDLE_NAME), mCurrPage + 1))
                .doOnUnsubscribe(() -> this.unsubcribe("observableNewsData"))
                .map(data -> {
                    mCurrPage = data.currentPage;
                    return data.contentlist;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(networkingIndicator);

        // 刷新/加载更多
        RxSwipeRefreshLayout.refreshes(getBinding().refresher)
                .doOnUnsubscribe(() -> this.unsubcribe("SwipeRefreshLayout"))
                .flatMap(avoid -> observableRefresherNewsData)
                .compose(bindToLifecycle())
                .subscribe(RxList.prependTo(mNews, getBinding().content), this::showError);

        RxEndlessRecyclerView.reachesEnd(getBinding().content)
                .doOnUnsubscribe(() -> this.unsubcribe("Recycler"))
                .flatMap(avoid -> observableLoadMoreNewsData)
                .compose(bindToLifecycle())
                .subscribe(RxList.appendTo(mNews), this::showError);

        // 首次进入手动加载
        observableRefresherNewsData
                .map(list -> {
                    mNews.clear();
                    return list;
                })
                .compose(bindToLifecycle())
                .subscribe(RxList.prependTo(mNews, getBinding().content), this::showError);

    }

    @Override
    protected void createApi(ImApp app) {
        mNewApi = app.createCoreApi(NewsApi.class);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return getArguments().getString(BUNDLE_NAME);
    }

    /**
     *
     * @return
     */
    public String getID() {
        return getArguments().getString(BUNDLE_ID);
    }

    private <T> Observable.Transformer<T, T> unsubcribe(String msg) {
        return observable -> observable
                .doOnSubscribe(() -> LogUtils.d(TAG, "unsubscribe " + msg))
                .doOnSubscribe(() -> getBinding().refresher.setRefreshing(false));
    }

    private void showError(Throwable error) {
        LogUtils.e(TAG, getString(R.string.error), error);
        if (error instanceof UnknownHostException) {
            View root = ((MainActivity) getActivity()).getBinding().coordinator;
            Snackbar.make(root, getString(R.string.error_network), Snackbar.LENGTH_LONG)
                    .setAction(R.string.setting, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openNetSetting();
                        }
                    }).show();
        } else {
            showMsgLong(getString(R.string.error_loading));
        }
    }

    /**
     * snack show an short message
     * @param msg
     */
    protected void showMsgShort(String msg) {
        showMsg(msg, Snackbar.LENGTH_SHORT);
    }

    /**
     * snack show an long message
     * @param msg
     */
    protected void showMsgLong(String msg) {
        showMsg(msg, Snackbar.LENGTH_SHORT);
    }

    private void showMsg(String msg, int duration) {
        View root = ((MainActivity) getActivity()).getBinding().coordinator;
        Snackbar.make(root, msg, duration).show();
    }

    /**
     * 打开网络设置
     */
    private void openNetSetting() {
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    private void setExitSharedElementCallback() {
//        ActivityCompat.setExitSharedElementCallback(getActivity(), new SharedElementCallback() {
//            @Override
//            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
////                Bundle reenterState = ((MainActivity) getActivity()).getReenterState();
////                if (reenterState != null) {
////                    int index = reenterState.getInt(ViewerActivity.INTENT_INDEX, 0);
////                    int position = reenterState.getInt(ViewerActivity.INTENT_POSTTION, 0);
////                    Image image = reenterState.getParcelable(ViewerActivity.INTENT_IMAGES);
////                    sharedElements.clear();
////                    names.clear();
////
////                    // set shared elements
////                    NewsAdapter.ViewHolder holder = (NewsAdapter.ViewHolder) getBinding().content.findViewHolderForLayoutPosition(position);
////                    if (holder != null && holder.binding != null) {
////                        ImageView iv = null;
////                        if (index == 0) {
////                            if (holder.binding.image.getVisibility() == View.GONE) {
////                                iv = holder.binding.imageOne;
////                            } else {
////                                iv = holder.binding.image;
////                            }
////                        } else if (index == 1) {
////                            iv = holder.binding.imageTwo;
////                        } else if (index == 2) {
////                            iv = holder.binding.imageThree;
////                        }
////                        names.add(String.format("%s.image", image.url));
////                        sharedElements.put(String.format("%s.image", image.url), iv);
////                    }
////                    ((MainActivity) getActivity()).setReenterState(null);
////                }
//                super.onMapSharedElements(names, sharedElements);
//            }
//        });
    }

    @Override
    public void onItemClick(NewsAdapter.ViewHolder holder, News news) {
        Intent intent = new Intent(getContext(), WebActivity.class);
        intent.putExtra(WebActivity.INTENT_URL, news.link);
        startActivity(intent);
    }

    @Override
    public void onImageClick(NewsAdapter.ViewHolder holder, ImageView iv, Image[] images, int index) {
        Intent intent = new Intent(getContext(), ViewerActivity.class);
        intent.putExtra(ViewerActivity.INTENT_POSTTION, holder.getAdapterPosition());
        intent.putExtra(ViewerActivity.INTENT_INDEX, index);
        intent.putExtra(ViewerActivity.INTENT_IMAGES, images);
        // 兼容旧版本
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), iv, String.format("%s.image", images[index].url));
        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }

    @Override
    public void onPause() {
        getBinding().refresher.setRefreshing(false);
        super.onPause();
    }
}
