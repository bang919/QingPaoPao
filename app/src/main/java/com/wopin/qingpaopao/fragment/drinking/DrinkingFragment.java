package com.wopin.qingpaopao.fragment.drinking;

import android.bluetooth.BluetoothDevice;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.CupListAdapter;
import com.wopin.qingpaopao.bean.response.CupListRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTodayRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTotalRsp;
import com.wopin.qingpaopao.bean.response.WifiConfigToCupRsp;
import com.wopin.qingpaopao.common.Constants;
import com.wopin.qingpaopao.fragment.BaseMainFragment;
import com.wopin.qingpaopao.presenter.DrinkingPresenter;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.view.DrinkingView;

import java.util.ArrayList;

public class DrinkingFragment extends BaseMainFragment<DrinkingPresenter> implements DrinkingView, CupListAdapter.OnCupItemClickCallback, DrinkingListView.OnDrinkingListViewCallback, DrinkingStartView.OnDrinkingStartCallback {

    private View mRootView;
    private DrinkingListView mDrinkingListView;
    private DrinkingStartView mDrinkingStartView;
    private Fragment currentFragment;
    private ArrayList<CupListRsp.CupBean> mOnlineCups;

    @Override
    protected int getLayout() {
        return R.layout.fragment_drinking;
    }

    @Override
    protected DrinkingPresenter initPresenter() {
        return new DrinkingPresenter(getContext(), this);
    }

    @Override
    protected void initView(View rootView) {
        mRootView = rootView;
    }

    private void setLoadingVisibility(boolean isVisibility) {
        mRootView.findViewById(R.id.progress_bar_layout).setVisibility(isVisibility ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initEvent() {
        mDrinkingListView = new DrinkingListView();
        mDrinkingListView.setOnDrinkingListViewCallback(this);
        mDrinkingListView.setOnCupItemClickCallback(this);
        mDrinkingStartView = new DrinkingStartView();
        mDrinkingStartView.setPresenterAndCallback(mPresenter, this);
        switchFragment(mDrinkingStartView);

        mPresenter.getCupList();
    }

    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (!targetFragment.isAdded()) {
            transaction
                    .add(R.id.fragment_layout, targetFragment)
                    .commit();
        } else {
            transaction
                    .show(targetFragment)
                    .commit();
        }
        currentFragment = targetFragment;
    }

    @Override
    public void refreshData() {

    }

    @Override
    public void backToDrinkingStartView() {
        mPresenter.getDrinkCount();
        switchFragment(mDrinkingStartView);
    }

    @Override
    public void onBluetoothDeviceFind(BluetoothDevice bluetoothDevice) {
        //这里是从新搜索出来的Device，所以调用firstTimeAddBleCup
        mPresenter.firstTimeAddCup(bluetoothDevice);
    }

    @Override
    public void onWifiDeviceFind(WifiConfigToCupRsp wifiConfigToCupRsp) {
        mPresenter.firstTimeAddCup(wifiConfigToCupRsp);
        ToastUtils.showShort(R.string.success_setting_wifi);
    }

    @Override
    public void onLoading() {
        setLoadingVisibility(true);
    }

    @Override
    public void onCupList(final ArrayList<CupListRsp.CupBean> cupBeanList, final CupListRsp.CupBean currentConnectCup) {
        mRootView.post(new Runnable() {
            @Override
            public void run() {
                setLoadingVisibility(false);
                mOnlineCups = new ArrayList<>();
                for (CupListRsp.CupBean cupBean : cupBeanList) {
                    if (cupBean.isConnecting()) {
                        mOnlineCups.add(cupBean);
                    }
                }
                mDrinkingStartView.setOnlineCups(mOnlineCups);
                mDrinkingListView.notifyCupList(cupBeanList, currentConnectCup);
            }
        });
    }

    @Override
    public void onTodayDrink(DrinkListTodayRsp drinkListTodayRsp) {
        mDrinkingStartView.setTodayDrink(drinkListTodayRsp);

    }

    @Override
    public void onTotalDrink(DrinkListTotalRsp drinkListTotalRsp) {
        mDrinkingStartView.setTotalDrink(drinkListTotalRsp);
    }

    @Override
    public void onError(String errorMsg) {
        setLoadingVisibility(false);
        ToastUtils.showShort(errorMsg);
    }

    @Override
    public void onCupItemClick(final CupListRsp.CupBean cupBean, int position) {
        DeviceDetailFragment.getDeviceDetailFragment(cupBean, new DeviceDetailFragment.DeviceDetailFragmentCallback() {
            @Override
            public void onRename(String newName) {
                mPresenter.renameCup(cupBean, newName);
            }

            @Override
            public void onColorChange(int cupColor) {
                mPresenter.updateCupColor(cupBean, cupColor);
            }
        }).show(getChildFragmentManager(), DeviceDetailFragment.TAG);
    }


    @Override
    public void onCupItemDelete(CupListRsp.CupBean cupBean, int position) {
        if (cupBean.isConnecting()) {
            mPresenter.disconnectCup(cupBean);
        }
        mPresenter.deleteACup(cupBean.getUuid());
    }

    @Override
    public void onCupItemTurn(CupListRsp.CupBean cupBean, boolean isOn) {
        if (cupBean.getType().equals(Constants.BLE) && isOn) {
            mPresenter.connectCup(cupBean);
        } else if (cupBean.getType().equals(Constants.WIFI) && isOn) {
            mPresenter.connectCup(cupBean);
        }
    }

    @Override
    public void showDrinkingDeviceList() {
        switchFragment(mDrinkingListView);
    }
}
