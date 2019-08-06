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

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Facility;

import java.text.DecimalFormat;
import java.util.List;

public class FacilityListAdapter extends RecyclerView.Adapter <FacilityListAdapter.FoodViewHolder>{

    LayoutInflater mInflater;
    List<Facility> mBeans;
    Context mContext;
    OnItemClickListener mOnItemClickListener;
    DecimalFormat df;

    public void setmOnNaviClickListener(OnNaviClickListener mOnNaviClickListener) {
        this.mOnNaviClickListener = mOnNaviClickListener;
    }

    OnNaviClickListener mOnNaviClickListener;
    public interface OnItemClickListener{
        void OnClick(View v, int position);
    }


    public interface OnNaviClickListener {
        void OnClick(Facility c);
    }
    public void setOnItemClickListener(OnItemClickListener x){
        this.mOnItemClickListener = x;

    }
    public FacilityListAdapter(Context context, List<Facility> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        mContext = context;
        df = new DecimalFormat("#0.00");
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View v = mInflater.inflate(R.layout.dynamic_facility_item,viewGroup,false);
       return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, final int i) {
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
                    mOnItemClickListener.OnClick(v,i);
                }
            });
        }
        //Glide.with(mContext).load(mBeans.get(i).getImageURL()).into(foodViewHolder.picture);
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder{

        TextView name,rate;
        ImageView button_navi;
        CardView card;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.facility_name);
           rate = itemView.findViewById(R.id.facility_rate);
            button_navi = itemView.findViewById(R.id.facility_navi_button);
            card = itemView.findViewById(R.id.facility_card);
        }
    }
}
