package me.imli.newme.widget.sb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class ButtomView extends View implements DragPercent{
	
	protected static final String TAG = "ButtomView";
	
	/**
	 * 方向枚举
	 */
	protected int mDirection = SwipeLayout.DIRECTION_HORIZONTAL | SwipeLayout.DIRECTION_VERTICAL;

	private static final float DEF_ZOOM = 0.2f;
	private static final int DEF_MOVE_DISTANCE = 25;
	
	private static final int DEF_MARGIN_LEFT = 0;
	private static final int DEF_MARGIN_TOP = 160;
	
	/** 提示文字 */
	private static final String DEF_TIP_TEXT = "Back";
	private static final float DEF_TIP_TEXX_SIZE = 24f;
	private static final int DEF_TIP_TEXX_COLOR = 0xffffffff;

	private final float density;
	
	/** 拖动百分比 */
	private float mDragPercent;

	/** 父 View 的宽高 */
	private int mPraentWidth, mPraentHeight;
	
	/** 边距 */
	private int mMarginBound, mMarginTop;
	
	/** 画笔 */
	private Paint mPaint;
	/** 显示的提示文字 */
	private String[] mTipTexts = new String[4];

	public ButtomView(Context context) {
		this(context, null);
	}

	public ButtomView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ButtomView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		for (int i = 0; i < mTipTexts.length; i++) {
			mTipTexts[i] = DEF_TIP_TEXT;
		}

		density = context.getResources().getDisplayMetrics().density;
		
		mMarginBound = (int) (DEF_MARGIN_LEFT * density);
		mMarginTop = (int) (DEF_MARGIN_TOP * density);
		
		mPaint = new Paint();
		mPaint.setTextSize(getFontSize(DEF_TIP_TEXX_SIZE));
		mPaint.setColor(DEF_TIP_TEXX_COLOR);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
	}
	
	/**
	 * 设置与父 View 的边框距离
	 * @param margin
	 */
	public void setMarginBound(int margin) {
		mMarginBound = margin;
	}
	
	/**
	 * 设置方向
	 * @param dir
	 */
	public void setDirection(int dir) {
		mDirection = dir;
	}
	
	/**
	 * 设置左、右两侧提示文字与顶部的距离
	 * @param margin
	 */
	public void setMarginTop(int margin) {
		mMarginTop = margin;
	}
	
	/**
	 * 设置提示文字
	 * @param text
	 */
	public void setTipText(String text) {
		for (int i = 0; i < mTipTexts.length; i++) {
			mTipTexts[i] = text;
		}
	}
	
	/**
	 * 设置提示文字
	 * @param text
	 * @param dir 方向
	 */
	public void setTipText(String text, int dir) {
		switch (dir) {
		case SwipeLayout.DIRECTION_LEFT:
			mTipTexts[0] = text;
			break;
		case SwipeLayout.DIRECTION_TOP:
			mTipTexts[1] = text;
			break;
		case SwipeLayout.DIRECTION_RIGHT:
			mTipTexts[2] = text;
			break;
		case SwipeLayout.DIRECTION_BUTTOM:
			mTipTexts[3] = text;
			break;

		default:
			break;
		}
	}
	
	/**
	 * 设置文字颜色
	 * @param color
	 */
	public void setTextColor(int color) {
		mPaint.setColor(color);
		invalidate();
	}
	
	/**
	 * 设置文字大小
	 * @param size
	 */
	public void setTextSize(int size) {
		mPaint.setTextSize(getFontSize(size));
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		View praent = (View)getParent();
		if (praent != null) {
			mPraentWidth = praent.getWidth();
			mPraentHeight = praent.getHeight();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (checkIsDraw(mDirection, 0)) drawLeft(canvas);
		if (checkIsDraw(mDirection, 1)) drawTop(canvas);
		if (checkIsDraw(mDirection, 2)) drawRight(canvas);
		if (checkIsDraw(mDirection, 3)) drawButtom(canvas);
	}
	
	/**
	 * 判断是否绘制
	 * @param dir
	 * @param position
	 * @return
	 */
	private boolean checkIsDraw(int dir, int position) {
		int size = dir >> position & 1;
		if  (size == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取移动的距离
	 * @return
	 */
	private float getMoveDestance() {
		return DEF_MOVE_DISTANCE * mDragPercent * density;
	}
	
	private float getZoom() {
		return DEF_ZOOM * mDragPercent + 1;
	}

	/**
	 * 绘制左侧
	 * @param canvas
	 */
	protected void drawLeft(Canvas canvas) {
		drawText(canvas, mTipTexts[0], getMoveDestance() + mMarginBound + getFontHeight(mPaint) / 2, mMarginTop + getFontWidth(mPaint, mTipTexts[0]) / 2, mPaint, -90, getZoom());
	}

	/**
	 * 绘制右侧
	 * @param canvas
	 */
	protected void drawRight(Canvas canvas) {
		drawText(canvas, mTipTexts[2], -getMoveDestance() + mPraentWidth - mMarginBound - getFontHeight(mPaint) / 2, mMarginTop - getFontWidth(mPaint, mTipTexts[2]) / 2, mPaint, 90, getZoom());
	}

	/**
	 * 绘制顶部
	 * @param canvas
	 */
	protected void drawTop(Canvas canvas) {
		drawText(canvas, mTipTexts[1], (mPraentWidth - getFontWidth(mPaint, mTipTexts[1])) / 2, getMoveDestance() + mMarginBound + getFontHeight(mPaint) / 2, mPaint, 0, getZoom());
	}
	
	/**
	 * 绘制底部
	 * @param canvas
	 */
	protected void drawButtom(Canvas canvas) {
		drawText(canvas, mTipTexts[3], (mPraentWidth + getFontWidth(mPaint, mTipTexts[3])) / 2, - getMoveDestance() + mPraentHeight - mMarginBound - getFontHeight(mPaint) / 2, mPaint, 180, getZoom());
	}
	
	/**
	 * 绘制文字
	 * @param canvas
	 * @param text
	 * @param x
	 * @param y
	 * @param paint
	 * @param angle
	 */
    protected void drawText(Canvas canvas ,String text , float x ,float y,Paint paint ,float angle){
    	this.drawText(canvas, text, x, y, paint, angle, 1);
    }
	
    /**
     * 绘制文字
     * @param canvas
     * @param text
     * @param x
     * @param y
     * @param paint
     * @param angle
     * @param zoom
     */
    protected void drawText(Canvas canvas ,String text , float x ,float y, Paint paint ,float angle, float zoom){

    	// 缩放
        Paint textPaint = new Paint(paint);
        textPaint.setTextSize(paint.getTextSize() * zoom);
        
        // 旋转
    	canvas.save();
        canvas.rotate(angle, x, y);
        x -= (getFontWidth(textPaint, text) - getFontWidth(paint, text))/2 ;
        y += (getFontHeight(textPaint) - getFontHeight(paint))/2 ;
        canvas.drawText(text, x, y, textPaint);
        canvas.restore();
    }
	
    /**
     * 获取文字大小
     * @param size
     * @return
     */
	protected float getFontSize(float size) {
		return density * size;
	}
	
	/**
	 * 获取文字宽度
	 * @param paint
	 * @param text
	 * @return
	 */
	protected float getFontWidth(Paint paint, String text) {
		return paint.measureText(text);
	}
	
	/**
	 * 获取文字高度
	 * @param paint
	 * @return
	 */
	protected float getFontHeight(Paint paint) {
        return Math.abs(paint.descent() - paint.ascent());  
	}
	
	/**
	 * 获取文字间的基准距离
	 * @param paint
	 * @return
	 */
	protected float getFontLeading(Paint paint) {
		return paint.getFontMetrics().leading;
	}

	@Override
	public void dragPercent(int percent) {
		mDragPercent = (float) (percent / 100.0);
		mPaint.setAlpha((int)(percent * 255 / 100));
		invalidate();
	}
	
}
