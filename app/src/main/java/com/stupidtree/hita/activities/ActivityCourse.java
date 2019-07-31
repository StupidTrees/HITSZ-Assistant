package com.stupidtree.hita.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.ChatSec.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Note;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.adapter.CourseNoteGridAdapter;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import cn.bmob.v3.util.V;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

public class ActivityCourse extends BaseActivity {
    Toolbar toolbar;
    TextView value2,value3,value4,value5;
    TextView date,name,noteText,courseProgress;
    ProgressBar courseProgressBar;
    Button bt_note,bt_subject;
    ArrayList<Note> gridItems;
    CourseNoteGridAdapter gridAdapter;
    RecyclerView notesGrid;
    EventItem ei;
    Subject subject;
    CardView ratingCard,subjectCard;
    RatingBar ratingBar;
    LinearLayout value3Detail,value2Detail;
    ImageView classroom_detail_icon;
    int courseNumber; //课程在科目中的序号
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        toolbar= findViewById(R.id.toolbar);
        noteText =findViewById(R.id.text_note);

        ei = (EventItem) getIntent().getExtras().getSerializable("eventitem");
       // Log.e("!!!", String.valueOf(ei));
        toolbar.setTitle("");
        name = findViewById(R.id.course_name);
        ratingCard = findViewById(R.id.card_rating);
        ratingBar = findViewById(R.id.ratingBar);
        subjectCard = findViewById(R.id.subject_card);
        classroom_detail_icon = findViewById(R.id.classroom_detail_icon);
        name.setText(ei.mainName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
       // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        initInfos();
        initNotesGrid();

        }
        void initInfos(){
            value2 =  findViewById(R.id.tt_dlg_value2);
            value3 =  findViewById(R.id.tt_dlg_value3);
            value4 =  findViewById(R.id.tt_dlg_value4);
            value5 =  findViewById(R.id.tt_dlg_value5);
            bt_note = findViewById(R.id.bt_note);
            bt_subject = findViewById(R.id.bt_course_subject);
            courseProgress = findViewById(R.id.course_course_in_subject);
            courseProgressBar = findViewById(R.id.course_progress);
            value3Detail = findViewById(R.id.tt_dlg_value3_detail);
            value2Detail = findViewById(R.id.tt_dlg_value2_detail);
            courseProgressBar.setMax(100);
            ratingBar.setStepSize(0.5f);

            if(getIntent().getBooleanExtra("showSubject",true)){
                subjectCard.setVisibility(View.VISIBLE);
            }else{
                subjectCard.setVisibility(View.GONE);
            }
            //nonenote = findViewById(R.id.none_text);
            int DOW = now.get(Calendar.DAY_OF_WEEK)==1?7:now.get(Calendar.DAY_OF_WEEK)-1;
            if(ei.hasPassed(now)){
                ratingCard.setVisibility(View.VISIBLE);
            }else{
                ratingCard.setVisibility(View.GONE);
            }
            date = findViewById(R.id.tt_dlg_date);
//            name.setText(ei.mainName);
            value2.setText(ei.tag2.isEmpty() ? "无" : ei.tag2);
            value3.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);
            value4.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
            value5.setText(ei.tag4.isEmpty() ? "无" : ei.tag4);
            Calendar c = allCurriculum.get(thisCurriculumIndex).getDateAtWOT(ei.week, ei.DOW);
            date.setText(c.get(Calendar.MONTH) + 1 + "月" + c.get(Calendar.DAY_OF_MONTH) + "日" + "(第" + ei.week + "周" + TextTools.words_time_DOW[ei.DOW - 1] + ")");

            bt_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ActivityCourse.this, ActivityNotes.class);
                    Bundle b = new Bundle();
                    b.putSerializable("event", ei);
                    i.putExtra("curriculum", allCurriculum.get(thisCurriculumIndex).name);
                    i.putExtras(b);
                    ActivityCourse.this.startActivity(i);
                }
            });

            bt_subject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(ActivityCourse.this);
                    Intent i = new Intent(ActivityCourse.this,ActivitySubject.class);
                    i.putExtra("subject",subject.name);
                    ActivityCourse.this.startActivity(i,op.toBundle());
                }
            });
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    //System.out.println(rating);
                    allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei).setRate(courseNumber,new Float(rating).doubleValue());
                }
            });
            value3Detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ActivityCourse.this,ActivityTeacher.class);
                    i.putExtra("name",ei.tag3);
                    ActivityCourse.this.startActivity(i);
                }
            });
            if(ei.tag2.isEmpty()){
                classroom_detail_icon.setVisibility(View.GONE);
            }else {
                classroom_detail_icon.setVisibility(View.VISIBLE);
                value2Detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtils.startLocationActivity_name(ActivityCourse.this,ei.tag2);
//                    Intent i = new Intent(ActivityCourse.this,ActivityExplore.class);
//                    i.putExtra("terminal",ei.tag2);
//                    ActivityCourse.this.startActivity(i);
                    }
                });
            }

        }
        void initNotesGrid(){
            //noteNum = findViewById(R.id.note_num);
            gridItems = new ArrayList<>();
            notesGrid = findViewById(R.id.course_recy_note);
            gridAdapter = new CourseNoteGridAdapter(this,gridItems);
            notesGrid.setAdapter(gridAdapter);
            StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            notesGrid.setLayoutManager(layoutManager);



        }

    public void syncNoteWithFile(){
        //if(gridItems==null) gridItems = new ArrayList<>();
        List<Note> temp = FileOperator.loadNoteFromFile(Objects.requireNonNull(getExternalFilesDir(null)),allCurriculum.get(thisCurriculumIndex).name,ei.week+"-"+ei.DOW,ei.tag4);
       gridItems.clear();
        if(temp!=null){
           for(Note n:temp){
               gridItems.add(n);
           }
       }
        }

    @Override
     protected void onResume() {
        super.onResume();
        new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class RefreshTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ratingBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (!FileOperator.verifyStoragePermissions(ActivityCourse.this)) {
                Toast.makeText(HContext, "请给本应用授权后后再使用本功能！", Toast.LENGTH_SHORT).show();
                return null;
            }
            syncNoteWithFile();
            subject = allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei);
            Map<String,Integer> res = new HashMap<>();
            List courses = subject.getCourses();
            res.put("total",courses.size());
            Collections.sort(courses);
            int now = courses.indexOf(ei)+1;
            res.put("now",now);
            return res;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                ratingBar.setVisibility(View.VISIBLE);
                Map<String,Integer> res = (Map<String, Integer>)o;
                gridAdapter.notifyDataSetChanged();
                courseNumber = res.get("now");
                courseProgress.setText("为本门课的第"+courseNumber+"次课");
                Double f = allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei).getRate(courseNumber);
                ratingBar.setRating(f.floatValue());
               // allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(ei).setRate(courseNumber,0.0);
                float all =  (float)res.get("total");
                int has = res.get("now");
                float progress = (float)has/all;
                courseProgressBar.setProgress((int) (progress*100));
                if(gridItems==null||gridItems.size()==0){
                   noteText.setText("没有笔记，点击添加");
                }else{
                   noteText.setText("这堂课共有"+gridItems.size()+"条笔记");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
