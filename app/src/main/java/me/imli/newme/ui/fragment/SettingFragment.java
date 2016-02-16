package me.imli.newme.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.qq.e.ads.appwall.APPWall;
import com.squareup.okhttp.Request;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.imli.newme.BuildConfig;
import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.listener.SexSelectedListener;
import me.imli.newme.model.Version;
import me.imli.newme.okhttp.callback.FileCallBack;
import me.imli.newme.rx.RxCache;
import me.imli.newme.rx.RxWaiting;
import me.imli.newme.ui.FeedbackActivity;
import me.imli.newme.ui.SettingActivity;
import me.imli.newme.utils.ApkUtils;
import me.imli.newme.utils.DialogUtils;
import me.imli.newme.utils.LogUtils;
import me.imli.newme.utils.MarketUtils;
import me.imli.newme.utils.SexUtil;
import me.imli.newme.utils.SharedPrefUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/12/17.
 */
public class SettingFragment extends PreferenceFragment {

    private static final String TAG = "SettingFragment";

    // App
    private ImApp app;

    private Observable<String> observableCache;

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefUtil.SHARED_PRE_NAME);
        addPreferencesFromResource(R.xml.setting);
        initialization();
        initObservable();
    }

    private void initialization() {
        app = ImApp.from(getActivity());
        Preference version = findPreference(getString(R.string.pre_version));
        Preference cache = findPreference(getString(R.string.pre_clear_cache));
        RxCache.getCacheSize(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cache::setSummary, e -> e.printStackTrace());
        version.setTitle(String.format(getString(R.string.version), BuildConfig.VERSION_NAME, ""));
    }


    private void initObservable() {
        observableCache = RxCache.clearCache(getActivity())
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxWaiting.bindWaiting(getWaitDialog()));
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.clear_cache_tip)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        observableCache.subscribe(result -> {
                            LogUtils.d(TAG, getString(R.string.success_clear_cache));
                            showMsgShort(getString(R.string.success_clear_cache));
                            setCacheSize(result);
                        }, e -> {
                            LogUtils.e(TAG, getString(R.string.error), e);
                            showMsgShort(getString(R.string.error_clear_cache));
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void setCacheSize(String size) {
        findPreference(getString(R.string.pre_clear_cache)).setSummary(size);
    }

    private Dialog getWaitDialog() {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getString(R.string.please_wait));
        return dialog;
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
        View root = ((SettingActivity) getActivity()).getBinding().coordinator;
        Snackbar.make(root, msg, duration).show();
    }

    /**
     * 分享
     */
    private void shareFriend() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(getString(R.string.share_type));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.share_title));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 评分
     */
    private void soureApp() {
//        // 打印已安装应用包名
//        for (PackageInfo info : getActivity().getPackageManager().getInstalledPackages(PackageManager.MATCH_ALL)) {
//            LogUtils.d("aaaa", "info " + info.toString() + " name: " + info.applicationInfo.loadLabel(getActivity().getPackageManager()) + " pname: " + info.applicationInfo.packageName);
//        }

        final ChoiceOnClickListener listener = new ChoiceOnClickListener();
        final List<ApplicationInfo> infos = MarketUtils.filterInstalledPkgs(getActivity());
        String[] appNames = new String[infos.size()];
        for (int i = 0; i < appNames.length; i++) {
            appNames[i] = infos.get(i).loadLabel(getActivity().getPackageManager()) + "";
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.please_select)
                .setSingleChoiceItems(appNames, 0, listener)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MarketUtils.launchAppDetail(getActivity(), BuildConfig.APPLICATION_ID, infos.get(listener.getWhich()).packageName);
                    }
                })
                .create()
                .show();
    }

    /**
     * 反馈
     */
    private void feedback() {
        Intent intent = new Intent(getActivity(), FeedbackActivity.class);
        startActivity(intent);
    }

    private void seleteSex(CheckBoxPreference preference) {
        if (preference.isChecked() && !SexUtil.isSelectedSex(getActivity())) {
            SexUtil.selectSex(getActivity(), new SexSelectedListener() {
                @Override
                public void onSelect(boolean select) {
                    if (!select) {
                        preference.setChecked(false);
                    }
                }
            });
        }
    }

    /**
     *
     */
    private void version() {
        app.getDB().get(Const.DB_NEWSME_VERSION, Version.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(version -> {
                    doCheckUpdate(version);
                }, e -> e.printStackTrace());
    }

    /**
     * 检查更新
     * @param version
     */
    private void doCheckUpdate(Version version) {
        try {
            if (BuildConfig.VERSION_CODE < Integer.valueOf(version.build)) {
                doShowUpdateTip(version);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示升级对话框
     */
    private void doShowUpdateTip(final Version version) {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.tip_update)
                .setMessage(getString(R.string.tip_msg_update) + "\n" + version.changelog)
                .setNegativeButton(getString(R.string.cancel), cancelClick(version))
                .setPositiveButton(getString(R.string.confirm), confirmClick(version.install_url))
                .create()
                .show();
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

    /**
     * 打开应用墙
     */
    private void appWall() {
        APPWall wall = new APPWall(getActivity(), Const.AD_APP_ID, Const.AD_W_ID);
        wall.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        wall.doShowAppWall();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (getString(R.string.pre_clear_cache).equals(key)) {
            clearCache();
        } else if (getString(R.string.pre_reputation).equals(key)) {
            soureApp();
        } else if (getString(R.string.pre_share).equals(key)) {
            shareFriend();
        } else if (getString(R.string.pre_feedback).equals(key)) {
            feedback();
        } else if (getString(R.string.pre_version).equals(key)) {
            version();
        } else if (getString(R.string.pre_hid_benefits).equals(key)) {
            seleteSex((CheckBoxPreference) preference);
        } else if (getString(R.string.pre_app_wall).equals(key)) {
            appWall();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     *
     */
    private static class ChoiceOnClickListener implements DialogInterface.OnClickListener {

        private int which = 0;
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            this.which = which;
        }

        public int getWhich() {
            return which;
        }
    }
}
