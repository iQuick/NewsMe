package me.imli.newme.widget;

import android.content.Context;
import android.util.AttributeSet;

import me.imli.newme.R;
import me.imli.newme.helper.ThemeHelper;
import me.imli.newme.widget.sb.SwipeBackLayout;

/**
 * Created by Em on 2015/12/18.
 */
public class OhSwipeBackLayout extends SwipeBackLayout {
    public OhSwipeBackLayout(Context context) {
        this(context, null);
    }

    public OhSwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OhSwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init() {
        int color = ThemeHelper.resolveColor(this.getContext(), R.attr.colorPrimaryDark, 0xff00ff);
        setButtomColor(color);
        setBackText(getContext().getString(R.string.swipe_back_tip));
    }

    @Override
    protected void stop(int dir) {
        super.stop(dir);
    }
}
