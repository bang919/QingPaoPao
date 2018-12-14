package com.wopin.qingpaopao.fragment.drinking;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.manager.Updater;
import com.wopin.qingpaopao.manager.WifiConnectManager;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.view.WifiSettingSuccessListener;

public class WifiFragmentPage2 extends Fragment implements View.OnClickListener {

    private View.OnClickListener mOnClickListener;
    private WifiSettingSuccessListener mWifiSettingSuccessListener;
    private Updater<WifiConnectManager.WifiUpdaterBean> mUpdater;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setWifiSettingSuccessListener(WifiSettingSuccessListener wifiSettingSuccessListener) {
        mWifiSettingSuccessListener = wifiSettingSuccessListener;
    }

    @Override
    public void onDestroy() {
        WifiConnectManager.getInstance().disconnectServer();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        WifiConnectManager.getInstance().removeUpdater(mUpdater);
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_page_2, container, false);
        rootView.findViewById(R.id.btn_scan).setOnClickListener(mOnClickListener);
        rootView.findViewById(R.id.btn_link_by_hand).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUpdater = new Updater<WifiConnectManager.WifiUpdaterBean>() {

            @Override
            public void onDissconnectDevice(WifiConnectManager.WifiUpdaterBean wifiUpdaterBean) {
                ToastUtils.showShort(getString(R.string.failure_connect_wifi));
            }

            @Override
            public void onConnectDevice(WifiConnectManager.WifiUpdaterBean wifiUpdaterBean) {
                jumpToWifiChooseListFragment(wifiUpdaterBean.getSsid());
            }
        };
        WifiConnectManager.getInstance().addUpdater(mUpdater);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_link_by_hand://第二页手动连接按钮
                LinkWifiByhandFragment linkWifiByhandFragment = new LinkWifiByhandFragment();
                linkWifiByhandFragment.setLinkWifiByhandCallback(new LinkWifiByhandFragment.LinkWifiByhandCallback() {
                    @Override
                    public void onWifiLinkByhand() {
                        jumpToWifiChooseListFragment("");
                    }
                });
                linkWifiByhandFragment.show(getChildFragmentManager(), LinkWifiByhandFragment.TAG);
                break;
        }
    }

    private void jumpToWifiChooseListFragment(String ssid) {
        WifiFragmentPageChooseList wifiFragmentPageChooseList = WifiFragmentPageChooseList.build(ssid);
        wifiFragmentPageChooseList.setWifiSettingSuccessListener(mWifiSettingSuccessListener);
        wifiFragmentPageChooseList.show(getChildFragmentManager(), WifiFragmentPageChooseList.TAG);
    }
}