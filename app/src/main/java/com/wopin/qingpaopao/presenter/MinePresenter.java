package com.wopin.qingpaopao.presenter;

import android.support.v4.app.Fragment;

import com.wopin.qingpaopao.bean.response.CheckNewMessageRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTodayRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTotalRsp;
import com.wopin.qingpaopao.bean.response.LoginRsp;
import com.wopin.qingpaopao.bean.response.NormalRsp;
import com.wopin.qingpaopao.model.DrinkingModel;
import com.wopin.qingpaopao.model.MessageModel;
import com.wopin.qingpaopao.model.MineModel;
import com.wopin.qingpaopao.view.MineView;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MinePresenter extends PhotoPresenter<MineView> {

    private MessageModel mMessageModel;
    private File portraitFile;

    public MinePresenter(Fragment fragment, MineView view) {
        super(fragment, view);
        mMessageModel = new MessageModel();
    }

    public void requestDrinkData() {
        DrinkingModel drinkingModel = new DrinkingModel();
        subscribeNetworkTask(getClass().getSimpleName().concat("requestDrinkData"),
                Observable.zip(drinkingModel.getDrinkList(), drinkingModel.getTodayDrinkList(),
                        new BiFunction<DrinkListTotalRsp, DrinkListTodayRsp, String>() {
                            @Override
                            public String apply(DrinkListTotalRsp drinkListTotalRsp, DrinkListTodayRsp drinkListTodayRsp) throws Exception {
                                mView.onTotalDrink(drinkListTotalRsp);
                                mView.onTodayDrink(drinkListTodayRsp);
                                return "Success";
                            }
                        }), new MyObserver<String>() {
                    @Override
                    public void onMyNext(String s) {

                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });

        subscribeNetworkTask(getClass().getSimpleName().concat("checkNewMessage"), mMessageModel.checkNewMessage(), new MyObserver<CheckNewMessageRsp>() {
            @Override
            public void onMyNext(CheckNewMessageRsp checkNewMessageRsp) {
                mView.onNewMessage(checkNewMessageRsp);
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }

    public void refreshUserData() {
        Observable<LoginRsp> userDataOb = new MineModel().getUserData();
        subscribeNetworkTask(getClass().getSimpleName().concat("refreshUserData"), userDataOb, new MyObserver<LoginRsp>() {
            @Override
            public void onMyNext(LoginRsp loginRsp) {
                mView.onRefreshUserData();
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }

    @Override
    public void onLuBanError(Throwable e) {
        if (portraitFile != null) {
            deletePhotoFile(portraitFile.getPath());
            portraitFile = null;
        }
    }

    @Override
    public void onLuBanSuccess(File file) {
        portraitFile = file;
        final MineModel mineModel = new MineModel();
        subscribeNetworkTask(getClass().getSimpleName().concat("updateImage"),
                mineModel.uploadImage(file).flatMap(new Function<NormalRsp, ObservableSource<LoginRsp>>() {
                    @Override
                    public ObservableSource<LoginRsp> apply(NormalRsp normalRsp) throws Exception {
                        return mineModel.getUserData();
                    }
                }), new MyObserver<LoginRsp>() {
                    @Override
                    public void onMyNext(LoginRsp normalRsp) {
                        deletePhotoFile();
                        mView.onRefreshUserData();
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        deletePhotoFile();
                        mView.onError(errorMessage);
                    }
                });
    }

    public void deletePhotoFile() {
        if (portraitFile != null) {
            deletePhotoFile(portraitFile.getPath());
            portraitFile = null;
        }
    }
}
