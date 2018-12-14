package com.wopin.qingpaopao.fragment.drinking;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.WifiRsp;
import com.wopin.qingpaopao.common.MyApplication;
import com.wopin.qingpaopao.fragment.BaseBarDialogFragment;
import com.wopin.qingpaopao.model.WifiCupPostModel;
import com.wopin.qingpaopao.presenter.BasePresenter;
import com.wopin.qingpaopao.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LinkWifiByhandFragment extends BaseBarDialogFragment {

    public static final String TAG = "LinkWifiByhandFragment";
    private LinkWifiByhandCallback mLinkWifiByhandCallback;
    private Disposable mDisposable;
    private String beforWifi;
    private String cupWifi;

    public void setLinkWifiByhandCallback(LinkWifiByhandCallback linkWifiByhandCallback) {
        mLinkWifiByhandCallback = linkWifiByhandCallback;
    }

    @Override
    protected String setBarTitle() {
        return getString(R.string.link_wifi);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_link_wifi_byhand;
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initView(View rootView) {
        beforWifi = getWifi();
    }

    @Override
    public void onPause() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        super.onPause();
    }

    private String getWifi() {
        WifiManager wifiMgr = (WifiManager) MyApplication.getMyApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMgr.getConnectionInfo();
        return info != null ? info.getSSID() : null;
    }


    @Override
    public void onResume() {
        super.onResume();
        String nowWifi = getWifi();
        if (!TextUtils.isEmpty(beforWifi) && !beforWifi.contains(nowWifi)) {
            cupWifi = nowWifi;
        }

        getObservable().subscribe(new Observer<ArrayList<WifiRsp>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(ArrayList<WifiRsp> wifiRsps) {
                if (wifiRsps != null && mLinkWifiByhandCallback != null) {
                    mLinkWifiByhandCallback.onWifiLinkByhand();
                    dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                mDisposable = null;
            }

            @Override
            public void onComplete() {
                mDisposable = null;
            }
        });
    }


    private Observable<ArrayList<WifiRsp>> getObservable() {
        final WifiCupPostModel wifiCupPostModel = new WifiCupPostModel();
        Observable<String> wifilistOb = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                WifiManager wifiMgr = (WifiManager) MyApplication.getMyApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiMgr.getConnectionInfo();
                String wifiId = info != null ? info.getSSID() : null;
                showConnectToast(wifiId);
                emitter.onNext("Success");
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return wifiCupPostModel.getWifiList();
            }
        });

        return wifilistOb
                .timeout(5, TimeUnit.SECONDS)
                .retryWhen(wifiCupPostModel.getRetryWhen())
                .map(new Function<String, ArrayList<WifiRsp>>() {
                    @Override
                    public ArrayList<WifiRsp> apply(String s) throws Exception {
                        String jsonString = "[" + s + "]";
                        Type founderListType = new TypeToken<ArrayList<WifiRsp>>() {
                        }.getType();
                        return new Gson().fromJson(jsonString, founderListType);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void showConnectToast(final String ssid) {
        Disposable subscribe = Observable.just(ssid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if (!TextUtils.isEmpty(cupWifi) && !cupWifi.contains(ssid)) {
                            ToastUtils.showLong(getString(R.string.please_disable_auto_switch, ssid));
                            dismiss();
                        }
                    }
                });
    }

    @Override
    protected void initEvent() {

    }

    public interface LinkWifiByhandCallback {
        void onWifiLinkByhand();
    }
}
