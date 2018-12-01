package com.wopin.qingpaopao.fragment.explore;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.activity.BuildBlogActivity;
import com.wopin.qingpaopao.adapter.ExploreListNorAdapter;
import com.wopin.qingpaopao.bean.response.ExploreListRsp;
import com.wopin.qingpaopao.fragment.BaseMainFragment;
import com.wopin.qingpaopao.presenter.ExplorePresenter;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.view.ExploreView;

public class ExploreFragment extends BaseMainFragment<ExplorePresenter> implements ExploreView, View.OnClickListener {

    private TabLayout mTabLayout;
    private EditText mSearchEt;
    private RecyclerView mExploreRv;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mLoadingView;
    private ExploreListNorAdapter mExploreListAdapter;
    private final static int PAGE_NUMBER = 10;
    private int page = 1;

    @Override
    public void onViewPagerFragmentPause() {
        super.onViewPagerFragmentPause();
        setLoadingVisibility(false);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_explore;
    }

    @Override
    protected ExplorePresenter initPresenter() {
        return new ExplorePresenter(getContext(), this);
    }

    @Override
    protected void initView(View rootView) {
        mSearchEt = rootView.findViewById(R.id.et_search);
        mTabLayout = rootView.findViewById(R.id.explore_tablayout);
        mExploreRv = rootView.findViewById(R.id.rv_explore_list);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeLayout);
        mLoadingView = rootView.findViewById(R.id.progress_bar_layout);

        rootView.findViewById(R.id.tv_issue).setOnClickListener(this);
    }

    private void setLoadingVisibility(boolean isVisibility) {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(isVisibility ? View.VISIBLE : View.GONE);
        }
        if (!isVisibility && mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void initEvent() {
        mExploreRv.setLayoutManager(new LinearLayoutManager(getContext()));
        mExploreListAdapter = new ExploreListNorAdapter(R.layout.item_explore_list, null);
        mExploreListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ExploreDetailFragment.build((ExploreListRsp.PostsBean) adapter.getItem(position)).show(getChildFragmentManager(), ExploreDetailFragment.TAG);
            }
        });
        mExploreListAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                String s = mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText().toString();
                if (s.equals(getString(R.string.hot_topic))) {//热门话题
                    mPresenter.listHotExplores(page, PAGE_NUMBER);
                } else if (s.equals(getString(R.string.newest_topic))) {//最新话题
                    mPresenter.listNewlyExplores(page, PAGE_NUMBER);
                } else {//我的话题
                    mPresenter.listMyExplores(page, PAGE_NUMBER);
                }
            }
        }, mExploreRv);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mExploreListAdapter.setNewData(null);
                page = 1;
                String s = mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText().toString();
                if (s.equals(getString(R.string.hot_topic))) {//热门话题
                    mPresenter.listHotExplores(page, PAGE_NUMBER);
                } else if (s.equals(getString(R.string.newest_topic))) {//最新话题
                    mPresenter.listNewlyExplores(page, PAGE_NUMBER);
                } else {//我的话题
                    mPresenter.listMyExplores(page, PAGE_NUMBER);
                }
            }
        });
        mExploreRv.setAdapter(mExploreListAdapter);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String s = tab.getText().toString();
                mExploreListAdapter.setNewData(null);
                page = 1;
                if (s.equals(getString(R.string.hot_topic))) {//热门话题
                    mPresenter.listHotExplores(page, PAGE_NUMBER);
                } else if (s.equals(getString(R.string.newest_topic))) {//最新话题
                    mPresenter.listNewlyExplores(page, PAGE_NUMBER);
                } else {//我的话题
                    mPresenter.listMyExplores(page, PAGE_NUMBER);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mSearchEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String s = mSearchEt.getText().toString();
                    if (TextUtils.isEmpty(s)) {
                        ToastUtils.showShort(R.string.search_your_favourite);
                        return true;
                    }
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mSearchEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mPresenter.searchExplores(s);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void refreshData() {
        mPresenter.listHotExplores(1, PAGE_NUMBER);
    }

    @Override
    public void onLoading() {
        setLoadingVisibility(true);
    }

    @Override
    public void onExploreList(ExploreListRsp exploreListRsp) {
        setLoadingVisibility(false);
        onDataRefreshFinish(true);

        mExploreListAdapter.addData(exploreListRsp.getPosts());
        if (exploreListRsp.getPosts().size() < PAGE_NUMBER) {
            mExploreListAdapter.loadMoreEnd();
        } else {
            page++;
            mExploreListAdapter.loadMoreComplete();
        }
    }

    @Override
    public void onError(String errorMsg) {
        setLoadingVisibility(false);
        ToastUtils.showShort(errorMsg);
        mExploreListAdapter.loadMoreFail();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_issue:
                jumpToActivity(BuildBlogActivity.class);
                break;
        }
    }
}
