package me.imli.newme.widget.sb;

import android.content.Context;
import android.util.AttributeSet;

public class SwipeBackLayout extends SwipeLayout {

	protected final static String TAG = "SwipeBackLayout";
	

	public SwipeBackLayout(Context context) {
		this(context, null);
	}

	public SwipeBackLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * 设置返回提示文字
	 * @param text
	 */
	public void setBackText(String text) {
		mButtomView.setTipText(text);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
	}
	
	@Override
	protected void stop(int dir) {
		super.stop(dir);
		if (mOnSwipeBackListener != null) {
			mOnSwipeBackListener.onBack();
		}
	}
	
	private OnSwipeBackListener mOnSwipeBackListener;
	public void setOnSwipeBackListener(OnSwipeBackListener listener) {
		mOnSwipeBackListener = listener;
	}
	
	/**
	 * 
	 * @author Doots
	 *
	 */
	public interface OnSwipeBackListener {
		/** 返回 */
		public void onBack();
	}

}
