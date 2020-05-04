package com.stupidtree.hita.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.util.EventsUtils;

import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class SubjectCoursesListAdapter extends BaseCheckableListAdapter<EventItem, SubjectCoursesListAdapter.CoursesViewHolder> {

    private static final int PASSED = 672;
    private static final int TODO = 222;
    private static final int TAG = 914;
    public SubjectCoursesListAdapter(Context context, List<EventItem> list){
        super(context, list);
    }

    @Override
    protected int getLayoutId(int viewType) {
        if (viewType == TAG) return R.layout.dynamic_subject_courseitem_tag;
        else if (viewType == TODO) return R.layout.dynamic_subject_courseitem;
        else return R.layout.dynamic_subject_courseitem_passed;

    }

    @Override
    public int getItemViewType(int position) {
        if (mBeans.get(position).getEventType() == EventItem.TAG) return TAG;
        else return mBeans.get(position).hasPassed(System.currentTimeMillis()) ? PASSED : TODO;
    }

    @Override
    public CoursesViewHolder createViewHolder(View v, int viewType) {
        return new CoursesViewHolder(v, viewType);
    }

    @Override
    void bindHolderData(CoursesViewHolder coursesViewHolder, int position, EventItem data) {
        if (coursesViewHolder.type == TAG) {
            Log.e("tag", "标签被刷新" + data.getMainName());
            if (data.getMainName().equals("more")) {
                coursesViewHolder.icon.setRotation(0f);
            } else {
                coursesViewHolder.icon.setRotation(180f);
            }
        } else {
            Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(data.getWeek(), data.getDOW());
            coursesViewHolder.date.setText(EventsUtils.getDateString(c, true, EventsUtils.TTY_REPLACE));
            if (EditMode) coursesViewHolder.icon.setVisibility(View.GONE);
            else coursesViewHolder.icon.setVisibility(View.VISIBLE);
        }

    }

    public interface OnItemClickListener {
        void OnClick(View v, int position, EventItem ei);
    }


    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class CoursesViewHolder extends BaseCheckableListAdapter.CheckableViewHolder {
        TextView date;
        ImageView icon;
        int type;

        CoursesViewHolder(@NonNull View itemView, int type) {
            super(itemView);
            this.type = type;
            icon = itemView.findViewById(R.id.icon);
            date = itemView.findViewById(R.id.subject_courselist_month);
        }
    }
}
