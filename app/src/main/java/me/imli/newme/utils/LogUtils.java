package me.imli.newme.utils;

import me.imli.newme.BuildConfig;
import me.imli.newme.utils.log.LogManager;

/**
 * Created by Em on 2015/9/11.
 */
public class LogUtils {

    public static void i(String tag, String msg) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().i(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().w(tag, msg);
        }
    }

    public static void e(String  tag, String msg) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().e(tag, msg);
        }
    }

    // == Log Throwable ===========
    public static void i(String tag, String msg, Throwable tr) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().i(tag, msg, tr);
        }
    }

    public static void v(String tag, String msg,Throwable tr) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg,Throwable tr) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().d(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg,Throwable tr) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().w(tag, msg, tr);
        }
    }

    public static void e(String  tag, String msg,Throwable tr) {
        if (BuildConfig.ISDEBUG) {
            LogManager.getLogger().e(tag, msg, tr);
        }
    }

}
