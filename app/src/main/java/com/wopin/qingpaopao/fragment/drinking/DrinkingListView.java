package com.wopin.qingpaopao.fragment.drinking;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.CupListAdapter;
import com.wopin.qingpaopao.bean.response.CupListRsp;
import com.wopin.qingpaopao.bean.response.WifiConfigToCupRsp;
import com.wopin.qingpaopao.utils.ScreenUtils;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;

public class DrinkingListView extends Fragment implements View.OnClickListener {

    private View mRootView;
    private View mBackBtn;
    private SwipeMenuRecyclerView mCupListRv;
    private Button mAddDeviceBt;
    private OnDrinkingListViewCallback mOnDrinkingListViewCallback;
    private CupListAdapter.OnCupItemClickCallback mOnCupItemClickCallback;
    private CupListAdapter mCupListAdapter;
    private ArrayList<CupListRsp.CupBean> cupBeanList;
    private CupListRsp.CupBean currentConnectCup;

    public void setOnDrinkingListViewCallback(OnDrinkingListViewCallback onDrinkingListViewCallback) {
        mOnDrinkingListViewCallback = onDrinkingListViewCallback;
    }

    public void setOnCupItemClickCallback(CupListAdapter.OnCupItemClickCallback onCupItemClickCallback) {
        mOnCupItemClickCallback = onCupItemClickCallback;
    }

    @Override
    public void onDestroy() {
        mCupListAdapter.destroy();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.view_drinking_list, container, false);
        mBackBtn = mRootView.findViewById(R.id.iv_back);
        mCupListRv = mRootView.findViewById(R.id.rv_device_list);
        mAddDeviceBt = mRootView.findViewById(R.id.bt_add_device);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 创建菜单：
        SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getContext())
                        .setBackground(R.color.colorRed)
                        .setText("Delete") // 文字。
                        .setImage(R.mipmap.ic_delete) // 图标。
                        .setTextColor(Color.WHITE) // 文字颜色。
                        .setTextSize(16) // 文字大小。
                        .setWidth(ScreenUtils.dip2px(getContext(), 70))
                        .setHeight(ScreenUtils.dip2px(getContext(), 75));
                rightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。.
            }
        };
        // 设置监听器。
        mCupListRv.setSwipeMenuCreator(mSwipeMenuCreator);

        SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
                menuBridge.closeMenu();
                int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
                int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
                mCupListAdapter.removeOneItem(adapterPosition);
            }
        };
// 菜单点击监听。
        mCupListRv.setSwipeMenuItemClickListener(mMenuItemClickListener);

        mAddDeviceBt.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mCupListRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mCupListAdapter = new CupListAdapter();
        mCupListAdapter.setOnCupItemClickCallback(mOnCupItemClickCallback);
        mCupListRv.setAdapter(mCupListAdapter);

        setDatasUi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_add_device:
                ChooseAddDeviceWayFragment chooseAddDeviceWayFragment = new ChooseAddDeviceWayFragment();
                chooseAddDeviceWayFragment.show(getChildFragmentManager(), ChooseAddDeviceWayFragment.TAG);
                chooseAddDeviceWayFragment.setOnDrinkingListViewCallback(mOnDrinkingListViewCallback);
                break;
            case R.id.iv_back:
                mOnDrinkingListViewCallback.backToDrinkingStartView();
                break;
        }
    }

    public void notifyCupList(ArrayList<CupListRsp.CupBean> cupBeanList, CupListRsp.CupBean currentConnectCup) {
        this.cupBeanList = cupBeanList;
        this.currentConnectCup = currentConnectCup;
        setDatasUi();
    }

    private void setDatasUi() {
//        if (mBackBtn != null) {
//            mBackBtn.setVisibility(currentConnectCup != null ? View.VISIBLE : View.GONE);
//        }
        if (mCupListAdapter != null && cupBeanList != null) {
            mCupListAdapter.setCupBeans(cupBeanList);
        }
    }


    public interface OnDrinkingListViewCallback {
        void backToDrinkingStartView();

        void onBluetoothDeviceFind(BluetoothDevice bluetoothDevice);

        void onWifiDeviceFind(WifiConfigToCupRsp wifiConfigToCupRsp);
    }
}
