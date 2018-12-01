package com.wopin.qingpaopao.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.ProductContent;
import com.wopin.qingpaopao.utils.GlideUtils;

import java.util.ArrayList;

public class ScoreMarketContentAdapter extends RecyclerView.Adapter<ScoreMarketContentAdapter.ScoreMarketContentViewHolder> {

    private ArrayList<ProductContent> mProductContents;
    private ScoreMarketContentClick mScoreMarketContentClick;

    public void setScoreMarketContentClick(ScoreMarketContentClick scoreMarketContentClick) {
        mScoreMarketContentClick = scoreMarketContentClick;
    }

    public void setProductContents(ArrayList<ProductContent> productContents) {
        mProductContents = productContents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreMarketContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScoreMarketContentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_welfare_scoremarket_content, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreMarketContentViewHolder holder, int position) {
        final ProductContent productContent = mProductContents.get(position);
        GlideUtils.loadImage(holder.mImageView, -1, productContent.getImages().size() == 0 ? null : productContent.getImages().get(0).getSrc(), new CenterCrop());
        holder.mTitle.setText(productContent.getName());
        holder.mScoreTv.setText(String.format(holder.mScoreTv.getContext().getString(R.string.score_number), productContent.getPrice()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScoreMarketContentClick != null) {
                    mScoreMarketContentClick.onScoreMarketContentClick(productContent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProductContents == null ? 0 : mProductContents.size();
    }

    class ScoreMarketContentViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTitle;
        private TextView mScoreTv;

        public ScoreMarketContentViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_item_scoremarket_content);
            mTitle = itemView.findViewById(R.id.tv_item_scoremarket_content_title);
            mScoreTv = itemView.findViewById(R.id.tv_item_scoremarket_content_content);
        }
    }

    public interface ScoreMarketContentClick {
        void onScoreMarketContentClick(ProductContent productContent);
    }
}
