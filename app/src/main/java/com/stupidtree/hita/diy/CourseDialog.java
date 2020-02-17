package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityNotes;
import com.stupidtree.hita.activities.ActivitySearch;
import com.stupidtree.hita.activities.ActivityTeacher;
import com.stupidtree.hita.adapter.CourseNoteGridAdapter;
import com.stupidtree.hita.timetable.Note;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;

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
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_EXAM;

public class CourseDialog extends AlertDialog {
    EventItem ei;
    TextView value2, value3, value4, value5, name;
    ImageView more, detail, classroom_detail_icon;
    LinearLayout teacher_detail, classroom_detail, card, moreLayout;
    TextView date;


    TextView noteText, courseProgress;
    ProgressBar courseProgressBar;
   // Button bt_note;
    ArrayList<Note> gridItems;
    CourseNoteGridAdapter gridAdapter;
    RecyclerView notesGrid;
    Subject subject;
    LinearLayout ratingCard, notesCard;
    RatingBar ratingBar;
    int courseNumber; //课程在科目中的序号


    boolean expanded = false;

    void initViews(View dlgView) {
        ratingCard = dlgView.findViewById(R.id.rating_card);
        notesCard = dlgView.findViewById(R.id.notes_card);
        value2 = dlgView.findViewById(R.id.tt_dlg_value2);
        value3 = dlgView.findViewById(R.id.tt_dlg_value3);
        more = dlgView.findViewById(R.id.more);
        value4 = dlgView.findViewById(R.id.tt_dlg_value4);
        value5 = dlgView.findViewById(R.id.tt_dlg_value5);
        name = dlgView.findViewById(R.id.tt_dlg_name);
        detail = dlgView.findViewById(R.id.dlg_bt_detail);
        teacher_detail = dlgView.findViewById(R.id.tt_dlg_value3_detail);
        classroom_detail = dlgView.findViewById(R.id.tt_dlg_value2_detail);
        card = dlgView.findViewById(R.id.card);
        moreLayout = dlgView.findViewById(R.id.more_layout);
        classroom_detail_icon = dlgView.findViewById(R.id.classroom_detail_icon);
        date = dlgView.findViewById(R.id.tt_dlg_date);
        name = dlgView.findViewById(R.id.tt_dlg_name);

        noteText = dlgView.findViewById(R.id.text_note);
        courseProgress = dlgView.findViewById(R.id.course_course_in_subject);
        courseProgressBar = dlgView.findViewById(R.id.course_progress);
        ratingBar = dlgView.findViewById(R.id.ratingBar);
        courseProgressBar.setMax(100);
        ratingBar.setStepSize(0.5f);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //System.out.println(rating);
                timeTableCore.getCurrentCurriculum().getSubjectByCourse(ei).setRate(courseNumber, new Float(rating).doubleValue());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(expanded){
            new RefreshTask().executeOnExecutor(TPE);
        }
    }

    @SuppressLint("SetTextI18n")
    void setInfos() {
        if (ei.hasPassed(now)) {
            ratingCard.setVisibility(View.VISIBLE);
        } else {
            ratingCard.setVisibility(View.GONE);
        }
        value2.setText(TextUtils.isEmpty(ei.tag2) ? HContext.getString(R.string.none): ei.tag2);
        value3.setText(TextUtils.isEmpty(ei.tag3) ?HContext.getString(R.string.none) : ei.tag3);
        value4.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
        value5.setText(TextUtils.isEmpty(ei.tag4) ? HContext.getString(R.string.none) : ei.tag4);
        //dialog.setTitle(ei.mainName);
        name.setText(ei.mainName);
        notesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ActivityNotes.class);
                Bundle b = new Bundle();
                b.putSerializable("event", ei);
                i.putExtra("curriculum",timeTableCore.getCurrentCurriculum().getName());
                i.putExtras(b);
                getContext().startActivity(i);
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float fromD,toD;
                if(!expanded){
                    moreLayout.setVisibility(View.VISIBLE);
                    new RefreshTask().executeOnExecutor(TPE);
                    fromD = 0f;
                    toD = 180f;
                    //more.setRotation(180f);

                }else{
                    moreLayout.setVisibility(View.GONE);
                    fromD = 180f;
                    toD = 0f;
                   // new RefreshTask().executeOnExecutor(TPE);
                   // more.setRotation(0f);
                }
                expanded = !expanded;
                RotateAnimation ra = new RotateAnimation(fromD,toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setInterpolator(new DecelerateInterpolator());
                ra.setDuration(300);//设置动画持续周期
                ra.setRepeatCount(0);//设置重复次数
                ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                more.setAnimation(ra);
                more.startAnimation(ra);


            }
        });
        teacher_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String[] names = ei.tag3.split("，");
                if (names.length > 1) {
                    AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(HContext.getString(R.string.pick_teacher)).setItems(names, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Intent il = new Intent(getContext(), ActivityTeacher.class);
//                            il.putExtra("name", names[i]);
//                            getContext().startActivity(il);
                            ActivityUtils.searchFor(getContext(),names[i],"teacher");
                        }
                    }).create();
                    ad.show();
                } else {
                    ActivityUtils.searchFor(getContext(),ei.tag3,"teacher");
//                    Intent i = new Intent(getContext(), ActivityTeacher.class);
//                    i.putExtra("name", ei.tag3);
//                    getContext().startActivity(i);
                }
            }
        });
        if (TextUtils.isEmpty(ei.tag2)) {
            classroom_detail_icon.setVisibility(View.GONE);
        } else {
            classroom_detail_icon.setVisibility(View.VISIBLE);
            classroom_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] cr = ei.tag2.split("，\\[");
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
                        AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(HContext.getString(R.string.pick_classroom)).setItems(classRoomItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityUtils.searchFor(getContext(),classRooms.get(i),"location");
                                //ActivityUtils.startLocationActivity_name(getContext(), classRooms.get(i));
                            }
                        }).create();
                        ad.show();
                    } else  ActivityUtils.searchFor(getContext(),ei.tag2,"location");
//                    Intent i = new Intent(a,ActivityExplore.class);
//                    i.putExtra("terminal",ei.tag2);
//                    a.startActivity(i);
                }
            });
        }
        name.setText(ei.mainName);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                    ActivityUtils.startSubjectActivity_name(getContext(), ei.mainName);
                } else if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM) {
                    if (ei.tag3.startsWith("科目代码：")) {
                        ActivityUtils.startSubjectActivity_code(getContext(), ei.tag3.substring(5));
                    } else if (ei.tag3.startsWith("科目名称：")) {
                        ActivityUtils.startSubjectActivity_name(getContext(), ei.tag3.substring(5));
                    } else {
                        ActivityUtils.startSubjectActivity_name(getContext(), ei.tag3);
                    }
                }
            }
        });
        final Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(ei.week, ei.DOW);
        date.setText(HContext.getResources().getStringArray(R.array.months_full)[c.get(Calendar.MONTH)] +
                String.format(HContext.getString(R.string.date_day),c.get(Calendar.DAY_OF_MONTH)) +
                "("+
                String.format(HContext.getString(R.string.week),ei.week) +" "+
                HContext.getResources().getStringArray(R.array.dow1)[ei.DOW - 1] + ")");
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(getContext(), v);
                int menuId;
                switch (ei.eventType) {
                    case TIMETABLE_EVENT_TYPE_COURSE:
                        menuId = R.menu.menu_opr_dialog_detail_course;
                        break;
                    case TIMETABLE_EVENT_TYPE_EXAM:
                        menuId = R.menu.menu_opr_dialog_detail_exam;
                        break;
                    default:
                        menuId = R.menu.menu_opr_dialog_detail_normal;
                }
                pm.inflate(menuId);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.opr_delete) {
                            android.app.AlertDialog ad = new android.app.AlertDialog.Builder(getContext()).
                                    setNegativeButton(HContext.getString(R.string.button_cancel), null)
                                    .setPositiveButton(HContext.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            if (timeTableCore.deleteEvent(ei, ei.eventType == TIMETABLE_EVENT_TYPE_DEADLINE)) {
                                                Toast.makeText(getContext(), HContext.getString(R.string.notif_delete_success), Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent();
                                                i.putExtra("week", ei.week);
                                                i.setAction("COM.STUPIDTREE.HITA.TIMETABLE_PAGE_REFRESH");
                                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                                                CourseDialog.this.dismiss();
                                            }
                                        }
                                    }).create();
                            ad.setTitle(HContext.getString(R.string.dialog_title_sure_delete));
                            if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                                ad.setMessage(HContext.getString(R.string.dialog_message_sure_delete));
                            }
                            ad.show();
                        } else if (item.getItemId() == R.id.opr_subject) {
                            if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                                ActivityUtils.startSubjectActivity_name(getContext(), ei.mainName);
                            } else if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM) {
                                if (ei.tag3.startsWith("科目代码：")) {
                                    ActivityUtils.startSubjectActivity_code(getContext(), ei.tag3.substring(5));
                                } else if (ei.tag3.startsWith("科目名称：")) {
                                    ActivityUtils.startSubjectActivity_name(getContext(), ei.tag3.substring(5));
                                } else {
                                    ActivityUtils.startSubjectActivity_name(getContext(), ei.tag3);
                                }
                            }

                        }
                        return true;
                    }
                });
                pm.show();
            }
        });
    }


    public CourseDialog(@NonNull Context context, EventItem ei) {
        super(context);
        this.ei = ei;
        View v = getLayoutInflater().inflate(R.layout.dialog_timetable_course, null);
        setView(v);
        initNotesGrid(v);
        initViews(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
        setInfos();
    }


    class RefreshTask extends AsyncTask {

        double rate = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ratingBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            syncNoteWithFile();
            subject = timeTableCore.getCurrentCurriculum().getSubjectByCourse(ei);
            Map<String, Integer> res = new HashMap<>();
            try {
                List courses = subject.getCourses();
                res.put("total", courses.size());
                Collections.sort(courses);
                int now = courses.indexOf(ei) + 1;
                res.put("now", now);
                rate = timeTableCore.getCurrentCurriculum().getSubjectByCourse(ei).getRate(courseNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                ratingBar.setVisibility(View.VISIBLE);
                Map<String, Integer> res = (Map<String, Integer>) o;
                gridAdapter.notifyDataSetChanged();
                courseNumber = res.get("now");
                courseProgress.setText(String.format(HContext.getString(R.string.dialog_this_course_p), courseNumber ));
                ratingBar.setRating((float) rate);
                // timeTableCore.getCurrentCurriculum().getSubjectByCourse(ei).setRate(courseNumber,0.0);
                float all = (float) res.get("total");
                int has = res.get("now");
                float progress = (float) has / all;
                courseProgressBar.setProgress((int) (progress * 100));
                if (gridItems == null || gridItems.size() == 0) {
                    noteText.setText(HContext.getString(R.string.dialog_no_notes));
                } else {
                    noteText.setText(String.format(HContext.getString(R.string.dialog_notes_num),gridItems.size()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    void initNotesGrid(View v) {
        //noteNum = findViewById(R.id.note_num);
        gridItems = new ArrayList<>();
        notesGrid = v.findViewById(R.id.course_recy_note);
        gridAdapter = new CourseNoteGridAdapter(getContext(), gridItems);
        notesGrid.setAdapter(gridAdapter);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        notesGrid.setLayoutManager(layoutManager);


    }

    public void syncNoteWithFile() {
        //if(gridItems==null) gridItems = new ArrayList<>();
        List<Note> temp = FileOperator.loadNoteFromFile(Objects.requireNonNull(getContext().getExternalFilesDir(null)), timeTableCore.getCurrentCurriculum().getName(), ei.week + "-" + ei.DOW, ei.tag4);
        gridItems.clear();
        if (temp != null) {
            for (Note n : temp) {
                gridItems.add(n);
            }
        }
    }

}
