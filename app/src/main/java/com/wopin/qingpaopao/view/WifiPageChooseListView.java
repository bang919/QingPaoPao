package com.wopin.qingpaopao.view;

import com.wopin.qingpaopao.bean.response.WifiConfigToCupRsp;
import com.wopin.qingpaopao.bean.response.WifiRsp;

import java.util.ArrayList;

public interface WifiPageChooseListView {

    void onWifiListResponse(ArrayList<WifiRsp> wifiRsps);

    void onWifiChangeAuto();

    void onWifiConfigToCupRsp(WifiConfigToCupRsp wifiConfigToCupRsp);

    void onError(String errorMsg);
}
