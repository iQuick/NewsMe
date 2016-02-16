package com.sina.sinavideo.coreplayer.splayer;

import com.sina.sinavideo.coreplayer.util.LogS;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class ContextUtils {

    public static int getVersionCode(Context ctx) {
        int version = 0;
        try {
            version = ctx.getPackageManager().getPackageInfo(ctx.getApplicationInfo().packageName, 0).versionCode;
        } catch (Exception e) {
            LogS.e("ContextUtils", "getVersionCode error," + e.toString());
        }
        return version;
    }

    public static String getDataDir(Context ctx) {
        ApplicationInfo ai = ctx.getApplicationInfo();
        if (ai.dataDir != null)
            return fixLastSlash(ai.dataDir);
        else
            return "/data/data/" + ai.packageName + "/";
    }

    public static String fixLastSlash(String str) {
        String res = str == null ? "/" : str.trim() + "/";
        if (res.length() > 2 && res.charAt(res.length() - 2) == '/')
            res = res.substring(0, res.length() - 1);
        return res;
    }
}
