package com.wopin.qingpaopao.fragment.welfare;

import android.graphics.Rect;
import android.support.constraint.Group;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.ScoreMarketContentAdapter;
import com.wopin.qingpaopao.adapter.WelfareBannerAdapter;
import com.wopin.qingpaopao.bean.response.ProductBanner;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.fragment.BaseMainFragment;
import com.wopin.qingpaopao.fragment.welfare.crowdfunding.CrowdFundingListFragment;
import com.wopin.qingpaopao.fragment.welfare.oldchangenew.OldChangeNewListFragment;
import com.wopin.qingpaopao.fragment.welfare.order.MyOrderFragment;
import com.wopin.qingpaopao.fragment.welfare.scoremarket.ScoreMarketContentDetailFragment;
import com.wopin.qingpaopao.fragment.welfare.scoremarket.ScoreMarketListFragment;
import com.wopin.qingpaopao.presenter.WelfarePresenter;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.widget.WelfareView;

import java.util.ArrayList;

public class WelfareFragment extends BaseMainFragment<WelfarePresenter> implements View.OnClickListener, WelfareView, ScoreMarketContentAdapter.ScoreMarketContentClick {

    private RecyclerView mBannerRv;
    private RecyclerView mScoreMarketRv;
    private View mLoadingView;
    private WelfareBannerAdapter mWelfareBannerAdapter;
    private ScoreMarketContentAdapter mScoreMarketContentAdapter;
    private Group mIconGroup;

    @Override
    protected int getLayout() {
        return R.layout.fragment_welfare;
    }

    @Override
    protected WelfarePresenter initPresenter() {
        return new WelfarePresenter(getContext(), this);
    }

    @Override
    protected void initView(View rootView) {
        mIconGroup = rootView.findViewById(R.id.icons);
        mBannerRv = rootView.findViewById(R.id.rv_banner);
        mScoreMarketRv = rootView.findViewById(R.id.rv_score_market_list);
        mLoadingView = rootView.findViewById(R.id.progress_bar_layout);
        rootView.findViewById(R.id.iv_score_market).setOnClickListener(this);
        rootView.findViewById(R.id.iv_old_change_new).setOnClickListener(this);
        rootView.findViewById(R.id.iv_crowd_funding).setOnClickListener(this);
        rootView.findViewById(R.id.iv_my_orders).setOnClickListener(this);

        mIconGroup.setVisibility(View.GONE);
    }

    private void setLoadingVisibility(boolean isVisibility) {
        mLoadingView.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initEvent() {
        mBannerRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mWelfareBannerAdapter = new WelfareBannerAdapter();
        mBannerRv.setAdapter(mWelfareBannerAdapter);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mBannerRv);
        mScoreMarketRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mScoreMarketContentAdapter = new ScoreMarketContentAdapter();
        mScoreMarketRv.setAdapter(mScoreMarketContentAdapter);
        mScoreMarketContentAdapter.setScoreMarketContentClick(this);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.right = 1;
                outRect.left = 1;
                outRect.top = 1;
                outRect.bottom = 1;
            }
        };
        mScoreMarketRv.addItemDecoration(itemDecoration);
    }

    @Override
    public void refreshData() {
        mPresenter.getScoreMarketProduct();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_score_market:
                new ScoreMarketListFragment().show(getChildFragmentManager(), ScoreMarketListFragment.TAG);
                break;
            case R.id.iv_old_change_new:
                new OldChangeNewListFragment().show(getChildFragmentManager(), OldChangeNewListFragment.TAG);
                break;
            case R.id.iv_crowd_funding:
                new CrowdFundingListFragment().show(getChildFragmentManager(), CrowdFundingListFragment.TAG);
                break;
            case R.id.iv_my_orders:
                new MyOrderFragment().show(getChildFragmentManager(), MyOrderFragment.TAG);
                break;
        }
    }

    @Override
    public void onProductBanner(ProductBanner productBanner) {
        mWelfareBannerAdapter.setProductBanner(productBanner);
    }

    @Override
    public void onProductContentList(ArrayList<ProductContent> productContents) {
        mScoreMarketContentAdapter.setProductContents(productContents);
    }

    @Override
    public void onLoading() {
        setLoadingVisibility(true);
    }

    @Override
    public void onRequestSuccess() {
        setLoadingVisibility(false);
        onDataRefreshFinish(true);
        mIconGroup.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String error) {
        setLoadingVisibility(false);
        ToastUtils.showShort(error);
    }

    @Override
    public void onScoreMarketContentClick(ProductContent productContent) {
        ScoreMarketContentDetailFragment.build(productContent).show(getChildFragmentManager(), ScoreMarketContentDetailFragment.TAG);
    }
}
