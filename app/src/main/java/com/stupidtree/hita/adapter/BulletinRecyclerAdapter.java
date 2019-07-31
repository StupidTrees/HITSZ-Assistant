package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;

public class BulletinRecyclerAdapter extends RecyclerView.Adapter<BulletinRecyclerAdapter.BulletinViewHolder> {
    List<Map<String,String>> mBeans;
    LayoutInflater mInflater;
    OnItemClickListener mOnItemClickListener;

    public BulletinRecyclerAdapter(Context c, List<Map<String, String>> res){
        mBeans = res;
        mInflater = LayoutInflater.from(c);
    }
    public interface OnItemClickListener{
        void OnClick(View v,int position);
    }
    public  void setmOnItemClickListener(OnItemClickListener x){
        this.mOnItemClickListener = x;
    }

    @NonNull
    @Override
    public BulletinViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View  v = mInflater.inflate(R.layout.dynamic_bulletin_card,viewGroup,false);
        return new BulletinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BulletinViewHolder bulletinViewHolder, final int i) {
        bulletinViewHolder.title.setText(mBeans.get(i).get("title"));
        bulletinViewHolder.time.setText(mBeans.get(i).get("time"));
        bulletinViewHolder.views.setText(mBeans.get(i).get("views"));
        if(mOnItemClickListener!=null){
            bulletinViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(v,i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class BulletinViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView time;
        TextView views;
        CardView card;
        public BulletinViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.bulletin_card_title);
            time = itemView.findViewById(R.id.bulletin_card_time);
            views = itemView.findViewById(R.id.bulletin_card_views);
            card = itemView.findViewById(R.id.bulletin_card);
        }
    }
}
