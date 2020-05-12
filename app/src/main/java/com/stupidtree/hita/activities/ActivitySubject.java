package com.stupidtree.hita.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.SubjectCoursesListAdapter;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.EventsUtils;
import com.stupidtree.hita.views.EditModeHelper;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.activities.ActivityMain.saveData;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;


public class ActivitySubject extends BaseActivity implements EditModeHelper.EditableContainer, BaseOperationTask.OperationListener, BasicRefreshTask.ListRefreshedListener<List<EventItem>> {

    boolean isFirst = true;
    public static final int RESULT_COLOR_CHANGED = 817;
    Subject subject;
    TextView ratingText;
    RecyclerView courseList;
    List<EventItem> courseListRes;
    SubjectCoursesListAdapter listAdapter;
    ArcProgress arcProgress;
    TextView name, point, attr, totalCourses, exam, school, xnxq, type, code, score_qz, score_qm, score_none, mooc;
    EditText pointEdit, attrEdit, totalCoursesEdit, schoolEdit, xnxqEdit, typeEdit, codeEdit;
    Switch examEdit, moocEdit;
    ViewGroup card_all_courses;
    View card_rate, card_color;
    LinearLayout qz_score_layout, qm_score_layout;
    ImageView pickColor, colorSample, editInfo, addCourse;
    DecimalFormat df = new DecimalFormat("#0.00");
    CardView jw_detail_entrance;
    CardView jw_detail_button;
    Button delete;
    //ImageView course_expand;
    boolean isCourseExpanded = false;
    List<EventItem> tempWholeCourses;

    boolean useCode;
    String subjectKey;
    boolean editMode = false;
    CollapsingToolbarLayout collapsingToolbarLayout;
    BroadcastReceiver receiver;
    //WebView webView;
    SharedPreferences SP;
    EditModeHelper editModeHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        SP = getSharedPreferences("timetable_pref", MODE_PRIVATE);
        useCode = getIntent().getBooleanExtra("useCode", false);
        subjectKey = getIntent().getStringExtra("subject");
        setContentView(R.layout.activity_subject);
        initViews();
        initToolBar();
        initCourseList();
        initReceiver();
    }


    void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isDestroyed()) return;
                Refresh();
            }
        };
        IntentFilter iF = new IntentFilter(TIMETABLE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, iF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    void initViews() {
        arcProgress = findViewById(R.id.subject_progress);
        delete = findViewById(R.id.delete);
        editInfo = findViewById(R.id.edit_info);
        addCourse = findViewById(R.id.course_add);
        jw_detail_entrance = findViewById(R.id.jw_subject_entrance);
        jw_detail_button = findViewById(R.id.jw_subject_button);
        name = findViewById(R.id.subject_name);
        point = findViewById(R.id.subject_credit);
        exam = findViewById(R.id.subject_exam);
        school = findViewById(R.id.subject_school);
        attr = findViewById(R.id.subject_attr);
        code = findViewById(R.id.subject_code);
        type = findViewById(R.id.subject_type);
        xnxq = findViewById(R.id.subject_xnxq);
        mooc = findViewById(R.id.subject_mooc);

        pointEdit = findViewById(R.id.edit_credit);
        examEdit = findViewById(R.id.edit_exam);
        schoolEdit = findViewById(R.id.edit_school);
        attrEdit = findViewById(R.id.edit_attr);
        codeEdit = findViewById(R.id.edit_code);
        typeEdit = findViewById(R.id.edit_type);
        xnxqEdit = findViewById(R.id.edit_xnxq);
        totalCoursesEdit = findViewById(R.id.edit_totalcourse);
        moocEdit = findViewById(R.id.edit_mooc);

        pickColor = findViewById(R.id.pick_color);
        colorSample = findViewById(R.id.color_sample);
        qz_score_layout = findViewById(R.id.score_qz_layout);
        qm_score_layout = findViewById(R.id.score_qm_layout);
        score_qz = findViewById(R.id.score_qz);
        score_qm = findViewById(R.id.score_qm);
        totalCourses = findViewById(R.id.subject_totalcourses);
        card_all_courses = findViewById(R.id.subject_card_allcourses);
        //course_expand = findViewById(R.id.course_expand);
        card_rate = findViewById(R.id.subject_card_rate);
        card_color = findViewById(R.id.subject_card_color);
        //  card_html = findViewById(R.id.subject_card_html);
        score_none = findViewById(R.id.score_none);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getThis()).setTitle(getString(R.string.delete_subject)).setMessage(getString(R.string.dialog_message_delete_subject)).
                        setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new deleteSubjectTask(ActivitySubject.this, subject.getName()).execute();
                            }
                        }).setNegativeButton(getString(R.string.button_cancel), null).
                        create();
                ad.show();
            }
        });
        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.stupidtree.hita.views.ColorPickerDialog(ActivitySubject.this)
                        .initColor(SP.getInt("color:" + subject.getName(), Color.YELLOW)).show(new com.stupidtree.hita.views.ColorPickerDialog.OnColorSelectedListener() {
                    @Override
                    public void OnSelected(int color) {
                        SP.edit().putInt("color:" + subject.getName(), color).apply();
                        colorSample.setColorFilter(color);
                        setResult(RESULT_COLOR_CHANGED);
                        Intent i = new Intent(TIMETABLE_CHANGED);
                        LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i);
                    }
                });

            }
        });
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subject == null) return;
                EventItem first = tempWholeCourses.get(0);
                new FragmentAddEvent().setInitialType("course")
//                        .setInitialFromTime(first.getStartTime())
//                        .setInitialToTime(first.getEndTime())
                        .setInitialTag2(first.getTag2())
                        .setInitialTag3(first.getTag3())
                        .setInitialExtraName(subject.getName())
                        .setTabSwitchable(false)
                        .setExtraEditable(false)
                        .show(getSupportFragmentManager(), "fae");
            }
        });

        pointEdit.setVisibility(View.GONE);
        examEdit.setVisibility(View.GONE);
        moocEdit.setVisibility(View.GONE);
        schoolEdit.setVisibility(View.GONE);
        attrEdit.setVisibility(View.GONE);
        codeEdit.setVisibility(View.GONE);
        typeEdit.setVisibility(View.GONE);
        xnxqEdit.setVisibility(View.GONE);
        totalCoursesEdit.setVisibility(View.GONE);

        point.setVisibility(View.VISIBLE);
        exam.setVisibility(View.VISIBLE);
        mooc.setVisibility(View.VISIBLE);
        school.setVisibility(View.VISIBLE);
        attr.setVisibility(View.VISIBLE);
        code.setVisibility(View.VISIBLE);
        type.setVisibility(View.VISIBLE);
        xnxq.setVisibility(View.VISIBLE);
        totalCourses.setVisibility(View.VISIBLE);
//        course_expand.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleCourseExpand();
//            }
//        });

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode = !editMode;
                switchEditMode();
                if (!editMode) {
                    editInfo.setImageResource(R.drawable.bt_edit);
                    saveEditInfo();
                    new saveTask(ActivitySubject.this, subject).executeOnExecutor(TPE);
                } else {
                    editInfo.setImageResource(R.drawable.fab_done);
                }
            }
        });

    }

    void switchEditMode() {
        if (!editMode) {
            pointEdit.setVisibility(View.GONE);
            examEdit.setVisibility(View.GONE);
            moocEdit.setVisibility(View.GONE);
            schoolEdit.setVisibility(View.GONE);
            attrEdit.setVisibility(View.GONE);
            codeEdit.setVisibility(View.GONE);
            typeEdit.setVisibility(View.GONE);
            xnxqEdit.setVisibility(View.GONE);
            totalCoursesEdit.setVisibility(View.GONE);
            point.setVisibility(View.VISIBLE);
            exam.setVisibility(View.VISIBLE);
            mooc.setVisibility(View.VISIBLE);
            school.setVisibility(View.VISIBLE);
            attr.setVisibility(View.VISIBLE);
            code.setVisibility(View.VISIBLE);
            type.setVisibility(View.VISIBLE);
            xnxq.setVisibility(View.VISIBLE);
            totalCourses.setVisibility(View.VISIBLE);
        } else {
            pointEdit.setVisibility(View.VISIBLE);
            examEdit.setVisibility(View.VISIBLE);
            moocEdit.setVisibility(View.VISIBLE);
            schoolEdit.setVisibility(View.VISIBLE);
            attrEdit.setVisibility(View.VISIBLE);
            codeEdit.setVisibility(View.VISIBLE);
            typeEdit.setVisibility(View.VISIBLE);
            xnxqEdit.setVisibility(View.VISIBLE);
            totalCoursesEdit.setVisibility(View.VISIBLE);

            pointEdit.setText(point.getText());
            examEdit.setChecked(subject.isExam());
            moocEdit.setChecked(subject.isMOOC());
            schoolEdit.setText(school.getText());
            attrEdit.setText(attr.getText());
            codeEdit.setText(code.getText());
            typeEdit.setText(type.getText());
            xnxqEdit.setText(xnxq.getText());
            totalCoursesEdit.setText(totalCourses.getText());

            point.setVisibility(View.GONE);
            exam.setVisibility(View.GONE);
            mooc.setVisibility(View.GONE);
            school.setVisibility(View.GONE);
            attr.setVisibility(View.GONE);
            code.setVisibility(View.GONE);
            type.setVisibility(View.GONE);
            xnxq.setVisibility(View.GONE);
            totalCourses.setVisibility(View.GONE);
        }
    }

    void initToolBar() {
//        toolbarLayout = findViewById(R.id.toolbarlayout);
//        toolbarLayout.setExpandedTitleColor(getTextColorPrimary());
//        toolbarLayout.setCollapsedTitleTextColor(getTextColorSecondary());
        //toolbarLayout.setTitle(subject.name);
        collapsingToolbarLayout = findViewById(R.id.collapse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ratingText = findViewById(R.id.text_rate);
        //toolbar.setTitle(subject.getName());
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
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
                if (item.getItemId() == R.id.action_subject_manager) {
                    Intent i = new Intent(ActivitySubject.this, ActivityCurriculumManager.class);
                    ActivitySubject.this.startActivity(i);
                }
                return true;
            }
        });
        // toolbarLayout.setExpandedTitleGravity(CollapsingToolbarLayout.TEXT_ALIGNMENT_CENTER);

    }

    void saveEditInfo() {
        subject.setExam(examEdit.isChecked());
        subject.setMOOC(moocEdit.isChecked());
        subject.setCompulsory(attrEdit.getText().toString());
        subject.setCredit(pointEdit.getText().toString());
        subject.setTotalCourses(totalCoursesEdit.getText().toString());
        subject.setCode(codeEdit.getText().toString());
        subject.setType(typeEdit.getText().toString());
        subject.setXnxq(xnxqEdit.getText().toString());
        subject.setSchool(schoolEdit.getText().toString());
    }

    void Refresh() {
        new InitSubjectTask(this, subjectKey, useCode).executeOnExecutor(TPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_subject, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SetTextI18n")
    void setInfo() {
        if (subject == null) return;
//        Log.e("id",subject.getId());
        collapsingToolbarLayout.setTitle(subject.getName());
        name.setText(subject.getName());
        attr.setText(subject.getCompulsory());
        point.setText(subject.getCredit());
        school.setText(subject.getSchool());
        code.setText(subject.getCode());
        xnxq.setText(subject.getXnxq());
        type.setText(subject.getType());
        colorSample.setColorFilter(SP.getInt("color:" + subject.getName(), Color.YELLOW));
        totalCourses.setText(subject.getTotalCourses());
        exam.setText(subject.isExam() ? getString(R.string.yes) : getString(R.string.no));
        mooc.setText(subject.isMOOC() ? getString(R.string.yes) : getString(R.string.no));
        if (subject.isMOOC()) {
            card_rate.setVisibility(View.GONE);
//            card_color.setVisibility(View.GONE);
            arcProgress.setVisibility(View.GONE);
            card_all_courses.setVisibility(View.GONE);
//            courseList.setVisibility(View.GONE);
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
        double rate;
        Double sum = 0.0;
        int size = 0;
        for (Double f : subject.getRatingMap().values()) {
            if (f < 0) continue;
            sum += f;
            size++;
        }
        if (size == 0) rate = 0.0;
        else rate = sum / size;
        ratingText.setText(rate + "/5");
//        if(subject.infoHTML!=null){
//
//            card_html.setVisibility(View.VISIBLE);
//            webView.loadData(subject.infoHTML,"text/html; charset=UTF-8", null);
//          //  Log.e("!!",subject.infoHTML);
//    }else

        //  card_html.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        if (editModeHelper.isEditMode()) editModeHelper.closeEditMode();
        else if (editMode) editInfo.callOnClick();
        else super.onBackPressed();
    }

    void initCourseList() {
        courseList = findViewById(R.id.subject_recycler);
        tempWholeCourses = new ArrayList<>();
        courseListRes = new ArrayList<>();
        listAdapter = new SubjectCoursesListAdapter(ActivitySubject.this, courseListRes);
        courseList.setAdapter(listAdapter);
//        courseList.setLayoutManager(new StaggeredGridLayoutManager(3,
//                StaggeredGridLayoutManager.HORIZONTAL));
        courseList.setLayoutManager(ChipsLayoutManager.newBuilder(this)
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setMaxViewsInRow(3)
//                .setRowBreaker(new IRowBreaker() {
//                    @Override
//                    public boolean isItemBreakRow(int i) {
//                        return i%3==0;
//                    }
//                })
                .build());
        listAdapter.setOnItemClickListener(new BaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View card, int position) {
                if (courseListRes.get(position).getEventType() == EventItem.TAG) {
//                  String name = courseListRes.get(position).getMainName();
//                  if(name.equals("more")){
//                      togg
//                  }
                    toggleCourseExpand();
                } else EventsUtils.showEventItem(getThis(), courseListRes.get(position));
            }
        });
        listAdapter.setOnItemLongClickListener(new BaseListAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                if (editModeHelper.isEditMode()) return false;
                editModeHelper.activateEditMode(position);
                return true;
            }
        });
        editModeHelper = new EditModeHelper(this, listAdapter, this);
        editModeHelper.init(this, R.id.edit_bar, R.layout.edit_mode_bar_2);
        editModeHelper.closeEditMode();
    }

    void refreshCourseList() {
        new RefreshCourseListTask(this, subject).executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    public void onEditClosed() {

    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onItemCheckedChanged(int position, boolean checked, int currentSelected) {

    }

    @Override
    public void onDelete(Collection toDelete) {
        new deleteCoursesTask(this, toDelete).execute();
    }

    void toggleCourseExpand() {
        Comparator<EventItem> comparator = new Comparator<EventItem>() {
            @Override
            public int compare(EventItem o1, EventItem o2) {
                if (o1.getEventType() == EventItem.TAG && o2.getEventType() == EventItem.TAG)
                    return 0;
                else return o1.compareTo(o2);
            }
        };
        BaseListAdapter.RefreshJudge<EventItem> refreshJudge = new BaseListAdapter.RefreshJudge<EventItem>() {
            @Override
            public boolean judge(EventItem data) {
                return data.getEventType() == EventItem.TAG;
            }
        };
        //   MaterialCircleAnimator.rotateTo(!isCourseExpanded,course_expand);
        if (isCourseExpanded) {
            int max = Math.min(tempWholeCourses.size(), 5);
            List<EventItem> temp = new ArrayList<>(tempWholeCourses.subList(0, max));
            if (tempWholeCourses.size() > 5) temp.add(EventItem.getTagInstance("more"));
            if (max > 0) listAdapter.notifyItemChangedSmooth(temp, refreshJudge, comparator);
            isCourseExpanded = false;
        } else {
            List<EventItem> temp = new ArrayList<>(tempWholeCourses);
            temp.add(EventItem.getTagInstance("less"));
            listAdapter.notifyItemChangedSmooth(temp, refreshJudge, comparator);
            isCourseExpanded = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object result) {

        switch (id) {
            case "init":
                subject = ((InitSubjectTask) task).subject;
                isFirst = false;
                if (isDestroyed() || isFinishing()) return;
                if ((boolean) result) {
                    setInfo();
                    double rate;
                    Double sum = 0.0;
                    int size = 0;
                    for (Double f : subject.getRatingMap().values()) {
                        if (f < 0) continue;
                        sum += f;
                        size++;
                    }
                    if (size == 0) rate = 0.0;
                    else rate = sum / size;
                    ratingText.setText(df.format(rate) + "/5");
                    if (!jwCore.hasLogin() || TextUtils.isEmpty(subject.getId())) {
                        jw_detail_entrance.setVisibility(View.GONE);
                    } else {
                        jw_detail_entrance.setVisibility(View.VISIBLE);
                        jw_detail_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityUtils.startJWSubjectActivity(getThis(), subject.getId());
                            }
                        });
                    }
                    refreshCourseList();
                } else {
                    Toast.makeText(getThis(), "数据库版本过低，请清除APP数据！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case "save":
                Intent i = new Intent(TIMETABLE_CHANGED);
                LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i);
                setInfo();
                saveData();
                break;
            case "delete_course":
                Refresh();
                break;
            case "delete_subject":
                if ((boolean) result) {
                    Toast.makeText(getThis(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getThis(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
                }
                Intent i2 = new Intent(TIMETABLE_CHANGED);
                LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i2);
                finish();
                break;

        }
    }

    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        arcProgress.setMax(100);
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<EventItem> result) {
        tempWholeCourses.clear();
        tempWholeCourses.addAll(result);
        if (isCourseExpanded) {
            List<EventItem> temp = new ArrayList<>(tempWholeCourses);
            temp.add(EventItem.getTagInstance("less"));
            listAdapter.notifyItemChangedSmooth(temp);
        } else {
            int max = Math.min(tempWholeCourses.size(), 5);
            List<EventItem> temp = new ArrayList<>(tempWholeCourses.subList(0, max));
            if (tempWholeCourses.size() > 5) temp.add(EventItem.getTagInstance("more"));
            if (max > 0)
                listAdapter.notifyItemChangedSmooth(temp, true, new Comparator<EventItem>() {
                    @Override
                    public int compare(EventItem o1, EventItem o2) {
                        if (o1.getEventType() == EventItem.TAG && o2.getEventType() == EventItem.TAG)
                            return 0;
                        else return o1.compareTo(o2);
                    }
                });
        }
        int finished = 0, unfinished = 0;
        for (EventItem ei : result) {
            if (ei.hasPassed(timeTableCore.getNow())) finished++;
            else unfinished++;
        }
        float percentage = ((float) finished) * 100.0f / (float) (finished + unfinished);
        ValueAnimator va = ValueAnimator.ofInt(arcProgress.getProgress(), (int) percentage);
        va.setDuration(500);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                arcProgress.setProgress((int) animation.getAnimatedValue());
            }
        });
        va.start();

    }


    static class RefreshCourseListTask extends BasicRefreshTask<List<EventItem>> {

        // float percentage;
        Subject subject;

        RefreshCourseListTask(ListRefreshedListener<? extends List<EventItem>> listRefreshedListener, Subject subject) {
            super(listRefreshedListener);
            this.subject = subject;
        }


        @Override
        protected List<EventItem> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            ArrayList<EventItem> result = timeTableCore.getCourses(subject);
            Collections.sort(result, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o1.compareTo(o2);
                }
            });

            return result;
        }

    }

    static class InitSubjectTask extends BaseOperationTask<Object> {

        boolean useCode;
        String subjectKey;
        Subject subject;

        InitSubjectTask(OperationListener listRefreshedListener, String subjectKey, boolean useCode) {
            super(listRefreshedListener);
            id = "init";
            this.subjectKey = subjectKey;
            this.useCode = useCode;
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            if (!useCode) {
                subject = timeTableCore.getSubjectByName(null,subjectKey);
            } else {
                subject = timeTableCore.getSubjectByCourseCode(null,subjectKey);
            }
            return subject != null;
        }


    }

    static class saveTask extends BaseOperationTask<Object> {

        Subject subject;

        saveTask(OperationListener listRefreshedListener, Subject subject) {
            super(listRefreshedListener);
            this.subject = subject;
            id = "save";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            if (subject == null) return null;
            timeTableCore.saveSubject(subject);
            return null;
        }

    }

    static class deleteCoursesTask extends BaseOperationTask<Object> {
        Collection target;

        deleteCoursesTask(OperationListener listRefreshedListener, Collection target) {
            super(listRefreshedListener);
            this.target = target;
            id = "delete_course";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            for (Object ei : target) {
                if(ei instanceof EventItem){
                    timeTableCore.deleteEvent((EventItem) ei, true);
                }

            }
            return null;
        }
    }

    static class deleteSubjectTask extends BaseOperationTask<Object> {

        String name;

        deleteSubjectTask(OperationListener listRefreshedListener, String name) {
            super(listRefreshedListener);
            this.name = name;
            id = "delete_subject";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            try {
                timeTableCore.deleteSubject(name, timeTableCore.getCurrentCurriculum().getCurriculumCode());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

    }
}
