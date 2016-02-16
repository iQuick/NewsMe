package me.imli.newme;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;

import java.util.HashMap;
import java.util.Locale;

import au.com.gridstone.rxstore.RxStore;
import au.com.gridstone.rxstore.converters.GsonConverter;
import me.imli.newme.api.ApiConst;
import me.imli.newme.api.ApiConverter.ApiConverterFactory;
import me.imli.newme.model.Channel;
import me.imli.newme.utils.LogUtils;
import me.imli.newme.utils.SharedPrefUtil;
import me.imli.newme.utils.log.LoggingInterceptor;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Em on 2015/11/26.
 */
public class ImApp extends Application {

    private final String TAG = "ImApp";

    private static Context mContext;

    // APIS
    private final HashMap<Class, Object> apis = new HashMap<>();

    // Retrofit
    private Retrofit mCoreRetrofit;
    private Retrofit mJokeRetrofit;
    private Retrofit mFirFit;

    private OkHttpClient mHttpClient;
    private Gson mGson;
    private RxStore mRxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        LeakCanary.install(this);
        this.init();
    }

    public static ImApp from(Context context) {
        Context application = context.getApplicationContext();
        if (application instanceof  ImApp) {
            return (ImApp) application;
        } else {
            throw new IllegalArgumentException("context must be from ImApp");
        }
    }

    public static Context getContext() {
        return mContext;
    }

    private String buildAcceptLanguage() {
        Locale locale = Locale.getDefault();
        return String.format("%s-%s,%s;q=0.8,en-US;q=0.6,en;q=0.4",
                locale.getLanguage(), locale.getCountry(), locale.getLanguage());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private String buildUserAgent() {
        String userAgent = String.format("Retrofit %s Android (%d/%s)", BuildConfig.VERSION_NAME, Build.VERSION.SDK_INT, Build.VERSION.RELEASE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealMetrics(metrics);
            userAgent += String.format(" (%d; %dx%d)", metrics.densityDpi, metrics.widthPixels, metrics.heightPixels);
        }
        return userAgent;
    }

    private void init() {
        initGson();
        initHttpClient();
        initRetrofit();
        initDBStore();
        initChannels();
    }

    private void initChannels() {
        if (!SharedPrefUtil.getBoolean(getApplicationContext(), getString(R.string.pre_is_create_channel), false)) {
            getDB()
                .putList(Const.DB_NEWS_CHANNEL, Channel.getChannelList(), Channel.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(avoid -> {
                            LogUtils.d(TAG, "save channels success");
                        }, e -> LogUtils.e(TAG, getString(R.string.error), e)
                );
            SharedPrefUtil.putBoolean(getApplicationContext(), getString(R.string.pre_is_create_channel), true);
        }
    }

    private void initGson() {
        mGson = new GsonBuilder()
//                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    private void initHttpClient() {
        mHttpClient = new OkHttpClient();
        mHttpClient.networkInterceptors()
                .add(chain -> chain.proceed(chain.request().newBuilder()
                        .header("Accept-Language", buildAcceptLanguage())
                        .header("User-Agent", buildUserAgent())
                        .build()));
        // 打印日志
        if (BuildConfig.ISDEBUG) {
            mHttpClient.networkInterceptors().add(new LoggingInterceptor());
        }
    }

    private void initRetrofit() {
        mCoreRetrofit = new Retrofit.Builder()
                .client(mHttpClient)
                .addConverterFactory(ApiConverterFactory.create(mGson))
//                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ApiConst.BASE)
                .build();

        mJokeRetrofit = new Retrofit.Builder()
                .client(mHttpClient)
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ApiConst.JOKE_BASE)
                .build();

        mFirFit = new Retrofit.Builder()
                .client(mHttpClient)
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ApiConst.FIR_BASE)
                .build();
    }

    private void initDBStore() {
        mRxStore = RxStore.withContext(getApplicationContext())
                .in(Const.DB_NAME)
                .using(new GsonConverter(mGson));
    }

    private void initSnack() {
//        Snackbar.SnackbarLayout layout ;
//        layout.settext
    }

    /**
     *
     * @return
     */
    public OkHttpClient getHttpClient() {
        return mHttpClient;
    }

    /**
     *
     * @return
     */
    public RxStore getDB() {
        return mRxStore;
    }

    /**
     * Use retrofit create an core api
     * @param service The api Class.class
     * @param <T> The api Class
     * @return
     */
    public <T> T createCoreApi(Class<T> service) {
        if (!apis.containsKey(service)) {
            T instance = mCoreRetrofit.create(service);
            apis.put(service, instance);
        }
        return (T) apis.get(service);
    }

    /**
     * Use retrofit create an joke api
     * @param service The api Class.class
     * @param <T> The api Class
     * @return
     */
    public <T> T createJokeApi(Class<T> service) {
        if (!apis.containsKey(service)) {
            T instance = mJokeRetrofit.create(service);
            apis.put(service, instance);
        }
        return (T) apis.get(service);
    }

    public <T> T createFirAai(Class<T> service) {
        if (!apis.containsKey(service)) {
            T instance = mFirFit.create(service);
            apis.put(service, instance);
        }
        return (T) apis.get(service);
    }

    public <T> T getApi(Class<T> service) {
        if (!apis.containsKey(service)) {
            T instance = mCoreRetrofit.create(service);
            apis.put(service, instance);
        }
        return (T) apis.get(service);
    }

}
