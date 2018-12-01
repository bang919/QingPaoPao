package com.wopin.qingpaopao.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

public class OldChangeNewListAdapter extends RecyclerView.Adapter<OldChangeNewListAdapter.OldChangeNewViewHolder> {

    private ArrayList<ProductContent> mOldChangeNewDatas;
    private OldChangeNewClickListener mOldChangeNewClickListener;

    public void setOldChangeNewDatas(ArrayList<ProductContent> oldChangeNewDatas) {
        mOldChangeNewDatas = oldChangeNewDatas;
        notifyDataSetChanged();
    }

    public void setOldChangeNewClickListener(OldChangeNewClickListener oldChangeNewClickListener) {
        mOldChangeNewClickListener = oldChangeNewClickListener;
    }

    @NonNull
    @Override
    public OldChangeNewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OldChangeNewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_oldchangenew_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OldChangeNewViewHolder holder, int position) {
        final ProductContent productContent = mOldChangeNewDatas.get(position);
        GlideUtils.loadImage(holder.mImageView, -1, productContent.getImages().size() == 0 ? null : productContent.getImages().get(0).getSrc(), new CenterCrop());
        holder.mTitleTv.setText(productContent.getName());
        holder.mPriceTv.setText(String.format(holder.mPriceTv.getContext().getString(R.string.price_float), Float.parseFloat(productContent.getPrice())));
        holder.mSubTitleTv.setText(Html.fromHtml(productContent.getShort_description()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOldChangeNewClickListener != null) {
                    mOldChangeNewClickListener.onItemClick(productContent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOldChangeNewDatas == null ? 0 : mOldChangeNewDatas.size();
    }

    class OldChangeNewViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private TextView mTitleTv;
        private TextView mSubTitleTv;
        private TextView mPriceTv;
        private TextView mGotoBuyBtn;

        public OldChangeNewViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_item);
            mTitleTv = itemView.findViewById(R.id.tv_title);
            mSubTitleTv = itemView.findViewById(R.id.tv_sub_title);
            mPriceTv = itemView.findViewById(R.id.tv_price);
            mGotoBuyBtn = itemView.findViewById(R.id.btn_goto_buy);
        }
    }

    public interface OldChangeNewClickListener {
        void onItemClick(ProductContent productContent);
    }
}
