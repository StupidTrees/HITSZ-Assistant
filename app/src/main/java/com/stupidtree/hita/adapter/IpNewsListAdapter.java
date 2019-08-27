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
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.CornerTransform;

import java.util.List;
import java.util.Map;

public class IpNewsListAdapter extends RecyclerView.Adapter<IpNewsListAdapter.NewsViewHolder> {

    Context mContext;
    List<Map<String,String>> mBeans;
    LayoutInflater mInflater;
    OnItemClickListener mOnItemClickListener;

    CornerTransform transformation;


    public interface OnItemClickListener{
        void OnClick(View v, int pos);
    }

    public  void setOnItemClickListener(OnItemClickListener x){
        this.mOnItemClickListener = x;
    }
    public IpNewsListAdapter(Context context, List<Map<String, String>> res){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        transformation = new CornerTransform(mContext, dip2px(mContext, 10));
        transformation.setExceptCorner(false, false, false, false);
    }
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_ipnews_card,viewGroup,false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsViewHolder newsViewHolder, final int i) {
        newsViewHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.OnClick(v,i);
            }
        });
        newsViewHolder.subtitle.setText(mBeans.get(i).get("subtitle"));
        newsViewHolder.title.setText(mBeans.get(i).get("title"));
        newsViewHolder.time.setText(mBeans.get(i).get("time"));
        //newsViewHolder.views.setText(mBeans.get(i).get("views"));
        if(mBeans.get(i).get("image")==null||mBeans.get(i).get("image").isEmpty()){
            newsViewHolder.image.setVisibility(View.GONE);
            //newsViewHolder.image.setImageResource(R.drawable.gradient_bg);
        }else{
            newsViewHolder.image.setVisibility(View.VISIBLE);

            Glide.with(mContext).load(mBeans.get(i).get("image"))
                    //.apply(RequestOptions.bitmapTransform(new RoundedCorners()))
                    .apply(RequestOptions.bitmapTransform(transformation))
                    .into(newsViewHolder.image);
    }
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    class NewsViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView subtitle;
        TextView time;
        //TextView views;
        CardView card;
        ImageView image;
       // ImageView channelLogo;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title);
            time = itemView.findViewById(R.id.news_time);
            card = itemView.findViewById(R.id.news_card);
            subtitle = itemView.findViewById(R.id.news_subtitle);
            image = itemView.findViewById(R.id.news_img);
        }
    }
}
