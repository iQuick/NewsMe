package me.imli.newme.utils.log;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Em on 2015/11/27.
 */
public class LoggingInterceptor implements Interceptor {

    private static final String TAG = "OkHttp";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Log.d(TAG, String.format("%s\n%s", request, request.headers()));
        Response response = chain.proceed(request);
        Log.d(TAG, String.format("%s\n%s", response, response.headers()));
        return response;
    }
}
