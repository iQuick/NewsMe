package me.imli.newme.ui.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.umeng.analytics.MobclickAgent;

import me.imli.newme.ImApp;
import me.imli.newme.utils.ActivityUtils;
import me.imli.newme.utils.ThemeUtils;

/**
 * Created by Em on 2015/11/26.
 */
public abstract class BaseActivity<V extends ViewDataBinding> extends RxAppCompatActivity {

    // Application
    private ImApp mImApp;

    // DataBinding
    V mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.add(this);
        this.init();
        ThemeUtils.setTheme(this);
        this.mBinding = DataBindingUtil.setContentView(this, inflateLayout());
        this.mImApp = ImApp.from(getActivity());
        this.createApi(mImApp);
        this.initialization();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     *
     * @return
     */
    protected abstract int inflateLayout();

    /**
     * Initialization
     */
    protected abstract void initialization();

    /**
     *
     * @param app
     */
    protected abstract void createApi(ImApp app);


    /**
     *
     * @return
     */
    protected ImApp getApp() {
        return mImApp;
    }

    /**
     *
     * @return
     */
    protected BaseActivity getActivity() {
        return this;
    }

    /**
     *
     * @return
     */
    public V getBinding() {
        return mBinding;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityUtils.remove(this);
    }
}
