package com.stupidtree.hita.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stupidtree.hita.diy.TaskDialog;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.timetable.Task;

import java.util.ArrayList;
import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;

import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    public static final int TYPE_FREE = -11;
    public static final int TYPE_ARRANGED_NOT_YET = -22;
    public static final int TYPE_ARRANGED_ONGOING = -33;
    public static final int TYPE_DONE= -44;
    ArrayList<Task> mBeans;
    LayoutInflater mInflater;
    OnItemLongClickListener mOnItemLongClickListener;
    OnFinishClickListener mOnFinishClickListener;
    Activity context;



    public interface OnItemLongClickListener{
        boolean OnClick(View v,int position);
    }
    public interface OnFinishClickListener{
        boolean OnClick(View v,int position);
    }
    public void setmOnItemLongClickListener(OnItemLongClickListener X){
        this.mOnItemLongClickListener = X;
    }


    public void setmOnFinishClickListener(OnFinishClickListener mOnFinishClickListener) {
        this.mOnFinishClickListener = mOnFinishClickListener;
    }

    public TaskListAdapter(Activity context, ArrayList<Task> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View v = null;
        switch(type){
            case TYPE_FREE:v = mInflater.inflate(R.layout.dynamic_tasks_card_free,viewGroup,false);break;
            case TYPE_ARRANGED_NOT_YET:v = mInflater.inflate(R.layout.dynamic_tasks_card_arranged,viewGroup,false);break;
            case TYPE_ARRANGED_ONGOING:v = mInflater.inflate(R.layout.dynamic_tasks_card_arranged_today,viewGroup,false);break;
            case TYPE_DONE: v = mInflater.inflate(R.layout.dynamic_tasks_card_done,viewGroup,false);break;
        }
        return new TaskViewHolder(v,type);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaskViewHolder taskViewHolder, final int position) {
        try {
            taskViewHolder.name.setText(mBeans.get(position).name);
            if(taskViewHolder.limit!=null) taskViewHolder.limit.
                    setText(mBeans.get(position).fW+"周"+ TextTools.words_time_DOW[mBeans.get(position).fDOW-1]
                    +"-"+mBeans.get(position).tW+"周"+ TextTools.words_time_DOW[mBeans.get(position).tDOW-1]
                    );
            if(taskViewHolder.progress!=null) {
                if(mBeans.get(position).isHas_length()) taskViewHolder.progress.setVisibility(View.VISIBLE);
                else taskViewHolder.progress.setVisibility(View.GONE);
                taskViewHolder.progress.setProgress(mBeans.get(position).getProgress());
            }
            if(taskViewHolder.done!=null) taskViewHolder.done.setChecked(false);
            if(mBeans.get(position).isHas_length()&&taskViewHolder.done!=null) taskViewHolder.done.setVisibility(View.GONE);
            else if(!mBeans.get(position).isHas_length()&&taskViewHolder.done!=null) taskViewHolder.done.setVisibility(View.VISIBLE);
            if(mOnItemLongClickListener!=null){
                taskViewHolder.card.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return mOnItemLongClickListener.OnClick(v,position);
                    }
                });
            }
            if(mOnFinishClickListener!=null&&taskViewHolder.done!=null){
                taskViewHolder.done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) mOnFinishClickListener.OnClick(taskViewHolder.card,position);
                    }
                });

            }
            taskViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   new TaskDialog(context,mBeans.get(position)).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public int getItemViewType(int position) {

        Task t = mBeans.get(position);
//        int dow = now.get(Calendar.DAY_OF_WEEK)==1?7:now.get(Calendar.DAY_OF_WEEK)-1;
//        if(t.isFinished()) return TYPE_DONE;
//        if(!t.has_deadline) return TYPE_FREE;
//        else{
//            if(thisWeekOfTerm>=t.fW&&thisWeekOfTerm<=t.tW){
//                if(thisWeekOfTerm==t.fW)return t.fDOW == dow? TYPE_ARRANGED_ONGOING : TYPE_ARRANGED_NOT_YET;
//                if(thisWeekOfTerm==t.tW)return t.tDOW == dow? TYPE_ARRANGED_ONGOING : TYPE_ARRANGED_NOT_YET;
//                return TYPE_ARRANGED_ONGOING;
//            }else{
//                return TYPE_ARRANGED_NOT_YET;
//            }
//        }
        return getTaskState(t);
    }

    public static int getTaskState(Task t){
        //int dow = now.get(Calendar.DAY_OF_WEEK)==1?7:now.get(Calendar.DAY_OF_WEEK)-1;
        if(t.isFinished()) return TYPE_DONE;
        if(!t.has_deadline) return TYPE_FREE;
        else{
            Calendar from = mainTimeTable.core.getDateAt(t.fW,t.fDOW,t.sTime);
            Calendar to = mainTimeTable.core.getDateAt(t.tW,t.tDOW,t.eTime);
            if(from.before(now)&&to.after(now)) return TYPE_ARRANGED_ONGOING;
            else if(from.after(now)) return TYPE_ARRANGED_NOT_YET;
            else if(to.before(now)) return TYPE_DONE;
//            if(thisWeekOfTerm>=t.fW&&thisWeekOfTerm<=t.tW){
//                if(thisWeekOfTerm==t.fW)return t.fDOW == dow? TYPE_ARRANGED_ONGOING : TYPE_ARRANGED_NOT_YET;
//                if(thisWeekOfTerm==t.tW)return t.tDOW == dow? TYPE_ARRANGED_ONGOING : TYPE_ARRANGED_NOT_YET;
//                return TYPE_ARRANGED_ONGOING;
//            }else{
//                return TYPE_ARRANGED_NOT_YET;
//            }
        }
        return TYPE_ARRANGED_NOT_YET;
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CardView card;
        TextView limit;
        CheckBox done;
        ProgressBar progress;
        int type;
        public TaskViewHolder(@NonNull View itemView,int type) {
            super(itemView);
            name = itemView.findViewById(R.id.task_card_name);
            card = itemView.findViewById(R.id.card);
            limit = itemView.findViewById(R.id.task_card_limit);
            done = itemView.findViewById(R.id.done);
            progress = itemView.findViewById(R.id.progress);
            this.type = type;
        }
    }


}
