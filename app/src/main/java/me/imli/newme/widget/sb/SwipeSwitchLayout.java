package me.imli.newme.widget.sb;

import android.content.Context;
import android.util.AttributeSet;

public class SwipeSwitchLayout extends SwipeLayout {

	protected final static String TAG = "SwipeSwitchLayout";

	public SwipeSwitchLayout(Context context) {
		this(context, null);
	}

	public SwipeSwitchLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeSwitchLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 设置默认方向为左、右滑动
		mDirection = DIRECTION_HORIZONTAL;
	}
	
	/**
	 * 设置方向
	 * 
	 * dir is DIRECTION_HORIZONTAL or DIRECTION_VERTICAL
	 */
	@Override
	public void setDirection(int dir) {
		if (dir == DIRECTION_HORIZONTAL || dir == DIRECTION_VERTICAL) {
			super.setDirection(dir);
		}
	}
	
	/**
	 * 设置上一页提示文字
	 * @param text
	 */
	public void setPreviousText(String text) {
		mButtomView.setTipText(text, DIRECTION_LEFT);
		mButtomView.setTipText(text, DIRECTION_TOP);
	}
	
	/**
	 * 设置下一页提示文字
	 * @param text
	 */
	public void setNextText(String text) {
		mButtomView.setTipText(text, DIRECTION_RIGHT);
		mButtomView.setTipText(text, DIRECTION_BUTTOM);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		// 设置默认提示文字
		mButtomView.setTipText("Previous Page", DIRECTION_LEFT);
		mButtomView.setTipText("Next Page", DIRECTION_RIGHT);
		mButtomView.setTipText("Previous Page", DIRECTION_TOP);
		mButtomView.setTipText("Next Page", DIRECTION_BUTTOM);
		mButtomView.setMarginTop((int)(density * 210));
	}

	@Override
	protected void change(int process) {
		super.change(process);
	}
	
	@Override
	protected void stop(int dir) {
		super.stop(dir);
		if (mOnSwipeSwitchListener != null) {
			switch (dir) {
			case DIRECTION_LEFT:
			case DIRECTION_TOP:
				mOnSwipeSwitchListener.onPrevious();
				break;
			case DIRECTION_RIGHT:
			case DIRECTION_BUTTOM:
				mOnSwipeSwitchListener.onNext();
				break;

			default:
				break;
			}
		}
	}
	
	private OnSwipeSwitchListener mOnSwipeSwitchListener;
	public void setOnSwipeSwitchListener(OnSwipeSwitchListener listener) {
		mOnSwipeSwitchListener = listener;
	}
	
	/**
	 * 
	 * @author Doots
	 *
	 */
	public interface OnSwipeSwitchListener {
		/** 上一页 */
		public void onPrevious();
		/** 下一页 */
		public void onNext();
	}
}
