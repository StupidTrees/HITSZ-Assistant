package com.stupidtree.hita.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.Task;
import com.stupidtree.hita.diy.TaskDialog;
import com.stupidtree.hita.hita.TextTools;

import java.util.ArrayList;
import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class TaskListAdapterMini extends RecyclerView.Adapter<TaskListAdapterMini.TaskViewHolder> {
    public static final int TYPE_FREE = -11;
    public static final int TYPE_ARRANGED_NOT_YET = -22;
    public static final int TYPE_ARRANGED_ONGOING = -33;
    public static final int TYPE_DONE= -44;
    ArrayList<Task> mBeans;
    LayoutInflater mInflater;
    OnItemLongClickListener mOnItemLongClickListener;
    OnFinishClickListener mOnFinishClickListener;
    BaseActivity context;



    public interface OnItemLongClickListener{
        boolean OnClick(View v, int position);
    }
    public interface OnFinishClickListener{
        boolean OnClick(View v, Task t, int position);
    }
    public void setmOnItemLongClickListener(OnItemLongClickListener X){
        this.mOnItemLongClickListener = X;
    }


    public void setmOnFinishClickListener(OnFinishClickListener mOnFinishClickListener) {
        this.mOnFinishClickListener = mOnFinishClickListener;
    }

    public TaskListAdapterMini(BaseActivity context, ArrayList<Task> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View v = mInflater.inflate(R.layout.dynamic_tasks_card_item,viewGroup,false);
        return new TaskViewHolder(v);
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
            if(taskViewHolder.done!=null){
                taskViewHolder.done.setChecked(mBeans.get(position).isFinished());
                if(mBeans.get(position).isFinished()){
                    taskViewHolder.name.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    taskViewHolder.name.setAlpha(0.3f);

                    //taskViewHolder.name.setTextColor(context.getTextColorSecondary());
                }else{
                    taskViewHolder.name.setPaintFlags( taskViewHolder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    //taskViewHolder.name.setTextColor(context.getTextColorPrimary());
                    taskViewHolder.name.setAlpha(1f);
                }
            }
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
                        //Log.e("checked:", String.valueOf(position));
                        if(buttonView.isPressed()&&mBeans.get(position).isFinished()!=isChecked) mOnFinishClickListener.OnClick(taskViewHolder.card,mBeans.get(position),position);
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


    public static int getTaskState(Task t){
        //int dow = now.get(Calendar.DAY_OF_WEEK)==1?7:now.get(Calendar.DAY_OF_WEEK)-1;
        if(t.isFinished()) return TYPE_DONE;
        if(!t.has_deadline) return TYPE_FREE;
        else{
            Calendar from = timeTableCore.getCurrentCurriculum().getDateAt(t.fW,t.fDOW,t.sTime);
            Calendar to = timeTableCore.getCurrentCurriculum().getDateAt(t.tW,t.tDOW,t.eTime);
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
        ViewGroup card;
        TextView limit;
        CheckBox done;
        ProgressBar progress;
        int type;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.task_card_name);
            card = itemView.findViewById(R.id.card);
            done = itemView.findViewById(R.id.done);
            progress = itemView.findViewById(R.id.progress);
            this.type = type;
        }
    }


}
