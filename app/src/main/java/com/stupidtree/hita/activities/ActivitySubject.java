package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.adapter.SubjectCoursesListAdapter;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.fragments.main.FragmentTimeLine.showEventDialog;


public class ActivitySubject extends BaseActivity {

    boolean isFirst = true;
    public static final int RESULT_COLOR_CHANGED = 817;
    Subject subject;
    TextView ratingText;
    RecyclerView courseList;
    SubjectCoursesListAdapter listAdapter;
    ArcProgress arcProgress;
    TextView name,point, attr, totalcourses, exam, school, xnxq, type, code, score_qz, score_qm, score_none;
    CardView   card_allcourses;
    View card_rate,card_color;
    LinearLayout qz_score_layout, qm_score_layout;
    InitProgressTask pageTask_progress;
    //RefreshRatingTask pageTask_rating;
    ImageView pickColor,colorSample;
    InitCourseListTask pageTask_courseList;
    DecimalFormat df = new DecimalFormat("#0.00");

    boolean useCode;
    String subjectKey;

    //WebView webView;


    @Override
    protected void stopTasks() {
        if(pageTask_progress!=null&&pageTask_progress.getStatus()!=AsyncTask.Status.FINISHED) pageTask_progress.cancel(true);
       if(pageTask_courseList!=null&&pageTask_courseList.getStatus()!=AsyncTask.Status.FINISHED) pageTask_courseList.cancel(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        useCode = getIntent().getBooleanExtra("useCode", false);
        subjectKey = getIntent().getStringExtra("subject");
        //requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);//申请动画
        //Transition explode = TransitionInflater.from(this).inflateTransition(android.R.transition.explode);
        //getWindow().setEnterTransition(explode);
        setContentView(R.layout.activity_subject);
        new InitSubjectTask().executeOnExecutor(TPE);

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
        pickColor = findViewById(R.id.pick_color);
        colorSample = findViewById(R.id.color_sample);
        qz_score_layout = findViewById(R.id.score_qz_layout);
        qm_score_layout = findViewById(R.id.score_qm_layout);
        score_qz = findViewById(R.id.score_qz);
        score_qm = findViewById(R.id.score_qm);
        totalcourses = findViewById(R.id.subject_totalcourses);
        card_allcourses = findViewById(R.id.subject_card_allcourses);
        card_rate = findViewById(R.id.subject_card_rate);
        card_color = findViewById(R.id.subject_card_color);
      //  card_html = findViewById(R.id.subject_card_html);
        score_none = findViewById(R.id.score_none);

        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.stupidtree.hita.diy.ColorPickerDialog(ActivitySubject.this)
                        .initColor(defaultSP.getInt("color:"+subject.getName(),Color.YELLOW)).show(new com.stupidtree.hita.diy.ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void OnSelected(int color) {
                        defaultSP.edit().putInt("color:"+subject.getName(),color).apply();
                        colorSample.setColorFilter(color);
                        setResult(RESULT_COLOR_CHANGED);
                    }
                });
//                new ColorPickerDialog.Builder(ActivitySubject.this)
//                        .attachAlphaSlideBar(false)
//                        .attachBrightnessSlideBar(true)
//                        .setTitle(R.string.pick_color)
//                        .setPositiveButton(R.string.button_confirm,
//                                new ColorEnvelopeListener() {
//                                    @Override
//                                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
//                                        defaultSP.edit().putInt("color:"+subject.getName(),envelope.getColor()).apply();
//                                        colorSample.setColorFilter(envelope.getColor());
//                                        setResult(RESULT_COLOR_CHANGED);
//                                            }
//                                })
//                        .setNegativeButton(R.string.button_cancel,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        dialogInterface.dismiss();
//                                    }
//                                })
//                        .attachAlphaSlideBar(true) // default is true. If false, do not show the AlphaSlideBar.
//                        .attachBrightnessSlideBar(true)  // default is true. If false, do not show the BrightnessSlideBar.
//                        .show();
            }
        });
    }

    void initToolBar() {
//        toolbarLayout = findViewById(R.id.toolbarlayout);
//        toolbarLayout.setExpandedTitleColor(getTextColorPrimary());
//        toolbarLayout.setCollapsedTitleTextColor(getTextColorSecondary());
        //toolbarLayout.setTitle(subject.name);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ratingText = findViewById(R.id.text_rate);
        toolbar.setTitle(subject.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.toolbar_subject);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.action_subject_manager){
                    Intent i = new Intent(ActivitySubject.this,ActivityCurriculumManager.class);
                    ActivitySubject.this.startActivity(i);
                }
                return true;
            }
        });
       // toolbarLayout.setExpandedTitleGravity(CollapsingToolbarLayout.TEXT_ALIGNMENT_CENTER);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_subject, menu);
        return super.onCreateOptionsMenu(menu);
    }

    void setInfos() {
        name.setText(subject.getName());
        attr.setText(subject.getCompulsory());
        point.setText(subject.getCredit());
        school.setText(subject.getSchool());
        code.setText(subject.getCode());
        xnxq.setText(subject.getXnxq());
        type.setText(subject.getType());
        colorSample.setColorFilter(defaultSP.getInt("color:"+subject.getName(),Color.YELLOW));
        totalcourses.setText(subject.getTotalCourses());
        exam.setText((subject.isExam() ? "是" : "否") + (subject.isDefault() ? "(默认)" : ""));
        if (subject.isMOOC()) {
            card_rate.setVisibility(View.GONE);
            card_color.setVisibility(View.GONE);
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
        Double rate = 0.0;
        Double sum = 0.0;
        int size = 0;
        for (Double f : subject.getRatingMap().values()) {
            if (f < 0) continue;
            sum += f;
            size++;
        }
        if (size == 0) rate = 0.0;
        else rate =  sum / size;
        ratingText.setText(rate + "/5");
//        if(subject.infoHTML!=null){
//
//            card_html.setVisibility(View.VISIBLE);
//            webView.loadData(subject.infoHTML,"text/html; charset=UTF-8", null);
//          //  Log.e("!!",subject.infoHTML);
//    }else

      //  card_html.setVisibility(View.GONE);

    }

    void initCourseList() {
        courseList = findViewById(R.id.subject_recycler);
        if(pageTask_courseList!=null&&pageTask_courseList.getStatus()!=AsyncTask.Status.FINISHED) pageTask_courseList.cancel(true);
        pageTask_courseList = new InitCourseListTask();
        pageTask_courseList.executeOnExecutor(HITAApplication.TPE);

    }

    void initProgress() {
        arcProgress = findViewById(R.id.subject_progress);
        //arcProgress.setTextColor(getColorAccent());
       if(pageTask_progress!=null&&pageTask_progress.getStatus()!=AsyncTask.Status.FINISHED) pageTask_progress.cancel(true);
       pageTask_progress =  new InitProgressTask();
       pageTask_progress.executeOnExecutor(HITAApplication.TPE);
    }

    @SuppressLint("StaticFieldLeak")
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



    @SuppressLint("StaticFieldLeak")
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
                LinearLayoutManager ll = new WrapContentLinearLayoutManager(ActivitySubject.this, LinearLayoutManager.HORIZONTAL, false);
                courseList.setLayoutManager(ll);
                listAdapter.setOnItemClickListener(new SubjectCoursesListAdapter.OnItemClickListener() {
                    @Override
                    public void OnClick(View v, int position, EventItem ei) {
//                        Intent i = new Intent(ActivitySubject.this, ActivityCourse.class);
//                        Bundle b = new Bundle();
//                        b.putSerializable("eventitem", ei);
//                        i.putExtra("showSubject", false);
//                        i.putExtras(b);
//                        ActivitySubject.this.startActivity(i);
                        showEventDialog(ActivitySubject.this,ei,v,null);
                    }
                });
            }

        }
    }


    @SuppressLint("StaticFieldLeak")
    class InitSubjectTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            if (!useCode) {
                subject = timeTableCore.getCurrentCurriculum().getSubjectByName(subjectKey);
            } else {
                subject = timeTableCore.getCurrentCurriculum().getSubjectByCourseCode(subjectKey);
            }
            return subject!=null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            isFirst = false;
            if((boolean)o){

                initViews();
                initToolBar();
                initCourseList();
                initProgress();
                setInfos();
                Double rate = 0.0;
                Double sum = 0.0;
                int size = 0;
                for (Double f : subject.getRatingMap().values()) {
                    if (f < 0) continue;
                    sum += f;
                    size++;
                }
                if (size == 0) rate = 0.0;
                else rate =  sum / size;
                ratingText.setText(df.format(rate) + "/5");
            }
        }
    }

    class RefreshSubjectTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            if (!useCode) {
                subject = timeTableCore.getCurrentCurriculum().getSubjectByName(subjectKey);
            } else {
                subject = timeTableCore.getCurrentCurriculum().getSubjectByCourseCode(subjectKey);
            }
            return subject!=null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if((boolean)o){
                setInfos();
                Double rate = 0.0;
                Double sum = 0.0;
                int size = 0;
                for (Double f : subject.getRatingMap().values()) {
                    if (f < 0) continue;
                    sum += f;
                    size++;
                }
                if (size == 0) rate = 0.0;
                else rate =  sum / size;
                ratingText.setText(df.format(rate) + "/5");
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!isFirst) new RefreshSubjectTask().executeOnExecutor(TPE);
    }
}
