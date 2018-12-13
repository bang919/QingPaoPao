package com.wopin.qingpaopao.fragment.drinking;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.fragment.BaseDialogFragment;
import com.wopin.qingpaopao.presenter.BasePresenter;
import com.wopin.qingpaopao.presenter.DrinkingPresenter;

public class LightSettingFragment extends BaseDialogFragment implements View.OnClickListener, ColorPicker.OnColorChangedListener {

    public static final String TAG = "LightSettingFragment";
    private ColorPicker mColorPicker;
    private View mLightBtn, mLightIv;
    private String mNowColor;
    private DrinkingPresenter mDrinkingPresenter;
    private Handler mHandler = new Handler();
    private ColorRunnable mColorRunnable = new ColorRunnable();

    public void setDrinkingPresenter(DrinkingPresenter drinkingPresenter) {
        mDrinkingPresenter = drinkingPresenter;
    }

    @Override
    public void onDestroy() {
        mDrinkingPresenter = null;
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_light_setting;
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initView(View rootView) {
        rootView.findViewById(R.id.iv_back).setOnClickListener(this);
        mLightBtn = rootView.findViewById(R.id.btn_light);
        mLightIv = rootView.findViewById(R.id.iv_light);
        mLightBtn.setOnClickListener(this);
        mLightBtn.setSelected(true);
        mLightIv.setSelected(true);

        mColorPicker = rootView.findViewById(R.id.picker);
        SVBar svBar = rootView.findViewById(R.id.svbar);
        mColorPicker.addSVBar(svBar);
        mColorPicker.setOldCenterColor(mColorPicker.getColor());
        mColorPicker.setShowOldCenterColor(false);
        mColorPicker.setShowNewCenterColor(false);
        mColorPicker.setOnColorChangedListener(this);

    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                dismiss();
                break;
            case R.id.btn_light:
                boolean isOn = !v.isSelected();
                mLightIv.setSelected(isOn);
                mLightBtn.setSelected(isOn);
                mDrinkingPresenter.switchCupLight(isOn);
                if (!TextUtils.isEmpty(mNowColor)) {
                    setColor(mNowColor);
                }
                break;
        }
    }

    @Override
    public void onColorChanged(int color) {

        if (!mLightIv.isSelected()) {//如果灯没有开，开灯
            mLightIv.setSelected(true);
            mLightBtn.setSelected(true);
            mDrinkingPresenter.switchCupLight(true);
        }

        String s = Integer.toHexString(color);
        String colorString = s.toUpperCase().replaceFirst("FF", "");
        mNowColor = colorString;
        setColor(colorString);
    }

    private void setColor(String colorString) {
        mDrinkingPresenter.setColor(colorString);
        mHandler.removeCallbacks(mColorRunnable);
        mColorRunnable.setColor(colorString);
        mHandler.postDelayed(mColorRunnable, 600);
    }

    class ColorRunnable implements Runnable {

        private String color;

        public void setColor(String color) {
            this.color = color;
        }

        @Override
        public void run() {
            if (mDrinkingPresenter != null) {
                mDrinkingPresenter.setColor(color);
            }
        }
    }
}
