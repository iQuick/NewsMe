package me.imli.newme.ui;

import android.graphics.Bitmap;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.ActivityWebBinding;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.utils.LogUtils;
import me.imli.newme.widget.sb.SwipeBackLayout;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Em on 2015/12/7.
 */
public class WebActivity extends BaseActivity<ActivityWebBinding> implements SwipeBackLayout.OnSwipeBackListener {

    // TAG
    private static final String TAG = "WebActivity";

    // INTENT
    public static final String INTENT_URL = "url";
    public static final String INTENT_TITLE = "title";

    private String mTitle;

    private MenuItem mRefreshMenu;

    @Override
    protected int inflateLayout() {
        return R.layout.activity_web;
    }

    @Override
    protected void initialization() {
        this.initActionBar();
        this.initData();
        this.initView();
    }

    private void initActionBar() {
        setSupportActionBar(getBinding().toolbar);
        getBinding().toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
    }

    private void initData() {
        mTitle = getIntent().getStringExtra(INTENT_TITLE);
    }

    private void initView() {
        getBinding().swipe.setOnSwipeBackListener(this);
        // WebView
        WebSettings webSettings = getBinding().web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setDefaultFontSize(18);
        getBinding().web.setWebViewClient(new MeWebViewClient());
        getBinding().web.setWebChromeClient(new MeWebChromeClient());
        getBinding().web.loadUrl(getIntent().getStringExtra(INTENT_URL));

        // ProgressBar

        // Title
        if (mTitle != null && !mTitle.equals("")) {
            getBinding().toolbar.setTitle(mTitle);
        } else {
            getBinding().toolbar.setTitle(R.string.loading);
        }
    }

    @Override
    protected void createApi(ImApp app) {
        RxSwipeRefreshLayout.refreshes(getBinding().refresher)
                .doOnUnsubscribe(() -> unsubcribe("refresher"))
                .observeOn(AndroidSchedulers.mainThread())
                .map(avoid -> {
                    doRefresh();
                    return null;
                })
                .compose(bindToLifecycle())
                .subscribe(avoid -> LogUtils.d(TAG, "refresher success"), e -> e.printStackTrace());
    }

    private void setRefreshing(boolean refreshing) {
//        if (mRefreshMenu == null) return;
//        if (refreshing)
//            mRefreshMenu.setActionView(R.layout.actionbar_refresh_progress);
//        else
//            mRefreshMenu.setActionView(null);
    }

    /**
     * WebView 重载
     */
    private void doRefresh() {
        setRefreshing(true);
        getBinding().web.reload();
    }

    /**
     * WebView back
     */
    private void doBack() {
        if (getBinding().web.canGoBack()) {
            getBinding().web.goBack();
        }
    }

    /**
     * WebView forward
     */
    private void doForward() {
        if (getBinding().web.canGoForward()) {
            getBinding().web.goForward();
        }
    }


    private <T> Observable.Transformer<T, T> unsubcribe(String msg) {
        return observable -> observable
                .doOnSubscribe(() -> LogUtils.d(TAG, "unsubscribe " + msg))
                .doOnSubscribe(() -> getBinding().refresher.setRefreshing(false));
    }

    /**
     * WebViewClient
     */
    private class MeWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.indexOf("tel:") < 0){//页面上有数字会导致连接电话
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            getBinding().pb.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            getBinding().pb.setVisibility(View.GONE);
            getBinding().refresher.setRefreshing(false);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            getBinding().pb.setVisibility(View.GONE);
            getBinding().refresher.setRefreshing(false);
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
    }

    /**
     * WebChromeClient
     */
    private class MeWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress < 100 && newProgress >= 0) {
                getBinding().pb.setProgress(newProgress);
            } else if (newProgress >= 100) {
                getBinding().pb.setProgress(0);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (getBinding().toolbar.getTitle().equals(getString(R.string.loading))) {
                getBinding().toolbar.setTitle(title);
            }
            super.onReceivedTitle(view, title);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.web, menu);
//        mRefreshMenu = menu.findItem(R.id.action_refresh);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId())  {
//            case R.id.action_refresh:
//                doRefresh();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onBackPressed() {
        if (!getBinding().web.getUrl().equals(getIntent().getStringExtra(INTENT_URL))) {
            doBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBack() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_f_right_in, R.anim.push_f_right_out);
    }

}
