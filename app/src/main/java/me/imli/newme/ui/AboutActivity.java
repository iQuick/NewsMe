package me.imli.newme.ui;

import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.view.View;

import java.util.List;
import java.util.Map;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.ActivityAboutBinding;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.widget.sb.SwipeBackLayout;

/**
 * Created by Em on 2015/12/9.
 */
public class AboutActivity extends BaseActivity<ActivityAboutBinding> implements SwipeBackLayout.OnSwipeBackListener {

    public static final String SHARE_IMAGE = "share_image";

    @Override
    protected int inflateLayout() {
        return R.layout.activity_about;
    }

    @Override
    protected void initialization() {
        this.initSwipeBack();
        this.initActionBar();
        this.initEnterSharedElement();
    }

    private void initSwipeBack() {
        getBinding().swipe.setOnSwipeBackListener(this);
    }

    private void initActionBar() {
        setSupportActionBar(getBinding().toolbar);
        getBinding().toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
    }

    private void initEnterSharedElement() {
        ActivityCompat.setEnterSharedElementCallback(this, new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                sharedElements.clear();
                sharedElements.put(SHARE_IMAGE, getBinding().image);
            }
        });
    }

    @Override
    protected void createApi(ImApp app) {
    }

    @Override
    public void onBack() {
        onBackPressed();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            overridePendingTransition(0, R.anim.push_f_right_out);
        }
    }
}
