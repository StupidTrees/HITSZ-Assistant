package com.stupidtree.hita.fragments.events;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.EventsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentCourse extends FragmentEventItem
        implements BaseOperationTask.OperationListener<Map<String, Integer>> {
    private TextView value2, value3, value4, value5, name;
    private ImageView classroom_detail_icon;
    private LinearLayout teacher_detail, classroom_detail;
    private TextView date;
    private View subject;
    private TextView courseProgress;
    private ProgressBar courseProgressBar;
    private RatingBar ratingBar;
    private int courseNumber; //课程在科目中的序号


    public FragmentCourse() {
    }

    private void initViews(View dlgView) {
        subject = dlgView.findViewById(R.id.subject);
        ViewGroup nameLayout = dlgView.findViewById(R.id.name_layout);
        value2 = dlgView.findViewById(R.id.tt_dlg_value2);
        value3 = dlgView.findViewById(R.id.tt_dlg_value3);
        //  more = dlgView.findViewById(R.id.more);
        value4 = dlgView.findViewById(R.id.tt_dlg_value4);
        value5 = dlgView.findViewById(R.id.tt_dlg_value5);
        name = dlgView.findViewById(R.id.tt_dlg_name);
        teacher_detail = dlgView.findViewById(R.id.tt_dlg_value3_detail);
        classroom_detail = dlgView.findViewById(R.id.tt_dlg_value2_detail);
        classroom_detail_icon = dlgView.findViewById(R.id.classroom_detail_icon);
        date = dlgView.findViewById(R.id.tt_dlg_date);
        name = dlgView.findViewById(R.id.tt_dlg_name);

        courseProgress = dlgView.findViewById(R.id.course_course_in_subject);
        courseProgressBar = dlgView.findViewById(R.id.course_progress);
        ratingBar = dlgView.findViewById(R.id.ratingBar);
        courseProgressBar.setMax(100);
        ratingBar.setStepSize(0.5f);
//        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                //System.out.println(rating);
//                timeTableCore.getSubjectByCourse(eventItem).setRate(courseNumber, Float.valueOf(rating).doubleValue());
//            }
//        });

        subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requireContext() instanceof ActivitySubject) {
                    Toast.makeText(requireContext(), "禁止套娃！", Toast.LENGTH_SHORT).show();
                    return;
                }
                ActivityUtils.startSubjectActivity_name(requireContext(), eventItem.mainName);
            }
        });

        nameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject.callOnClick();
            }
        });
        View delete = dlgView.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                android.app.AlertDialog ad = new android.app.AlertDialog.Builder(requireContext()).
                        setNegativeButton(getString(R.string.button_cancel), null)
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                deleteEvent();
                            }
                        }).create();
                ad.setTitle(getString(R.string.dialog_title_sure_delete));
                ad.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new RefreshTask(this, eventItem).executeOnExecutor(TPE);
    }

    @SuppressLint("SetTextI18n")
    private void setInfo() {
        value2.setText(TextUtils.isEmpty(eventItem.tag2) ? HContext.getString(R.string.none) : eventItem.tag2);
        value3.setText(TextUtils.isEmpty(eventItem.tag3) ? HContext.getString(R.string.none) : eventItem.tag3);
        value4.setText(eventItem.startTime.tellTime() + "-" + eventItem.endTime.tellTime());
        value5.setText(TextUtils.isEmpty(eventItem.tag4) ? HContext.getString(R.string.none) : eventItem.tag4);
        name.setText(eventItem.mainName);
        teacher_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (TextUtils.isEmpty(eventItem.tag3)) return;
                ActivityUtils.searchFor(requireContext(), eventItem.tag3, "teacher");

            }
        });
        if (TextUtils.isEmpty(eventItem.tag2)) {
            classroom_detail_icon.setVisibility(View.GONE);
        } else {
            classroom_detail_icon.setVisibility(View.VISIBLE);
            classroom_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(eventItem.tag2) || Objects.equals(eventItem.tag2, "无地点"))
                        return;
                    final String[] cr = eventItem.tag2.split("，\\[");
                    final ArrayList<String> classRooms = new ArrayList<>(Arrays.asList(cr));
                    if (classRooms.size() > 1) {
                        ArrayList<String> toRemove = new ArrayList<>();
                        for (int i = 0; i < classRooms.size(); i++) {
                            classRooms.set(i, classRooms.get(i).substring(classRooms.get(i).lastIndexOf("周") + 1));
                        }
                        for (String x : classRooms) {
                            if (TextUtils.isEmpty(x)) toRemove.add(x);
                        }
                        classRooms.removeAll(toRemove);
                        String[] classRoomItems = new String[classRooms.size()];
                        for (int i = 0; i < classRoomItems.length; i++)
                            classRoomItems[i] = classRooms.get(i);
                        AlertDialog ad = new AlertDialog.Builder(requireContext()).setTitle(HContext.getString(R.string.pick_classroom)).setItems(classRoomItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityUtils.searchFor(requireContext(), classRooms.get(i), "location");
                                //ActivityUtils.startLocationActivity_name(requireContext(), classRooms.get(i));
                            }
                        }).create();
                        ad.show();
                    } else ActivityUtils.searchFor(requireContext(), eventItem.tag2, "location");
//                    Intent i = new Intent(a,ActivityExplore.class);
//                    i.putExtra("terminal",eventItem.tag2);
//                    a.startActivity(i);
                }
            });
        }
        name.setText(eventItem.mainName);
        final Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(eventItem.week, eventItem.DOW);
        date.setText(EventsUtils.getDateString(c, false, EventsUtils.TTY_FOLLOWING)
                + "\n" +
                EventsUtils.getWeekDowString(eventItem, false, EventsUtils.TTY_WK_FOLLOWING));

    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_timetable_course;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setInfo();

    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {
        ratingBar.setVisibility(View.INVISIBLE);
        courseProgress.setText("...");
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Map<String, Integer> res) {
        if (null == res) {
            popupRoot.callDismiss();
            return;
        }
        try {
            ratingBar.setVisibility(View.VISIBLE);
            courseNumber = Objects.requireNonNull(res.get("now"));
            courseProgress.setText(String.format(HContext.getString(R.string.dialog_this_course_p), courseNumber));
            float all = (float) Objects.requireNonNull(res.get("total"));
            int has = Objects.requireNonNull(res.get("now"));
            float progress = (float) has / all;
            ValueAnimator va = ValueAnimator.ofInt(0, (int) (progress * 100));
            va.setDuration(500);
            va.setInterpolator(new DecelerateInterpolator());
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    courseProgressBar.setProgress(value);

                }
            });
            va.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    static class RefreshTask extends BaseOperationTask<Map<String, Integer>> {

        //  double rate = 0;
        EventItem eventItem;

        RefreshTask(OperationListener listRefreshedListener, EventItem eventItem) {
            super(listRefreshedListener);
            this.eventItem = eventItem;
        }

        @Override
        protected Map<String, Integer> doInBackground(OperationListener listRefreshedListener, Boolean... booleans) {
            Map<String, Integer> res = new HashMap<>();
            try {
                Subject subject = timeTableCore.getSubjectByCourse(eventItem);
                List courses = timeTableCore.getCourses(subject);
                res.put("total", courses.size());
                Collections.sort(courses);
                int now = courses.indexOf(eventItem) + 1;
                res.put("now", now);
                //rate = timeTableCore.getCurrentCurriculum().getSubjectByCourse(eventItem).getRate(courseNumber);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return res;
        }

    }


}
