package me.imli.newme.widget.sb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class SwipeLayout extends FrameLayout {

	protected final static String TAG = "SwipeLayout";

	/**
	 * 方向枚举
	 */
	public static final int DIRECTION_LEFT = 1;											// 左侧
	public static final int DIRECTION_TOP = 2;											// 右侧
	public static final int DIRECTION_RIGHT = 4;										// 顶部
	public static final int DIRECTION_BUTTOM = 8;										// 底部
	public static final int DIRECTION_HORIZONTAL = DIRECTION_LEFT | DIRECTION_RIGHT;	// 水平方向
	public static final int DIRECTION_VERTICAL = DIRECTION_TOP | DIRECTION_BUTTOM;		// 垂直方向
	protected int mDirection = DIRECTION_LEFT;

	private static final int DEFAULT_OVERHANG_SIZE = 60; // dp;
	private static final int MIN_FLING_VELOCITY = 400; // dips per second
	
	/** density */
	protected final float density;

	private GestureDetectorCompat mGestureDetectorCompat;
	
	/** 触摸释放触发事件时，是否关闭滑出的菜单 */
	private boolean mEndCloseEnable = false;
	
	/** 滑动的方向 */
	private int mEndDirection = 0;

	private View mActionView;
	private View mContentView;
	
	/**
	 * 底部 View
	 */
	protected ButtomView mButtomView;

	/** 移动的距离 */
	private float mDraggedX;

	/** DragHelper */
	private ViewDragHelper mDragHelper;

	public SwipeLayout(Context context) {
		this(context, null);
	}

	public SwipeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		density = context.getResources().getDisplayMetrics().density;

		setWillNotDraw(false);

		// 初始化底部 View
		mButtomView = new ButtomView(getContext());
		mButtomView.setDirection(mDirection);

		mGestureDetectorCompat = new GestureDetectorCompat(context, onScrollDetector());
		mDragHelper = ViewDragHelper.create(this, onDragHelperCallback());
		mDragHelper.setMinVelocity(MIN_FLING_VELOCITY * density);
	}
	
	/**
	 * 设置触摸释放触发事件时，是否关闭滑出的菜单 
	 * 
	 * @param enable
	 */
	public void setEndColseEnable(boolean enable) {
		mEndCloseEnable = enable;
	}
	
	/**
	 * 设置方向
	 * @param dir
	 */
	public void setDirection(int dir) {
		mDirection = dir;
		mButtomView.setDirection(dir);
	}
	
	/**
	 * 添加底部 View
	 * @param view
	 */
	protected void addButtomView(View view) {
		if (mContentView != null) {
			((ViewGroup) mContentView).addView(view);
		}
	}
	
	/**
	 * 设置底部 View
	 * @param view
	 */
	public void setButtomView(ButtomView view) {
		mButtomView = view;
	}

	/**
	 * 设置底部 View 的颜色
	 * @param color
	 */
	public void setButtomColor(int color) {
		mButtomView.setBackgroundColor(color);
	}

	/**
	 * start
	 */
	protected void start() {
		if (mOnSwipeListener != null) {
			mOnSwipeListener.start();
		}
	}
	
	/**
	 * change 
	 * 
	 * @param process
	 */
	protected void change(int process) {
		mButtomView.dragPercent(process);
		if (mOnSwipeListener != null) {
			mOnSwipeListener.change(process);
		}
	}
	
	/**
	 * stop
	 */
	protected void stop(int dir) {
		if (mEndCloseEnable) {
			close();
		}
		if (mOnSwipeListener != null) {
			mOnSwipeListener.stop(dir);
		}
	}
	
	/**
	 * 拖动距离
	 * 
	 * @return
	 */
	protected int getOverhangSize() {
		return (int) (DEFAULT_OVERHANG_SIZE * density);
	}

	/**
	 * 
	 * @return
	 */
	private ScrollDetector onScrollDetector() {
		return new ScrollDetector();
	}

	/**
	 * 
	 * @return
	 */
	private ViewDragHelper.Callback onDragHelperCallback() {
		return new ViewDragHelper.Callback() {
			@Override
			public boolean tryCaptureView(View arg0, int arg1) {
				return arg0 == mActionView;
			}

			@Override
			public int clampViewPositionHorizontal(View child, int left, int dx) {
				if (mDirection == DIRECTION_HORIZONTAL) {
					if (left > getPaddingLeft()) {
		                return Math.max(Math.min(left, getPaddingLeft() + getOverhangSize()), getPaddingLeft());
					} else if (left < getPaddingLeft()){
						return Math.min(Math.max(left, getPaddingLeft() - getOverhangSize()), getPaddingLeft());
					}
					return left;
				} else if (mDirection == DIRECTION_LEFT) {
	                return Math.max(Math.min(left, getPaddingLeft() + getOverhangSize()), getPaddingLeft());
				} else if (mDirection == DIRECTION_RIGHT) {
					return Math.min(Math.max(left, getPaddingLeft() - getOverhangSize()), getPaddingLeft());
				}
				return getPaddingLeft();
			}
			
			@Override
			public int clampViewPositionVertical(View child, int top, int dy) {
				if (mDirection == DIRECTION_VERTICAL) {
					if (top > getPaddingTop()) {
		                return Math.max(Math.min(top, getPaddingLeft() + getOverhangSize()), getPaddingTop());
					} else if (top < getPaddingTop()) {
						return Math.min(Math.max(top, getPaddingLeft() - getOverhangSize()), getPaddingTop());
					}
				} else if (mDirection == DIRECTION_TOP) {
	                return Math.max(Math.min(top, getPaddingLeft() + getOverhangSize()), getPaddingTop());
				} else if (mDirection == DIRECTION_BUTTOM) {
					return Math.min(Math.max(top, getPaddingLeft() - getOverhangSize()), getPaddingTop());
				}
				return getPaddingTop();
			}

			@Override
			public int getViewHorizontalDragRange(View child) {
				return getOverhangSize();
			}
			
			@Override
			public int getViewVerticalDragRange(View child) {
				return getOverhangSize();
			}

			@Override
			public void onViewReleased(View releasedChild, float xvel, float yvel) {
				super.onViewReleased(releasedChild, xvel, yvel);
				
				if (mDraggedX >= (getOverhangSize() - 10)) {
					stop(mEndDirection);
				} else {
					close();
				}
				mEndDirection = 0;
			}

			@Override
			public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
				switch (mDirection) {
				case DIRECTION_LEFT:
				case DIRECTION_RIGHT:
				case DIRECTION_LEFT | DIRECTION_RIGHT:
					mDraggedX = Math.abs(left - getPaddingLeft());
					break;
				case DIRECTION_TOP:
				case DIRECTION_BUTTOM:
				case DIRECTION_TOP | DIRECTION_BUTTOM:
					mDraggedX = Math.abs(top - getPaddingTop());
					break;
				default:
					mDraggedX = 0;
					break;
				}
				
				if (left > getPaddingLeft()) {
					mEndDirection = DIRECTION_LEFT;
				} else if (left < getPaddingLeft()) {
					mEndDirection = DIRECTION_RIGHT;
				} else if (top > getPaddingTop()) {
					mEndDirection = DIRECTION_TOP;
				} else if (top < getPaddingTop()) {
					mEndDirection = DIRECTION_BUTTOM;
				}
				
				change((int)(mDraggedX * 100 / getOverhangSize()));
				if (mActionView != null && mActionView.getVisibility() == View.GONE) {
					mActionView.setVisibility(View.VISIBLE);
				}
				invalidate();
			}

			
		};
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mDragHelper.shouldInterceptTouchEvent(ev) && mGestureDetectorCompat.onTouchEvent(ev);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
			start();
		}
		try {
			mDragHelper.processTouchEvent(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 关闭
	 */
	protected void close() {
		close(true);
	}

	/**
	 * 关闭
	 * @param animate 是否开启动画
	 */
	protected void close(boolean animate) {
		if (animate) {
			mDragHelper.smoothSlideViewTo(mActionView, getPaddingLeft(), getPaddingTop());
			ViewCompat.postInvalidateOnAnimation(this);
		} else {
			mActionView.layout(getPaddingLeft(), getPaddingTop(), mActionView.getWidth(), mActionView.getHeight());
		}
	}
	

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		RelativeLayout layout = new RelativeLayout(getContext());
		
		layout.setBackgroundColor(0xff000000);
		addView(layout, 0, new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		mContentView = getChildAt(0);
		mActionView = getChildAt(1);

		// 如果背景为空，则设置白色的背景
		if (mActionView.getBackground() == null) {
			mActionView.setBackgroundColor(0xffffffff);
		}
		
		// 添加底部 View
		if (mButtomView != null) {
			addButtomView(mButtomView);
		}
	}

	/**
	 * 
	 * @author Doots
	 * 
	 */
	class ScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return Math.abs(distanceY) <= Math.abs(distanceX);
		}
	}
	
	private OnSwipeListener mOnSwipeListener;
	public void setOnSwipeListener(OnSwipeListener listener) {
		mOnSwipeListener = listener;
	}
	/**
	 * 
	 * @author Doots
	 *
	 */
	public interface OnSwipeListener {
		public void start();
		public void change(int process);
		public void stop(int dir);
	}
}
