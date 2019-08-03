package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.mBlurTransformation;
import com.stupidtree.hita.online.Canteen;

import java.text.DecimalFormat;
import java.util.List;

public class CanteenListAdapter extends RecyclerView.Adapter<CanteenListAdapter.FoodViewHolder> {

    LayoutInflater mInflater;
    List<Canteen> mBeans;
    Context mContext;
    OnItemClickListener mOnItemClickListener;
    DecimalFormat df;

    public void setmOnNaviClickListener(OnNaviClickListener mOnNaviClickListener) {
        this.mOnNaviClickListener = mOnNaviClickListener;
    }

    OnNaviClickListener mOnNaviClickListener;

    public interface OnItemClickListener {
        void OnClick(View v, ImageView transitionImage,int position);
    }


    public interface OnNaviClickListener {
        void OnClick(Canteen c);
    }

    public void setOnItemClickListener(OnItemClickListener x) {
        this.mOnItemClickListener = x;

    }

    public CanteenListAdapter(Context context, List<Canteen> res) {
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        mContext = context;
        df = new DecimalFormat("#0.00");
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_canteen_item, viewGroup, false);
        return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FoodViewHolder foodViewHolder, final int i) {
        foodViewHolder.rate.setText(df.format(mBeans.get(i).getRate()) + "/10");
        foodViewHolder.rank.setText("排名第" + (i + 1));
        foodViewHolder.name.setText(mBeans.get(i).getName());
        foodViewHolder.company.setText(mBeans.get(i).getCompany());
        if (mOnNaviClickListener != null) {
            foodViewHolder.button_navi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNaviClickListener.OnClick(mBeans.get(i));
                }
            });
        }
        if (mOnItemClickListener != null) {
            foodViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(v,foodViewHolder.image,i);
                }
            });
        }
        Glide.with(mContext).load(mBeans.get(i).getImageURL()).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(foodViewHolder.image);
        //Glide.with(mContext).load(mBeans.get(i).getImageURL()).placeholder(R.drawable.timeline_head_bg).apply(RequestOptions.bitmapTransform(new mBlurTransformation(mContext))).into(foodViewHolder.background);


    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView name, rate, rank,company;
        ImageView button_navi, image,background;
        CardView card;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.canteen_name);
            rank = itemView.findViewById(R.id.canteen_rank);
            rate = itemView.findViewById(R.id.canteen_rate);
            button_navi = itemView.findViewById(R.id.canteen_navi_button);
            card = itemView.findViewById(R.id.canteen_card);
            image = itemView.findViewById(R.id.canteen_img);
            background = itemView.findViewById(R.id.canteen_bg);
            company = itemView.findViewById(R.id.canteen_company);
        }
    }
}
