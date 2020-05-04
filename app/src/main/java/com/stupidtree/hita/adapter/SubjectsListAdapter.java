package com.stupidtree.hita.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BasicOperationTask;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.EventItemHolder;
import com.stupidtree.hita.timetable.packable.Subject;

import java.util.ArrayList;

import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class SubjectsListAdapter extends BaseCheckableListAdapter<Subject, SubjectsListAdapter.SubjectViewHolder> {

    private static final int NORMAL = 967;
    private static final int MOOC = 731;
    private static final int TITLE = 971;
    private static final int FOOT = 608;
    private SharedPreferences timetableSP;


    public SubjectsListAdapter(Context context, ArrayList<Subject> subjects, SharedPreferences timetableSP) {
        super(context, subjects);
        this.timetableSP = timetableSP;
    }



    @Override
    protected int getLayoutId(int i) {
        if (i == TITLE) return R.layout.dynamic_subject_list_title;
        else if (i == MOOC) return R.layout.dynamic_subjects_mooc_item;
        else if (i == FOOT) return R.layout.dynamic_subjects_foot;
        return R.layout.dynamic_subjects_item;
    }

    @Override
    public SubjectViewHolder createViewHolder(View v, int viewType) {
        return new SubjectViewHolder(v, viewType);
    }


    @Override
    void bindHolderData(final SubjectViewHolder holder, int position, final Subject s) {
        if (s != null && (holder.type == NORMAL || holder.type == MOOC)) {
            int color = -1;
            final boolean colorfulMode = timetableSP.getBoolean("subjects_color_enable", false);
            if (colorfulMode) {
                color = timetableSP.getInt("color:" + s.getName(), Color.parseColor("#00000000"));
            }
            holder.name.setText(s.getName());
            if (color != -1) holder.icon.setColorFilter(color);
            else holder.icon.clearColorFilter();
            final int finalColor = color;
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!colorfulMode) return;
                    new com.stupidtree.hita.views.ColorPickerDialog(mContext)
                            .initColor(finalColor).show(new com.stupidtree.hita.views.ColorPickerDialog.OnColorSelectedListener() {
                        @Override
                        public void OnSelected(int color) {
                            timetableSP.edit().putInt("color:" + s.getName(), color).apply();
                            holder.icon.setColorFilter(color);
                        }
                    });
                }
            });
            if (holder.type != MOOC && holder.progressBar != null)
                new CalcProgressTask(holder, s).executeOnExecutor(HITAApplication.TPE);
            if (holder.type == MOOC && holder.label != null) {
                String t = TextUtils.isEmpty(s.getSchool()) ? mContext.getString(R.string.unknown_department) : s.getSchool();
                holder.label.setText(t);
            }
            if (EditMode) {
                holder.icon.setVisibility(View.GONE);
            } else {
                holder.icon.setVisibility(View.VISIBLE);
            }
        } else if (holder.type == TITLE) {
            holder.name.setText(mBeans.get(position).getName());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mBeans.size()) return FOOT;
        if (mBeans.get(position).getType().equals(Subject.TAG)) return TITLE;
        else return mBeans.get(position).isMOOC() ? MOOC : NORMAL;
    }

    @Override
    public int getItemCount() {
        return mBeans.size() + 1;
    }

    static class CalcProgressTask extends BasicOperationTask<Integer> {

        Subject subject;

        CalcProgressTask(OperationListener listRefreshedListener, Subject subject) {
            super(listRefreshedListener);
            this.subject = subject;
        }

        @Override
        protected Integer doInBackground(OperationListener listRefreshedListener, Boolean... booleans) {
            int finished = 0, unfinished = 0;
            ArrayList<EventItem> result = new ArrayList<>();
            SQLiteDatabase sd = mDBHelper.getReadableDatabase();
            Cursor c = sd.query("timetable", null, "name=? and type=?",
                    new String[]{subject.getName(), TimetableCore.COURSE + ""}, null, null, null);


            while (c.moveToNext()) {
                EventItemHolder eih = new EventItemHolder(c);
                result.addAll(eih.getAllEvents());
            }
            for (EventItem ei : result) {
                if (ei.hasPassed(timeTableCore.getNow())) finished++;
                else unfinished++;
            }
            float x = ((float) finished) * 100.0f / (float) (finished + unfinished);
            return (int) x;
        }

    }

    class SubjectViewHolder extends BaseCheckableListAdapter.CheckableViewHolder implements BasicOperationTask.OperationListener<Integer> {

        TextView name, progress, label;//,code;
        ImageView icon;
        ProgressBar progressBar;
        int type;

        SubjectViewHolder(@NonNull View itemView, int type) {
            super(itemView);
            this.type = type;
            name = itemView.findViewById(R.id.usercenter_subjectitem_name);
            progress = itemView.findViewById(R.id.usercenter_subjectitem_progress);
            progressBar = itemView.findViewById(R.id.usercenter_subjectitem_progressBar);
            label = itemView.findViewById(R.id.label);
            icon = itemView.findViewById(R.id.usercenter_subject_item_label);
        }

        @Override
        public void onOperationStart(String id, Boolean[] params) {

        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onOperationDone(String id, Boolean[] params, Integer result) {

            ValueAnimator va = ValueAnimator.ofInt(progressBar.getProgress(), result);
            va.setDuration(500);
            va.setInterpolator(new DecelerateInterpolator());
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int curValue = (int) animation.getAnimatedValue();
                    progressBar.setProgress(curValue);
                    progress.setText(curValue + "%");
                }
            });
            va.start();
        }


    }

}
