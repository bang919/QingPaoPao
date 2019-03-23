package com.wopin.qingpaopao.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.wopin.qingpaopao.bean.response.WifiConfigToCupRsp;
import com.wopin.qingpaopao.http.HttpClient;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class WifiCupPostModel {

    private static final int WAIT_EACH_TIME = 15;//每次API请求Timtout为5秒钟

    public Observable<String> getWifiList() {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "scan:1\n");
        return HttpClient.getApiInterface().getWifiList(body);
    }

    public Observable<WifiConfigToCupRsp> sendWifiConfigToCup(String ssid, String password) {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "ssid: " + ssid + "\n" + "password: " + password + "\n");
        Log.d("sendWifiConfigToCup", "sending request");
        return HttpClient.getApiInterface().sendWifiConfigToCup(body)
                .timeout(WAIT_EACH_TIME, TimeUnit.SECONDS)
                .retryWhen(getRetryWhen())
                .map(new Function<String, WifiConfigToCupRsp>() {
                    @Override
                    public WifiConfigToCupRsp apply(String s) throws Exception {
                        Log.d("sendWifiConfigToCup", s);
                        return new Gson().fromJson(s, WifiConfigToCupRsp.class);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public Function<Observable<Throwable>, ObservableSource<Long>> getRetryWhen() {
        return new Function<Observable<Throwable>, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.zipWith(Observable.range(1, 30), new BiFunction<Throwable, Integer, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable, Integer integer) throws Exception {//最多等待30次
                        if (integer >= 30) {
                            throw new Exception("retry time over range 30.");
                        }
                        return integer;
                    }
                }).flatMap(new Function<Integer, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Integer integer) throws Exception {
                        return Observable.timer(1, TimeUnit.SECONDS);//每次间隔1秒
                    }
                });
            }
        };
    }
}
