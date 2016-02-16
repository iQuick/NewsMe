package me.imli.newme.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.TypedValue;

import me.imli.newme.R;
import me.imli.newme.utils.ThemeUtils;

/**
 * Created by Em on 2015/12/14.
 */
public class OhSwipeRefreshLayout extends SwipeRefreshLayout {
    public OhSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public OhSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedValue colorAccent = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorAccent, colorAccent, true);
        setColorSchemeResources(colorAccent.resourceId);

        // 设置圆的样式
        if (ThemeUtils.getTheme(getContext()).tag.equals("dark")) {
            setProgressBackgroundColor(R.color.black_primary_light);
        }
    }
}
