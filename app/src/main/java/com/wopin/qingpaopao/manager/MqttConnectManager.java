package com.wopin.qingpaopao.manager;

import android.os.Handler;
import android.util.Log;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.CupListRsp;
import com.wopin.qingpaopao.command.mqtt.MqttColorCommand;
import com.wopin.qingpaopao.command.mqtt.MqttConnectDeviceCommand;
import com.wopin.qingpaopao.command.mqtt.MqttSwitchCleanCommand;
import com.wopin.qingpaopao.command.mqtt.MqttSwitchElectrolyzeCommand;
import com.wopin.qingpaopao.command.mqtt.MqttSwitchLightCommand;
import com.wopin.qingpaopao.model.DrinkingModel;
import com.wopin.qingpaopao.presenter.BasePresenter;
import com.wopin.qingpaopao.utils.HttpUtil;
import com.wopin.qingpaopao.utils.ToastUtils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class MqttConnectManager extends ConnectManager<MqttConnectManager.MqttUpdaterBean> {

    private static final String TAG = "MqttConnectManager";
    private static MqttConnectManager mMqttConnectManager;
    //Mqtt Related
    private static final String URL = "tcp://wifi.h2popo.com:8083";
    private static final String username = "wopin";
    private static final String password = "wopinH2popo";
    private static final String clientId = "clientId";

    private MqttClient client;
    private long lastSetColorTime;//限制setColor每0.5秒才能set一次

    private TreeMap<String, MqttUpdaterBean> mOnlineMqttBeans;
    private Handler mHandler;
    private static final int CHECK_DISCONNECT_TIME = 10000;//每10秒检测一下是否有离线Cup（10秒内都没接收到消息，视为掉线），同时请求Cuplist以自动连接设备
    private Runnable mDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: checking disconnect device");
            Iterator<Map.Entry<String, MqttUpdaterBean>> iterator = mOnlineMqttBeans.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, MqttUpdaterBean> next = iterator.next();
                if (next.getValue().getOnlineTime() < (System.currentTimeMillis() - CHECK_DISCONNECT_TIME)) {//10秒内都没接收到消息，视为掉线
                    Log.d(TAG, "find one device dissconnect: " + next.getKey());
                    onDissconnectDevice(next.getValue());
                    iterator.remove();
                }
            }
            HttpUtil.subscribeNetworkTask(new DrinkingModel().getCupList(), null);//同时请求Cuplist以自动连接设备
            mHandler.postDelayed(mDisconnectRunnable, CHECK_DISCONNECT_TIME);
        }
    };

    private MqttConnectManager() {
        mOnlineMqttBeans = new TreeMap<>();
        mHandler = new Handler();
    }

    public static MqttConnectManager getInstance() {
        if (mMqttConnectManager == null) {
            mMqttConnectManager = new MqttConnectManager();
        }
        return mMqttConnectManager;
    }

    @Override
    protected void connectToServer(final OnServerConnectCallback onServerConnectCallback) {
        //Check Network
        Log.d(TAG, "connectToServer: try to connectToServer");
        if (client == null || !client.isConnected()) {
            if (client != null) {
                disconnectServer();
            }
            DrinkingModel drinkingModel = new DrinkingModel();
            HttpUtil.subscribeNetworkTask(
                    drinkingModel.getCupList()
                            .timeout(5, TimeUnit.SECONDS)
                            .retryWhen(new Function<Observable<Throwable>, ObservableSource<Long>>() {
                                @Override
                                public ObservableSource<Long> apply(Observable<Throwable> throwableObservable) throws Exception {
                                    return throwableObservable.zipWith(Observable.range(1, 20), new BiFunction<Throwable, Integer, Integer>() {
                                        @Override
                                        public Integer apply(Throwable throwable, Integer integer) throws Exception {
                                            if (integer >= 10) {
                                                throw new Exception("retry time over range 10.");
                                            }
                                            return integer;
                                        }
                                    }).flatMap(new Function<Integer, ObservableSource<Long>>() {
                                        @Override
                                        public ObservableSource<Long> apply(Integer integer) throws Exception {
                                            return Observable.timer(2, TimeUnit.SECONDS);
                                        }
                                    });
                                }
                            })
                    , new BasePresenter.MyObserver<CupListRsp>() {
                        @Override
                        public void onMyNext(CupListRsp cupListRsp) {
                            haveNetworkToConnectMqtt(onServerConnectCallback);
                        }

                        @Override
                        public void onMyError(String errorMessage) {
                            ToastUtils.showShort(R.string.failure_to_mqtt);
                            onServerConnectCallback.onDisconnectServerCallback();
                        }
                    });
        } else {
            onServerConnectCallback.onConnectServerCallback();
        }

    }

    private void haveNetworkToConnectMqtt(final OnServerConnectCallback onServerConnectCallback) {
        try {
            if (client == null || !client.isConnected()) {
                if (client != null) {
                    disconnectServer();
                }
                MqttConnectOptions conOpt = new MqttConnectOptions();
                conOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
                conOpt.setCleanSession(true);
                conOpt.setPassword(password.toCharArray());
                conOpt.setUserName(username);
                // Construct an MQTT blocking mode client
                String tmpDir = System.getProperty("java.io.tmpdir");
                MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
                client = new MqttClient(URL, clientId, dataStore);
                // Set this wrapper as the callback handler
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        disconnectServer();
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        //收到消息
                        Log.d(TAG, "messageArrived: " + topic + "    " + message.toString());

                        MqttUpdaterBean t = new MqttUpdaterBean();
                        t.setSsid(topic);
                        t.setMessage(message.toString());

                        if (mOnlineMqttBeans.get(topic) == null) {//没有连接过
                            onConnectDevice(t);
                        }
                        t.setOnlineTime(System.currentTimeMillis());
                        mOnlineMqttBeans.put(topic, t);

                        onDatasUpdate(t);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
                client.connect(conOpt);
                Log.d(TAG, "connect MqttConnectManager");
                mHandler.removeCallbacks(mDisconnectRunnable);
                mHandler.postDelayed(mDisconnectRunnable, CHECK_DISCONNECT_TIME);
            }
            onServerConnectCallback.onConnectServerCallback();
        } catch (MqttException e) {
            e.printStackTrace();
            onServerConnectCallback.onDisconnectServerCallback();
        }
    }

    @Override
    public void disconnectServer() {
        Log.d(TAG, "disconnect MqttConnectManager");
        mHandler.removeCallbacks(mDisconnectRunnable);
        for (Map.Entry<String, MqttUpdaterBean> next : mOnlineMqttBeans.entrySet()) {
            onDissconnectDevice(next.getValue());
        }
        mOnlineMqttBeans.clear();
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            client = null;
        }
    }

    public boolean subscribe(String topicName) {
        boolean flag = false;

        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topicName, 0);
                flag = true;
            } catch (MqttException e) {

            }
        }
        return flag;
    }

    public boolean unsubscribe(String topicName) {
        boolean flag = false;

        if (client != null && client.isConnected()) {
            try {
                client.unsubscribe(topicName);
                flag = true;
            } catch (MqttException e) {

            }
        }
        return flag;
    }

    public boolean publish(String topicName, String message) {
        Log.d(TAG, "publish() called with: topicName = [" + topicName + "], message = [" + message + "]");
        boolean flag = false;
        byte[] payload = message.getBytes();
        if (client != null && client.isConnected()) {
            // Create and configure a message
            MqttMessage mqttMessage = new MqttMessage(payload);
            mqttMessage.setQos(0);

            // Send the message to the server, control is not returned until
            // it has been delivered to the server meeting the specified
            // quality of service.
            try {
                client.publish(topicName + "-D", mqttMessage);
                flag = true;
            } catch (MqttException e) {

            }
        }
        return flag;
    }

    /**
     * =============================================  Device =============================================
     */

    public void connectDevice(String ssid) {
        super.connectDevice(new MqttConnectDeviceCommand(ssid));
    }

    public void disconnectDevice(String ssid) {
        Iterator<Map.Entry<String, MqttUpdaterBean>> iterator = mOnlineMqttBeans.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MqttUpdaterBean> next = iterator.next();
            if (next.getKey().equals(ssid)) {
                onDissconnectDevice(next.getValue());
                iterator.remove();
            }
        }
    }

    public void switchCupElectrolyze(String ssid, int time) {
        super.switchCupElectrolyze(new MqttSwitchElectrolyzeCommand(ssid, time));
    }

    public void switchCupLight(String ssid, boolean isLightOn) {
        super.switchCupLight(new MqttSwitchLightCommand(ssid, isLightOn));
    }

    public void switchCupClean(String ssid, boolean isClean) {
        super.switchCupClean(new MqttSwitchCleanCommand(ssid, isClean));
    }

    public void setColor(String ssid, String color) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - 500 > lastSetColorTime) {
            super.setColor(new MqttColorCommand(ssid, color));
            lastSetColorTime = currentTime;
        }
    }



    /**
     * =============================================  Updater =============================================
     */

    public class MqttUpdaterBean {
        private String ssid;
        private String message;
        private long onlineTime;

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getOnlineTime() {
            return onlineTime;
        }

        public void setOnlineTime(long onlineTime) {
            this.onlineTime = onlineTime;
        }
    }
}
