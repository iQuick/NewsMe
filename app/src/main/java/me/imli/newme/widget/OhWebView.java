package me.imli.newme.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by Em on 2015/12/28.
 */
public class OhWebView extends WebView {
    public OhWebView(Context context) {
        super(context);
    }

    public OhWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OhWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OhWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
