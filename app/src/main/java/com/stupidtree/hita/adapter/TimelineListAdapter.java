package com.stupidtree.hita.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.diy.TimeLineEventItem;

import java.text.DecimalFormat;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.TimeWatcher.nowEvent;
import static com.stupidtree.hita.TimeWatcher.nowProgress;
import static com.stupidtree.hita.TimeWatcher.todaysEvents;


public class TimelineListAdapter extends RecyclerView.Adapter<TimelineListAdapter.timelineHolder> {
    List<EventItem> mBeans;
    LayoutInflater mInflater;
    OnItemClickLitener mOnItemClickLitener;
    OnItemLongClickLitener mOnItemLongClickLitener;
    OnNaviClickListener mOnNaviClickListener;
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

    public OnNaviClickListener getmOnNaviClickListener() {
        return mOnNaviClickListener;
    }

    public void setmOnNaviClickListener(OnNaviClickListener mOnNaviClickListener) {
        this.mOnNaviClickListener = mOnNaviClickListener;
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
        switch (type%1000) {
            case TimeTable.TIMETABLE_EVENT_TYPE_COURSE:
            case TimeTable.TIMETABLE_EVENT_TYPE_EXAM:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_important, viewGroup, false);
                break;
            case TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_arrangement, viewGroup, false);
                break;
            case TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_dynamic, viewGroup, false);
                break;
            case TIMELINE_EVENT_TYPE_PASSED:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_passed, viewGroup, false);
                break;
            case TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_deadline, viewGroup, false);
                break;
            case TimeTable.TIMETABLE_EVENT_TYPE_REMIND:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_remind, viewGroup, false);
                break;
        }
        return new timelineHolder(v,type);
    }


    @Override
    public int getItemViewType(int position) {
        int type;
        if (mBeans.get(position).endTime.compareTo(new HTime(now)) < 0) type = TIMELINE_EVENT_TYPE_PASSED;
        else type = mBeans.get(position).eventType;
        return type+position*1000;
    }

    @Override
    public void onBindViewHolder(@NonNull final timelineHolder timelineHolder, final int position) {
       // Log.e("onBind",""+position);
        timelineHolder.timelineView.initLine(TimelineView.getTimeLineViewType(position,mBeans.size()));
        //timelineHolder.timelineView.setVisibility(View.GONE);
        timelineHolder.tv_name.setText(mBeans.get(position).mainName);
        if(timelineHolder.type==TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE||timelineHolder.type==TimeTable.TIMETABLE_EVENT_TYPE_REMIND){
            if(timelineHolder.tv_time!=null)timelineHolder.tv_time.setText(mBeans.get(position).startTime.tellTime());
        }else{
            if(timelineHolder.tv_time!=null)timelineHolder.tv_time.setText(mBeans.get(position).startTime.tellTime()+"-"+mBeans.get(position).endTime.tellTime());
        }
        if(timelineHolder.tv_duration!=null){
            int duration = mBeans.get(position).startTime.getDuration(mBeans.get(position).endTime);
            if (duration >= 60)  timelineHolder.tv_duration.setText(duration / 60 + "小时"+(duration%60==0?"":duration%60+"分钟"));
            else  timelineHolder.tv_duration.setText(duration + "min");
        }
        if(timelineHolder.progressBar!=null) {
            if (mBeans.get(position)==nowEvent) {
                timelineHolder.progressBar.setVisibility(View.VISIBLE);
                timelineHolder.progressBar.setProgress((int) (nowProgress*100));
                timelineHolder.timelineView.setMarker(mContext.getDrawable(R.drawable.timeline_marker_now));
            } else {
                timelineHolder.progressBar.setVisibility(View.GONE);
            }
        }
        if(timelineHolder.tv_place!=null){
            timelineHolder.tv_place.setText(mBeans.get(position).tag2);
        }
        if(mOnNaviClickListener!=null&&timelineHolder.naviButton!=null){
            timelineHolder.naviButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnNaviClickListener.onNaviClick(v,position,mBeans.get(position).eventType,mBeans.get(position).tag2);
                }
            });
        }

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
        TimelineView timelineView;
        LinearLayout naviButton;
        public timelineHolder(@NonNull View itemView,int type) {
            super(itemView);
            this.type = type%1000;
            tv_time = itemView.findViewById(R.id.tl_tv_time);
            tv_name = itemView.findViewById(R.id.tl_tv_name);
            tv_duration = itemView.findViewById(R.id.tl_tv_duration);
            itemCard = itemView.findViewById(R.id.tl_card);
            progressBar = itemView.findViewById(R.id.event_progressbar);
            timelineView = itemView.findViewById(R.id.timelineview);
            tv_place = itemView.findViewById(R.id.tl_tv_place);
            naviButton = itemView.findViewById(R.id.tl_bt_navi);
            timelineView.initLine(TimelineView.getTimeLineViewType(type/1000,mBeans.size()));
        }
    }


}
