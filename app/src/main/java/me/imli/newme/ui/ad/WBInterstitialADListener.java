package me.imli.newme.ui.ad;

import com.qq.e.ads.interstitial.AbstractInterstitialADListener;

import me.imli.newme.utils.LogUtils;


/**
 * Created by Em on 2016/1/21.
 */
public class WBInterstitialADListener extends AbstractInterstitialADListener {

    private static final String TAG = "WBInterstitialADListener";

    @Override
    public void onADClicked() {
        super.onADClicked();
        LogUtils.d(TAG, "The ad is clicked!");
    }

    @Override
    public void onADReceive() {
        LogUtils.i(TAG, "The ad load success!");
    }

    @Override
    public void onNoAD(int i) {
        LogUtils.w(TAG, "The ad load error, error_code: " + i);
    }
}
