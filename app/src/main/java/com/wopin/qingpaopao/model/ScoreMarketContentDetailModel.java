package com.wopin.qingpaopao.model;

import com.wopin.qingpaopao.bean.request.PaymentBean;
import com.wopin.qingpaopao.bean.response.NormalRsp;
import com.wopin.qingpaopao.http.HttpClient;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ScoreMarketContentDetailModel {

    public Observable<NormalRsp> payMentScores(PaymentBean paymentBean) {
        return HttpClient.getApiInterface().payMentScores(paymentBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
