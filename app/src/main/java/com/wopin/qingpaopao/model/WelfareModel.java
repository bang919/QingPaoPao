package com.wopin.qingpaopao.model;

import com.wopin.qingpaopao.bean.response.CrowdfundingOrderTotalMoneyRsp;
import com.wopin.qingpaopao.bean.response.CrowdfundingOrderTotalPeopleRsp;
import com.wopin.qingpaopao.bean.response.ProductBanner;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.http.HttpClient;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class WelfareModel {


    public Observable<CrowdfundingOrderTotalMoneyRsp> crowdfundingOrderTotalMoney(int goodsId) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{\"goodsId\":\"" + goodsId + "\"}");
        return HttpClient.getApiInterface().crowdfundingOrderTotalMoney(requestBody)
                .retry(2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CrowdfundingOrderTotalPeopleRsp> crowdfundingOrderTotalPeople(int goodsId) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{\"goodsId\":\"" + goodsId + "\"}");
        return HttpClient.getApiInterface().crowdfundingOrderTotalPeople(requestBody)
                .retry(2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ProductBanner> getProductBanner(int categoryId) {
        return HttpClient.getApiInterface().getProductBanner(String.valueOf(categoryId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ArrayList<ProductContent>> getProductContent(int categoryId) {
        return HttpClient.getApiInterface().getProductContent(String.valueOf(categoryId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
