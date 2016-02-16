package me.imli.newme.ui.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.support.RxFragment;
import com.umeng.analytics.MobclickAgent;

import me.imli.newme.ImApp;

/**
 * Created by Em on 2015/11/26.
 */
public abstract class BaseFragment<V extends ViewDataBinding> extends RxFragment {

    // Application
    private ImApp mImApp;

    // Binding
    private V binding;

    protected abstract int inflateLayout();

    protected abstract void initialization();

    protected abstract void createApi(ImApp app);

    /**
     *
     * @return
     */
    public V getBinding() {
        return binding;
    }

    public ImApp getApp() {
        return mImApp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mImApp = ImApp.from(getActivity());
    }

    @Nullable
    public V onCreateBinding(LayoutInflater inflater, @Nullable ViewGroup container,  @Nullable Bundle savedInstanceState) {
        return DataBindingUtil.inflate(inflater, inflateLayout(), container, false);
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = onCreateBinding(inflater, container, savedInstanceState);
        return binding == null ? null : binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.createApi(mImApp);
        this.initialization();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
    }

}
