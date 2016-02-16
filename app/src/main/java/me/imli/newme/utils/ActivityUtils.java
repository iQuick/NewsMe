package me.imli.newme.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Em on 2015/11/26.
 */
public class ActivityUtils {
    /**
     *
     */
    private static List<Activity> mActivityList = new ArrayList<Activity>();

    /**
     * 添加到 activity 到列表中
     * @param activity
     */
    public static void add(Activity activity) {
        mActivityList.add(activity);
    }

    /**
     * 从列表中删除 activity
     * @param activity
     */
    public static void remove(Activity activity) {
        mActivityList.remove(activity);
    }

    /**
     * finish 所有的存活的Activity
     * 并杀死应用进程
     */
    public static void finishKill() {
        finishAll();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * finish 所有的存活的Activity
     */
    public static void finishAll() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }
    }
}
