package com.wopin.qingpaopao.fragment.welfare.oldchangenew;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.ScoreMarketContentDetailAdapter;
import com.wopin.qingpaopao.bean.request.TrackingNumberSettingBean;
import com.wopin.qingpaopao.bean.response.OrderBean;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.fragment.BaseBarDialogFragment;
import com.wopin.qingpaopao.fragment.welfare.order.SetTrackingNumberFragment;
import com.wopin.qingpaopao.presenter.OldChangeNewContentDetailPresenter;
import com.wopin.qingpaopao.utils.TimeFormatUtils;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.utils.WebViewUtil;
import com.wopin.qingpaopao.view.OldChangeNewContentDetailView;
import com.wopin.qingpaopao.widget.RecyclerViewAdDotLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OldChangeNewContentDetailFragment extends BaseBarDialogFragment<OldChangeNewContentDetailPresenter> implements OldChangeNewContentDetailView, View.OnClickListener, BuyOldChangeNewProduceDialog.BuyOldChangeNewCallback {

    public static final String TAG = "OldChangeNewContentDetailFragment";
    private ProductContent mProductContent;
    private ProductContent.AttributeBean mOldProduct;
    private RecyclerView mGoodsDetailRv;
    private Spinner mChooseOldSpinner;
    private RecyclerViewAdDotLayout mRecyclerViewAdDotLayout;
    private View mBuyBtn;

    public static OldChangeNewContentDetailFragment build(ProductContent productContent) {
        OldChangeNewContentDetailFragment oldChangeNewContentDetailFragment = new OldChangeNewContentDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(TAG, productContent);
        oldChangeNewContentDetailFragment.setArguments(args);
        return oldChangeNewContentDetailFragment;
    }

    @Override
    protected String setBarTitle() {
        return getString(R.string.old_change_new);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_old_change_new_content_detail;
    }

    @Override
    protected OldChangeNewContentDetailPresenter initPresenter() {
        return new OldChangeNewContentDetailPresenter(getContext(), this);
    }

    @Override
    protected void initView(View rootView) {
        mProductContent = getArguments().getParcelable(TAG);

        WebView descriptionWebView = rootView.findViewById(R.id.rv_goods_detail_linearlayout);
        WebViewUtil.loadWebView(OldChangeNewContentDetailFragment.this, descriptionWebView, mProductContent.getDescription());

        ((TextView) rootView.findViewById(R.id.tv_title)).setText(mProductContent.getName());
        ((TextView) rootView.findViewById(R.id.tv_subtitle)).setText(Html.fromHtml(mProductContent.getShort_description()));
        try {
            TextView dayTv = rootView.findViewById(R.id.tv_time);
            int daySaleTo = TimeFormatUtils.getDaysDifference(mProductContent.getDate_on_sale_to());
            int daySaleFrom = TimeFormatUtils.getDaysDifference(mProductContent.getDate_on_sale_from());

            if (daySaleFrom > 0) {
                dayTv.setText(String.format(getString(R.string.will_start_at), TimeFormatUtils.formatToTime(mProductContent.getDate_on_sale_from(), new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))));
            } else if (daySaleTo > 0) {
                dayTv.setText(String.format(getString(R.string.leave_day_number), String.valueOf(daySaleTo)));
                String count = mProductContent.getStock_quantity();
                if (Integer.valueOf(TextUtils.isEmpty(count) ? "0" : count) > 0) {
                    mBuyBtn = rootView.findViewById(R.id.btn_i_want_to_buy);
                    mBuyBtn.setBackgroundColor(Color.RED);
                    mBuyBtn.setOnClickListener(this);
                }
            } else {
                dayTv.setText(R.string.activity_over);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ((TextView) rootView.findViewById(R.id.tv_price)).setText(String.format(getString(R.string.price_number), mProductContent.getPrice()));
        String count = mProductContent.getStock_quantity();
        ((TextView) rootView.findViewById(R.id.tv_count)).setText(String.format(getString(R.string.residue_count), Integer.valueOf(TextUtils.isEmpty(count) ? "0" : count)));

        mGoodsDetailRv = rootView.findViewById(R.id.rv_goods_top_detail);
        mChooseOldSpinner = rootView.findViewById(R.id.choose_old_to_new_produce);
        mRecyclerViewAdDotLayout = rootView.findViewById(R.id.rv_advertising_decorate);


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

        mOldProduct = mProductContent.getAttributes().get(0);
        ArrayList<String> datas = new ArrayList<>();
        datas.add(mOldProduct.getOptions().get(0));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, datas);
        mChooseOldSpinner.setEnabled(false);
        mChooseOldSpinner.setAdapter(arrayAdapter);
    }

    @Override
    public void onPayMentExchangeSubmit(final OrderBean orderBean) {
        mPresenter.payOrderByWechat(getContext(), orderBean);
    }

    @Override
    public void onPaySuccess(final OrderBean orderBean) {
        setLoadingVisibility(false);
        ToastUtils.showShort(R.string.old_change_new_pay_success);
        SetTrackingNumberFragment setTrackingNumberFragment = SetTrackingNumberFragment.build(orderBean);
        setTrackingNumberFragment.setTrackingNumberSettingCallback(new SetTrackingNumberFragment.TrackingNumberSettingCallback() {
            @Override
            public void onTrackingNumberSetting(TrackingNumberSettingBean trackingNumberSettingBean) {
                setLoadingVisibility(true);
                mPresenter.exchangeOrderUpdate(trackingNumberSettingBean);
            }
        });
        setTrackingNumberFragment.show(getChildFragmentManager(), SetTrackingNumberFragment.TAG);
    }

    @Override
    public void onOrderUpdateSuccess() {
        setLoadingVisibility(false);
        ToastUtils.showShort(R.string.application_form_finish);
        dismiss();
    }

    @Override
    public void onError(String errorSting) {
        setLoadingVisibility(false);
        ToastUtils.showShort(errorSting);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_i_want_to_buy:
                BuyOldChangeNewProduceDialog build = BuyOldChangeNewProduceDialog.build(mProductContent, mOldProduct);
                build.setBuyOldChangeNewCallback(this);
                build.show(getChildFragmentManager(), BuyOldChangeNewProduceDialog.TAG);
                break;
        }
    }

    @Override
    public void OnBuyInformation(int number, String addressId) {
        mPresenter.payMentExchange(addressId, mProductContent.getName(), mProductContent.getImages().size() == 0 ? null : mProductContent.getImages().get(0).getSrc(), mProductContent.getId(), number, Integer.valueOf(mProductContent.getPrice()), mOldProduct == null ? 0 : Integer.valueOf(mOldProduct.getName()));
    }
}
