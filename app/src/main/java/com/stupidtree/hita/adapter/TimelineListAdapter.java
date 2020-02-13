package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;

import java.text.DecimalFormat;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.TimeWatcher.nowEvent;
import static com.stupidtree.hita.TimeWatcher.nowProgress;


public class TimelineListAdapter extends RecyclerView.Adapter<TimelineListAdapter.timelineHolder> {
    List<EventItem> mBeans;
    LayoutInflater mInflater;
    OnItemClickLitener mOnItemClickLitener;
    OnItemLongClickLitener mOnItemLongClickLitener;
    //OnNaviClickListener mOnNaviClickListener;
    Context mContext;
    public static final int TIMELINE_EVENT_TYPE_PASSED = 13;
    DecimalFormat df = new DecimalFormat("#.##");

    //设置回调接口
    public interface OnItemClickLitener{
        void onItemClick(View card,View time,View name, int position);
    }
    public interface OnItemLongClickLitener{
        void onItemLongClick(View view, int position);
    }
    public interface OnNaviClickListener{
        void onNaviClick(View view,int position,int type,String terminal);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setOnItemLongClickLitener(OnItemLongClickLitener mOnItemLongClickLitener){
        this.mOnItemLongClickLitener = mOnItemLongClickLitener;
    }

    public TimelineListAdapter(Context mContext, List<EventItem> res){
        mBeans = res;
        mInflater = LayoutInflater.from(mContext);
        //this.mBeanRoots = mBeanRoots;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public timelineHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View v = null;
        switch (type) {
            case TimetableCore.TIMETABLE_EVENT_TYPE_COURSE:
            case TimetableCore.TIMETABLE_EVENT_TYPE_EXAM:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_important, viewGroup, false);
                break;
            case TimetableCore.TIMETABLE_EVENT_TYPE_ARRANGEMENT:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_arrangement, viewGroup, false);
                break;
           case TIMELINE_EVENT_TYPE_PASSED:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_passed, viewGroup, false);
                break;
            case TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_deadline, viewGroup, false);
                break;
            case TimetableCore.TIMETABLE_EVENT_TYPE_REMIND:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_deadline, viewGroup, false);
                break;
        }
        return new timelineHolder(v,type);
    }


    @Override
    public int getItemViewType(int position) {
        int type;
        if (mBeans.get(position).endTime.compareTo(new HTime(now)) < 0) type = TIMELINE_EVENT_TYPE_PASSED;
        else type = mBeans.get(position).eventType;
        return type;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final timelineHolder timelineHolder, final int position) {
       // Log.e("onBind",""+position);
        try {
            if(timelineHolder.timeline!=null){
                timelineHolder.timeline.determineTimelineType(position,mBeans.size());
                if(mBeans.size()==1) timelineHolder.timeline.setVisibility(View.GONE);
                else timelineHolder.timeline.setVisibility(View.VISIBLE);
            }
            if(position>=mBeans.size()||position<0) return;
            //timelineHolder.timelineView.initLine(TimelineView.getTimeLineViewType(position,mBeans.size()));
            //timelineHolder.timelineView.setVisibility(View.GONE);
            timelineHolder.tv_name.setText(mBeans.get(position).mainName);
            if(timelineHolder.type==TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE||timelineHolder.type==TimetableCore.TIMETABLE_EVENT_TYPE_REMIND){
                if(timelineHolder.tv_time!=null)timelineHolder.tv_time.setText(mBeans.get(position).startTime.tellTime());
            }else{
                if(timelineHolder.tv_time!=null)timelineHolder.tv_time.setText(mBeans.get(position).startTime.tellTime()+"-"+mBeans.get(position).endTime.tellTime());
            }
            if(timelineHolder.tv_duration!=null){
                int duration = mBeans.get(position).startTime.getDuration(mBeans.get(position).endTime);
                if (duration >= 60)  timelineHolder.tv_duration.setText(duration / 60 + "h "+(duration%60==0?"":duration%60+"min"));
                else  timelineHolder.tv_duration.setText(duration + "min");
            }
            if(timelineHolder.progressBar!=null) {
                if (mBeans.get(position)==nowEvent) {
                    timelineHolder.progressBar.setVisibility(View.VISIBLE);
                    timelineHolder.progressBar.setProgress((int) (nowProgress*100));
                    timelineHolder.timeline.setImageDrawable(mContext.getDrawable(R.drawable.ic_timelapse));
                } else {
                    timelineHolder.progressBar.setVisibility(View.GONE);
                }
            }
            if(timelineHolder.tv_place!=null){
                String result = TextUtils.isEmpty(mBeans.get(position).tag2)?mContext.getString(R.string.unknown_location):mBeans.get(position).tag2;
                timelineHolder.tv_place.setText(result);
            }
//            if(mOnNaviClickListener!=null&&timelineHolder.naviButton!=null){
//                timelineHolder.naviButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mOnNaviClickListener.onNaviClick(v,position,mBeans.get(position).eventType,mBeans.get(position).tag2);
//                    }
//                });
//            }

            if(mOnItemClickLitener!=null){
                timelineHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onItemClick(v,timelineHolder.tv_time,timelineHolder.tv_name,position);
                    }
                });
            }
            if(mOnItemLongClickLitener!=null){
                timelineHolder.itemCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mOnItemLongClickLitener.onItemLongClick(v,position);
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    @Override
    public int getItemCount() {
        return mBeans.size();
    }
    class timelineHolder extends RecyclerView.ViewHolder{
        int type;
        TextView tv_time;
        TextView tv_name;
        TextView tv_duration;
        TextView tv_place;
        ProgressBar progressBar;
        CardView itemCard;
       // TimelineView timelineView;
        com.alorma.timeline.TimelineView timeline;
        //LinearLayout naviButton;
        public timelineHolder(@NonNull View itemView,int type) {
            super(itemView);
            this.type = type;
            tv_time = itemView.findViewById(R.id.tl_tv_time);
            tv_name = itemView.findViewById(R.id.tl_tv_name);
            tv_duration = itemView.findViewById(R.id.tl_tv_duration);
            itemCard = itemView.findViewById(R.id.tl_card);
            progressBar = itemView.findViewById(R.id.event_progressbar);
           // timelineView = itemView.findViewById(R.id.timelineview);
            tv_place = itemView.findViewById(R.id.tl_tv_place);
            //naviButton = itemView.findViewById(R.id.tl_bt_navi);
            timeline = itemView.findViewById(R.id.timeline);
            //timelineView.initLine(TimelineView.getTimeLineViewType(type/1000,mBeans.size()));
        }
    }


}
