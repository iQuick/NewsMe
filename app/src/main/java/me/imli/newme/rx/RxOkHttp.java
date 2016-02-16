package me.imli.newme.rx;

import android.app.ProgressDialog;
import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

import me.imli.newme.http.ProgressHelper;
import me.imli.newme.http.UIProgressResponseListener;
import me.imli.newme.utils.DialogUtils;
import me.imli.newme.utils.Utils;
import rx.Observable;

/**
 * Created by Em on 2015/12/23.
 */
public class RxOkHttp {

    public static Observable<File> download(Context context, OkHttpClient client, String url) {
        return Observable.defer(() -> {
            try {
                ProgressDialog progressDialog = DialogUtils.getHorizontalProgressDialog(context);
                progressDialog.setCancelable(false);

                Request request = new Request.Builder().url(url).build();
                Response response = ProgressHelper.addProgressResponseListener(client, progressResponseListener(progressDialog)).newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                File file = new File("me.apk");
                Utils.stream2File(response.body().byteStream(), file);
                return Observable.just(file);
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    private static UIProgressResponseListener progressResponseListener(ProgressDialog dialog) {
        return new UIProgressResponseListener() {
            @Override
            public void onUIResponseProgress(long bytesRead, long contentLength, boolean done) {
                int  progress = (int) ((100 * bytesRead) / contentLength);
                if (bytesRead <= 0) {
                    dialog.show();
                }
                if (done) {
                    dialog.dismiss();
                }
                dialog.setProgress(progress);
            }
        };
    }
}
