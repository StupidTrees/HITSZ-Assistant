package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Canteen;

import java.text.DecimalFormat;
import java.util.List;

public class CanteenListAdapter extends BaseListAdapter<Canteen, CanteenListAdapter.FoodViewHolder> {

    private DecimalFormat df;
    private OnNaviClickListener mOnNaviClickListener;
    public CanteenListAdapter(Context context, List<Canteen> res) {
        super(context, res);
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        mContext = context;
        df = new DecimalFormat("#0.00");
    }


    public interface OnItemClickListener {
        void OnClick(View v, ImageView transitionImage, int position);
    }


    public interface OnNaviClickListener {
        void OnClick(Canteen c);
    }

    public void setOnNavigationClickListener(OnNaviClickListener mOnNaviClickListener) {
        this.mOnNaviClickListener = mOnNaviClickListener;
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.dynamic_canteen_item;
    }

    @Override
    public FoodViewHolder createViewHolder(View v, int viewType) {
        return new FoodViewHolder(v);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final FoodViewHolder foodViewHolder, final int i) {
        foodViewHolder.rate.setText(df.format(mBeans.get(i).getRate()) + "/10");
        foodViewHolder.rank.setText((i + 1)+"");
        foodViewHolder.name.setText(mBeans.get(i).getName());
        foodViewHolder.company.setText(mBeans.get(i).getCompany());
        if (mOnItemClickListener != null) {
            foodViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, i);
                }
            });
        }
        Glide.with(mContext).load(mBeans.get(i).getImageURL()).into(foodViewHolder.image);
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView name, rate, rank,company;
        ImageView image,background;
        CardView card;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.canteen_name);
            rank = itemView.findViewById(R.id.canteen_rank);
            rate = itemView.findViewById(R.id.canteen_rate);
            card = itemView.findViewById(R.id.canteen_card);
            image = itemView.findViewById(R.id.canteen_img);
            background = itemView.findViewById(R.id.canteen_bg);
            company = itemView.findViewById(R.id.canteen_company);
        }
    }
}
