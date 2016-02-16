package me.imli.newme.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.BannerView;
import com.squareup.okhttp.Request;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.imli.newme.BuildConfig;
import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.api.ApiConst;
import me.imli.newme.api.FirApi;
import me.imli.newme.databinding.ActivityMainBinding;
import me.imli.newme.model.Image;
import me.imli.newme.model.Version;
import me.imli.newme.okhttp.callback.FileCallBack;
import me.imli.newme.rx.RxCache;
import me.imli.newme.ui.ad.WBBannerADListener;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.ui.base.BaseChanneFragment;
import me.imli.newme.ui.fragment.JokeFragment;
import me.imli.newme.ui.fragment.MeiziFragment;
import me.imli.newme.ui.fragment.NewsFragment;
import me.imli.newme.ui.fragment.VideoFragment;
import me.imli.newme.utils.ApkUtils;
import me.imli.newme.utils.DialogUtils;
import me.imli.newme.utils.LogUtils;
import me.imli.newme.utils.SharedPrefUtil;
import me.imli.newme.utils.ThemeUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "MainActivity";

    // ReenterState
    private Bundle mReenterState;

    // Fragments
    private String mCurrFragmentTag = null;
    private Map<String, BaseChanneFragment> mFragments = new HashMap<>();

    // API
    private FirApi apiFir;
    private Observable<Version> observableVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int inflateLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initialization() {
        this.initActionBar();
        this.initFragment();
        this.initObservables();
        this.initAd();
    }


    private void initActionBar() {
        setSupportActionBar(getBinding().toolbar);

        DrawerLayout drawer = getBinding().drawLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, getBinding().toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        getBinding().navigationView.setNavigationItemSelectedListener(this);
    }

    private void initFragment() {
        mFragments.put(NewsFragment.TAG, NewsFragment.newInstance());
        mFragments.put(JokeFragment.TAG, JokeFragment.newInstance());
        mFragments.put(VideoFragment.TAG, VideoFragment.newInstance());
        mFragments.put(MeiziFragment.TAG, MeiziFragment.newInstance());

        // init first fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().add(R.id.content, mFragments.get(NewsFragment.TAG), NewsFragment.TAG);
        transaction.show(mFragments.get(NewsFragment.TAG)).commit();
        getBinding().navigationView.setCheckedItem(R.id.nav_news);
        mCurrFragmentTag = NewsFragment.TAG;
    }

    private void initObservables() {
        observableVersion = Observable.defer(() -> apiFir.latest(ApiConst.FIR_ME_TOKEN))
                .flatMap(version -> getApp().getDB().put(Const.DB_NEWSME_VERSION, version));

        observableVersion
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(version -> {
                    LogUtils.d(TAG, version.toString());
                    // 自动检查更新
                    boolean isCheckUpdate = SharedPrefUtil.getBoolean(getActivity(), getString(R.string.pre_check_update), true);
                    if (isCheckUpdate || isForceUpdate(version)) {
                        this.doCheckUpdate(version);
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    protected void createApi(ImApp app) {
        apiFir = app.createFirAai(FirApi.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCurrFragmentTag != null && mCurrFragmentTag.equals(VideoFragment.TAG)) {
            VideoFragment videoFragment = (VideoFragment) getFragment(mCurrFragmentTag);
            if (videoFragment.holdGoBack()) {
                return videoFragment.onBack(event);
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.draw_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public Bundle getReenterState() {
        return mReenterState;
    }

    public void setReenterState(Bundle bundle) {
        mReenterState = bundle;
    }

    private Fragment getFragment(String tag) {
        return mFragments.get(tag);
    }

    private void doOpenSettingActivity() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void doOpenAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        // 兼容旧版本
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), getOpenAboutShareView(), AboutActivity.SHARE_IMAGE);
        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }

    private View getOpenAboutShareView() {
        return getBinding().navigationView.findViewById(R.id.image);
    }

    /**
     * 切换 Channel
     * @param from
     * @param to
     * @param tag
     */
    private void doSwitchChannel(Fragment from, Fragment to, String tag) {
        getBinding().drawLayout.closeDrawer(Gravity.LEFT);
        doSwitchFragment(from, to, tag);
    }

    /**
     * 切换 Fragment
     * @param to
     * @param tag
     */
    private void doSwitchFragment(Fragment from, Fragment to, String tag) {
        mCurrFragmentTag = tag;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!to.isAdded()) {    // 先判断是否被add过
            transaction.hide(from).add(R.id.content, to, tag).commit(); // 隐藏当前的fragment，add下一个到Activity中
        } else {
            transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
    }


    private void doCheckUpdate(Version version) {
        try {
            if (BuildConfig.VERSION_CODE < Integer.valueOf(version.build)) {
                doShowUpdateTip(version);
            } else {
//                new AlertDialog.Builder(getActivity())
//                        .setTitle(R.string.tip)
//                        .setMessage(R.string.no_update)
//                        .setPositiveButton(R.string.confirm, null)
//                        .create()
//                        .show();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示升级对话框
     */
    private void doShowUpdateTip(final Version version) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.tip_update)
                .setMessage(getString(R.string.tip_msg_update) + "\n" + version.changelog)
                .setNegativeButton(getString(R.string.cancel), cancelClick(version))
                .setPositiveButton(getString(R.string.confirm), confirmClick(version.install_url))
                .create()
                .show();
    }

    /**
     * 是否强制升级
     * @param version
     * @return
     */
    private boolean isForceUpdate(Version version) {
        int currVersion = Integer.valueOf(BuildConfig.VERSION_NAME.substring(0, BuildConfig.VERSION_NAME.indexOf(".")));
        int netVersion = Integer.valueOf(version.versionShort.substring(0, version.versionShort.indexOf(".")));
        if (netVersion - currVersion >= 1) {
            return true;
        }
        return false;
    }

    /**
     * 取消升级，如果当前版本与最新版本相隔一个版本的话，则强制升级
     * 取消则关闭应用
     * @return
     */
    private DialogInterface.OnClickListener cancelClick(final Version version) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isForceUpdate(version)) {
                    finish();
                }
            }
        };
    }

    /**
     * 确认升级操作
     * @return
     */
    private DialogInterface.OnClickListener confirmClick(String download) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ProgressDialog progressDialog = DialogUtils.getHorizontalProgressDialog(getActivity());
                progressDialog.setCancelable(false);
                progressDialog.show();

                ApkUtils.downloadApk(download, new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Const.EX_FILE_DIRECTORY, Const.NEWSME_APK) {
                    @Override
                    public void inProgress(float progress) {
                        int pro = (int) (progress * 100);
                        progressDialog.setProgress(pro);
                    }

                    @Override
                    public void onError(Request request, Exception e) {
                        LogUtils.d(TAG, getString(R.string.error), e);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(File response) {
                        progressDialog.dismiss();
                        ApkUtils.installApk(getActivity(), response);
                    }
                });
            }
        };
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        data.setExtrasClassLoader(Image.class.getClassLoader());
        mReenterState = new Bundle(data.getExtras());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_news:
                doSwitchChannel(getFragment(mCurrFragmentTag), getFragment(NewsFragment.TAG), NewsFragment.TAG);
                return true;
            case R.id.nav_jack:
                doSwitchChannel(getFragment(mCurrFragmentTag), getFragment(JokeFragment.TAG), JokeFragment.TAG);
                return true;
            case R.id.nav_meizi:
//                DialogUtils.doShowTip(MainActivity.this, getString(R.string.tip), getString(R.string.tip_msg_construction));
                doSwitchChannel(getFragment(mCurrFragmentTag), getFragment(MeiziFragment.TAG), MeiziFragment.TAG);
                return true;
            case R.id.nav_video:
//                DialogUtils.doShowTip(getString(R.string.tip), getString(R.string.tip_msg_construction));
                doSwitchChannel(getFragment(mCurrFragmentTag), getFragment(VideoFragment.TAG), VideoFragment.TAG);
                return true;
            case R.id.nav_theme:
                ThemeUtils.changeTheme(this);
                return true;
            case R.id.nav_setting:
                doOpenSettingActivity();
                return true;
            case R.id.nav_about:
                doOpenAboutActivity();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isShowFuli = SharedPrefUtil.getBoolean(getActivity(), getString(R.string.pre_hid_benefits), false);
        getBinding().navigationView.getMenu().findItem(R.id.nav_meizi).setVisible(isShowFuli);
        if (!isShowFuli && mCurrFragmentTag.equals(MeiziFragment.TAG)) {
            getBinding().navigationView.setCheckedItem(R.id.nav_news);
            doSwitchFragment(getFragment(mCurrFragmentTag), getFragment(NewsFragment.TAG), NewsFragment.TAG);
        }
    }


    @Override
    protected void onDestroy() {
        boolean isClear = SharedPrefUtil.getBoolean(getActivity(), getString(R.string.pre_app_out_clear), false);
        LogUtils.d(TAG, "is clear the cache : " + isClear);
        if (isClear) {
            RxCache.clearCache(getActivity())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .subscribe(is -> LogUtils.d(TAG, "clear cache succes, size : " + is), e -> e.printStackTrace());
        }
        super.onDestroy();
    }

    // =========================== AD =====================
    private static final int LOAD_AD_lINTERVAL_TIEM = 20;
    private ADHandler mHandler;
    private BannerView adv;
    private void initAd() {
        mHandler = new ADHandler(getMainLooper());
        addAdView();
    }

    private void addAdView() {
        adv = new BannerView(this, ADSize.BANNER, Const.AD_APP_ID, Const.AD_B_ID);
        getBinding().adLayout.addView(adv);
        adv.setRefresh(30);
        adv.setADListener(new WBBannerADListener() {
            @Override
            public void onADClosed() {
                super.onADClosed();
                restartAD();
            }
        });
        loadAd();
    }

    private void loadAd() {
        adv.loadAD();
    }

    private void restartAD() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getBinding().adLayout.removeAllViews();
                addAdView();
            }
        }, LOAD_AD_lINTERVAL_TIEM * 1000);
    }

    /**
     *
     */
    private class ADHandler extends Handler {
        public ADHandler(Looper loop) {
            super(loop);
        }

        @Override
        public void handleMessage(Message msg) {
        }
    }

}
