package com.sina.sinavideo.coreplayer.whitelist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;

/**
 * 白名单需要的设备信息 <br>
 * 包含：<br>
 * CPU信息
 * 
 * @author sunxiao
 * 
 */
public class VDDeviceInfo {

    public enum eAndroidOS {
        UNKNOWN, MIUI, EmotionUI, Flyme, NubiaUI, Nokia_X, ColorOS, HTC, ZTE, FuntouchOS,
    };

    private static String mSystemProperty = getSystemProperty();

    private final static String TAG = "VDDeviceInfo";

    /**
     * 屏幕宽高
     * 
     * @return
     */
    @SuppressLint("NewApi")
    public static int[] getScreenSize(Context context) {
        int[] size = new int[]{0, 0};
        if (getOSApi() > 13) {
            Point point = new Point(0, 0);
            ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
            size[0] = point.x;
            size[1] = point.y;
        } else {
            size[0] = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
            size[1] = ((Activity) context).getWindowManager().getDefaultDisplay().getHeight();
        }
        return new int[]{0};
    }

    public static int getOSApi() {
        return Build.VERSION.SDK_INT;
    }

    public static String getBrand() {
        return Build.BRAND;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getCPU() {
        return Build.CPU_ABI;
    }

    public static String getSDKRelease() {
        return Build.VERSION.RELEASE;
    }

    public static int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }

    public static eAndroidOS getOS() {
        return filterOS();
    }

    private static String getSystemProperty() {
        String line = "";
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 2048);
            String ret = input.readLine();
            while (ret != null) {
                line += ret + "\n";
                ret = input.readLine();
            }
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop", ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    private static eAndroidOS filterOS() {
        String prop = mSystemProperty;
        if (prop.contains("miui")) {
            return eAndroidOS.MIUI;
        } else if (prop.contains("EmotionUI")) {
            return eAndroidOS.EmotionUI;
        } else if (prop.contains("flyme")) {
            return eAndroidOS.Flyme;
        } else if (prop.contains("[ro.build.user]: [nubia]")) {
            return eAndroidOS.NubiaUI;
        } else if (prop.contains("Nokia_X")) {
            return eAndroidOS.Nokia_X;
        } else if (prop.contains("[ro.build.soft.version]: [A.")) {
            return eAndroidOS.ColorOS;
        } else if (prop.contains("ro.htc.")) {
            return eAndroidOS.HTC;
        } else if (prop.contains("[ro.build.user]: [zte")) {
            return eAndroidOS.ZTE;
        } else if (prop.contains("[ro.product.brand]: [vivo")) {
            return eAndroidOS.FuntouchOS;
        }
        return eAndroidOS.UNKNOWN;
    }
}
