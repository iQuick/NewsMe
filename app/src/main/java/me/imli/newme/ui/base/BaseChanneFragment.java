package me.imli.newme.ui.base;

import android.databinding.ViewDataBinding;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

import me.imli.newme.ui.MainActivity;

/**
 * Created by Em on 2015/12/9.
 */
public abstract class BaseChanneFragment<V extends ViewDataBinding> extends BaseFragment<V> {


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
        View root = ((MainActivity) getActivity()).getBinding().coordinator;
        Snackbar.make(root, msg, duration).show();
    }

    @Override
    public void onPause() {
        onStopRefresh();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            onStopRefresh();
        }
        super.onHiddenChanged(hidden);
    }

    protected abstract void onStopRefresh();

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }


}
