package com.wopin.qingpaopao.presenter;

import android.content.Context;

import com.wopin.qingpaopao.bean.response.OrderResponse;
import com.wopin.qingpaopao.model.MyOrderModel;
import com.wopin.qingpaopao.view.MyOrderView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

public class MyOrderPresenter extends BasePresenter<MyOrderView> {

    private MyOrderModel mMyOrderModel;

    public MyOrderPresenter(Context context, MyOrderView view) {
        super(context, view);
        mMyOrderModel = new MyOrderModel();
    }

    public void getOrderListDatas() {
        subscribeNetworkTask(getClass().getSimpleName().concat("getOrderListDatas"), Observable.zip(
                mMyOrderModel.getScoresOrder()
                        .doOnNext(new Consumer<OrderResponse>() {
                            @Override
                            public void accept(OrderResponse orderResponse) throws Exception {
                                mView.onScoresOrder(orderResponse);
                            }
                        }),
                mMyOrderModel.getExchangeOrder()
                        .doOnNext(
                                new Consumer<OrderResponse>() {
                                    @Override
                                    public void accept(OrderResponse orderResponse) throws Exception {
                                        mView.onExchangeOrder(orderResponse);
                                    }
                                }
                        ),
                mMyOrderModel.getCrowdfundingOrder()
                        .doOnNext(new Consumer<OrderResponse>() {
                            @Override
                            public void accept(OrderResponse orderResponse) throws Exception {
                                mView.onCrowdfundingOrder(orderResponse);
                            }
                        }),
                new Function3<OrderResponse, OrderResponse, OrderResponse, String>() {
                    @Override
                    public String apply(OrderResponse normalRsp, OrderResponse normalRsp2, OrderResponse normalRsp3) throws Exception {
                        return "success";
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                new MyObserver<String>() {
                    @Override
                    public void onMyNext(String s) {
                        mView.onDataResponseSuccess();
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });
    }
}