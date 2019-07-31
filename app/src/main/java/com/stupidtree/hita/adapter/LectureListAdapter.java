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
import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;

public class LectureListAdapter extends RecyclerView.Adapter<LectureListAdapter.LectureViewHolder> {

    Context mContext;
    List<Map<String,String>> mBeans;
    LayoutInflater mInflater;
    OnItemClickListener mOnItemClickListener;
    OnNaviClickListener mOnNaviClickListener;

    public interface OnItemClickListener{
        void OnClick(View v,int pos);
    }
    public interface OnNaviClickListener{
        void OnClick(View v,String termial);
    }
    public  void setOnItemClickListener(OnItemClickListener x){
        this.mOnItemClickListener = x;
    }
    public void setmOnNaviClickListener(OnNaviClickListener x) {this.mOnNaviClickListener = x;}
    public LectureListAdapter(Context context, List<Map<String, String>> res){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mBeans = res;
    }
    @NonNull
    @Override
    public LectureViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_lecture_card,viewGroup,false);
        return new LectureViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureViewHolder lectureViewHolder, final int i) {
        if(mBeans.get(i).get("title")==null||mBeans.get(i).get("title").equals("")){
            lectureViewHolder.title.setVisibility(View.GONE);
        }else{
            lectureViewHolder.title.setVisibility(View.VISIBLE);
            lectureViewHolder.title.setText(mBeans.get(i).get("title"));
        }
//        if(mBeans.get(i).get("host")==null||mBeans.get(i).get("host").equals("")){
//            lectureViewHolder.host.setVisibility(View.GONE);
//        }else{
//            lectureViewHolder.host.setVisibility(View.VISIBLE);
//            lectureViewHolder.host.setText(mBeans.get(i).get("host"));
//        }
        if(mBeans.get(i).get("place")==null||mBeans.get(i).get("place").equals("")){
            lectureViewHolder.place.setVisibility(View.GONE);
        }else{
            lectureViewHolder.place.setVisibility(View.VISIBLE);
            lectureViewHolder.place.setText(mBeans.get(i).get("place"));
        }

        if(mBeans.get(i).get("time")==null||mBeans.get(i).get("time").equals("")){
            lectureViewHolder.time.setVisibility(View.GONE);
        }else{
            lectureViewHolder.time.setVisibility(View.VISIBLE);
            lectureViewHolder.time.setText(mBeans.get(i).get("time"));
        }

        if(mBeans.get(i).get("releasetime")==null||mBeans.get(i).get("releasetime").equals("")){
            lectureViewHolder.releaseTime.setVisibility(View.GONE);
        }else{
            lectureViewHolder.releaseTime.setVisibility(View.VISIBLE);
            lectureViewHolder.releaseTime.setText(mBeans.get(i).get("releasetime"));
        }

        if(mBeans.get(i).get("picture")!=null&&!mBeans.get(i).get("picture").isEmpty()){
            lectureViewHolder.picture.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mBeans.get(i).get("picture")).into(lectureViewHolder.picture);
        }else{
            lectureViewHolder.picture.setVisibility(View.GONE);
        }

        if(mOnItemClickListener!=null){
            lectureViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(v,i);
                }
            });
        }
        if(mOnNaviClickListener!=null){
            lectureViewHolder.navi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNaviClickListener.OnClick(v,mBeans.get(i).get("place").replace("讲座地点：",""));
                }
            });
        }




    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class LectureViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView host;
        TextView place;
        TextView time;
        TextView releaseTime;
        //extView views;
        ImageView picture,navi;
        CardView card;

        public LectureViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.lecture_title);
            host = itemView.findViewById(R.id.lecture_host);
            place = itemView.findViewById(R.id.lecture_place);
            time = itemView.findViewById(R.id.lecture_time);
            releaseTime = itemView.findViewById(R.id.lecture_releasetime);
            //views = itemView.findViewById(R.id.lecture_views);
            picture = itemView.findViewById(R.id.lecture_picture);
            card = itemView.findViewById(R.id.lecture_card);
            navi = itemView.findViewById(R.id.lecture_card_navi);
        }
    }
}
