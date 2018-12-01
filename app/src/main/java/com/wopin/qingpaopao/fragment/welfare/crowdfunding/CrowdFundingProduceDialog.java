package com.wopin.qingpaopao.fragment.welfare.crowdfunding;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.CrowdFundingGradeAdapter;
import com.wopin.qingpaopao.bean.response.LoginRsp;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.fragment.welfare.address.AddressListFragment;
import com.wopin.qingpaopao.presenter.LoginPresenter;
import com.wopin.qingpaopao.utils.GlideUtils;
import com.wopin.qingpaopao.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class CrowdFundingProduceDialog extends DialogFragment implements View.OnClickListener, AddressListFragment.AddressListClickCallback, CrowdFundingGradeAdapter.CrowdFundingGradeAdapterCallback {

    public static final String TAG = "CrowdFundingProduceDialog";
    public static final String ATTRIBUTEPOSITIONCHOOSE = "attributePositionChoose";
    private View mRootView;
    private TextView mBuyCountTv;
    private int mBuyCount;
    private TextView mAddressTv;
    private RecyclerView mGradeRv;
    private LoginRsp.ResultBean.AddressListBean mCurrentAddress;
    private SupportInformationDialogCallback mSupportInformationDialogCallback;
    private ProductContent mProductContent;
    private ProductContent.AttributeBean mCurrentAttributeBean;

    public static CrowdFundingProduceDialog build(ProductContent productContent, int attributePositionChoose) {
        CrowdFundingProduceDialog crowdFundingProduceDialog = new CrowdFundingProduceDialog();
        Bundle args = new Bundle();
        args.putParcelable(TAG, productContent);
        args.putInt(ATTRIBUTEPOSITIONCHOOSE, attributePositionChoose);
        crowdFundingProduceDialog.setArguments(args);
        return crowdFundingProduceDialog;
    }

    public void setSupportInformationDialogCallback(SupportInformationDialogCallback supportInformationDialogCallback) {
        mSupportInformationDialogCallback = supportInformationDialogCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable());
        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog_bottom_to_top);

        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        mRootView = inflater.inflate(R.layout.dialog_crowd_funding_produce, container, false);
        mBuyCountTv = mRootView.findViewById(R.id.tv_number);
        mBuyCount = Integer.valueOf(mBuyCountTv.getText().toString());
        mAddressTv = mRootView.findViewById(R.id.tv_receiver_address_value);
        mAddressTv.setOnClickListener(this);
        mGradeRv = mRootView.findViewById(R.id.rv_choose_grade);
        mRootView.findViewById(R.id.iv_close).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_add).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_minus).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_support_at_once).setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProductContent = getArguments().getParcelable(TAG);

        ArrayList<LoginRsp.ResultBean.AddressListBean> addressList = LoginPresenter.getAccountMessage().getResult().getAddressList();
        if (addressList != null) {
            for (LoginRsp.ResultBean.AddressListBean addressListBean : addressList) {
                if (addressListBean.isIsDefault()) {
                    setAddressMessage(addressListBean);
                    break;
                }
            }
        }
        initGradeRecyclerView(getArguments().getInt(ATTRIBUTEPOSITIONCHOOSE, 0));
        notifyTotalScore();
    }

    private void initGradeRecyclerView(int attributepositionchoose) {
        mGradeRv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        List<ProductContent.AttributeBean> attributes = mProductContent.getAttributes();
        CrowdFundingGradeAdapter crowdFundingGradeAdapter = new CrowdFundingGradeAdapter(this);
        mGradeRv.setAdapter(crowdFundingGradeAdapter);
        crowdFundingGradeAdapter.setAttributes(attributes, attributepositionchoose);
        onCrowdFundingGradeItemClick(attributes.get(attributepositionchoose), attributepositionchoose);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.btn_add:
                mBuyCountTv.setText(String.valueOf(++mBuyCount));
                notifyTotalScore();
                break;
            case R.id.btn_minus:
                if (mBuyCount > 1) {
                    mBuyCountTv.setText(String.valueOf(--mBuyCount));
                    notifyTotalScore();
                }
                break;
            case R.id.btn_support_at_once:
                if (mCurrentAddress == null) {
                    ToastUtils.showShort(R.string.need_choose_address);
                    return;
                }
                if (mSupportInformationDialogCallback != null) {
                    mSupportInformationDialogCallback.onSupportInformation(mCurrentAttributeBean, mBuyCount, mCurrentAddress.getAddressId());
                }
                break;
            case R.id.tv_receiver_address_value:
                AddressListFragment addressListFragment = new AddressListFragment();
                addressListFragment.setAddressListClickCallback(this);
                addressListFragment.show(getChildFragmentManager(), AddressListFragment.TAG);
                break;
        }
    }

    private void notifyTotalScore() {
        int priceEach = Integer.valueOf(mCurrentAttributeBean.getName());
        ((TextView) mRootView.findViewById(R.id.tv_need_pay_value)).setText(String.format(getString(R.string.price_number), String.valueOf(priceEach * mBuyCount)));
    }

    @Override
    public void onAddressItemClick(LoginRsp.ResultBean.AddressListBean addressListBean) {
        setAddressMessage(addressListBean);
    }

    private void setAddressMessage(LoginRsp.ResultBean.AddressListBean addressListBean) {
        mCurrentAddress = addressListBean;
        mAddressTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mAddressTv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        mAddressTv.setTextColor(Color.BLACK);
        mAddressTv.setText(addressListBean.getUserName() + " " + addressListBean.getTel() + "\n" + addressListBean.getAddress1() + addressListBean.getAddress2());
    }

    @Override
    public void onCrowdFundingGradeItemClick(ProductContent.AttributeBean attributeBean, int position) {
        mCurrentAttributeBean = attributeBean;
        if (mCurrentAttributeBean == null) {
            ToastUtils.showShort(R.string.known_error);
            dismiss();
        }
        showAttributeBeanDetail();
        notifyTotalScore();
    }

    private void showAttributeBeanDetail() {
        TextView gradePriceTv = mRootView.findViewById(R.id.grade_price);
        TextView gradeContentTv = mRootView.findViewById(R.id.grade_content);
        ImageView gradeImage = mRootView.findViewById(R.id.iv_grade_image);
        gradePriceTv.setText(String.format(getString(R.string.price_number), mCurrentAttributeBean.getName()));
        if (mCurrentAttributeBean.getOptions() != null && mCurrentAttributeBean.getOptions().size() > 0) {
            gradeContentTv.setText(mCurrentAttributeBean.getOptions().get(0));
        }
        GlideUtils.loadImage(gradeImage, -1, mProductContent.getImages().size() > 0 ? mProductContent.getImages().get(0).getSrc() : null, new CenterCrop());
    }

    public interface SupportInformationDialogCallback {
        void onSupportInformation(ProductContent.AttributeBean attributeBean, int number, String addressId);
    }
}
