package me.imli.newme.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.FragmentViewerBinding;
import me.imli.newme.listener.GlideRequestListenerAdapter;
import me.imli.newme.listener.SimpleTransitionListener;
import me.imli.newme.model.Image;
import me.imli.newme.rx.RxFileInputStream;
import me.imli.newme.rx.RxFiles;
import me.imli.newme.rx.RxGlide;
import me.imli.newme.rx.RxWallpaperManager;
import me.imli.newme.ui.ViewerActivity;
import me.imli.newme.ui.base.BaseFragment;
import me.imli.newme.utils.EnterTransitionCompatUtils;
import me.imli.newme.utils.LogUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/12/2.
 */
public class ViewerFragment extends BaseFragment<FragmentViewerBinding> {

    // TAG
    private static final String TAG = "ViewerFragment";

    private static final String BUNDLE_IMAGE = "image";
    private static final String BUNDLE_THUMBNAIL = "thumbnail";
    private static final String TRANSITION_EXECUTED = "transition_executed";

    // intent data
    private String mThumbnail;
    private Image mImage;

    private boolean hasSharedElementTransition = false;
    private boolean isTransitionExecuted = false;

    private View sharedElement;

    private Observable<File> observableDownload;
    private Observable<File> observableSave;

    private static Observable<File> ensureExternalDirectory(String name) {
        return RxFiles.mkdirsIfNotExists(new File(Environment.getExternalStorageDirectory(), name));
    }

    private static String makeFileName(Image image) {
        LogUtils.d(TAG, image.url);
        int start = image.url.lastIndexOf("/") + "/".length();
        return String.format("newme-%s-%dx%d.jpg", image.url.substring(start), image.width, image.height);
    }

    public static BaseFragment newInstance(Image image, String thumbnail) {
        BaseFragment fragment = new ViewerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_IMAGE, image);
        bundle.putString(BUNDLE_THUMBNAIL, thumbnail);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isTransitionExecuted = savedInstanceState.getBoolean(TRANSITION_EXECUTED, false);
        }
    }

    @Override
    protected int inflateLayout() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void initialization() {
        this.initData();
        this.initObservable();
        this.initView();
    }

    private void initData() {
        mImage = getArguments().getParcelable(BUNDLE_IMAGE);
        mThumbnail = getArguments().getString(BUNDLE_THUMBNAIL);
        hasSharedElementTransition = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(mThumbnail);
        setHasOptionsMenu(true);
    }

    private void initObservable() {
        observableDownload = RxGlide.download(Glide.with(this), mImage.url);
        observableSave = ensureExternalDirectory(Const.EX_DIRECTORY)
                .map(dir -> new File(dir, makeFileName(mImage)))
                .withLatestFrom(observableDownload, (Func2<File, File, Pair<File, File>>) Pair::new)
                .flatMap(pair -> RxFiles.copy(pair.second, pair.first));
    }

    private void initView() {
        getBinding().image.setSingleTapListener(((ViewerActivity) getActivity())::toggleFade);
        getBinding().image.setDoubleTapListener(((ViewerActivity) getActivity())::fadeOut);
        getBinding().setImage(mImage);

        sharedElement = getBinding().thumbnail;

        loadImage();
    }

    @Override
    protected void createApi(ImApp app) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRANSITION_EXECUTED, isTransitionExecuted);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.viewer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                share();
                return true;
            case R.id.save:
                save();
                return true;
            case R.id.set_wallpaper:
                setWallpaper();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public View getSharedElement() {
        return sharedElement;
    }

    private void startPostponedEnterTransition() {
        if (hasSharedElementTransition) {
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadImage() {
        if (hasSharedElementTransition && !isTransitionExecuted) {
            isTransitionExecuted = true;
            loadThumbnail();
            EnterTransitionCompatUtils.addListener(getActivity().getWindow(), new SimpleTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    EnterTransitionCompatUtils.removeListener(getActivity().getWindow(), this);
                    loadFullImage();
                }
            });
        } else {
            loadFullImage();
        }
    }

    private void loadThumbnail() {
        Glide.with(this).load(mThumbnail)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade(0)
                .listener(new GlideRequestListenerAdapter<String, GlideDrawable>() {
                    @Override
                    protected void onComplete() {
                        startPostponedEnterTransition();
                    }
                })
                .into(getBinding().thumbnail);
    }

    private void loadFullImage() {
        Glide.with(this).load(mImage.url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade(0)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new GlideRequestListenerAdapter<String, GlideDrawable>() {
                    @Override
                    protected void onSuccess(GlideDrawable resource) {
                        sharedElement = getBinding().image;
                        fadeInFullImage();
                    }
                })
                .into(getBinding().image);
    }

    private void fadeInFullImage() {
        getBinding().image.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getBinding().thumbnail.setVisibility(View.GONE);
            }
        }).start();
    }

    private void save() {
        observableSave
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(file -> {
                    notifyMediaScanning(file);
                    Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.success_save), Snackbar.LENGTH_LONG).show();
                }, e -> showError(e, getString(R.string.error_save)));
    }

    private void share() {
        observableSave
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(file -> {
                    notifyMediaScanning(file);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jepg");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                }, e -> this.showError(e, getString(R.string.error_share)));
    }

    private void setWallpaper() {
        observableDownload
                .flatMap(RxFileInputStream::create)
                .flatMap(stream -> RxWallpaperManager.setStream(getContext(), stream))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(avoid -> Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.success_set_wallpaper), Snackbar.LENGTH_LONG).show(),
                        e -> this.showError(e, getString(R.string.error_set_wallpaper)));
    }

    private void showError(Throwable e, String msg) {
        LogUtils.e(TAG, getString(R.string.error), e);
        Snackbar.make(getActivity().getWindow().getDecorView(), msg, Snackbar.LENGTH_LONG).show();
    }


    private void notifyMediaScanning(File file) {
        MediaScannerConnection.scanFile(getContext().getApplicationContext(), new String[]{file.getPath()}, null, null);
    }

}
