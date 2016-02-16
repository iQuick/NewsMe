package com.sina.sinavideo.coreplayer.splayer;

import java.io.Closeable;

import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class IOUtils {

    private static final String TAG = "IOUtils";

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }

    public static void closeSilently(ParcelFileDescriptor c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }

    public static void closeSilently(Cursor cursor) {
        try {
            if (cursor != null)
                cursor.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }
}
