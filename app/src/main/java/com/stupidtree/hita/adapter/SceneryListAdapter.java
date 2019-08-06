package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Scenery;

import java.text.DecimalFormat;
import java.util.List;

public class SceneryListAdapter extends RecyclerView.Adapter <SceneryListAdapter.FoodViewHolder>{

    LayoutInflater mInflater;
    List<Scenery> mBeans;
    Context mContext;
    OnItemClickListener mOnItemClickListener;
    DecimalFormat df;

    public void setmOnNaviClickListener(OnNaviClickListener mOnNaviClickListener) {
        this.mOnNaviClickListener = mOnNaviClickListener;
    }

    OnNaviClickListener mOnNaviClickListener;
    public interface OnItemClickListener{
        void OnClick(View v,ImageView transition ,int position);
    }


    public interface OnNaviClickListener {
        void OnClick(Scenery c);
    }
    public void setOnItemClickListener(OnItemClickListener x){
        this.mOnItemClickListener = x;

    }
    public SceneryListAdapter(Context context, List<Scenery> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        mContext = context;
        df = new DecimalFormat("#0.00");
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View v = mInflater.inflate(R.layout.dynamic_scenery_item,viewGroup,false);
       return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FoodViewHolder foodViewHolder, final int i) {
        foodViewHolder.rate.setText(df.format(mBeans.get(i).getRate())+"/10");
        foodViewHolder.name.setText(mBeans.get(i).getName());
        if(mOnNaviClickListener!=null){
            foodViewHolder.button_navi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNaviClickListener.OnClick(mBeans.get(i));
                }
            });
        }
        if(mOnItemClickListener!=null){
            foodViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(v,foodViewHolder.image,i);
                }
            });
        }
        Glide.with(mContext).load(mBeans.get(i).getImageURL()).into(foodViewHolder.image);
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder{

        TextView name,rate;
        ImageView button_navi,image;
        CardView card;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.scenery_name);
           rate = itemView.findViewById(R.id.scenery_rate);
            button_navi = itemView.findViewById(R.id.scenery_navi_button);
            card = itemView.findViewById(R.id.scenery_card);
            image = itemView.findViewById(R.id.scenery_image);
        }
    }
}
