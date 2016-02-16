package me.imli.newme.widget;

import android.content.Context;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;

/**
 * Created by Em on 2015/12/25.
 */
public class OhDrawerLayout extends DrawerLayout {
    public OhDrawerLayout(Context context) {
        this(context, null);
    }

    public OhDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OhDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            setFitsSystemWindows(true);
        }
    }
}
