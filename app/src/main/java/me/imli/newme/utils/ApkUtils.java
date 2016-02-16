package me.imli.newme.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.imli.newme.Const;
import me.imli.newme.R;
import me.imli.newme.http.ProgressHelper;
import me.imli.newme.http.UIProgressResponseListener;
import me.imli.newme.listener.ApkDownloadListener;
import me.imli.newme.okhttp.OkHttpUtils;
import me.imli.newme.okhttp.callback.FileCallBack;

/**
 * Created by Em on 2015/12/23.
 */
public class ApkUtils {

    private static final String TAG = "ApkUtils";
    private static final String TYPE = "application/vnd.android.package-archive";

    /**
     * 下载 Apk
     * @param url
     * @param callbakc
     */
    public static void downloadApk(String url, FileCallBack callbakc) {
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(callbakc);
    }

    /**
     * 下载 Apk
     * @param context
     * @param client
     * @param url
     * @param downloadListener
     */
    public static void downloadApk(Context context, OkHttpClient client, String url, ApkDownloadListener downloadListener) {
        ProgressDialog progressDialog = DialogUtils.getHorizontalProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.show();

        final UIProgressResponseListener listener = new UIProgressResponseListener() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int  progress = (int) ((100 * bytesRead) / contentLength);
                LogUtils.d(TAG, "progress: " + progress);
                progressDialog.setProgress(progress);
            }
        };

        final Callback callback = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                LogUtils.e(TAG, context.getString(R.string.error), e);
                progressDialog.dismiss();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                File file = new File(Environment.getExternalStorageDirectory(), Const.EX_DIRECTORY + "/" + Const.NEWSME_APK);
                InputStream stream = response.body().byteStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] b = new byte[1024];
                while((stream.read(b)) != -1){
                    fos.write(b);
                }
                stream.close();
                fos.close();

                new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        // 结束
                        if (downloadListener != null) {
                            downloadListener.onEnd(file);
                        }
                    }
                }, 500);
            }
        };

        // Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        ProgressHelper.addProgressResponseListener(client, listener).newCall(request).enqueue(callback);

    }

    public static void installApk(Fragment fragment, File file) {
        //代码安装
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), TYPE);
        fragment.startActivityForResult(intent, 1);
    }

    public static void installApk(Activity activity, File file) {
        //代码安装
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), TYPE);
        activity.startActivityForResult(intent, 1);
    }


}
