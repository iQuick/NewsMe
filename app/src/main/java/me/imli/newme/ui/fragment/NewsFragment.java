package me.imli.newme.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.FragmentNewsBinding;
import me.imli.newme.model.Channel;
import me.imli.newme.ui.CustomNewsActivity;
import me.imli.newme.ui.adapter.ChannelPagerAdapter;
import me.imli.newme.ui.base.BaseChanneFragment;
import me.imli.newme.utils.LogUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/12/9.
 */
public class NewsFragment extends BaseChanneFragment<FragmentNewsBinding> {

    /**
     * TAG
     */
    public static final String TAG = "NewsFragment";
    private static final int REQUEST_CODE = 0x1001;

    private ChannelPagerAdapter adapter;

    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int inflateLayout() {
        return R.layout.fragment_news;
    }

    @Override
    protected void initialization() {
        this.setHasOptionsMenu(true);
        this.initViewPager();
        this.updateView();
    }

    private void initViewPager() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        adapter = new ChannelPagerAdapter(getFragmentManager(), getContext());
        getBinding().pager.setOffscreenPageLimit(5);
        getBinding().pager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        getBinding().slidingTabs.setupWithViewPager(getBinding().pager);
    }

    @Override
    protected void createApi(ImApp app) {
    }

    /**
     * 到 Channel 定义页面
     */
    private void doGoCustom() {
        Intent intent = new Intent(getContext(), CustomNewsActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 更新页面
     */
    private void updateView() {
        getApp().getDB().getList(Const.DB_NEWS_CHANNEL, Channel.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(channels -> {
                    adapter.onChange(channels);
                    getBinding().slidingTabs.setupWithViewPager(getBinding().pager);
                }, e -> LogUtils.e(TAG, "update channel error", e));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())  {
            case R.id.action_custom:
                doGoCustom();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    updateView();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStopRefresh() {

    }
}
