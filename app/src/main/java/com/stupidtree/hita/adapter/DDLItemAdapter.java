package com.stupidtree.hita.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.util.EventsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;



public class DDLItemAdapter extends BaseCheckableListAdapter<EventItem, DDLItemAdapter.xHolder> {

    private static final int TODO = 942;
    private static final int PASSED = 954;
    private static final int TAG = 212;


    public DDLItemAdapter(Context mContext, List<EventItem> mBeans) {
        super(mContext, mBeans);
    }

    @Override
    protected int getLayoutId(int viewType) {
        int layout = R.layout.dynamic_ddl_item;
        switch (viewType) {
            case TAG:
                layout = R.layout.dynamic_ddl_tag;
                break;
            case PASSED:
                layout = R.layout.dynamic_ddl_passed;
                break;
            case TODO:
                layout = R.layout.dynamic_ddl_item;
                break;
        }
        return layout;
    }

    @Override
    public xHolder createViewHolder(View v, int viewType) {
        return new xHolder(v, viewType);
    }

    @Override
    void bindHolderData(xHolder holder, int position, EventItem data) {
        if (holder.type == TAG) {
            holder.title.setText(data.getMainName());
        } else {
            holder.time_date.setText(EventsUtils.getWeekDowString(data, true, EventsUtils.TTY_REPLACE | EventsUtils.TTY_WK_REPLACE));
            holder.title.setText(data.getMainName());
            if (data.isWholeDay()) holder.time_time.setText(mContext.getString(R.string.wholeday));
            else holder.time_time.setText(data.startTime.tellTime());
            if (holder.type == TODO) {
                holder.remain.setText(EventsUtils.itWillStartIn(TimetableCore.getNow(), data, true));
            } else holder.remain.setText(R.string.ddl_passed);
            if (EditMode) {
                holder.remain.setVisibility(View.GONE);
            } else {
                holder.remain.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        EventItem ei = mBeans.get(position);
        if (ei.getEventType() == EventItem.TAG) return TAG;
        if (!ei.hasPassed(System.currentTimeMillis())) return TODO;
        else return PASSED;
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    public void notifyItemChangedSmooth(List<EventItem> newL) {
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<EventItem> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            if (!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, mBeans.size());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            EventItem ei = newL.get(i);
            if (!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            mBeans.add(index, newL.get(index));
            notifyItemInserted(index);
            notifyItemRangeChanged(index, mBeans.size());
        }
        for (EventItem ei : remains) { //保留的
            int oldIndex = mBeans.indexOf(ei);
            int newIndex = newL.indexOf(ei);
            if (oldIndex == newIndex) notifyItemChanged(newIndex);
            else {
                mBeans.add(newIndex, mBeans.remove(oldIndex));
                notifyItemMoved(oldIndex, newIndex);
                notifyItemRangeChanged(Math.min(oldIndex, newIndex), mBeans.size());
            }
        }
    }

    class xHolder extends BaseCheckableListAdapter.CheckableViewHolder {
        TextView title, time_date, time_time, remain;
        int type;

        xHolder(@NonNull View itemView, int type) {
            super(itemView);
            this.type = type;
            title = itemView.findViewById(R.id.title);
            time_date = itemView.findViewById(R.id.time_date);
            time_time = itemView.findViewById(R.id.time_time);
            remain = itemView.findViewById(R.id.time_remain);
        }
    }
}
