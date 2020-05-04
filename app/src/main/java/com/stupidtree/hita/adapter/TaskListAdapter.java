package com.stupidtree.hita.adapter;

import android.graphics.Paint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.timetable.packable.Task;

import java.util.ArrayList;

public class TaskListAdapter extends BaseCheckableListAdapter<Task, TaskListAdapter.TaskViewHolder> {
    private static final int TAG = 692;
    private static final int ITEM = 95;
    private static final int FINISHED = 366;
    private OnFinishClickListener mOnFinishClickListener;


    public TaskListAdapter(BaseActivity context, ArrayList<Task> res) {
        super(context, res);
        mBeans = res;
    }

    public void setOnFinishClickListener(OnFinishClickListener mOnFinishClickListener) {
        this.mOnFinishClickListener = mOnFinishClickListener;
    }

    @Override
    protected int getLayoutId(int viewType) {
        if (viewType == TAG) return R.layout.dynamic_ddl_tag;
        else if (viewType == ITEM) return R.layout.dynamic_tasks_item;
        else return R.layout.dynamic_tasks_item_finished;
    }

    @Override
    public int getItemViewType(int position) {
        if (mBeans.get(position).getType() == Task.TAG) return TAG;
        else if (mBeans.get(position).isFinished()) return FINISHED;
        else return ITEM;
    }

    @Override
    public TaskViewHolder createViewHolder(View v, int viewType) {
        return new TaskViewHolder(v, viewType);
    }

    @Override
    void bindHolderData(final TaskViewHolder holder, final int position, Task data) {

        if (holder.type == TAG) {
            holder.name.setText(mBeans.get(position).name);
        } else {
            holder.name.setText(mBeans.get(position).name);
            if (holder.done != null) {
                holder.done.setChecked(mBeans.get(position).isFinished());
                if (mBeans.get(position).isFinished()) {
                    holder.name.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.name.setAlpha(0.3f);
                } else {
                    holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.name.setAlpha(1f);
                }
            }
            if (!EditMode && holder.done != null) holder.done.setVisibility(View.VISIBLE);
            else if (holder.done != null) holder.done.setVisibility(View.GONE);
            if (mOnFinishClickListener != null && holder.done != null) {
                holder.done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Log.e("checked:", String.valueOf(position));
                        if (buttonView.isPressed() && mBeans.get(position).isFinished() != isChecked)
                            mOnFinishClickListener.OnClick(holder.item, mBeans.get(position), position);
                    }
                });

            }
        }

    }

    @Override
    boolean willNotifyNormalChange() { //如果位置不变，就不刷新
        return false;
    }


    public interface OnFinishClickListener {
        boolean OnClick(View v, Task t, int position);
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class TaskViewHolder extends BaseCheckableListAdapter.CheckableViewHolder {
        TextView name;
        CheckBox done;
        int type;

        TaskViewHolder(@NonNull View itemView, int type) {
            super(itemView);
            name = itemView.findViewById(R.id.title);
            done = itemView.findViewById(R.id.done);
            this.type = type;
        }
    }


}
