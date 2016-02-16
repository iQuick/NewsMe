package com.sina.sinavideo.coreplayer.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class AnimationToast implements Runnable {

	private static final String TAG = "AnimationToast";
	/**
	 * Show the view or text notification for a short period of time. This time
	 * could be user-definable. This is the default.
	 * 
	 * @see #setDuration
	 */
	public static final int LENGTH_SHORT = 0;

	/**
	 * Show the view or text notification for a long period of time. This time
	 * could be user-definable.
	 * 
	 * @see #setDuration
	 */
	public static final int LENGTH_LONG = 1;

	private final static int PADDING = 8;
	// private final static int DEFAULT_ANIMATION_STYLE =
	// R.style.AnimationToast;

	private int mDuration;
	private PopupWindow mPopToast;
	private View mParent;// 所在的父布局
	private int width = 300;// toast初始宽度
	private int height = 80;// toast初始高度
	private boolean isShowing;// 是否显示
	private TextView mTextView;
	private int mGravity = Gravity.BOTTOM;// 显示位置
	private int mOffSetY = 80;

	public AnimationToast(Context context, View view, int bgResId,
			int textSizeResId) {
		if (view instanceof TextView) {
			mTextView = (TextView) view;
		}
		// mPopToast = new PopupWindow(view, width, height);
		mPopToast = new PopupWindow(view);
		// mPopToast.setAnimationStyle(DEFAULT_ANIMATION_STYLE);
		mPopToast.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		// int drawableBg = context.getResources().getIdentifier(
		// "toast_animation_bg", "drawable", context.getPackageName());
		Resources resources = context.getResources();
		float value = resources.getDisplayMetrics().scaledDensity;
		mTextView.setTextSize(resources.getDimension(textSizeResId) / value);
		mPopToast.setBackgroundDrawable(resources.getDrawable(bgResId));
		mPopToast.setFocusable(false);
		// mPopToast.setOutsideTouchable(true);
	}

	public void show() {
		mPopToast.showAtLocation(mParent, mGravity, 0, mOffSetY);
		isShowing = true;

		// LONG→2000ms SHORT→1000ms
		long duration = mDuration == LENGTH_LONG ? 2000 : 1000;

		mParent.postDelayed(this, duration);
	}

	public void cancel() {
		isShowing = false;
		if (mParent != null) {
			mParent.removeCallbacks(this);
		}
		mPopToast.dismiss();
	}

	/** 用语连续点击弹出Toast，取消callbacks **/
	public void cancel2RemoveCallbacks() {
		if (mParent != null) {
			mParent.removeCallbacks(this);
		}
	}

	/**
	 * Set the view to show.
	 * 
	 * @see #getView
	 */
	public void setView(View view) {
		mPopToast.setContentView(view);
	}

	/**
	 * Return the view.
	 * 
	 * @see #setView
	 */
	public View getView() {
		return mPopToast.getContentView();
	}

	public void setParent(View parent) {
		mParent = parent;
	}

	public View getParent() {
		return mParent;
	}

	/**
	 * Set how long to show the view for.
	 * 
	 * @see #LENGTH_SHORT
	 * @see #LENGTH_LONG
	 */
	public void setDuration(int duration) {
		mDuration = duration;
	}

	/**
	 * Return the duration.
	 * 
	 * @see #setDuration
	 */
	public int getDuration() {
		return mDuration;
	}

	public void setWidth(int w) {
		width = w;
	}

	public void setHeight(int h) {
		height = h;
	}

	/**
	 * Make a standard toast that just contains a text view.
	 * 
	 * @param context
	 *            The context to use. Usually your
	 *            {@link android.app.Application} or
	 *            {@link android.app.Activity} object.
	 * @param text
	 *            The text to show. Can be formatted text.
	 * @param duration
	 *            How long to display the message. Either {@link #LENGTH_SHORT}
	 *            or {@link #LENGTH_LONG}
	 * @param parent
	 *            AnimationToast use a PopupWindow, so need a parent. suggestion
	 *            → using activity.getWindow().getDecorView()
	 * 
	 */
	public static AnimationToast makeText(Context context, CharSequence text,
			int duration, View parent, int bgResId, int textSizeResId) {

		TextView tv = new TextView(context);
		tv.setTextColor(Color.WHITE);
		tv.setGravity(Gravity.CENTER);
		tv.setText(text);
		tv.setPadding(PADDING, PADDING, PADDING, PADDING);
		AnimationToast result = new AnimationToast(context, tv, bgResId,
				textSizeResId);
		result.setParent(parent);
		result.setDuration(duration);

		return result;
	}

	/**
	 * Make a standard toast that just contains a text view with the text from a
	 * resource.
	 * 
	 * @param context
	 *            The context to use. Usually your
	 *            {@link android.app.Application} or
	 *            {@link android.app.Activity} object.
	 * @param resId
	 *            The resource id of the string resource to use. Can be
	 *            formatted text.
	 * @param duration
	 *            How long to display the message. Either {@link #LENGTH_SHORT}
	 *            or {@link #LENGTH_LONG}
	 * @param parent
	 *            AnimationToast use a PopupWindow, so need a parent. suggestion
	 *            → using activity.getWindow().getDecorView()
	 * 
	 * @throws Resources.NotFoundException
	 *             if the resource can't be found.
	 */
	public static AnimationToast makeText(Context context, int resId,
			int duration, View parent, int bgResId, int textSizeResId)
			throws Resources.NotFoundException {
		return makeText(context, context.getResources().getText(resId),
				duration, parent, bgResId, textSizeResId);
	}

	public boolean isShowing() {
		return isShowing;
	}

	@Override
	public void run() {
		cancel();
	}

	public void setText(int resId) {
		if (mTextView != null) {
			mTextView.setText(resId);
		}
	}

	public void setText(String text) {
		if (mTextView != null) {
			mTextView.setText(text);
		}
	}

	public void setAnimationStyle(int style) {
		mPopToast.setAnimationStyle(style);
	}

	public void setGravity(int gravity) {
		mGravity = gravity;
	}

	public void setOffsetY(int offSetY) {
		mOffSetY = offSetY;
	}
}