package com.wopin.qingpaopao.presenter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.CupListRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTodayRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTotalRsp;
import com.wopin.qingpaopao.bean.response.NormalRsp;
import com.wopin.qingpaopao.bean.response.WifiConfigToCupRsp;
import com.wopin.qingpaopao.common.Constants;
import com.wopin.qingpaopao.common.MyApplication;
import com.wopin.qingpaopao.manager.BleConnectManager;
import com.wopin.qingpaopao.manager.MqttConnectManager;
import com.wopin.qingpaopao.manager.Updater;
import com.wopin.qingpaopao.model.DrinkingModel;
import com.wopin.qingpaopao.utils.SPUtils;
import com.wopin.qingpaopao.view.DrinkingView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;


public class DrinkingPresenter extends BasePresenter<DrinkingView> {

    private DrinkingModel mDrinkingModel;
    private ArrayList<CupListRsp.CupBean> mCupBeans;//API获取连接过的设备
    private ArrayList<String> mOnlineCups;//可以控制的设备，记录uuid
    private CupListRsp.CupBean mCurrentControlCup;//当前控制的设备
    private Updater<BleConnectManager.BleUpdaterBean> mBleUpdater;
    private Updater<MqttConnectManager.MqttUpdaterBean> mMqttUpdater;
    private Handler mHandler;
    private Runnable mDeviceConnectTimeoutR;

    private Object mFirstTimeAddDevice;//会在onConnectDevice后清空  BluetoothDevice/WifiRsp

    public DrinkingPresenter(Context context, DrinkingView view) {
        super(context, view);
        mHandler = new Handler();
        mDrinkingModel = new DrinkingModel();
        mOnlineCups = new ArrayList<>();
        mBleUpdater = new Updater<BleConnectManager.BleUpdaterBean>() {
            @Override
            public void onConnectDevice(BleConnectManager.BleUpdaterBean bleUpdaterBean) {
                if (mOnlineCups.indexOf(bleUpdaterBean.getUuid()) < 0) {
                    mOnlineCups.add(bleUpdaterBean.getUuid());
                }
                boolean needAdd = true;
                if (mCupBeans != null) {
                    for (CupListRsp.CupBean cupBean : mCupBeans) {
                        if (cupBean.getUuid().equals(bleUpdaterBean.getUuid())) {
                            needAdd = false;
                        }
                    }
                }
                if (needAdd && mFirstTimeAddDevice != null) {
                    addACup(Constants.BLE, bleUpdaterBean.getUuid(), ((BluetoothDevice) mFirstTimeAddDevice).getName(), bleUpdaterBean.getAddress());
                } else {
                    updateCupListUi();
                }
                mFirstTimeAddDevice = null;
            }

            @Override
            public void onDissconnectDevice(BleConnectManager.BleUpdaterBean bleUpdaterBean) {
                String address = bleUpdaterBean.getAddress();
                for (CupListRsp.CupBean cupBean : mCupBeans) {
                    if (address.equals(cupBean.getAddress())) {
                        mOnlineCups.remove(cupBean.getUuid());
                        updateCupListUi();
                        break;
                    }
                }
            }
        };
        mMqttUpdater = new Updater<MqttConnectManager.MqttUpdaterBean>() {
            @Override
            public void onConnectDevice(MqttConnectManager.MqttUpdaterBean mqttUpdaterBean) {
                if (mOnlineCups.indexOf(mqttUpdaterBean.getSsid()) < 0) {
                    mOnlineCups.add(mqttUpdaterBean.getSsid());
                }
                boolean needAdd = true;
                if (mCupBeans != null) {
                    for (CupListRsp.CupBean cupBean : mCupBeans) {
                        if (cupBean.getUuid().equals(mqttUpdaterBean.getSsid())) {
                            needAdd = false;
                        }
                    }
                }
                if (needAdd && mFirstTimeAddDevice != null) {
                    addACup(Constants.WIFI, mqttUpdaterBean.getSsid(), mqttUpdaterBean.getSsid(), null);
                } else {
                    updateCupListUi();
                }
                mFirstTimeAddDevice = null;
                mHandler.removeCallbacks(mDeviceConnectTimeoutR);
            }

            @Override
            public void onDissconnectDevice(MqttConnectManager.MqttUpdaterBean mqttUpdaterBean) {
                mOnlineCups.remove(mqttUpdaterBean.getSsid());
                updateCupListUi();
            }
        };
        BleConnectManager.getInstance().addUpdater(mBleUpdater);
        MqttConnectManager.getInstance().addUpdater(mMqttUpdater);
    }

    //更新在线设备的UI
    private void updateCupListUi() {
        for (CupListRsp.CupBean cupBean : mCupBeans) {
            boolean isConnect = mOnlineCups.indexOf(cupBean.getUuid()) != -1;
            if (isConnect && mCurrentControlCup == null) {
                mCurrentControlCup = cupBean;
            }
            cupBean.setConnecting(isConnect);
        }
        mView.onCupList(mCupBeans, mCurrentControlCup);
    }

    @Override
    public void destroy() {
        mHandler.removeCallbacks(mDeviceConnectTimeoutR);
        BleConnectManager.getInstance().removeUpdater(mBleUpdater);
        MqttConnectManager.getInstance().removeUpdater(mMqttUpdater);
        super.destroy();
    }

    public void setCurrentControlCup(CupListRsp.CupBean cup) {
        mCurrentControlCup = cup;
    }

    public CupListRsp.CupBean getCurrentControlCup() {
        return mCurrentControlCup;
    }

    /**
     * 第一次手动连接杯子
     */
    public <T> void firstTimeAddCup(T device) {
        mFirstTimeAddDevice = device;
        if (device instanceof BluetoothDevice) {//连接没有失败的回调，暂时不加loading，以后可以加个超时
            BleConnectManager.getInstance().connectDevice(((BluetoothDevice) device).getAddress());
        } else if (device instanceof WifiConfigToCupRsp) {
            mView.onLoading();
            MqttConnectManager.getInstance().connectDevice(((WifiConfigToCupRsp) device).getDevice_id());
            //设置连接超时45秒
            mDeviceConnectTimeoutR = new Runnable() {
                @Override
                public void run() {
                    if (mFirstTimeAddDevice != null) {
                        mView.onError(mContext.getString(R.string.connect_timeout));
                    }
                }
            };
            mHandler.removeCallbacks(mDeviceConnectTimeoutR);
            mHandler.postDelayed(mDeviceConnectTimeoutR, 45000);
        } else {
            mView.onError(mContext.getString(R.string.known_error));
        }
    }

    /**
     * 点击list连接杯子
     */
    public void connectCup(CupListRsp.CupBean cupBean) {
        if (cupBean.getType().equals(Constants.BLE)) {
            BleConnectManager.getInstance().connectDevice(cupBean.getAddress());
        } else if (cupBean.getType().equals(Constants.WIFI)) {
//            mView.onLoading();//手动连接还是不要loading啦，不然如果wifi模块不在线怎么办？或者设置一个超时也行
            MqttConnectManager.getInstance().connectDevice(cupBean.getUuid());
        } else {
            mView.onError(mContext.getString(R.string.known_error));
        }
    }

    /**
     * 断开杯子连接
     */
    public void disconnectCup(CupListRsp.CupBean cupBean) {
        if (mCurrentControlCup != null && cupBean.getUuid().equals(mCurrentControlCup.getUuid())) {
            mCurrentControlCup = null;
        }
        if (cupBean != null) {
            if (cupBean.getType().equals(Constants.BLE)) {//Ble
                BleConnectManager.getInstance().disconnectDevice(cupBean.getAddress());
            } else {//Wifi
                MqttConnectManager.getInstance().disconnectDevice(cupBean.getUuid());
            }
        }
    }

    /**
     * 开关电解
     */
    public void switchCupElectrolyze(int time) {
        if (mCurrentControlCup != null) {
            if (mCurrentControlCup.getType().equals(Constants.BLE)) {//Ble
                BleConnectManager.getInstance().switchCupElectrolyze(mCurrentControlCup.getAddress(), time);
            } else {//Wifi
                MqttConnectManager.getInstance().switchCupElectrolyze(mCurrentControlCup.getUuid(), time);
            }
        }
        MyLocation location = getLocation();
        if (time > 0 && location != null) {
            subscribeNetworkTask(mDrinkingModel.sendLocal(mCurrentControlCup.getUuid(), location.getLatitude(), location.getLongtitude()));
        }
    }

    /**
     * 开关灯
     */
    public void switchCupLight(boolean isLightOn) {
        if (mCurrentControlCup != null) {
            if (mCurrentControlCup.getType().equals(Constants.BLE)) {//Ble
                BleConnectManager.getInstance().switchCupLight(mCurrentControlCup.getAddress(), isLightOn);
            } else {//Wifi
                MqttConnectManager.getInstance().switchCupLight(mCurrentControlCup.getUuid(), isLightOn);
            }
        }
    }

    /**
     * 清洗杯子
     */
    public void switchCupClean(boolean isClean) {
        if (mCurrentControlCup != null) {
            if (mCurrentControlCup.getType().equals(Constants.BLE)) {//Ble
                BleConnectManager.getInstance().switchCupClean(mCurrentControlCup.getAddress(), isClean);
            } else {//Wifi
                MqttConnectManager.getInstance().switchCupClean(mCurrentControlCup.getUuid(), isClean);
            }
        }
    }

    /**
     * 调节颜色
     */
    public void setColor(String color) {
        if (mCurrentControlCup != null) {
            if (mCurrentControlCup.getType().equals(Constants.BLE)) {//Ble
                BleConnectManager.getInstance().setColor(mCurrentControlCup.getAddress(), color);
            } else {//Wifi
                MqttConnectManager.getInstance().setColor(mCurrentControlCup.getUuid(), color);
            }
        }
    }

    public void getCupList() {
        subscribeNetworkTask(getClass().getSimpleName().concat("getCupList"), mDrinkingModel.getCupList(), new MyObserver<CupListRsp>() {
            @Override
            public void onMyNext(CupListRsp cupListRsp) {
                mCupBeans = cupListRsp.getResult();
                updateCupListUi();
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }

    public void deleteACup(String uuid) {
        if (mCurrentControlCup != null && mCurrentControlCup.getUuid().equals(uuid)) {
            mCurrentControlCup = null;
        }
        subscribeNetworkTask(getClass().getSimpleName().concat("deleteACup"), mDrinkingModel.deleteACup(uuid), new MyObserver<NormalRsp>() {
            @Override
            public void onMyNext(NormalRsp normalRsp) {
                getCupList();
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }

    private void addACup(String type, String uuid, String name, String address) {
        subscribeNetworkTask(getClass().getSimpleName().concat("addACup"), mDrinkingModel.addOrUpdateACup(type, uuid, name, address, true),
                new MyObserver<NormalRsp>() {
                    @Override
                    public void onMyNext(NormalRsp normalRsp) {
                        getCupList();
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });
    }

    public void renameCup(CupListRsp.CupBean cupBean, String newName) {
        subscribeNetworkTask(getClass().getSimpleName().concat("renameCup"),
                mDrinkingModel.addOrUpdateACup(cupBean.getType(), cupBean.getUuid(), newName, cupBean.getAddress(), false),
                new MyObserver<NormalRsp>() {
                    @Override
                    public void onMyNext(NormalRsp normalRsp) {
                        getCupList();
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });
    }

    public void updateCupColor(CupListRsp.CupBean cupBean, int cupColor) {
        subscribeNetworkTask(getClass().getSimpleName().concat("updateCupColor"), mDrinkingModel.updateCupColor(cupBean.getUuid(), cupColor), new MyObserver<NormalRsp>() {
            @Override
            public void onMyNext(NormalRsp normalRsp) {
                getCupList();
            }

            @Override
            public void onMyError(String errorMessage) {
                mView.onError(errorMessage);
            }
        });
    }

    public void getDrinkCount() {
        //2秒延时，防止API drink 后数据还没改好
        subscribeNetworkTask(getClass().getSimpleName().concat("getDrinkList"),
                Observable.timer(2, TimeUnit.SECONDS).flatMap(new Function<Long, ObservableSource<DrinkListTotalRsp>>() {
                    @Override
                    public ObservableSource<DrinkListTotalRsp> apply(Long aLong) throws Exception {
                        return mDrinkingModel.getDrinkList();
                    }
                }), new MyObserver<DrinkListTotalRsp>() {
                    @Override
                    public void onMyNext(DrinkListTotalRsp drinkListTotalRsp) {
                        mView.onTotalDrink(drinkListTotalRsp);
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });
        subscribeNetworkTask(getClass().getSimpleName().concat("getTodayDrinkList"),
                Observable.timer(2, TimeUnit.SECONDS).flatMap(new Function<Long, ObservableSource<DrinkListTodayRsp>>() {
                    @Override
                    public ObservableSource<DrinkListTodayRsp> apply(Long aLong) throws Exception {
                        return mDrinkingModel.getTodayDrinkList();
                    }
                }), new MyObserver<DrinkListTodayRsp>() {
                    @Override
                    public void onMyNext(DrinkListTodayRsp drinkListTodayRsp) {
                        mView.onTodayDrink(drinkListTodayRsp);
                    }

                    @Override
                    public void onMyError(String errorMessage) {
                        mView.onError(errorMessage);
                    }
                });
    }

    public static class MyLocation implements Serializable {

        double latitude;
        double longtitude;

        public double getLongtitude() {
            return longtitude;
        }

        public void setLongtitude(double longtitude) {
            this.longtitude = longtitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
    }

    public static void setLocation(Location location) {
        MyLocation myLocation = new MyLocation();
        myLocation.setLatitude(location.getLatitude());
        myLocation.setLongtitude(location.getLongitude());
        SPUtils.putObject(MyApplication.getMyApplicationContext(), Constants.MY_LOCATION, myLocation);
    }

    public static MyLocation getLocation() {
        try {
            return SPUtils.getObject(MyApplication.getMyApplicationContext(), Constants.MY_LOCATION);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
