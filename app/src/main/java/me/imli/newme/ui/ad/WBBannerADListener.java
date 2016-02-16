package me.imli.newme.ui.ad;

import com.qq.e.ads.banner.BannerADListener;

import me.imli.newme.utils.LogUtils;


/**
 * Created by Em on 2016/1/26.
 */
public class WBBannerADListener implements BannerADListener {

    private static final String TAG = "WBBannerADListener";

    @Override
    public void onNoAD(int i) {
        LogUtils.d(TAG, "The ad is onNoAD! error code: " + i);
    }

    @Override
    public void onADReceiv() {
        LogUtils.d(TAG, "The ad is onADReceiv!");
    }

    @Override
    public void onADExposure() {
        LogUtils.d(TAG, "The ad is onADExposure!");
    }

    @Override
    public void onADClosed() {
        LogUtils.d(TAG, "The ad is onADClosed!");
    }

    @Override
    public void onADClicked() {
        LogUtils.d(TAG, "The ad is onADClicked!");
    }

    @Override
    public void onADLeftApplication() {
        LogUtils.d(TAG, "The ad is onADLeftApplication!");
    }

    @Override
    public void onADOpenOverlay() {
        LogUtils.d(TAG, "The ad is onADOpenOverlay!");
    }

    @Override
    public void onADCloseOverlay() {
        LogUtils.d(TAG, "The ad is onADCloseOverlay!");
    }
}
