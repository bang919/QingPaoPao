package com.wopin.qingpaopao.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.wopin.qingpaopao.command.wifi.WifiConnectDeviceCommand;
import com.wopin.qingpaopao.common.MyApplication;
import com.wopin.qingpaopao.utils.WifiConenctUtil;

public class WifiConnectManager extends ConnectManager<WifiConnectManager.WifiUpdaterBean> {

    private static final String TAG = "WifiConnectManager";
    private static final int CONNECT_WAIT_TIME = 5000;
    private static WifiConnectManager mWifiConnectManager;
    private WifiManager wifiManager;
    private BroadcastReceiver mWifiConnectBroadcastReceiver;
    private WifiInfo mBeforeConnectionInfo;
    private Handler mHandler;
    private Runnable mDisconnectRunnable;
    private boolean tryingConnect;
    private WifiUpdaterBean tryingConnectWifiBean;

    private WifiConnectManager() {
        wifiManager = (WifiManager) MyApplication.getMyApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mBeforeConnectionInfo = wifiManager.getConnectionInfo();
        mDisconnectRunnable = new Runnable() {
            @Override
            public void run() {
                tryingConnect = false;
                onDissconnectDevice(tryingConnectWifiBean);
                disconnectServer();
            }
        };
    }

    public static WifiConnectManager getInstance() {
        if (mWifiConnectManager == null) {
            mWifiConnectManager = new WifiConnectManager();
        }
        return mWifiConnectManager;
    }

    @Override
    protected void connectToServer(final OnServerConnectCallback onServerConnectCallback) {
        if (mWifiConnectBroadcastReceiver == null) {
            Log.d(TAG, "connectToServer");
            mHandler = new Handler();
            mWifiConnectBroadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                        int wifState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                        if (wifState != WifiManager.WIFI_STATE_ENABLED) {//没有wifi

                        }
                    } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                        int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                        if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                            Log.e(TAG, "onReceive:密码错误");
                        }
                    } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        NetworkInfo.DetailedState state = ((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState();
                        setWifiState(state);
                    }
                }
            };
            MyApplication.getMyApplicationContext().registerReceiver(mWifiConnectBroadcastReceiver, makeFilter());
        }
        //延迟callback，防止WifiState返回第一个CONNECTED造成数据错乱
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onServerConnectCallback.onConnectServerCallback();
            }
        }, 500);
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        return filter;
    }

    @Override
    public void disconnectServer() {
        Log.d(TAG, "disConenctServer");
        if (mHandler != null) {
            mHandler.removeCallbacks(mDisconnectRunnable);
            mHandler = null;
        }
        if (mWifiConnectBroadcastReceiver != null) {
            MyApplication.getMyApplicationContext().unregisterReceiver(mWifiConnectBroadcastReceiver);
            mWifiConnectBroadcastReceiver = null;
        }
        if (mBeforeConnectionInfo != null) {
            wifiManager.enableNetwork(mBeforeConnectionInfo.getNetworkId(), true);//重新连接之前的wifi
        }
    }

    /**
     * 显示wifi状态
     *
     * @param state
     */
    private void setWifiState(final NetworkInfo.DetailedState state) {
        if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
            Log.d(TAG, "AUTHENTICATING");
        } else if (state == NetworkInfo.DetailedState.BLOCKED) {
            Log.d(TAG, "BLOCKED");
        } else if (state == NetworkInfo.DetailedState.CONNECTED) {//连接成功
            Log.d(TAG, "连接成功");
            if (tryingConnect && tryingConnectWifiBean != null) {
                tryingConnect = false;
                mHandler.removeCallbacks(mDisconnectRunnable);
                onConnectDevice(tryingConnectWifiBean);
                tryingConnectWifiBean = null;
            }
        } else if (state == NetworkInfo.DetailedState.CONNECTING) {//连接中...
            Log.d(TAG, "连接中...");
        } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {//断开连接
            mHandler.postDelayed(mDisconnectRunnable, CONNECT_WAIT_TIME);//5秒后断开连接
            tryingConnect = true;
            Log.d(TAG, "断开连接");
        } else if (state == NetworkInfo.DetailedState.DISCONNECTING) {//断开连接中...
        } else if (state == NetworkInfo.DetailedState.FAILED) {//连接失败
            Log.d(TAG, "连接失败");
        } else if (state == NetworkInfo.DetailedState.IDLE) {
            Log.d(TAG, "IDLE");
        } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
            Log.d(TAG, "OBTAINING_IPADDR");
        } else if (state == NetworkInfo.DetailedState.SCANNING) {
            Log.d(TAG, "SCANNING");
        } else if (state == NetworkInfo.DetailedState.SUSPENDED) {
            Log.d(TAG, "SUSPENDED");
        }
    }

    /**
     * =============================================  Device =============================================
     */

    public void connectDevice(String ssid, String password) {
        tryingConnectWifiBean = new WifiUpdaterBean();
        tryingConnectWifiBean.setSsid(ssid);
        super.connectDevice(new WifiConnectDeviceCommand(wifiManager, WifiConenctUtil.createWifiInfo(wifiManager, ssid, password)));
    }

    public void disconnectDevice() {
    }

    public void switchCupElectrolyze(boolean isOn) {
    }

    public void switchCupLight(boolean isLightOn) {
    }

    public void switchCupClean(boolean isClean) {
    }

    public void setColor(String color) {
    }

    /**
     * =============================================  Updater =============================================
     */

    public class WifiUpdaterBean {
        private String ssid;

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }
    }
}
