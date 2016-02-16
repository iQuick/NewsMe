package me.imli.newme.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.transition.Transition;
import android.view.View;

import java.util.List;
import java.util.Map;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.ActivityViewerBinding;
import me.imli.newme.listener.SimpleTransitionListener;
import me.imli.newme.model.Image;
import me.imli.newme.ui.adapter.ViewerAdapter;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.ui.fragment.ViewerFragment;
import me.imli.newme.utils.ImmersiveUtil;
import ooo.oxo.library.widget.PullBackLayout;

/**
 * Created by Em on 2015/12/2.
 */
public class ViewerActivity extends BaseActivity<ActivityViewerBinding> implements PullBackLayout.Callback {

    // TAG
    private final static String TAG = "ViewerActivity";

    public final static String INTENT_POSTTION = "position";
    public final static String INTENT_INDEX = "index";
    public final static String INTENT_IMAGES = "thumbnail";

    private Image[] mImages = null;

    private ViewerAdapter mAdapter;
    private ColorDrawable mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int inflateLayout() {
        return R.layout.activity_viewer;
    }

    @Override
    protected void initialization() {
        this.initActionBar();
        this.initView();
        this.initPagerVier();
        this.initEnterSharedElement();
    }

    private void initActionBar() {
        setTitle(null);
        setSupportActionBar(getBinding().toolbar);
        getBinding().toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        Parcelable[] pars = getIntent().getParcelableArrayExtra(INTENT_IMAGES);
        mImages = new Image[pars.length] ;
        for (int i = 0; i < pars.length; i++) {
            mImages[i] = (Image) pars[i];
        }
        getBinding().puller.setCallback(this);
        supportPostponeEnterTransition();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new SimpleTransitionListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onTransitionEnd(Transition transition) {
                    getWindow().getEnterTransition().removeListener(this);
                    fadeIn();
                }
            });
        } else {
            fadeIn();
        }

        mBackground = new ColorDrawable(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getBinding().getRoot().setBackground(mBackground);
        } else {
            getBinding().getRoot().setBackgroundDrawable(mBackground);
        }

        setLocImages(getIntent().getIntExtra(INTENT_INDEX, 0) + 1);
    }

    private void initPagerVier() {
        mAdapter = new ViewerAdapter(getSupportFragmentManager(), this, mImages);
        getBinding().pager.setAdapter(mAdapter);
        getBinding().pager.setCurrentItem(getIntent().getIntExtra(INTENT_INDEX, 0));
        getBinding().pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    fadeOut();
                }
            }

            @Override
            public void onPageSelected(int position) {
                setLocImages(position + 1);
                super.onPageSelected(position);
            }
        });
    }

    private void initEnterSharedElement() {
        ActivityCompat.setEnterSharedElementCallback(this, new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Image image = mImages[getBinding().pager.getCurrentItem()];
                sharedElements.clear();
                sharedElements.put(String.format("%s.image", image.url), getCurrent().getSharedElement());
            }
        });
    }

    @Override
    protected void createApi(ImApp app) {
    }

    private void setLocImages(int index) {
        if (mImages.length <= 1) {
            getBinding().locImages.setVisibility(View.GONE);
        } else {
            getBinding().locImages.setVisibility(View.VISIBLE);
            getBinding().locImages.setText(index + " / " + mImages.length);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void fadeIn() {
        getBinding().toolbar.fadeIn();
        showSystemUi();
    }

    public void fadeOut() {
        getBinding().toolbar.fadeOut();
        hideSystemUi();
    }

    public void toggleFade() {
        if (getBinding().toolbar.getAlpha() == 0) {
            fadeIn();
        } else {
            fadeOut();
        }
    }

    private void showSystemUi() {
        ImmersiveUtil.exit(getBinding().getRoot());
    }

    private void hideSystemUi() {
        ImmersiveUtil.enter(getBinding().getRoot());
    }

    @Override
    public void onPullStart() {
        fadeOut();
        showSystemUi();
    }

    @Override
    public void onPull(float progress) {
        progress = Math.min(1f, progress * 3f);
        mBackground.setAlpha((int) (0xff * (1f - progress)));
    }

    @Override
    public void onPullCancel() {
        fadeIn();
    }

    @Override
    public void onPullComplete() {
        supportFinishAfterTransition();
    }

    public ViewerFragment getCurrent() {
        return (ViewerFragment) mAdapter.instantiateItem(getBinding().pager, getBinding().pager.getCurrentItem());
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(ViewerActivity.INTENT_POSTTION, getIntent().getIntExtra(ViewerActivity.INTENT_POSTTION, 0));
        data.putExtra(ViewerActivity.INTENT_INDEX, getBinding().pager.getCurrentItem());
        data.putExtra(ViewerActivity.INTENT_IMAGES, mImages[getBinding().pager.getCurrentItem()]);
        setResult(RESULT_OK, data);
        showSystemUi();

        super.supportFinishAfterTransition();
    }


//    private final ObservableList.OnListChangedCallback<ObservableList<Image>> listener =
//            new ObservableList.OnListChangedCallback<ObservableList<Image>>() {
//                @Override
//                public void onChanged(ObservableList<Image> sender) {
//                    mAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onItemRangeChanged(ObservableList<Image> sender, int positionStart, int itemCount) {
//                    mAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onItemRangeInserted(ObservableList<Image> sender, int positionStart, int itemCount) {
//                    mAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onItemRangeMoved(ObservableList<Image> sender, int fromPosition, int toPosition, int itemCount) {
//                    mAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onItemRangeRemoved(ObservableList<Image> sender, int positionStart, int itemCount) {
//                    mAdapter.notifyDataSetChanged();
//                }
//            };

}
