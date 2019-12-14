package com.stupidtree.hita.diy;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityCourse;
import com.stupidtree.hita.activities.ActivityNotes;
import com.stupidtree.hita.activities.ActivityTeacher;
import com.stupidtree.hita.adapter.CourseNoteGridAdapter;
import com.stupidtree.hita.core.Note;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.timetable.EventItem;
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
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_EXAM;

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
        detail = dlgView.findViewById(R.id.dlg_bt_detail);

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
                allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei).setRate(courseNumber, new Float(rating).doubleValue());
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

    void setInfos() {
        if (ei.hasPassed(now)) {
            ratingCard.setVisibility(View.VISIBLE);
        } else {
            ratingCard.setVisibility(View.GONE);
        }
        value2.setText(TextUtils.isEmpty(ei.tag2) ? "无" : ei.tag2);
        value3.setText(TextUtils.isEmpty(ei.tag3) ? "无" : ei.tag3);
        value4.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
        value5.setText(TextUtils.isEmpty(ei.tag4) ? "无" : ei.tag4);
        //dialog.setTitle(ei.mainName);
        name.setText(ei.mainName);
        notesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ActivityNotes.class);
                Bundle b = new Bundle();
                b.putSerializable("event", ei);
                i.putExtra("curriculum", allCurriculum.get(thisCurriculumIndex).name);
                i.putExtras(b);
                getContext().startActivity(i);
            }
        });
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivityOptionsCompat ops = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) a,card,"card");
                Intent i = new Intent(getContext(), ActivityCourse.class);
                Bundle b = new Bundle();
                b.putSerializable("eventitem", ei);
                i.putExtras(b);
                getContext().startActivity(i);
                //dialog.dismiss();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!expanded){
                    moreLayout.setVisibility(View.VISIBLE);
                    new RefreshTask().executeOnExecutor(TPE);
                    more.setRotation(180f);

                }else{
                    moreLayout.setVisibility(View.GONE);
                   // new RefreshTask().executeOnExecutor(TPE);
                    more.setRotation(0f);
                }
                expanded = !expanded;



            }
        });
        teacher_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] names = ei.tag3.split("，");
                if (names.length > 1) {
                    AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("选择教师").setItems(names, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent il = new Intent(getContext(), ActivityTeacher.class);
                            il.putExtra("name", names[i]);
                            getContext().startActivity(il);
                        }
                    }).create();
                    ad.show();
                } else {
                    Intent i = new Intent(getContext(), ActivityTeacher.class);
                    i.putExtra("name", ei.tag3);
                    getContext().startActivity(i);
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
                    final String cr[] = ei.tag2.split("，\\[");
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
                        String classRoomItems[] = new String[classRooms.size()];
                        for (int i = 0; i < classRoomItems.length; i++)
                            classRoomItems[i] = classRooms.get(i);
                        AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("选择教室").setItems(classRoomItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityUtils.startLocationActivity_name(getContext(), classRooms.get(i));
                            }
                        }).create();
                        ad.show();
                    } else ActivityUtils.startLocationActivity_name(getContext(), ei.tag2);
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
        final Calendar c = allCurriculum.get(thisCurriculumIndex).getDateAtWOT(ei.week, ei.DOW);
        date.setText(c.get(Calendar.MONTH) + 1 + "月" + c.get(Calendar.DAY_OF_MONTH) + "日" + "(第" + ei.week + "周" + TextTools.words_time_DOW[ei.DOW - 1] + ")");
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
                                    setNegativeButton("取消", null)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            if (mainTimeTable.deleteEvent(ei, ei.eventType == TIMETABLE_EVENT_TYPE_DEADLINE)) {
                                                Toast.makeText(getContext(), "删除成功！", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent();
                                                i.putExtra("week", ei.week);
                                                i.setAction("COM.STUPIDTREE.HITA.TIMETABLE_PAGE_REFRESH");
                                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                                                CourseDialog.this.dismiss();
                                            }
                                        }
                                    }).create();
                            ad.setTitle("确定删除吗？");
                            if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                                ad.setMessage("删除课程后,可以通过导入课表或同步云端数据恢复初始课表");
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ratingBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            syncNoteWithFile();
            subject = allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei);
            Map<String, Integer> res = new HashMap<>();
            try {
                List courses = subject.getCourses();
                res.put("total", courses.size());
                Collections.sort(courses);
                int now = courses.indexOf(ei) + 1;
                res.put("now", now);
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
                courseProgress.setText("为本科目的第" + courseNumber + "次课");
                Double f = allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei).getRate(courseNumber);
                ratingBar.setRating(f.floatValue());
                // allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei).setRate(courseNumber,0.0);
                float all = (float) res.get("total");
                int has = res.get("now");
                float progress = (float) has / all;
                courseProgressBar.setProgress((int) (progress * 100));
                if (gridItems == null || gridItems.size() == 0) {
                    noteText.setText("没有笔记，点击添加");
                } else {
                    noteText.setText("这堂课共有" + gridItems.size() + "条笔记");
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
        List<Note> temp = FileOperator.loadNoteFromFile(Objects.requireNonNull(getContext().getExternalFilesDir(null)), allCurriculum.get(thisCurriculumIndex).name, ei.week + "-" + ei.DOW, ei.tag4);
        gridItems.clear();
        if (temp != null) {
            for (Note n : temp) {
                gridItems.add(n);
            }
        }
    }

}
