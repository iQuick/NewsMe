package me.imli.newme.server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.http.ProgressHelper;
import me.imli.newme.http.UIProgressResponseListener;
import me.imli.newme.listener.DownloadResultListener;
import me.imli.newme.utils.LogUtils;

/**
 * Created by Em on 2015/12/23.
 */
public class DownloadServer extends Service {

    private static final String TAG = "DownloadServer";

    private DownloadBinder serviceBinder = new DownloadBinder();

    private ImApp app;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = ImApp.from(getApplicationContext());
    }

    public void download(String url, String name, DownloadResultListener listener) {
        if (listener != null) {
            listener.onStart();
        }

        final UIProgressResponseListener progressResponseListener = new UIProgressResponseListener() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int progress = (int) ((100 * bytesRead) / contentLength);
                LogUtils.d(TAG, "progress: " + progress);
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }
        };

        // Request
        final Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient okHttpClient = app.getHttpClient();
        ProgressHelper.addProgressResponseListener(okHttpClient, progressResponseListener).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                LogUtils.e(TAG, getString(R.string.error), e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (listener != null) {
                    listener.onEnd();
                }
            }
        });
    }


    /**
     *
     */
    public class DownloadBinder extends Binder {

        DownloadServer getServer() {
           return DownloadServer.this;
        }
    }
}
