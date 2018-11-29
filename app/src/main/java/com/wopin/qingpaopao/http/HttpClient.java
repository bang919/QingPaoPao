package com.wopin.qingpaopao.http;

import android.content.Context;
import android.util.ArrayMap;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.wopin.qingpaopao.BuildConfig;
import com.wopin.qingpaopao.common.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2017/8/17.
 */

public class HttpClient {

    private static final int cacheSize = 10 * 1024 * 1024; // 10 MiB
    private static final int TIME_OUT = 20;// Second
    private static OkHttpClient mOkHttpClient;
    private static ApiInterface mApiInterface;

    private static ArrayMap<String, String> mHeads = new ArrayMap<>();

    public static ApiInterface getApiInterface() {
        initOkhttpClient();
        initApiInterface();
        return mApiInterface;
    }

    public static ApiInterface addHeads(String key, String value) {
        mHeads.put(key, value);
        return mApiInterface;
    }

    public static ApiInterface clearHeads() {
        mHeads.clear();
        return mApiInterface;
    }

    private static void initApiInterface() {
        if (mApiInterface == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(mOkHttpClient)
                    .baseUrl(BuildConfig.basicUrlHttps)
                    .addConverterFactory(NotJsonResponseConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            mApiInterface = retrofit.create(ApiInterface.class);
        }
    }

    private static void initOkhttpClient() {
        if (mOkHttpClient == null) {

            Interceptor headInterceptor = getHeadInterceptor();
            HttpLoggingInterceptor httpLoggingInterceptor = getLoggingInterceptor();
            Cache cache = getCache();

            ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getMyApplicationContext()));

            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .cookieJar(cookieJar)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession sslSession) {
                            return hostname.equalsIgnoreCase("wifi.h2popo.com")
                                    || hostname.equalsIgnoreCase("public-api.wordpress.com");
                        }
                    })
                    .cache(cache)
//                    .addNetworkInterceptor(headInterceptor)
//                    .addInterceptor(httpLoggingInterceptor)
                    .build();
        }
    }

    private static Interceptor getHeadInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder = request.newBuilder();

                for (Map.Entry<String, String> entry : mHeads.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
//                builder.addHeader("Content-Type", "application/json"); //豆瓣API不允许增加这个Header
                return chain.proceed(builder.build());
            }
        };
    }

    private static HttpLoggingInterceptor getLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    private static Cache getCache() {
        Context applicationContent = MyApplication.getMyApplicationContext();
        File httpCacheDirectory = new File(applicationContent.getCacheDir(), "responses");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        return cache;
    }
}
