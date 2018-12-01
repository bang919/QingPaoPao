package com.wopin.qingpaopao.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.CrowdfundingOrderTotalMoneyRsp;
import com.wopin.qingpaopao.bean.response.CrowdfundingOrderTotalPeopleRsp;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.model.WelfareModel;
import com.wopin.qingpaopao.presenter.BasePresenter;
import com.wopin.qingpaopao.utils.GlideUtils;
import com.wopin.qingpaopao.utils.HttpUtil;
import com.wopin.qingpaopao.utils.TimeFormatUtils;
import com.wopin.qingpaopao.utils.ToastUtils;

import java.text.ParseException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

public class CrowdFundingListAdapter extends RecyclerView.Adapter<CrowdFundingListAdapter.CrowdFundingViewHolder> {

    private ArrayList<ProductContent> mCrowdFundingDatas;
    private CrowdFundingListAdapterCallback mCrowdFundingListAdapterCallback;
    private WelfareModel mWelfareModel;

    public CrowdFundingListAdapter() {
        mWelfareModel = new WelfareModel();
    }

    public void setCrowdFundingDatas(ArrayList<ProductContent> numberousGoodsDatas) {
        mCrowdFundingDatas = numberousGoodsDatas;
        notifyDataSetChanged();
    }

    public void setCrowdFundingListAdapterCallback(CrowdFundingListAdapterCallback crowdFundingListAdapterCallback) {
        mCrowdFundingListAdapterCallback = crowdFundingListAdapterCallback;
    }

    @NonNull
    @Override
    public CrowdFundingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CrowdFundingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_crowd_funding_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final CrowdFundingViewHolder holder, int position) {
        Object tag = holder.itemView.getTag();
        if (tag != null && tag instanceof Disposable) {
            ((Disposable) tag).dispose();
        }
        final ProductContent productContent = mCrowdFundingDatas.get(position);
        GlideUtils.loadImage(holder.mImageView, -1, productContent.getImages().size() == 0 ? null : productContent.getImages().get(0).getSrc(), new CenterCrop());
        holder.mTitleTv.setText(productContent.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCrowdFundingListAdapterCallback != null) {
                    mCrowdFundingListAdapterCallback.onItemClick(productContent);
                }
            }
        });
        holder.mProgressBar.setProgress(0);
        int daysDifference = 1;
        try {
            daysDifference = TimeFormatUtils.getDaysDifference(productContent.getDate_on_sale_to());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.mLeaveTime.setText(String.format(holder.itemView.getContext().getString(R.string.day_number), String.valueOf(daysDifference)));
        crowdfundingOrderTotalMoneyAndPeople(productContent.getId(), productContent.getPrice(), holder);
    }

    public void crowdfundingOrderTotalMoneyAndPeople(int goodsId, final String goodsPrice, final CrowdFundingViewHolder holder) {
        final Context context = holder.itemView.getContext();
        HttpUtil.subscribeNetworkTask(Observable.zip(
                mWelfareModel.crowdfundingOrderTotalMoney(goodsId)
                        .doOnNext(new Consumer<CrowdfundingOrderTotalMoneyRsp>() {
                            @Override
                            public void accept(CrowdfundingOrderTotalMoneyRsp crowdfundingOrderTotalMoneyRsp) throws Exception {
                                float currentPrice = 0;
                                for (CrowdfundingOrderTotalMoneyRsp.ResultBean r : crowdfundingOrderTotalMoneyRsp.getResult()) {
                                    currentPrice += r.getTotalPrice();
                                }
                                float percent = currentPrice / Float.valueOf(goodsPrice);

                                holder.mAlreadyPrice.setText(String.format(context.getString(R.string.price_number), String.valueOf(currentPrice)));
                                holder.mProgressBar.setProgress((int) (percent * 100));
                                TextView barText = holder.mBarText;
                                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) barText.getLayoutParams();
                                layoutParams.horizontalBias = percent;
                                barText.setLayoutParams(layoutParams);
                                barText.setText(String.format(context.getString(R.string.percent_float), percent * 100));
                            }
                        }),
                mWelfareModel.crowdfundingOrderTotalPeople(goodsId)
                        .doOnNext(new Consumer<CrowdfundingOrderTotalPeopleRsp>() {
                            @Override
                            public void accept(CrowdfundingOrderTotalPeopleRsp crowdfundingOrderTotalMoneyRsp) throws Exception {
                                int supportCount = 0;
                                for (CrowdfundingOrderTotalPeopleRsp.ResultBean r : crowdfundingOrderTotalMoneyRsp.getResult()) {
                                    supportCount += r.getTotalPeople();
                                }
                                holder.mSupportCount.setText(String.valueOf(supportCount));
                            }
                        }),
                new BiFunction<CrowdfundingOrderTotalMoneyRsp, CrowdfundingOrderTotalPeopleRsp, String>() {
                    @Override
                    public String apply(CrowdfundingOrderTotalMoneyRsp crowdfundingOrderTotalMoneyRsp, CrowdfundingOrderTotalPeopleRsp crowdfundingOrderTotalPeopleRsp) throws Exception {
                        return "Success";
                    }
                }), new BasePresenter.MyObserver<String>() {
            @Override
            public void onMyNext(String s) {

            }

            @Override
            public void onMyError(String errorMessage) {
                ToastUtils.showShort(errorMessage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCrowdFundingDatas == null ? 0 : mCrowdFundingDatas.size();
    }

    class CrowdFundingViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTitleTv;
        private TextView mAlreadyPrice;
        private TextView mLeaveTime;
        private TextView mSupportCount;
        private ProgressBar mProgressBar;
        private TextView mBarText;

        public CrowdFundingViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_item);
            mTitleTv = itemView.findViewById(R.id.tv_title);
            mAlreadyPrice = itemView.findViewById(R.id.tv_already_price_value);
            mLeaveTime = itemView.findViewById(R.id.tv_leave_time_value);
            mSupportCount = itemView.findViewById(R.id.tv_support_count_value);
            mProgressBar = itemView.findViewById(R.id.progress_bar_percent);
            mBarText = itemView.findViewById(R.id.bar_text);
        }
    }

    public interface CrowdFundingListAdapterCallback {
        void onItemClick(ProductContent productContent);
    }
}
