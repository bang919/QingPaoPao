package com.wopin.qingpaopao.command.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.wopin.qingpaopao.command.IConnectDeviceCommand;

import java.lang.reflect.Method;
import java.util.List;

public class WifiConnectDeviceCommand extends IConnectDeviceCommand<WifiConfiguration> {

    private static final String TAG = "WifiConnectCommand";
    private WifiManager wifiManager;

    public WifiConnectDeviceCommand(WifiManager wifiManager, WifiConfiguration target) {
        super(target);
        this.wifiManager = wifiManager;
    }

    @Override
    public void execute() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration c : configuredNetworks) {
                wifiManager.disableNetwork(c.networkId);
            }
        }
        wifiManager.disconnect();
        int networkId = getTarget().networkId > 0 ? getTarget().networkId : wifiManager.addNetwork(getTarget());
//        if (connectWifiByReflectMethod(wifiManager, networkId) == null) {
//            wifiManager.enableNetwork(networkId, true);
//            wifiManager.reconnect();
//        }
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
        wifiManager = null;
    }

    /**
     * 通过反射出不同版本的connect方法来连接Wifi
     */
    private Method connectWifiByReflectMethod(WifiManager wifiManager, int netId) {
        Method connectMethod = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.d(TAG, "connectWifiByReflectMethod road 1");
            // 反射方法： connect(int, listener) , 4.2 <= phone's android version
            for (Method methodSub : wifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connect".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wifiManager, netId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "connectWifiByReflectMethod Android "
                            + Build.VERSION.SDK_INT + " error!");
                    return null;
                }
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            // 反射方法: connect(Channel c, int networkId, ActionListener listener)
            // 暂时不处理4.1的情况 , 4.1 == phone's android version
            Log.d(TAG, "connectWifiByReflectMethod road 2");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Log.d(TAG, "connectWifiByReflectMethod road 3");
            // 反射方法：connectNetwork(int networkId) ,
            // 4.0 <= phone's android version < 4.1
            for (Method methodSub : wifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wifiManager, netId);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "connectWifiByReflectMethod Android "
                            + Build.VERSION.SDK_INT + " error!");
                    return null;
                }
            }
        } else {
            // < android 4.0
            return null;
        }
        return connectMethod;
    }

}
