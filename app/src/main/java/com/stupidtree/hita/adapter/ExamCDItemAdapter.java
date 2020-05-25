package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.util.EventsUtils;

import java.util.List;



public class ExamCDItemAdapter extends BaseCheckableListAdapter<EventItem, ExamCDItemAdapter.yHolder> {

    private static final int TODO = 986;
    private static final int PASSED = 282;
    private static final int TAG = 11;

    public ExamCDItemAdapter(Context mContext, List<EventItem> mBeans) {
        super(mContext, mBeans);
    }

    @Override
    protected int getLayoutId(int viewType) {
        if (viewType == TAG) return R.layout.dynamic_ddl_tag;
        else if (viewType == PASSED) return R.layout.dynamic_exam_passed;
        return R.layout.dynamic_exam_item_todo;
    }

    @Override
    public int getItemViewType(int position) {
        if (mBeans.get(position).getEventType() == EventItem.TAG) return TAG;
        if (mBeans.get(position).hasPassed(System.currentTimeMillis())) return PASSED;
        return TODO;
    }

    @Override
    public yHolder createViewHolder(View v, int viewType) {
        return new yHolder(v, viewType);
    }


    @SuppressLint("SetTextI18n")
    @Override
    void bindHolderData(yHolder holder, int position, EventItem data) {
        if (holder.type == TAG) {
            holder.title.setText(data.getMainName());
        } else {
            holder.title.setText(data.getMainName());
            if (holder.type == TODO) {
                holder.remain.setText(EventsUtils.itWillStartIn(TimetableCore.getNow(), data, true));
            } else holder.remain.setText(R.string.exam_passed_tag);
            if (EditMode) {
                holder.remain.setVisibility(View.GONE);
            } else {
                holder.remain.setVisibility(View.VISIBLE);
            }
        }
    }


    class yHolder extends BaseCheckableListAdapter.CheckableViewHolder {
        TextView title, time, remain;
        int type;

        yHolder(@NonNull View itemView, int type) {
            super(itemView);
            this.type = type;
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            remain = itemView.findViewById(R.id.time_remain);
        }
    }
}

