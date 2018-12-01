package com.wopin.qingpaopao.fragment.welfare.crowdfunding;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.CrowdFundingGradeAdapter;
import com.wopin.qingpaopao.adapter.ScoreMarketContentDetailAdapter;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.fragment.BaseBarDialogFragment;
import com.wopin.qingpaopao.presenter.CrowdFundingContentDetailPresenter;
import com.wopin.qingpaopao.utils.GlideUtils;
import com.wopin.qingpaopao.utils.TimeFormatUtils;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.utils.WebViewUtil;
import com.wopin.qingpaopao.view.CrowdFundingDetailView;
import com.wopin.qingpaopao.widget.RecyclerViewAdDotLayout;

import java.text.ParseException;
import java.util.List;

public class CrowdFundingDetailFragment extends BaseBarDialogFragment<CrowdFundingContentDetailPresenter> implements View.OnClickListener, CrowdFundingDetailView, CrowdFundingProduceDialog.SupportInformationDialogCallback, CrowdFundingGradeAdapter.CrowdFundingGradeAdapterCallback {

    public static final String TAG = "CrowdFundingDetailFragment";
    private ProductContent mProductContent;
    private View mRootView;
    private RecyclerView mGoodsDetailRv;
    private RecyclerViewAdDotLayout mRecyclerViewAdDotLayout;

    private RecyclerView mGradeRv;
    private ProductContent.AttributeBean mCurrentAttributeBean;
    private int attributePositionChoose;

    public static CrowdFundingDetailFragment build(ProductContent productContent) {
        CrowdFundingDetailFragment scoreMarketContentDetailFragment = new CrowdFundingDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(TAG, productContent);
        scoreMarketContentDetailFragment.setArguments(args);
        return scoreMarketContentDetailFragment;
    }

    @Override
    protected String setBarTitle() {
        return getString(R.string.commodity_details);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_crowd_funding_content_detail;
    }

    @Override
    protected CrowdFundingContentDetailPresenter initPresenter() {
        return new CrowdFundingContentDetailPresenter(getContext(), this);
    }

    @Override
    protected void initView(View rootView) {
        mRootView = rootView;
        mProductContent = getArguments().getParcelable(TAG);

        WebView descriptionWebView = rootView.findViewById(R.id.rv_goods_detail_linearlayout);
        WebViewUtil.loadWebView(CrowdFundingDetailFragment.this, descriptionWebView, mProductContent.getDescription());

        ((TextView) rootView.findViewById(R.id.tv_title)).setText(mProductContent.getName());
        ((TextView) rootView.findViewById(R.id.tv_subtitle)).setText(mProductContent.getShort_description());
        ((TextView) rootView.findViewById(R.id.target_price_value)).setText(String.format(getString(R.string.price_number), mProductContent.getPrice()));

        int daysDifference = 1;
        try {
            daysDifference = TimeFormatUtils.getDaysDifference(mProductContent.getDate_on_sale_to());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ((TextView) rootView.findViewById(R.id.tv_leave_time_value)).setText(String.format(getString(R.string.day_number), String.valueOf(daysDifference)));

        mGoodsDetailRv = rootView.findViewById(R.id.rv_goods_top_detail);
        mRecyclerViewAdDotLayout = rootView.findViewById(R.id.rv_advertising_decorate);
        rootView.findViewById(R.id.btn_i_want_to_support).setOnClickListener(this);

        mGradeRv = mRootView.findViewById(R.id.rv_choose_grade);
    }

    @Override
    protected void initEvent() {
        mGoodsDetailRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ScoreMarketContentDetailAdapter scoreMarketContentDetailAdapter = new ScoreMarketContentDetailAdapter();
        scoreMarketContentDetailAdapter.setScoreMarketContentDetails(mProductContent.getImages());
        mGoodsDetailRv.setAdapter(scoreMarketContentDetailAdapter);
        mRecyclerViewAdDotLayout.attendToRecyclerView(mGoodsDetailRv);
        mRecyclerViewAdDotLayout.setDotCount(mProductContent.getImages().size());
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mGoodsDetailRv);

        mPresenter.crowdfundingOrderTotalMoneyAndPeople(mProductContent.getId(), mProductContent.getPrice());
        setLoadingVisibility(true);

        initGradeRecyclerView();
    }

    private void initGradeRecyclerView() {
        mGradeRv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        List<ProductContent.AttributeBean> attributes = mProductContent.getAttributes();
        CrowdFundingGradeAdapter crowdFundingGradeAdapter = new CrowdFundingGradeAdapter(this);
        mGradeRv.setAdapter(crowdFundingGradeAdapter);
        crowdFundingGradeAdapter.setAttributes(attributes);
        onCrowdFundingGradeItemClick(attributes != null && attributes.size() > 0 ? attributes.get(0) : null, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_i_want_to_support:
                CrowdFundingProduceDialog build = CrowdFundingProduceDialog.build(mProductContent, attributePositionChoose);
                build.setSupportInformationDialogCallback(this);
                build.show(getChildFragmentManager(), CrowdFundingProduceDialog.TAG);
                break;
        }
    }

    @Override
    public void onCrowdFundingPrice(String targetPrice, String currentPrice, float percent) {
        ((TextView) mRootView.findViewById(R.id.tv_already_price_value)).setText(String.format(getString(R.string.price_number), currentPrice));
        ((ProgressBar) mRootView.findViewById(R.id.progress_bar_percent)).setProgress((int) (percent * 100));
        TextView barText = mRootView.findViewById(R.id.bar_text);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) barText.getLayoutParams();
        layoutParams.horizontalBias = percent;
        barText.setLayoutParams(layoutParams);
        barText.setText(String.format(getString(R.string.percent_float), percent * 100));
    }

    @Override
    public void onCrowdFundingSupportCount(int totalPeople) {
        ((TextView) mRootView.findViewById(R.id.tv_support_count_value)).setText(String.valueOf(totalPeople));
    }

    @Override
    public void onDataSuccess() {
        setLoadingVisibility(false);
    }

    @Override
    public void onCrowdFundingPayMentSuccess() {
        setLoadingVisibility(false);
        ToastUtils.showShort(R.string.buy_success);
    }

    @Override
    public void onError(String error) {
        setLoadingVisibility(false);
        ToastUtils.showShort(error);
    }

    @Override
    public void onSupportInformation(ProductContent.AttributeBean attributeBean, int number, String addressId) {
        mPresenter.payMentCrowdfunding(addressId, attributeBean.getOptions().get(0), mProductContent.getImages().size() == 0 ? null : mProductContent.getImages().get(0).getSrc(), mProductContent.getId(), number, Integer.valueOf(attributeBean.getName()));
    }

    @Override
    public void onCrowdFundingGradeItemClick(ProductContent.AttributeBean attributeBean, int position) {
        mCurrentAttributeBean = attributeBean;
        attributePositionChoose = position;
        if (mCurrentAttributeBean == null) {
            ToastUtils.showShort(R.string.known_error);
            dismiss();
        }
        showAttributeBeanDetail();
    }

    private void showAttributeBeanDetail() {
        TextView gradePriceTv = mRootView.findViewById(R.id.grade_price);
        TextView gradeContentTv = mRootView.findViewById(R.id.grade_content);
        ImageView gradeImage = mRootView.findViewById(R.id.iv_grade_image);
        gradePriceTv.setText(String.format(getString(R.string.price_number), mCurrentAttributeBean.getName()));
        if (mCurrentAttributeBean.getOptions() != null && mCurrentAttributeBean.getOptions().size() > 0) {
            gradeContentTv.setText(mCurrentAttributeBean.getOptions().get(0));
        }
        String imageUrl = mProductContent.getAttributes() != null && mProductContent.getAttributes().size() > 0 && mProductContent.getAttributes().get(0).getOptions() != null && mProductContent.getAttributes().get(0).getOptions().size() > 1 ? mProductContent.getAttributes().get(0).getOptions().get(1) : mProductContent.getImages().get(0).getSrc();
        GlideUtils.loadImage(gradeImage, -1, imageUrl, new CenterCrop());
    }
}
