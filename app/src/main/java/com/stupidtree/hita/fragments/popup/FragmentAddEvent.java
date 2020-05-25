package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.timetable.TimeTableGenerator;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.timetable.packable.TimePeriod;
import com.stupidtree.hita.util.EventsUtils;
import com.stupidtree.hita.views.MaterialCircleAnimator;
import com.stupidtree.hita.views.PickCourseTimeDialog;
import com.stupidtree.hita.views.PickInfoDialog;
import com.stupidtree.hita.views.PickSingleTimeDialog;
import com.stupidtree.hita.views.PickSubjectDialog;
import com.stupidtree.hita.views.PickTeacherDialog;
import com.stupidtree.hita.views.PickTimePeriodDialog;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;
import static com.stupidtree.hita.timetable.TimetableCore.ARRANGEMENT;
import static com.stupidtree.hita.timetable.TimetableCore.COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.DDL;
import static com.stupidtree.hita.timetable.TimetableCore.EXAM;
import static com.stupidtree.hita.timetable.TimetableCore.contains_integer;
import static com.stupidtree.hita.timetable.TimetableCore.getNumberAtTime;

@SuppressLint("ValidFragment")
public class FragmentAddEvent extends FragmentRadiusPopup
        implements BaseOperationTask.OperationListener<Object> {
    private boolean timeSet = false, subjectSet = false, taskSet = false, locationSet = false, teacherSet = false;
    private String teacher;
    private boolean timeSet_course = false;
    private RadioGroup mRadioGroup;
    private EditText name, tag2, tag3, extra;
    private String locationStr = "";
    private FloatingActionButton done;
    private String subjectCode, subjectName;
    private String init_name = null, init_tag2 = null, init_tag3 = null, init_extraName = null;
    private boolean extra_editable = true, init_all_day = false, tab_switchable = true;
    private Task task;
    private LinearLayout nameLayout;
    private LinearLayout extraLayout;
    private ExpandableLayout mExpandableLayout;
    private CheckBox wholeDaySwitch;
    private ImageView extra_button;
    private HTime fromT = new HTime(TimetableCore.getNow()), toT = new HTime(TimetableCore.getNow());
    private int week = 1;
    private int dow = 1;
    private String initIndex = "arrangement";
    private List<Integer> weeks = new ArrayList<>();
    private int begin, last;
    private TextView title;
    private CardView pickTime;
    private ImageView pickTimeIcon;
    private TextView pickTimeText;
    private CardView pickTask;
    private ImageView pickTaskIcon;
    private TextView pickTaskText;
    private ImageView pickTaskCancel;
    private CardView pickSubject;
    private ImageView pickSubjectIcon;
    private TextView pickSubjectText;
    private CardView pickLocation;
    private ImageView pickLocationIcon;
    private TextView pickLocationText;
    private ImageView pickLocationCancel;
    private ImageView pickTeacherCancel;
    private CardView pickTeacher;
    private ImageView pickTeacherIcon;
    private TextView pickTeacherText;
    private boolean EditMode = false;
    private EventItem EditEvent = null;


    public FragmentAddEvent() {

    }


    public static FragmentAddEvent newInstance() {
        return new FragmentAddEvent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = View.inflate(requireContext(), R.layout.fragment_add_event, null);
        initViews(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        int initCheckedId = R.id.ade_arrange;
        switch (initIndex) {
            case "arrangement":
                initCheckedId = R.id.ade_arrange;
                break;
            case "ddl":
                initCheckedId = R.id.ade_ddl;
                break;
            case "exam":
                initCheckedId = R.id.ade_exam;
                break;
            case "course":
                initCheckedId = R.id.ade_course;
                break;
        }
        mRadioGroup.check(initCheckedId);
        if (init_extraName != null) {
            extra.setText(init_extraName);
            if (!EditMode) {
                subjectSet = true;
                subjectName = init_extraName;
            }
        }
        extra.setEnabled(extra_editable);
        if (!extra_editable) extra_button.setVisibility(View.GONE);
        else if(initIndex.equals("course"))extra_button.setVisibility(View.VISIBLE);
        mRadioGroup.setEnabled(tab_switchable);
        for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
            View v = mRadioGroup.getChildAt(i);
            if (v instanceof RadioButton) {
                v.setEnabled(tab_switchable);
            }
        }
        if (init_name != null) name.setText(init_name);
        if (init_tag2 != null) {
            tag2.setText(init_tag2);
            locationSet = true;
            locationStr = init_tag2;
        }
        if (init_tag3 != null) {
            tag3.setText(init_tag3);
            teacherSet = true;
            teacher = init_tag3;
        }
        wholeDaySwitch.setChecked(init_all_day);
        refreshTimeBlock();
        refreshExamBlock();
        refreshLocationBlock();
        refreshTeacherBlock();
        refreshTaskBlock();
        if (!EditMode) {
            switch (mRadioGroup.getCheckedRadioButtonId()) {
                case R.id.ade_course:
                case R.id.ade_exam:
                    if (extra_editable) {
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                        extraLayout.requestFocus();
                    }
                    break;
                case R.id.ade_arrange:
                case R.id.ade_ddl:
                    name.requestFocus();
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    break;

            }
        }
        refreshFabState();
    }

    private void initViews(View v) {
        title = v.findViewById(R.id.title);
        extra_button = v.findViewById(R.id.ade_extra_button);
        mRadioGroup = v.findViewById(R.id.ade_radiogroup);
        name = v.findViewById(R.id.ade_name);
        tag2 = v.findViewById(R.id.ade_tag2);
        tag3 = v.findViewById(R.id.ade_tag3);
        pickTimeIcon = v.findViewById(R.id.pick_time_icon);
        pickTimeText = v.findViewById(R.id.time_show);
        pickTime = v.findViewById(R.id.pick_time);
        extra = v.findViewById(R.id.ade_extra);
        //pickTimeBG = v.findViewById(R.id.pick_time_bg);
        pickTaskIcon = v.findViewById(R.id.pick_task_icon);
        pickTaskText = v.findViewById(R.id.pick_task_text);
        pickTask = v.findViewById(R.id.pick_task);
        pickTaskCancel = v.findViewById(R.id.pick_task_cancel);
        pickSubjectIcon = v.findViewById(R.id.pick_subject_icon);
        pickSubjectText = v.findViewById(R.id.pick_subject_text);
        pickSubject = v.findViewById(R.id.pick_subject);
        pickLocationIcon = v.findViewById(R.id.pick_location_icon);
        pickLocationCancel = v.findViewById(R.id.pick_location_cancel);
        pickTeacherCancel = v.findViewById(R.id.pick_teacher_cancel);
        pickLocationText = v.findViewById(R.id.pick_location_text);
        pickLocation = v.findViewById(R.id.pick_location);
        pickTeacherIcon = v.findViewById(R.id.pick_teacher_icon);
        pickTeacherText = v.findViewById(R.id.pick_teacher_text);
        pickTeacher = v.findViewById(R.id.pick_teacher);
        nameLayout = v.findViewById(R.id.ade_namelayout);
        extraLayout = v.findViewById(R.id.ade_extra_layout);
        mExpandableLayout = v.findViewById(R.id.ade_expandlayout);
        final ImageView bt_expand = v.findViewById(R.id.ade_expand_button);
        done = v.findViewById(R.id.ade_bt_done);
        wholeDaySwitch = v.findViewById(R.id.ade_switch_wholeday);
        TextWatcher editTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshFabState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        name.addTextChangedListener(editTextWatcher);
        extra.addTextChangedListener(editTextWatcher);
        pickTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!timeSet) {
                    Toast.makeText(HContext, getString(R.string.ade_set_time_first), Toast.LENGTH_SHORT).show();
                    return false;
                }
                SparseArray<HTime> times = TimeTableGenerator.autoAdd_getTime(TimetableCore.getNow(), week, dow, 25);
                if (times != null) {
                    setFromTime(times.get(0).hour, times.get(0).minute);
                    setToTime(times.get(1).hour, times.get(1).minute);
                } else {
                    Toast.makeText(HContext, getString(R.string.ade_no_suitable_allocation), Toast.LENGTH_SHORT).show();
                    return false;
                }
                refreshTimeBlock();
                return true;
            }
        });
        wholeDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pickTask.setVisibility(View.GONE);
                    taskSet = false;
                    task = null;
                } else {

                    switch (mRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.ade_arrange:
                            //if(app_task_enabled) pickTask.setVisibility(View.VISIBLE);
                            break;
                        case R.id.ade_ddl:
                            break;
                    }
                }
                refreshTimeBlock();
            }
        });
        bt_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableLayout.toggle();
                MaterialCircleAnimator.rotateTo(mExpandableLayout.isExpanded(), bt_expand);
            }
        });
        pickSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PickSubjectDialog(requireContext(), getString(R.string.choose_or_add_subject), new PickSubjectDialog.OnPickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void OnPick(Subject subject) {
                        subjectSet = true;
                        subjectCode = subject.getCode();
                        subjectName = subject.getName();
                        if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_exam)
                            extra.setText(subjectName + "考试");
                        refreshExamBlock();
                    }
                }).show();
                // new showSubjectsDialogTask().executeOnExecutor(HITAApplication.TPE);
            }
        });
        pickTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new showTasksDialogTask(FragmentAddEvent.this).executeOnExecutor(HITAApplication.TPE);
            }
        });
        pickTaskCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskSet = false;
                task = null;
                name.setText("");
                refreshTaskBlock();
            }
        });
        pickLocationCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationSet = false;
                locationStr = null;
                refreshLocationBlock();
            }
        });
        pickTeacherCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teacherSet = false;
                teacher = null;
                refreshTeacherBlock();
            }
        });
        pickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickInfoDialog(requireContext(), getString(R.string.pick_location), PickInfoDialog.LOCATION_ALL, new PickInfoDialog.OnPickListener() {
                    @Override
                    public void OnPick(String title, Object obj) {
                        locationSet = true;
                        locationStr = title;
                        refreshLocationBlock();
                    }
                }).show();
            }
        });

        pickTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickTeacherDialog(requireContext(), getString(R.string.pick_teacher), new PickTeacherDialog.OnPickListener() {
                    @Override
                    public void OnPick(String name) {
                        teacherSet = true;
                        teacher = name;
                        refreshTeacherBlock();
                    }
                }).setInitial(teacher).show();
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                wholeDaySwitch.setChecked(false);
                int typeName = R.string.ade_arrangement;
                switch (checkedId) {
                    case R.id.ade_arrange:
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        // autoAllocation.setVisibility(View.VISIBLE);
                        pickLocation.setVisibility(View.GONE);
                        pickTeacher.setVisibility(View.GONE);
                        pickSubject.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        extraLayout.setVisibility(View.GONE);
                        typeName = R.string.ade_arrangement;
                        break;
                    case R.id.ade_ddl:
                        pickTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        // autoAllocation.setVisibility(View.VISIBLE);
                        pickLocation.setVisibility(View.GONE);
                        pickTeacher.setVisibility(View.GONE);
                        pickSubject.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        extraLayout.setVisibility(View.GONE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        typeName = R.string.ade_ddl;
                        break;
                    case R.id.ade_exam:
                        pickTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.GONE);
                        //autoAllocation.setVisibility(View.GONE);
                        pickTeacher.setVisibility(View.GONE);
                        pickLocation.setVisibility(View.VISIBLE);
                        pickSubject.setVisibility(View.VISIBLE);
                        nameLayout.setVisibility(View.GONE);
                        extraLayout.setVisibility(View.VISIBLE);
                        extra.setText("");
                        extra.setHint(getString(R.string.ade_exam_name));
                        extra_button.setVisibility(View.GONE);
                        mExpandableLayout.setVisibility(View.GONE);
                        typeName = R.string.ade_exam;
                        break;
                    case R.id.ade_course:
                        pickTask.setVisibility(View.GONE);
                        extra.setText("");
                        wholeDaySwitch.setVisibility(View.GONE);
                        pickTeacher.setVisibility(View.VISIBLE);
                        //autoAllocation.setVisibility(View.GONE);
                        pickLocation.setVisibility(View.VISIBLE);
                        pickSubject.setVisibility(View.GONE);
                        extraLayout.setVisibility(View.VISIBLE);
                        extra.setHint(R.string.ade_subject_name);
                        extra_button.setVisibility(View.VISIBLE);
                        nameLayout.setVisibility(View.GONE);
                        mExpandableLayout.setVisibility(View.GONE);
                        typeName = R.string.ade_course;
                        break;
                }
                if (EditMode) {
                    title.setText(getString(R.string.ade_title_edit, getString(typeName)));
                } else {
                    title.setText(getString(R.string.ade_title, getString(typeName)));
                }

                refreshTimeBlock();
            }
        });

        extra_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PickSubjectDialog(requireContext(), getString(R.string.choose_subject), new PickSubjectDialog.OnPickListener() {
                    @Override
                    public void OnPick(Subject subject) {
                        extra.setText(subject.getName());
                    }
                }).show();
            }
        });
        pickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_ddl) {
                    if (!wholeDaySwitch.isChecked()) {
                        PickSingleTimeDialog pstd = new PickSingleTimeDialog((BaseActivity) FragmentAddEvent.this.requireActivity(), new PickSingleTimeDialog.onDialogConformListener() {
                            @Override
                            public void onClick(int week, int dow, int hour, int minute, boolean dateSet) {
                                timeSet = dateSet;
                                if (dateSet) {
                                    FragmentAddEvent.this.dow = dow;
                                    FragmentAddEvent.this.week = week;
                                    fromT.setTime(hour, minute);
                                    toT.setTime(hour, minute);
                                    refreshTimeBlock();
                                }
                                //Log.e("!!",week+"x"+dow);
                            }
                        });
                        if (timeSet) pstd.setInitialValue(week, dow, fromT);
                        pstd.show();
                    } else {
                        PickTimePeriodDialog ptpd = new PickTimePeriodDialog((BaseActivity) requireActivity(), new PickTimePeriodDialog.onDialogConformListener() {
                            @Override
                            public void onClick(int week, int dow, int hour1, int minute1, int hour2, int minute2, boolean dateSet) {
                                timeSet = dateSet;
                                if (dateSet) {
                                    FragmentAddEvent.this.dow = dow;
                                    FragmentAddEvent.this.week = week;
                                    refreshTimeBlock();
                                }
                            }
                        });
                        if (timeSet) ptpd.setInitialValue(week, dow, fromT, toT);
                        ptpd.dateOnly();
                        ptpd.show();
                    }


                } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_course) {
                    PickCourseTimeDialog pctd = new PickCourseTimeDialog((BaseActivity) requireActivity(), new PickCourseTimeDialog.onDialogConformListener() {
                        @Override
                        public void onClick(List<Integer> newWeeks, int newDow, int newBegin, int newLast) {
                            timeSet_course = true;
                            weeks.clear();
                            weeks.addAll(newWeeks);
                            begin = newBegin;
                            last = newLast;
                            dow = newDow;
                            refreshTimeBlock();
                        }
                    });
                    if (timeSet_course) pctd.setInitialValue(weeks, dow, begin, last);
                    pctd.show();
                } else {
                    PickTimePeriodDialog ptpd = new PickTimePeriodDialog((BaseActivity) requireActivity(), new PickTimePeriodDialog.onDialogConformListener() {
                        @Override
                        public void onClick(int week, int dow, int hour1, int minute1, int hour2, int minute2, boolean dateSet) {
                            timeSet = dateSet;
                            FragmentAddEvent.this.dow = dow;
                            FragmentAddEvent.this.week = week;
                            fromT.setTime(hour1, minute1);
                            toT.setTime(hour2, minute2);
                            refreshTimeBlock();
                        }
                    });
                    if (timeSet) ptpd.setInitialValue(week, dow, fromT, toT);
                    if (wholeDaySwitch.isChecked()) ptpd.dateOnly();
                    ptpd.show();
                }

            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = 0;
                switch (mRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.ade_arrange:
                        type = TimetableCore.ARRANGEMENT;
                        break;
                    case R.id.ade_ddl:
                        type = DDL;
                        break;
                    case R.id.ade_exam:
                        type = TimetableCore.EXAM;
                        break;
                    case R.id.ade_course:
                        type = COURSE;
                        break;
                }
                if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_course) {
                    new addCourseTask(FragmentAddEvent.this, TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode(), weeks,
                            extra.getText().toString(), locationStr, teacher, begin, last, dow).executeOnExecutor(HITAApplication.TPE);
                    return;
                }

                String Tname, Ttag2, Ttag3;
                if (type == TimetableCore.EXAM) {
                    Ttag2 = TextUtils.isEmpty(locationStr) ? "" : locationStr;
                    if (TextUtils.isEmpty(subjectCode)) Ttag3 = "科目名称：" + subjectName;
                    else Ttag3 = "科目代码：" + subjectCode;
                    Tname = extra.getText().toString();
                } else {
                    Tname = name.getText().toString().isEmpty() ? "" : name.getText().toString();
                    Ttag2 = tag2.getText().toString().isEmpty() ? "" : tag2.getText().toString();
                    Ttag3 = tag3.getText().toString().isEmpty() ? "" : tag3.getText().toString();
                }
                HTime tempToTime = type == TimetableCore.ARRANGEMENT || type == TimetableCore.EXAM ? toT : fromT;
                if (!EditMode) {
                    new addEventTask(FragmentAddEvent.this, TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode(),
                            type, Tname, Ttag2, Ttag3, fromT, tempToTime, week, dow, wholeDaySwitch.isChecked()).executeOnExecutor(HITAApplication.TPE);

                } else {
                    new addEventTask(FragmentAddEvent.this, TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode(),
                            type, Tname, Ttag2, Ttag3, fromT, tempToTime, week, dow, wholeDaySwitch.isChecked(), EditEvent).executeOnExecutor(HITAApplication.TPE);

                }
            }
        });

        if (EditMode) {
            title.setText(getString(R.string.ade_title_edit, getString(EditEvent.getTypeName())));
            if (!initIndex.equals("exam")) mExpandableLayout.expand();
            MaterialCircleAnimator.rotateTo(true, bt_expand);
            mRadioGroup.setVisibility(View.GONE);
            // if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_exam) mExpandableLayout.setVisibility(View.GONE);
        } else {
            title.setText(getString(R.string.ade_title, getString(R.string.ade_arrangement)));
            mExpandableLayout.collapse();
            MaterialCircleAnimator.rotateTo(false, bt_expand);
            mRadioGroup.setVisibility(View.VISIBLE);
        }
    }

    private void refreshFabState() {
        if (canIAddNow()) {
            done.show();
        } else {
            done.hide();
        }
    }

    private boolean canIAddNow() {
        int type = R.id.ade_arrange;
        switch (mRadioGroup.getCheckedRadioButtonId()) {
            case R.id.ade_arrange:
                type = TimetableCore.ARRANGEMENT;
                break;
            case R.id.ade_ddl:
                type = DDL;
                break;
            case R.id.ade_exam:
                type = EXAM;
                break;
            case R.id.ade_course:
                type = COURSE;
                break;
        }
        if (type == EXAM) {
            return subjectSet && !TextUtils.isEmpty(extra.getText().toString()) && (wholeDaySwitch.isChecked() || fromT.before(toT));
        } else if (type == COURSE) {
            return timeSet_course && !TextUtils.isEmpty(extra.getText().toString()) && last > 0 && weeks.size() > 0;
        } else if (type == ARRANGEMENT) {
            return timeSet && !TextUtils.isEmpty(name.getText().toString()) && (wholeDaySwitch.isChecked() || fromT.before(toT));
        } else if (type == DDL) {
            return timeSet && !TextUtils.isEmpty(name.getText().toString());
        }
        return false;
    }

    public FragmentAddEvent setInitialData(int week, int dow, TimePeriod tp) {
        this.week = week;
        weeks.add(week);
        this.dow = dow;
        timeSet = true;
        timeSet_course = true;
        fromT = tp.start;
        toT = tp.end;
        begin = getNumberAtTime(tp.start);
        int end = getNumberAtTime(tp.end);
        // Log.e("begin,end",begin+"-"+end);
        last = end - begin + 1;
        // hasInitialData = true;
        return this;
    }

    public FragmentAddEvent setInitialDate(Calendar c) {
        int week = TimetableCore.getInstance(HContext).getCurrentCurriculum().getWeekOfTerm(c);
        int DOW = c.get(Calendar.DAY_OF_WEEK);
        int dow = DOW == 1 ? 7 : DOW - 1;
        this.week = week;
        weeks.add(week);
        this.dow = dow;
        timeSet = true;
        timeSet_course = true;
        return this;
    }

    public FragmentAddEvent setInitialFromTime(HTime fromT) {
        this.fromT = fromT;
        toT = fromT;
        begin = getNumberAtTime(fromT);
        timeSet = true;
        timeSet_course = true;
        return this;
    }

    public FragmentAddEvent setInitialToTime(HTime toT) {
        this.toT = toT;
        timeSet = true;
        timeSet_course = true;
        int end = getNumberAtTime(toT);
        last = end - begin + 1;
        return this;
    }

    public FragmentAddEvent setInitialWeeks(List<Integer> weeks) {
        this.weeks.clear();
        this.weeks.addAll(weeks);
        timeSet_course = true;
        return this;
    }

    public FragmentAddEvent setTabSwitchable(boolean switchable) {
        tab_switchable = switchable;
        return this;
    }

    public FragmentAddEvent setInitialType(String type) {
        initIndex = type;
        return this;
    }

    public FragmentAddEvent setInitialName(String name) {
        if (!TextUtils.isEmpty(name)) init_name = name;
        return this;
    }

    public FragmentAddEvent setExtraEditable(boolean enable) {
        extra_editable = enable;
        return this;
    }

    public FragmentAddEvent setInitialExtraName(String name) {
        if (!TextUtils.isEmpty(name)) init_extraName = name;
        return this;
    }

    public FragmentAddEvent setInitialTag2(String tag2) {
        if (!TextUtils.isEmpty(tag2)) init_tag2 = tag2;
        return this;
    }

    public FragmentAddEvent setInitialTag3(String tag3) {
        if (!TextUtils.isEmpty(tag3)) {
            init_tag3 = tag3;
            if (init_tag3.startsWith("科目名称")) {
                subjectName = init_tag3.replaceAll("科目名称：", "");
                subjectSet = true;
            } else if (init_tag3.startsWith("科目代码")) {
                subjectCode = init_tag3.replaceAll("科目代码：", "");
                subjectSet = true;
            }
        }
        return this;
    }

    private FragmentAddEvent setInitialDate(int week, int dow) {
        this.week = week;
        weeks.add(week);
        this.dow = dow;
        timeSet = true;
        timeSet_course = true;
        return this;
    }

    public FragmentAddEvent setEditEvent(EventItem eventItem) {
        String typeS = "arrangement";
        switch (eventItem.getEventType()) {
            case DDL:
                typeS = "ddl";
                break;
            case ARRANGEMENT:
                typeS = "arrangement";
                break;
            case COURSE:
                typeS = "course";
                break;
            case EXAM:
                typeS = "exam";
                break;
        }
        EditMode = true;
        EditEvent = eventItem;
        init_all_day = eventItem.isWholeDay();
        return setInitialType(typeS)
                .setInitialName(eventItem.getMainName())
                .setInitialExtraName(eventItem.getMainName())
                .setExtraEditable(true)
                .setInitialTag2(eventItem.getTag2())
                .setInitialTag3(eventItem.getTag3())
                .setInitialDate(eventItem.getWeek(), eventItem.getDOW())
                .setInitialFromTime(eventItem.getStartTime())
                .setInitialToTime(eventItem.getEndTime());
    }

    @SuppressLint("SetTextI18n")
    private void refreshTimeBlock() {
        if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_course) {
            if (timeSet_course) {
                pickTime.setCardBackgroundColor(((BaseActivity) requireActivity()).getColorAccent());
                pickTimeIcon.setColorFilter(((BaseActivity) requireActivity()).getColorAccent());
                pickTimeText.setTextColor(((BaseActivity) requireActivity()).getColorAccent());
                pickTimeText.setText(getResources().getStringArray(R.array.dow1)[dow - 1] + " " + begin + "-" + (begin + last - 1));
            } else {
                pickTimeText.setText(getString(R.string.ade_set_time_period));
                pickTime.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_control_normal));
                pickTimeText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.text_color_secondary));
                pickTimeIcon.clearColorFilter();
                //pickTimeIcon.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.color_control_normal), PorterDuff.Mode.SRC_IN);
            }
            refreshFabState();
            return;
        }
        if (timeSet) {

            pickTime.setCardBackgroundColor(((BaseActivity) requireActivity()).getColorAccent());
            pickTimeIcon.setColorFilter(((BaseActivity) requireActivity()).getColorAccent());
            pickTimeText.setTextColor(((BaseActivity) requireActivity()).getColorAccent());
            // pickTimeBG.setColorFilter(((BaseActivity)requireActivity()).getColorAccent());
//            String weekTempl = getString(R.string.week);
//            String[] dows = getResources().getStringArray(R.array.dow1);
            if (wholeDaySwitch.isChecked()) {

                pickTimeText.setText(EventsUtils.getWeekDowString(week, dow, true, EventsUtils.TTY_REPLACE | EventsUtils.TTY_WK_REPLACE));
            } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_ddl) {
                pickTimeText.setText(EventsUtils.getWeekDowString(week, dow, true, EventsUtils.TTY_REPLACE | EventsUtils.TTY_WK_REPLACE) + " " + fromT.tellTime());
            } else {
                pickTimeText.setText(EventsUtils.getWeekDowString(week, dow, true, EventsUtils.TTY_REPLACE | EventsUtils.TTY_WK_REPLACE) + " " + fromT.tellTime() + "-" + toT.tellTime());
            }
        } else {
            pickTime.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_control_normal));
            pickTimeText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.text_color_secondary));
            pickTimeIcon.clearColorFilter();
            //pickTimeIcon.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.color_control_normal));
            if (wholeDaySwitch.isChecked()) {
                pickTimeText.setText(getString(R.string.ade_set_date));
            } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.ade_ddl) {
                pickTimeText.setText(getString(R.string.ade_set_time_date));
            } else {
                pickTimeText.setText(getString(R.string.ade_set_time_period));
            }
        }
        refreshFabState();
    }

    private void refreshTaskBlock() {
        if (taskSet && task != null) {

            pickTaskIcon.setColorFilter(((BaseActivity) requireActivity()).getColorAccent());
            pickTaskText.setTextColor(((BaseActivity) requireActivity()).getColorAccent());
            pickTask.setCardBackgroundColor(((BaseActivity) requireActivity()).getColorAccent());
            pickTaskText.setText(task.name);
            pickTaskCancel.setVisibility(View.VISIBLE);
        } else {
            pickTaskText.setText(getString(R.string.ade_pick_task));
            pickTask.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_control_normal));
            pickTaskText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.text_color_secondary));
            // pickTaskIcon.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.color_control_normal));
            pickTaskIcon.clearColorFilter();
            pickTaskCancel.setVisibility(View.GONE);
        }
        refreshFabState();
    }

    private void refreshTeacherBlock() {
        if (teacherSet && teacher != null) {

            pickTeacherIcon.setColorFilter(((BaseActivity) requireActivity()).getColorAccent());
            pickTeacherText.setTextColor(((BaseActivity) requireActivity()).getColorAccent());
            pickTeacher.setCardBackgroundColor(((BaseActivity) requireActivity()).getColorAccent());
            pickTeacherText.setText(teacher);
            pickTeacherCancel.setVisibility(View.VISIBLE);
        } else {
            pickTeacherText.setText(getString(R.string.ade_pick_teacher));
            pickTeacher.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_control_normal));
            pickTeacherText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.text_color_secondary));
            // pickTeacherIcon.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.color_control_normal));
            pickTeacherCancel.setVisibility(View.GONE);
            pickTeacherIcon.clearColorFilter();
        }
        refreshFabState();
    }

    private void refreshLocationBlock() {
        if (locationSet) {
            pickLocationIcon.setColorFilter(((BaseActivity) requireActivity()).getColorAccent());
            pickLocationText.setTextColor(((BaseActivity) requireActivity()).getColorAccent());
            pickLocation.setCardBackgroundColor(((BaseActivity) requireActivity()).getColorAccent());
            pickLocationText.setText(locationStr);
            pickLocationCancel.setVisibility(View.VISIBLE);

        } else {
            pickLocationText.setText(getString(R.string.ade_pick_location));
            pickLocationText.postInvalidate();
            pickLocation.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_control_normal));
            pickLocationText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.text_color_secondary));
            pickLocationIcon.clearColorFilter();
            //pickLocationIcon.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.color_control_normal), PorterDuff.Mode.SRC_IN);
            pickLocationCancel.setVisibility(View.GONE);
        }
        refreshFabState();
    }

    private void refreshExamBlock() {
        if (subjectSet && subjectName != null) {
            pickSubjectIcon.setColorFilter(((BaseActivity) requireActivity()).getColorAccent());
            pickSubjectText.setTextColor(((BaseActivity) requireActivity()).getColorAccent());
            pickSubject.setCardBackgroundColor(((BaseActivity) requireActivity()).getColorAccent());
            pickSubjectText.setText(subjectName);
        } else {
            pickTaskText.setText(getString(R.string.ade_pick_subject));
        }
        refreshFabState();
    }

    private void setFromTime(int hour, int minute) {
        fromT.hour = hour;
        fromT.minute = minute;
    }

    private void setToTime(int hour, int minute) {
        toT.hour = hour;
        toT.minute = minute;
    }

    private void sendRefreshMessages(int[] weeks) {
        Intent i = new Intent();
        if (weeks != null && weeks.length > 0) {
            if (weeks.length == 1) i.putExtra("week", weeks[0]);
            else i.putExtra("weeks", weeks);
        }
        i.setAction(TIMETABLE_CHANGED);
        //Intent i2 = new Intent();
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(i);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask bt, Boolean[] params, Object o) {
        switch (id) {
            case "course":
                if (o instanceof Boolean && (boolean) o) {
                    Toast.makeText(HContext, R.string.new_subject_created, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HContext, R.string.add_course_done, Toast.LENGTH_SHORT).show();
                }
                ActivityMain.saveData();
                int[] arg = new int[weeks.size()];
                for (int i = 0; i < arg.length; i++) arg[i] = weeks.get(i);
                sendRefreshMessages(arg);
                dismiss();
                break;
            case "event":
                if (o instanceof List) {
                    List eis = (List) o;
                    String[] dialogItems = new String[eis.size()];
                    for (int i = 0; i < eis.size(); i++) {
                        EventItem ei = (EventItem) eis.get(i);
                        dialogItems[i] = ei.getMainName() + " " + ei.getStartTime().tellTime() + "-" + ei.getEndTime().tellTime();
                    }
                    AlertDialog ad = new AlertDialog.Builder(requireContext()).setTitle("事件时间与以下事件重叠：").setItems(dialogItems, null).setPositiveButton("修改时间", null).create();
                    ad.show();
                } else {
                    int hint = EditMode ? R.string.edit_event_done : R.string.add_event_done;
                    Toast.makeText(HContext, hint, Toast.LENGTH_SHORT).show();
                    ActivityMain.saveData();
                    sendRefreshMessages(new int[]{week});
                    dismiss();
                }
                break;
            case "task":
                if (o instanceof Boolean) {
                    showTasksDialogTask st = (showTasksDialogTask) bt;
                    String[] res = st.res;
                    final List<Task> tasks = st.tasks;
                    boolean b = (boolean) o;
                    if (b) {
                        AlertDialog dialog = new AlertDialog.Builder(FragmentAddEvent.this.requireContext()).
                                setTitle(R.string.ade_pick_task)
                                .setItems(res, new DialogInterface.OnClickListener() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        taskSet = true;
                                        task = tasks.get(which);
                                        name.setText("处理任务：" + task.name);
                                        if (fromT.getDuration(toT) > task.getLength()) {
                                            Toast.makeText(HContext, "时长超过任务时长！", Toast.LENGTH_SHORT).show();
                                            toT = new HTime(TimetableCore.getNow());
                                        }
                                        refreshTaskBlock();
                                    }
                                }).create();
                        dialog.show();
                    } else {
                        Toast.makeText(requireContext(), "没有待处理的有时长任务！", Toast.LENGTH_SHORT).show();
                    }
                }


        }

    }


    static class showTasksDialogTask extends BaseOperationTask<Object> {
        ArrayList<Task> tasks;
        String[] res;
        int[] dealtTime;

        showTasksDialogTask(OperationListener<?> listRefreshedListener) {
            super(listRefreshedListener);
            id = "task";
        }


        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            tasks = TimetableCore.getInstance(HContext).getUnfinishedTaskWithLength();
            List<Task> toRemove = new ArrayList<>();
            for (Task t : tasks) {
                int left = t.getLength() - t.getDealtTime_All();
                if (left <= 0) {
                    toRemove.add(t);
                }
            }
            tasks.removeAll(toRemove);
            res = new String[tasks.size()];
            dealtTime = new int[tasks.size()];
            for (int i = 0; i < tasks.size(); i++) {
                int dealt = tasks.get(i).getDealtTime_All();
                dealtTime[i] = dealt;
                // Log.e("task:",tasks.get(i).name+",all:"+tasks.get(i).getLength()+",dealt:"+delt);
                res[i] = tasks.get(i).name;
            }
            return res.length != 0;
        }

//        @Override
//        protected void onPostExecute(OperationListener<Object> listRefreshedListener, Object ts) {
//            super.onPostExecute(listRefreshedListener, ts);
//            listRefreshedListener.onOperationDone(id, params, Triple.of(tasks, res, ts));
//        }
    }

    static class addEventTask extends BaseOperationTask<Object> {
        String curriculumCode;
        int type;
        String eventName;
        String tag2;
        String tag3;
        String tag4;
        HTime start;
        HTime end;
        int week;
        int DOW;
        boolean isWholeDay;
        String uuid;

        EventItem eventItem = null;

        addEventTask(OperationListener<Object> listener,
                     String curriculumCode, int type, String eventName, String tag2, String tag3, HTime start, HTime end, int week, int DOW, boolean isWholeDay) {
            super(listener);
            this.curriculumCode = curriculumCode;
            this.type = type;
            this.eventName = eventName;
            this.tag2 = tag2;
            this.tag3 = tag3;
            this.start = start;
            this.end = end;
            this.week = week;
            this.DOW = DOW;
            this.isWholeDay = isWholeDay;
            id = "event";
        }

        addEventTask(OperationListener<Object> listener,
                     String curriculumCode, int type, String eventName, String tag2, String tag3, HTime start, HTime end, int week, int DOW, boolean isWholeDay, EventItem editItem) {
            super(listener);
            this.curriculumCode = curriculumCode;
            this.type = type;
            this.eventName = eventName;
            this.tag2 = tag2;
            this.tag3 = tag3;
            this.start = start;
            this.end = end;
            this.week = week;
            this.DOW = DOW;
            this.isWholeDay = isWholeDay;
            eventItem = editItem;
            id = "event";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            if (type == EXAM) {
                //tag4 = TextUtils.isEmpty(subjectCode)?subjectName:subjectCode;
                tag4 = start.tellTime() + "-" + end.tellTime();
            }
            int[] types_length = new int[]{TimetableCore.EXAM
                    , TimetableCore.COURSE, TimetableCore.ARRANGEMENT, TimetableCore.DYNAMIC};
            List<EventItem> overlapEvents = TimetableCore.getInstance(HContext).getEventFrom_typeLimit(week, DOW, start, week, DOW, end, types_length);
            List<EventItem> toRemove = new ArrayList<>();
            for (EventItem ei : overlapEvents) {
                if (ei.isWholeDay() || !ei.hasCross_Strict(end) && !ei.hasCross(start))
                    toRemove.add(ei);
            }
            overlapEvents.removeAll(toRemove);
            if (eventItem == null && !isWholeDay && contains_integer(types_length, type) && overlapEvents.size() > 0) {
                return overlapEvents;
            } else {
                EventItem toAdd = new EventItem(null, TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, start, end, week, DOW, isWholeDay);
                if (eventItem != null) {
                    toAdd.setUuid(eventItem.getUuid());
                }
                uuid = TimetableCore.getInstance(HContext).addEvent(toAdd);
//                if (dealWithTask && task != null && taskSet)
//                    task.putEventMap(uuid+":::"+toAdd.week, false);
//                if (type == TimetableCore.DDL) {
//                    ddl_task.setDdlName(uuid, week + "");
//                    TimetableCore.getInstance(HContext).addTask(ddl_task);
//                }
                return null;
            }
        }


    }

    static class addCourseTask extends BaseOperationTask<Object> {
        String curriculumCode;
        List<Integer> weeks;
        String teacher;
        String location;
        String name;
        int from;
        int last;
        int dow;
        boolean newSubject = false;


        addCourseTask(OperationListener<Object> listener, String curriculumCode, List<Integer> weeks, String name, String location, String teacher, int from, int last, int dow) {
            super(listener);
            this.curriculumCode = curriculumCode;
            this.weeks = weeks;
            this.teacher = teacher;
            this.location = location;
            this.name = name;
            this.from = from;
            this.last = last;
            this.dow = dow;
            id = "course";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < last; i++) {
                sb.append(i + from);
                if (i != last - 1) sb.append(",");
            }
            if (TimetableCore.getInstance(HContext).getSubjectByName(null, name) == null) {
                newSubject = true;
                Subject s = new Subject(TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode(), name, teacher);
                TimetableCore.getInstance(HContext).insertSubject(s);
            }
            TimetableCore.getInstance(HContext).addEvents(weeks, dow, COURSE, name, location, teacher, sb.toString(), from, last, false);
            return newSubject;
        }
    }
}
