package me.imli.newme.utils;

import android.os.Build;

/**
 * 
 * @author Em
 *
 */
public class VersionUtil {
    public static final boolean IS_JBMR2 = Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2;
    public static final boolean IS_ISC = Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    public static final boolean IS_GINGERBREAD_MR1 = Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1;

}
