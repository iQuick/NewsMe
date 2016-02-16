package me.imli.newme.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import me.imli.newme.R;

/**
 * Created by Em on 2015/12/23.
 */
public class DialogUtils {

    /**
     * 显示提示对话框
     * @param title
     * @param msg
     */
    public static void doShowTip(Context context, String title, String msg) {
        new AlertDialog.Builder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     *
     * @param context
     * @return
     */
    public static ProgressDialog getSpinnerProgressDialog(Context context) {
        return getProgressDialog(context, ProgressDialog.STYLE_SPINNER);
    }

    /**
     *
     * @param context
     * @return
     */
    public static ProgressDialog getHorizontalProgressDialog(Context context) {
        return getProgressDialog(context, ProgressDialog.STYLE_HORIZONTAL);
    }

    /**
     *
     * @param context
     * @return
     */
    private static ProgressDialog getProgressDialog(Context context, int style) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(style);
        dialog.setTitle(R.string.downloading);
        return dialog;
    }
}
