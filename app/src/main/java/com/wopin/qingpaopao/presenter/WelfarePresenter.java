package com.wopin.qingpaopao.presenter;

import android.content.Context;

import com.wopin.qingpaopao.bean.response.ProductBanner;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.model.WelfareModel;
import com.wopin.qingpaopao.widget.WelfareView;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WelfarePresenter extends BasePresenter<WelfareView> {

    private WelfareModel mWelfareModel;

    public WelfarePresenter(Context context, WelfareView view) {
        super(context, view);
        mWelfareModel = new WelfareModel();
    }

    public void getScoreMarketProduct() {
        getWelfareProduct(20);
    }

    public void getOldChangeNewProduct() {
        getWelfareProduct(16);
    }

    public void getCrowdFundings() {
        getWelfareProduct(28);
    }

    public void getWelfareProduct(int categoryId) {
        mView.onLoading();
        subscribeNetworkTask(getClass().getSimpleName().concat("getScoreMarketProduct"),
                Observable.zip(
                        mWelfareModel.getProductBanner(categoryId)
                                .doOnNext(new Consumer<ProductBanner>() {
                                    @Override
                                    public void accept(ProductBanner productBanner) throws Exception {
                                        mView.onProductBanner(productBanner);
                                    }
                                }),
                        mWelfareModel.getProductContent(categoryId)
                                .doOnNext(new Consumer<ArrayList<ProductContent>>() {
                                    @Override
                                    public void accept(ArrayList<ProductContent> productContents) throws Exception {
                                        mView.onProductContentList(productContents);
                                    }
                                }),
                        new BiFunction<ProductBanner, ArrayList<ProductContent>, String>() {
                            @Override
                            public String apply(ProductBanner productBanner, ArrayList<ProductContent> productContents) throws Exception {
                                return "Success";
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                new MyObserver<String>() {
                    @Override
                    public void onMyNext(String s) {
                        mView.onRequestSuccess();
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });
    }
}
