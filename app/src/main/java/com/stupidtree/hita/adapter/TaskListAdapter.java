package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.timetable.Task;

import java.util.ArrayList;
import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.now;

import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    public static final int TYPE_FREE = -11;
    public static final int TYPE_ARRANGED= -22;
    public static final int TYPE_ARRANGED_TODAY = -33;
    public static final int TYPE_DONE= -44;
    ArrayList<Task> mBeans;
    LayoutInflater mInflater;
    OnItemLongClickListener mOnItemLongClickListener;
    OnFinishClickListener mOnFinishClickListener;


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

    public TaskListAdapter(Context context, ArrayList<Task> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View v = null;
        switch(type){
            case TYPE_FREE:v = mInflater.inflate(R.layout.dynamic_tasks_card_free,viewGroup,false);break;
            case TYPE_ARRANGED:v = mInflater.inflate(R.layout.dynamic_tasks_card_arranged,viewGroup,false);break;
            case TYPE_ARRANGED_TODAY:v = mInflater.inflate(R.layout.dynamic_tasks_card_arranged_today,viewGroup,false);break;
            case TYPE_DONE: v = mInflater.inflate(R.layout.dynamic_tasks_card_done,viewGroup,false);
        }
        return new TaskViewHolder(v,type);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaskViewHolder taskViewHolder, final int position) {
        taskViewHolder.name.setText(mBeans.get(position).name);
        if(taskViewHolder.limit!=null) taskViewHolder.limit.
                setText(mBeans.get(position).fW+"周"+ TextTools.words_time_DOW[mBeans.get(position).fDOW-1]
                +"-"+mBeans.get(position).tW+"周"+ TextTools.words_time_DOW[mBeans.get(position).tDOW-1]
                );
        if(taskViewHolder.progressLayout!=null) {
            if(mBeans.get(position).isHas_length()) taskViewHolder.progressLayout.setVisibility(View.VISIBLE);
            else taskViewHolder.progressLayout.setVisibility(View.GONE);
            taskViewHolder.progress.setProgress(mBeans.get(position).getProgress());
            taskViewHolder.progressText.setText(mBeans.get(position).getProgress()+"% 共"+mBeans.get(position).getLength()+"min");
        }
        if(mOnItemLongClickListener!=null){
            taskViewHolder.card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mOnItemLongClickListener.OnClick(v,position);
                }
            });
        }
        if(mOnFinishClickListener!=null&&taskViewHolder.bt_done!=null){
            taskViewHolder.bt_done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnFinishClickListener.OnClick(taskViewHolder.card,position);
                }
            });
        }
    }


    @Override
    public int getItemViewType(int position) {

        Task t = mBeans.get(position);
        int dow = now.get(Calendar.DAY_OF_WEEK)==1?7:now.get(Calendar.DAY_OF_WEEK)-1;
        if(t.isFinished()) return TYPE_DONE;
        if(!t.has_deadline) return TYPE_FREE;
        else{
            if(thisWeekOfTerm>=t.fW&&thisWeekOfTerm<=t.tW){
                if(thisWeekOfTerm==t.fW)return t.fDOW == dow?TYPE_ARRANGED_TODAY:TYPE_ARRANGED;
                if(thisWeekOfTerm==t.tW)return t.tDOW == dow?TYPE_ARRANGED_TODAY:TYPE_ARRANGED;
                return TYPE_ARRANGED_TODAY;
            }else{
                return  TYPE_ARRANGED;
            }
        }


    }

    public static int getTaskState(Task t){
        int dow = now.get(Calendar.DAY_OF_WEEK)==1?7:now.get(Calendar.DAY_OF_WEEK)-1;
        if(!t.has_deadline) return TYPE_FREE;
        else{
            if(thisWeekOfTerm>=t.fW&&thisWeekOfTerm<=t.tW){
                if(thisWeekOfTerm==t.fW)return t.fDOW == dow?TYPE_ARRANGED_TODAY:TYPE_ARRANGED;
                if(thisWeekOfTerm==t.tW)return t.tDOW == dow?TYPE_ARRANGED_TODAY:TYPE_ARRANGED;
                return TYPE_ARRANGED_TODAY;
            }else{
                return  TYPE_ARRANGED;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CardView card;
        TextView limit;
        ImageView bt_done;
        ProgressBar progress;
        TextView progressText;
        LinearLayout progressLayout;
        int type;
        public TaskViewHolder(@NonNull View itemView,int type) {
            super(itemView);
            name = itemView.findViewById(R.id.task_card_name);
            card = itemView.findViewById(R.id.card);
            limit = itemView.findViewById(R.id.task_card_limit);
            bt_done = itemView.findViewById(R.id.task_card_done);
            progress = itemView.findViewById(R.id.progress);
            progressText = itemView.findViewById(R.id.progress_text);
            progressLayout = itemView.findViewById(R.id.progress_layout);
            this.type = type;
        }
    }


}
