package me.imli.newme.ui;

import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import me.imli.newme.Const;
import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.ui.base.BaseActivity;
import me.imli.newme.utils.LogUtils;


public class SplashActivity extends BaseActivity implements SplashADListener {

	private static final String TAG = "SplashActivity";

	private final int SPLASH_DISPLAY_LENGHT = 0; //延迟二秒


	@Override
	protected int inflateLayout() {
		return R.layout.activity_splash;
	}

	@Override
	public void initialization() {
		SplashAD plashAD = new SplashAD(this, (ViewGroup) findViewById(R.id.ad_layout), Const.AD_APP_ID, Const.AD_S_ID, this);
	}

	@Override
	protected void createApi(ImApp app) {

	}


	private void gotoCropActivity() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, SPLASH_DISPLAY_LENGHT);
	}

	//防止用户返回键退出APP
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onADDismissed() {
		LogUtils.d(TAG, "onADDismissed");
		gotoCropActivity();
	}

	@Override
	public void onNoAD(int i) {
		LogUtils.i(TAG, "LoadSplashADFail,ecode=" + i);
		gotoCropActivity();
	}

	@Override
	public void onADPresent() {
		LogUtils.d(TAG, "onADPresent");
	}
}
