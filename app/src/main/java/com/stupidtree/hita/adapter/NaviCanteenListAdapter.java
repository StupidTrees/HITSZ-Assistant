package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.CornerTransform;
import com.stupidtree.hita.online.Canteen;

import java.text.DecimalFormat;
import java.util.List;

import static com.stupidtree.hita.adapter.IpNewsListAdapter.dip2px;

public class NaviCanteenListAdapter extends RecyclerView.Adapter <NaviCanteenListAdapter.FoodViewHolder>{

    LayoutInflater mInflater;
    List<Canteen> mBeans;
    Context mContext;
    OnItemClickListener mOnItemClickListener;
    DecimalFormat df;
    CornerTransform transformation;

    public void setmOnNaviClickListener(OnNaviClickListener mOnNaviClickListener) {
        this.mOnNaviClickListener = mOnNaviClickListener;
    }

    OnNaviClickListener mOnNaviClickListener;
    interface OnItemClickListener{
        void OnClick(View v, int position);
    }


    public interface OnNaviClickListener {
        void OnClick(Canteen c);
    }
    void setmOnItemClickListener(OnItemClickListener x){
        this.mOnItemClickListener = x;

    }

    public NaviCanteenListAdapter(Context context, List<Canteen> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        mContext = context;
        df = new DecimalFormat("#0.00");
        transformation = new CornerTransform(mContext, dip2px(mContext, 12));
        transformation.setExceptCorner(false, false, false, false);

    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View v = mInflater.inflate(R.layout.dynamic_navi_canteen_item,viewGroup,false);
       return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, final int i) {
        foodViewHolder.rate.setText(df.format(mBeans.get(i).getRate())+"/10");
        foodViewHolder.name.setText(mBeans.get(i).getName());
        Glide.with(mContext).load(mBeans.get(i).getImageURL()).
                apply(RequestOptions.bitmapTransform(transformation)).
                into(foodViewHolder.image);
        foodViewHolder.rank.setText((i+1)+"");
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder{

        TextView name,rate,rank;
        ImageView image;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.canteen_name);
           rate = itemView.findViewById(R.id.canteen_rate);
           image = itemView.findViewById(R.id.canteen_image);
           rank = itemView.findViewById(R.id.canteen_rank);
        }
    }
}
