package com.stupidtree.hita.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.adapter.SubjectCoursesListAdapter;

import java.util.ArrayList;
import java.util.Collections;


import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;


public class ActivitySubject extends BaseActivity {

    Subject subject;
    TextView ratingText;
    RecyclerView courseList;
    SubjectCoursesListAdapter listAdapter;
    ArcProgress arcProgress;
    CollapsingToolbarLayout toolbarLayout;
    TextView name,point, attr, totalcourses, exam, school, xnxq, type, code, score_qz, score_qm, score_none;
    CardView  card_scores, card_rate, card_allcourses, card_html;
    LinearLayout qz_score_layout, qm_score_layout;
    InitProgressTask pageTask_progress;
    RefreshRatingTask pageTask_rating;
    InitCourseListTask pageTask_courseList;

    //WebView webView;


    @Override
    protected void stopTasks() {
        if(pageTask_progress!=null&&!pageTask_progress.isCancelled()) pageTask_progress.cancel(true);
        if(pageTask_rating!=null&&!pageTask_rating.isCancelled()) pageTask_rating.cancel(true);
        if(pageTask_courseList!=null&&!pageTask_courseList.isCancelled()) pageTask_courseList.cancel(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        //requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);//申请动画
        Transition explode = TransitionInflater.from(this).inflateTransition(android.R.transition.explode);
        getWindow().setEnterTransition(explode);
        setContentView(R.layout.activity_subject);
        if (!getIntent().getBooleanExtra("useCode", false)) {
            subject = allCurriculum.get(thisCurriculumIndex).getSubjectByName(getIntent().getStringExtra("subject"));
        } else {
            subject = allCurriculum.get(thisCurriculumIndex).getSubjectByCourseCode(getIntent().getStringExtra("subject"));
        }
        if (subject == null) return;
        initViews();
        initToolBar();
        initCourseList();

        initProgress();
    }

    void initViews() {
        name = findViewById(R.id.subject_name);
        point = findViewById(R.id.subject_point);
        exam = findViewById(R.id.subject_exam);
        school = findViewById(R.id.subject_school);
        attr = findViewById(R.id.subject_attr);
        code = findViewById(R.id.subject_code);
        type = findViewById(R.id.subject_type);
        xnxq = findViewById(R.id.subject_xnxq);
        qz_score_layout = findViewById(R.id.score_qz_layout);
        qm_score_layout = findViewById(R.id.score_qm_layout);
        score_qz = findViewById(R.id.score_qz);
        score_qm = findViewById(R.id.score_qm);
        totalcourses = findViewById(R.id.subject_totalcourses);
        card_allcourses = findViewById(R.id.subject_card_allcourses);
        card_rate = findViewById(R.id.subject_card_rate);
        card_html = findViewById(R.id.subject_card_html);
        score_none = findViewById(R.id.score_none);
    }

    void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarLayout = findViewById(R.id.toolbarlayout);
        ratingText = findViewById(R.id.text_rate);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbarLayout.setExpandedTitleGravity(CollapsingToolbarLayout.TEXT_ALIGNMENT_CENTER);
        toolbarLayout.setExpandedTitleColor(Color.parseColor("#00FFFFFF"));
        toolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this,R.color.material_text_icon_white));


    }

    void setInfos() {
        name.setText(subject.name);
        attr.setText(subject.compulsory);
        point.setText(subject.credit);
        school.setText(subject.school);
        code.setText(subject.code);
        xnxq.setText(subject.xnxq);
        type.setText(subject.type);
        totalcourses.setText(subject.totalCourses);
        exam.setText((subject.exam ? "是" : "否") + (subject.Default ? "(默认)" : ""));
        if (subject.isMOOC) {
            card_rate.setVisibility(View.GONE);
            arcProgress.setVisibility(View.GONE);
            card_allcourses.setVisibility(View.GONE);
            courseList.setVisibility(View.GONE);
        }

        if (subject.getScores().size() == 0) {
            score_none.setVisibility(View.VISIBLE);
            qz_score_layout.setVisibility(View.GONE);
            qm_score_layout.setVisibility(View.GONE);
        } else {
            score_none.setVisibility(View.GONE);
            if (subject.getScores().get("qz") != null) {
                qz_score_layout.setVisibility(View.VISIBLE);
                score_qz.setText(subject.getScores().get("qz"));
            } else {
                qz_score_layout.setVisibility(View.GONE);
            }

            if (subject.getScores().get("qm") != null) {
                qm_score_layout.setVisibility(View.VISIBLE);
                score_qm.setText(subject.getScores().get("qm"));
            } else {
                qm_score_layout.setVisibility(View.GONE);
            }
        }

//        if(subject.infoHTML!=null){
//
//            card_html.setVisibility(View.VISIBLE);
//            webView.loadData(subject.infoHTML,"text/html; charset=UTF-8", null);
//          //  Log.e("!!",subject.infoHTML);
//    }else

        card_html.setVisibility(View.GONE);

    }

    void initCourseList() {
        courseList = findViewById(R.id.subject_recycler);
        if(pageTask_courseList!=null&&!pageTask_courseList.isCancelled()) pageTask_courseList.cancel(true);
        pageTask_courseList = new InitCourseListTask();
        pageTask_courseList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    void initProgress() {
        arcProgress = findViewById(R.id.subject_progress);
        //arcProgress.setTextColor(getColorAccent());
       if(pageTask_progress!=null&&!pageTask_progress.isCancelled()) pageTask_progress.cancel(true);
       pageTask_progress =  new InitProgressTask();
       pageTask_progress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class InitProgressTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            int finished = 0, unfinished = 0;
            for (EventItem ei : subject.getCourses()) {
                if (ei.hasPassed(now)) finished++;
                else unfinished++;
            }
            float x = ((float) finished) * 100.0f / (float) (finished + unfinished);
            return (int) x;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            arcProgress.setMax(100);
            arcProgress.setProgress(0);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            arcProgress.setProgress(integer);
        }
    }

    class RefreshRatingTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... strings) {
            Double sum = 0.0;
            int size = 0;
            for (Double f : subject.getRatingMap().values()) {
                if (f < 0) continue;
                sum += f;
                size++;
            }
            if (size == 0) return 0.0;
            return sum / size;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Double x) {
            super.onPostExecute(x);
            ratingText.setText(x + "/5");
        }
    }

    class InitCourseListTask extends AsyncTask<String, Integer, ArrayList<EventItem>> {

        @Override
        protected ArrayList<EventItem> doInBackground(String... strings) {
            ArrayList<EventItem> result = subject.getCourses();
            Collections.sort(result);
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<EventItem> eventItems) {
            super.onPostExecute(eventItems);
            if (eventItems != null) {
                listAdapter = new SubjectCoursesListAdapter(ActivitySubject.this, eventItems);
                courseList.setAdapter(listAdapter);
                LinearLayoutManager ll = new LinearLayoutManager(ActivitySubject.this, LinearLayoutManager.HORIZONTAL, false);
                courseList.setLayoutManager(ll);
                listAdapter.setOnItemClickListener(new SubjectCoursesListAdapter.OnItemClickListener() {
                    @Override
                    public void OnClick(View v, int position, EventItem ei) {
                        Intent i = new Intent(ActivitySubject.this, ActivityCourse.class);
                        Bundle b = new Bundle();
                        b.putSerializable("eventitem", ei);
                        i.putExtra("showSubject", false);
                        i.putExtras(b);
                        ActivitySubject.this.startActivity(i);
                        //showEventDialog(ActivitySubject.this,ei,v,null);
                    }
                });
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getIntent().getBooleanExtra("useCode", false)) {
            subject = allCurriculum.get(thisCurriculumIndex).getSubjectByName(getIntent().getStringExtra("subject"));
        } else {
            subject = allCurriculum.get(thisCurriculumIndex).getSubjectByCourseCode(getIntent().getStringExtra("subject"));
        }
        setInfos();
        if (subject != null){
            if(pageTask_rating!=null&&!pageTask_rating.isCancelled()) pageTask_rating.cancel(true);
            pageTask_rating = new RefreshRatingTask();
            pageTask_rating.execute();
        }
    }
}
