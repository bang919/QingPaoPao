package com.wopin.qingpaopao.fragment.drinking;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.PopupWindowListAdapter;
import com.wopin.qingpaopao.bean.response.CupListRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTodayRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTotalRsp;
import com.wopin.qingpaopao.manager.MessageProxy;
import com.wopin.qingpaopao.manager.MessageProxyCallback;
import com.wopin.qingpaopao.presenter.DrinkingPresenter;
import com.wopin.qingpaopao.utils.PopupWindowUtil;
import com.wopin.qingpaopao.utils.ToastUtils;

import java.util.ArrayList;

public class DrinkingStartView extends Fragment implements View.OnClickListener {
    private View mRootView;
    private OnDrinkingStartCallback mOnDrinkingStartCallback;
    private TextView mCurrentDrinkTV;
    private TextView mTotalDrinkTV;
    private TextView mTimeTv;
    private SeekBar mSeekBar;
    private TextView mSwitchElectrolyzeBtn;
    private boolean isSeekbarOntouch;
    private DrinkingPresenter mDrinkingPresenter;
    private DrinkListTodayRsp mDrinkListTodayRsp;
    private DrinkListTotalRsp mDrinkListTotalRsp;
    private TextView mCurrentDeviceName;
    private ArrayList<CupListRsp.CupBean> mOnlineCups;
    private int battery;
    private int cleanTime = 5 * 60;//Clean倒数时间
    private Handler mHandler;
    private Runnable mBackwardsRunnable;

    public void setPresenterAndCallback(DrinkingPresenter drinkingPresenter, OnDrinkingStartCallback onDrinkingStartCallback) {
        mDrinkingPresenter = drinkingPresenter;
        mOnDrinkingStartCallback = onDrinkingStartCallback;
    }

    public void setTodayDrink(DrinkListTodayRsp drinkListTodayRsp) {
        mDrinkListTodayRsp = drinkListTodayRsp;
        setTodayDrink();
    }

    private void setTodayDrink() {
        int count = 0;
        if (mDrinkListTodayRsp != null && mDrinkListTodayRsp.getResult() != null) {
            count = mDrinkListTodayRsp.getResult().getDrinks().size();
        }
        if (getContext() != null) {
            mCurrentDrinkTV.setText(String.format(getString(R.string.cup), count));
        }
    }

    public void setTotalDrink(DrinkListTotalRsp drinkListTotalRsp) {
        mDrinkListTotalRsp = drinkListTotalRsp;
        setTotalDrink();
    }

    private void setTotalDrink() {
        int count = 0;
        if (mDrinkListTotalRsp != null && mDrinkListTotalRsp.getResult() != null) {
            for (DrinkListTotalRsp.ResultBean resultBean : mDrinkListTotalRsp.getResult()) {
                count += resultBean.getDrinks().size();
            }
        }
        if (getContext() != null) {
            mTotalDrinkTV.setText(String.format(getString(R.string.cup), count));
        }
    }

    public void setOnlineCups(ArrayList<CupListRsp.CupBean> onlineCups) {
        mOnlineCups = onlineCups;
        if (mCurrentDeviceName != null && TextUtils.isEmpty(mCurrentDeviceName.getText().toString())) {
            refreshCurrentCup(null);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            refreshCurrentCup(null);
        } else {
            MessageProxy.clearMessageProxyCallback();
        }
    }

    @Override
    public void onResume() {
        refreshCurrentCup(null);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.view_drinking_start, container, false);
        mCurrentDrinkTV = mRootView.findViewById(R.id.number_current_drink_quantity);
        mTotalDrinkTV = mRootView.findViewById(R.id.number_total_drink_quantity);
        mTimeTv = mRootView.findViewById(R.id.tv_time);
        mSeekBar = mRootView.findViewById(R.id.seek_bar);
        mCurrentDeviceName = mRootView.findViewById(R.id.tv_current_device_name);

        mHandler = new Handler();
        mBackwardsRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = mSeekBar.getProgress();
                if (progress >= 1) {
                    mSeekBar.setProgress(--progress);
                    mHandler.postDelayed(mBackwardsRunnable, 1000);
                } else {
                    setElectrolyzeStart(false);
                }
            }
        };
        return mRootView;
    }

    private void refreshCurrentCup(CupListRsp.CupBean cupBean) {
        if (cupBean != null) {
            mDrinkingPresenter.setCurrentControlCup(cupBean);
        }
        CupListRsp.CupBean currentControlCup = mDrinkingPresenter.getCurrentControlCup();
        if (mCurrentDeviceName != null && currentControlCup != null) {
            mCurrentDeviceName.setText(currentControlCup.getName());

            setElectrolyzeStart(false);
            mSeekBar.setProgress(5 * 60);
            MessageProxy.clearMessageProxyCallback();
            MessageProxy.addMessageProxyCallback(currentControlCup.getUuid(), new MessageProxyCallback() {

                @Override
                public void onBattery(String uuid, int battery) {
                    DrinkingStartView.this.battery = battery;
                }

                @Override
                public void onTime(String uuid, String minute, String second) {
                    if (getCurrentStatus() != CupListRsp.CupBean.ELECTROLYZE_STATUS) {
                        if (getCurrentStatus() == CupListRsp.CupBean.CLEAN_STATUS) {
                            cleanTime = Integer.valueOf(minute) * 60 + Integer.valueOf(second);
                        }
                        setElectrolyzeStart(false);
                        return;
                    }
                    int progress = Integer.valueOf(minute) * 60 + Integer.valueOf(second);
                    if (!isSeekbarOntouch) {
                        mSeekBar.setProgress(progress);
                    }
                    if (progress > 0) {
                        setElectrolyzeStart(true);
                    } else {
                        setElectrolyzeStart(false);
                    }
                }

                @Override
                public void onElectrolyzeEnd(String uuid) {
                    if (getCurrentStatus() != CupListRsp.CupBean.DEFAULT_STATUS || mSwitchElectrolyzeBtn.isSelected()) {
                        mDrinkingPresenter.getCurrentControlCup().setStatus(CupListRsp.CupBean.DEFAULT_STATUS);
                        setElectrolyzeStart(false);
                    }
                }

                @Override
                public void onCleaneEnd(String uuid) {
                    setCurrentStatus(CupListRsp.CupBean.CLEAN_END_STATUS);
                }

                @Override
                public void onElectrolyzing(String uuid) {
                    setCurrentStatus(CupListRsp.CupBean.ELECTROLYZE_STATUS);
                }

                @Override
                public void onCleaning(String uuid) {
                    setCurrentStatus(CupListRsp.CupBean.CLEAN_STATUS);
                }
            });
        }
    }

    public int getCurrentStatus() {
        if (mDrinkingPresenter.getCurrentControlCup() == null) {
            return CupListRsp.CupBean.NO_DEVICE_CHOOSE;
        }
        return mDrinkingPresenter.getCurrentControlCup().getStatus();
    }

    public void setCurrentStatus(int status) {
        if (getCurrentStatus() != status) {
            mDrinkingPresenter.getCurrentControlCup().setStatus(status);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRootView.findViewById(R.id.tv_device_list).setOnClickListener(this);
        mRootView.findViewById(R.id.iv_light_setting).setOnClickListener(this);
        mRootView.findViewById(R.id.iv_cup_clean).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_select_device).setOnClickListener(this);
        mCurrentDeviceName.setOnClickListener(this);
        mSwitchElectrolyzeBtn = mRootView.findViewById(R.id.btn_switch_electrolyze);
        mSwitchElectrolyzeBtn.setOnClickListener(this);

        mSeekBar.setMax(20 * 60);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String m = "0" + progress / 60;
                String s = "0" + progress % 60;
                mTimeTv.setText(m.substring(m.length() - 2).concat(":").concat(s.substring(s.length() - 2)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarOntouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarOntouch = false;
                if (getCurrentStatus() == CupListRsp.CupBean.ELECTROLYZE_STATUS) {
                    mDrinkingPresenter.switchCupElectrolyze(mSeekBar.getProgress());
                }
            }
        });
        setTodayDrink();
        setTotalDrink();
    }

    private void setElectrolyzeStart(boolean startElectrolyze) {
        if (mSwitchElectrolyzeBtn.isSelected() && !startElectrolyze && mDrinkingPresenter != null) {
            mDrinkingPresenter.getDrinkCount();//停止电解的时候刷新喝水数量
        }
        mSwitchElectrolyzeBtn.setSelected(startElectrolyze);
        mSwitchElectrolyzeBtn.setText(startElectrolyze ? R.string.stop_electrolysis : R.string.start_electrolysis);
        mHandler.removeCallbacks(mBackwardsRunnable);
        if (startElectrolyze) {
            mHandler.postDelayed(mBackwardsRunnable, 1000);
        }
    }

    @Override
    public void onDestroy() {
        mDrinkingPresenter = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_device_list:
                if (mOnDrinkingStartCallback != null) {
                    mOnDrinkingStartCallback.showDrinkingDeviceList();
                }
                break;
            case R.id.btn_switch_electrolyze: {
                if (mDrinkingPresenter.getCurrentControlCup() == null) {
                    ToastUtils.showShort(R.string.no_choose_device);
                    return;
                }
                if (getCurrentStatus() == CupListRsp.CupBean.CLEAN_STATUS) {
                    ToastUtils.showShort(R.string.cleaning_please_change_water);
                    return;
                } else if (getCurrentStatus() == CupListRsp.CupBean.CLEAN_END_STATUS) {
                    ToastUtils.showShort(R.string.clean_finish_please_change_water);
                    return;
                }
                if (mDrinkingPresenter != null) {
                    setElectrolyzeStart(!v.isSelected());
                    mDrinkingPresenter.switchCupElectrolyze(v.isSelected() ? mSeekBar.getProgress() : 0);
                }
                break;
            }
            case R.id.iv_light_setting:
                if (mDrinkingPresenter.getCurrentControlCup() == null) {
                    ToastUtils.showShort(R.string.no_choose_device);
                    return;
                }
                if (getCurrentStatus() == CupListRsp.CupBean.CLEAN_STATUS) {
                    ToastUtils.showShort(R.string.cleaning_please_change_water);
                    return;
                } else if (getCurrentStatus() == CupListRsp.CupBean.CLEAN_END_STATUS) {
                    ToastUtils.showShort(R.string.clean_finish_please_change_water);
                    return;
                }
                LightSettingFragment lightSettingFragment = new LightSettingFragment();
                lightSettingFragment.setDrinkingPresenter(mDrinkingPresenter);
                lightSettingFragment.show(getChildFragmentManager(), LightSettingFragment.TAG);
                break;
            case R.id.iv_cup_clean: {
                if (mDrinkingPresenter.getCurrentControlCup() == null) {
                    ToastUtils.showShort(R.string.no_choose_device);
                    return;
                }
                if (getCurrentStatus() == CupListRsp.CupBean.CLEAN_END_STATUS) {
                    ToastUtils.showShort(R.string.clean_finish_please_change_water);
                    return;
                }
                if (battery < 1) {
                    ToastUtils.showShort(R.string.cant_clean_in_low_battery);
                    return;
                }
                setCurrentStatus(CupListRsp.CupBean.CLEAN_STATUS);
                setElectrolyzeStart(false);
                CleanCupFragment cleanCupFragment = new CleanCupFragment();
                cleanCupFragment.setDrinkingPresenterAndTime(mDrinkingPresenter, cleanTime);
                cleanCupFragment.show(getChildFragmentManager(), CleanCupFragment.TAG);
                break;
            }
            case R.id.tv_current_device_name:
            case R.id.btn_select_device:
                if (mOnlineCups == null || mOnlineCups.size() == 0) {
                    ToastUtils.showShort(R.string.no_connect_device);
                    return;
                }
                ArrayList<String> datas = new ArrayList<>();
                for (CupListRsp.CupBean cupBean : mOnlineCups) {
                    datas.add(cupBean.getName());
                }
                PopupWindowUtil.buildListPpp(mCurrentDeviceName, datas, 200, 300, new PopupWindowListAdapter.PopupWindowListAdapterCallback() {
                    @Override
                    public void onItemClick(String name, int position) {
                        refreshCurrentCup(mOnlineCups.get(position));
                    }
                });
                break;
        }
    }

    public interface OnDrinkingStartCallback {
        void showDrinkingDeviceList();
    }
}
