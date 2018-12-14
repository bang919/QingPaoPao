package com.wopin.qingpaopao.presenter;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.WifiConfigToCupRsp;
import com.wopin.qingpaopao.bean.response.WifiRsp;
import com.wopin.qingpaopao.common.MyApplication;
import com.wopin.qingpaopao.model.WifiCupPostModel;
import com.wopin.qingpaopao.view.WifiPageChooseListView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class WifiPageChooseListPresenter extends BasePresenter<WifiPageChooseListView> {

    private WifiCupPostModel mWifiCupPostModel;

    public WifiPageChooseListPresenter(Context context, WifiPageChooseListView view) {
        super(context, view);
        mWifiCupPostModel = new WifiCupPostModel();
    }

    public void getWifiList(final String popoId) {
        Observable<String> wifilistOb = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                WifiManager wifiMgr = (WifiManager) MyApplication.getMyApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiMgr.getConnectionInfo();
                String wifiId = info != null ? info.getSSID() : null;
                if (!TextUtils.isEmpty(popoId) && !TextUtils.isEmpty(wifiId) && !("\"" + popoId + "\"").contains(wifiId)) {
                    mView.onWifiChangeAuto();
                    emitter.onComplete();
                }
                emitter.onNext("Success");
            }
        }).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return mWifiCupPostModel.getWifiList();
            }
        });
        Observable<ArrayList<WifiRsp>> arrayListObservable = wifilistOb
                .timeout(5, TimeUnit.SECONDS)
                .retryWhen(mWifiCupPostModel.getRetryWhen())
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

        subscribeNetworkTask(getClass().getSimpleName().concat("getWifiList"), arrayListObservable, new MyObserver<ArrayList<WifiRsp>>() {
            @Override
            public void onMyNext(ArrayList<WifiRsp> wifiRsps) {
                mView.onWifiListResponse(wifiRsps);
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }

    public void sendWifiConfigToCup(String ssid, String password) {
        if (TextUtils.isEmpty(ssid)) {
            mView.onError(mContext.getString(R.string.click_to_choose_wifi));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mView.onError(mContext.getString(R.string.please_input_password));
            return;
        }
        subscribeNetworkTask(getClass().getSimpleName().concat("sendWifiConfigToCup"), mWifiCupPostModel.sendWifiConfigToCup(ssid, password), new MyObserver<WifiConfigToCupRsp>() {
            @Override
            public void onMyNext(WifiConfigToCupRsp wifiConfigToCupRsp) {
                mView.onWifiConfigToCupRsp(wifiConfigToCupRsp);
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }
}
