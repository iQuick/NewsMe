package me.imli.newme.utils;

import android.os.Build;
import android.widget.AbsListView;

/**
 * Created by Em on 2015/12/1.
 */
public class ScrollUtils {
    /**
     * 滚动列表到顶端
     *
     * @param listView
     */
    public static void smoothScrollListViewToTop(final AbsListView listView) {
        if (listView == null) {
            return;
        }
        smoothScrollListView(listView, 0);
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(0);
            }
        }, 200);
    }

    /**
     * 滚动列表到position
     *
     * @param listView
     * @param position
     */
    public static void smoothScrollListView(AbsListView listView, int position) {
        if (Build.VERSION.SDK_INT > 7) {
            listView.smoothScrollToPositionFromTop(0, 0);
        } else {
            listView.setSelection(position);
        }
    }
}
