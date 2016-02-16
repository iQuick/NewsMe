package me.imli.newme.ui;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.databinding.ActivitySettingBinding;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.ui.fragment.SettingFragment;
import me.imli.newme.widget.sb.SwipeBackLayout;

/**
 * Created by Em on 2015/12/9.
 */
public class SettingActivity extends BaseActivity<ActivitySettingBinding> implements SwipeBackLayout.OnSwipeBackListener {

    @Override
    protected int inflateLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initialization() {
        this.initSwipeBack();
        this.initActionBar();
        this.initFragment();
    }

    private void initSwipeBack() {
        getBinding().swipe.setOnSwipeBackListener(this);
    }

    private void initActionBar() {
        setSupportActionBar(getBinding().toolbar);
        getBinding().toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());
    }

    private void initFragment() {
        getFragmentManager().beginTransaction().replace(R.id.content, SettingFragment.newInstance()).commit();
    }

    @Override
    protected void createApi(ImApp app) {
    }

    @Override
    public void onBack() {
        onBackPressed();
        overridePendingTransition(0, R.anim.push_f_right_out);
    }
}
