package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.TimeTableGenerator;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.diy.HDatePickerDialog;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.getDataState;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.activities.ActivityMain.app_task_enabled;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.core.TimeTable.contains_integer;

@SuppressLint("ValidFragment")
public class FragmentAddEvent extends BottomSheetDialogFragment {
    private boolean dateSet = false, fromTSet = false, toTSet = false, courseSet = false, taskSet = false;
    private RadioGroup mRadioGroup;
    private EditText name, tag2, tag3;
    private TextView fromTimeShow, toTimeShow, dateShow, courseShow, examPlace, taskShow;
    FloatingActionButton done;
    private String subjectCode,subjectName;
    private Task task;
    private LinearLayout pickToTimeLayout, pickFromTimeLayout, pickCourseLayout, nameLayout, pickTaskLayout;
    private ExpandableLayout mExpandableLayout;
    private Switch wholeDaySwitch, autoAllocation, dealWithTask;
    private HTime fromT, toT;
    private int week = 1;
    private int dow = 1;

    public FragmentAddEvent() {

    }

    public static FragmentAddEvent newInstance() {
        return new FragmentAddEvent();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromT = new HTime(now);
        toT = new HTime(now);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_add_event, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        dateSet = false;
        fromTSet = false;
        toTSet = false;
        courseSet = false;
        taskSet = false;
        initViews(view);
        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    private void initViews(View v) {
        mRadioGroup = v.findViewById(R.id.ade_radiogroup);
        mRadioGroup.check(R.id.ade_arrange);
        name = v.findViewById(R.id.ade_name);
        tag2 = v.findViewById(R.id.ade_tag2);
        tag3 = v.findViewById(R.id.ade_tag3);
        dealWithTask = v.findViewById(R.id.ade_switch_dealwithtask);
        if(!app_task_enabled) dealWithTask.setVisibility(View.GONE);
        taskShow = v.findViewById(R.id.ade_choose_deal_task);
        nameLayout = v.findViewById(R.id.ade_namelayout);
        pickCourseLayout = v.findViewById(R.id.ade_courselayout);
        pickTaskLayout = v.findViewById(R.id.ade_picktasklayout);
        mExpandableLayout = v.findViewById(R.id.ade_expandlayout);
        ImageView bt_expand = v.findViewById(R.id.ade_expand_button);
        fromTimeShow = v.findViewById(R.id.ade_time_from_show);
        toTimeShow = v.findViewById(R.id.ade_time_to_show);
        dateShow = v.findViewById(R.id.ade_date_show);
        courseShow = v.findViewById(R.id.ade_text_course);
        examPlace = v.findViewById(R.id.ade_nexam_place);
        courseShow.setText("选择考试科目");
        taskShow.setText("选择任务");
        ImageView pickFromTime = v.findViewById(R.id.ade_bt_picktime_from);
        ImageView pickToTime = v.findViewById(R.id.ade_bt_picktime_to);
        ImageView pickDate = v.findViewById(R.id.ade_bt_pickdate);
        ImageView pickCourse = v.findViewById(R.id.ade_pick_course_button);
        ImageView pickTask = v.findViewById(R.id.ade_pick_task_button);
        done = v.findViewById(R.id.ade_bt_done);
        pickToTimeLayout = v.findViewById(R.id.ade_picktotimelayout);
        pickFromTimeLayout = v.findViewById(R.id.ade_pickfromtimelayout);
        wholeDaySwitch = v.findViewById(R.id.ade_switch_wholeday);
        autoAllocation = v.findViewById(R.id.ade_switch_autoallocation);
        pickTaskLayout.setVisibility(View.GONE);
        autoAllocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!dateSet){
                    Toast.makeText(HContext,"请先设置日期！",Toast.LENGTH_SHORT).show();
                    autoAllocation.setChecked(!isChecked);
                    return;
                }
                if (isChecked) {
                    SparseArray<HTime> times = TimeTableGenerator.autoAdd_getTime(now,week,dow,40);
                    if(times!=null){
                        setFromTime(times.get(0).hour,times.get(0).minute);
                        setToTime(times.get(1).hour,times.get(1).minute);
                    }else{
                        Toast.makeText(HContext,"没有找到合适的分配时间！",Toast.LENGTH_SHORT).show();
                        autoAllocation.setChecked(false);
                        return;
                    }
                    wholeDaySwitch.setChecked(false);
                    wholeDaySwitch.setVisibility(View.GONE);
                } else {
                    wholeDaySwitch.setChecked(false);
                    wholeDaySwitch.setVisibility(View.VISIBLE);

                }
            }
        });
        wholeDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pickTaskLayout.setVisibility(View.GONE);
                    dealWithTask.setVisibility(View.GONE);
                    autoAllocation.setChecked(false);
                    autoAllocation.setVisibility(View.GONE);
                    pickToTimeLayout.setVisibility(View.GONE);
                    pickFromTimeLayout.setVisibility(View.GONE);
                } else {
                    dealWithTask.setChecked(false);
                    pickTaskLayout.setVisibility(View.GONE);
                    autoAllocation.setChecked(false);
                    autoAllocation.setVisibility(View.VISIBLE);
                    switch (mRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.ade_arrange:
                            pickFromTimeLayout.setVisibility(View.VISIBLE);
                            pickToTimeLayout.setVisibility(View.VISIBLE);
                            if(app_task_enabled)dealWithTask.setVisibility(View.VISIBLE);
                            break;
                        case R.id.ade_remind:
                            pickFromTimeLayout.setVisibility(View.VISIBLE);
                            pickToTimeLayout.setVisibility(View.GONE);
                            break;
                        case R.id.ade_ddl:
                            pickFromTimeLayout.setVisibility(View.VISIBLE);
                            pickToTimeLayout.setVisibility(View.GONE);
                            break;
                    }
                }
            }
        });
        bt_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableLayout.toggle();
            }
        });
        pickCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new showSubjectsDialogTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        pickTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new showTasksDialogTask().execute();
            }
        });
        dealWithTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) pickTaskLayout.setVisibility(View.VISIBLE);
                else pickTaskLayout.setVisibility(View.GONE);
            }
        });

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                wholeDaySwitch.setChecked(false);
                autoAllocation.setChecked(false);
                switch (checkedId) {
                    case R.id.ade_arrange:
                        pickTaskLayout.setVisibility(View.GONE);
                        if(app_task_enabled) dealWithTask.setVisibility(View.VISIBLE);
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        autoAllocation.setVisibility(View.VISIBLE);
                        pickCourseLayout.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        pickFromTimeLayout.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_remind:
                        pickTaskLayout.setVisibility(View.GONE);
                        dealWithTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        autoAllocation.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.GONE);
                        pickCourseLayout.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_ddl:
                        pickTaskLayout.setVisibility(View.GONE);
                        dealWithTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        autoAllocation.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.GONE);
                        pickCourseLayout.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_exam:
                        pickTaskLayout.setVisibility(View.GONE);
                        dealWithTask.setVisibility(View.GONE);
                        pickFromTimeLayout.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.VISIBLE);
                        wholeDaySwitch.setVisibility(View.GONE);
                        autoAllocation.setVisibility(View.GONE);
                        pickCourseLayout.setVisibility(View.VISIBLE);
                        nameLayout.setVisibility(View.GONE);
                        mExpandableLayout.setVisibility(View.GONE);
                        break;

                }
            }
        });
        pickFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog TPD = new TimePickerDialog(FragmentAddEvent.this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setFromTime(hourOfDay,minute);
                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                TPD.create();
                TPD.show();

            }
        });
        pickToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog TPD = new TimePickerDialog(FragmentAddEvent.this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        setToTime(hourOfDay,minute);
                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                TPD.create();
                TPD.show();
            }
        });

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HDatePickerDialog dlg = new HDatePickerDialog((BaseActivity) FragmentAddEvent.this.getActivity(), dateShow);
                dlg.setOnDialogConformListener(new HDatePickerDialog.onDialogConformListener() {
                    @Override
                    public void onClick(int week, int dow, boolean dateSet) {

                        FragmentAddEvent.this.dateSet = dateSet;
                        FragmentAddEvent.this.dow = dow;
                        FragmentAddEvent.this.week = week;
                        //Log.e("!!",week+"x"+dow);
                    }
                });
                dlg.showDatePickerDialog();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = 0;
                switch (mRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.ade_remind:
                        type = TimeTable.TIMETABLE_EVENT_TYPE_REMIND;
                        break;
                    case R.id.ade_arrange:
                        type = TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT;
                        break;
                    case R.id.ade_ddl:
                        type = TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;
                        break;
                    case R.id.ade_exam:
                        type = TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
                        break;
                }
                if (type != TimeTable.TIMETABLE_EVENT_TYPE_EXAM && TextUtils.isEmpty(name.getText())) {
                    Toast.makeText(HContext, "请输入事件名称!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!dateSet) {
                    Toast.makeText(HContext, "请设置事件日期！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!fromTSet && pickFromTimeLayout.getVisibility() == View.VISIBLE) {
                    Toast.makeText(HContext, "请设置开始时间！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!toTSet && pickToTimeLayout.getVisibility() == View.VISIBLE) {
                    Toast.makeText(HContext, "请设置结束时间！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type != TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE && type != TimeTable.TIMETABLE_EVENT_TYPE_REMIND && fromT.compareTo(toT) > 0) {
                    Toast.makeText(HContext, "请输入正确的事件时间！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type == TimeTable.TIMETABLE_EVENT_TYPE_EXAM && !courseSet) {
                    Toast.makeText(HContext, "请选择考试科目！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (dealWithTask.isChecked() && !taskSet) {
                    Toast.makeText(HContext, "请选择处理的任务！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (dealWithTask.isChecked() && taskSet && fromTSet && toTSet && fromT.getDuration(toT) > task.getLength()) {
                    Toast.makeText(HContext, "时长超过任务时长！", Toast.LENGTH_SHORT).show();
                    toTSet = true;
                    toT = fromT.getAdded(task.getLength());
                    toTimeShow.setText(toT.tellTime());
                    // toTimeShow.setTextColor(ContextCompat.getColor(HContext,R.color.material_secondary_text));
                    return;
                }
                String Tname, Ttag2, Ttag3;
                if (type == TimeTable.TIMETABLE_EVENT_TYPE_EXAM) {
                    Ttag2 = examPlace.getText().toString().isEmpty() ? "" : examPlace.getText().toString();
                    if(TextUtils.isEmpty(subjectCode)){
                        Ttag3 = "科目名称：" + subjectName;
                    }else Ttag3 = "科目代码："+subjectCode;

                    Tname = courseShow.getText() + "考试";
                    //  Ttag4 = fromT.tellTime()+"-"+toT.tellTime();
                } else {
                    Tname = name.getText().toString().isEmpty() ? "" : name.getText().toString();
                    Ttag2 = tag2.getText().toString().isEmpty() ? "" : tag2.getText().toString();
                    Ttag3 = tag3.getText().toString().isEmpty() ? "" : tag3.getText().toString();
                    // Ttag4 = tag4.getText().toString().isEmpty()?"":tag4.getText().toString();
                }

//                if (autoAllocation.isChecked()) {
//                    uuid = TimeTableGenerator.autoAdd(week, dow, Tname, Ttag2, Ttag3, tag4, type, 120);
//                } else {
                HTime tempToTime = type == TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT || type == TimeTable.TIMETABLE_EVENT_TYPE_EXAM ? toT : fromT;
                new addEventTask(mainTimeTable.core.curriculumCode,
                        type,Tname,Ttag2,Ttag3,fromT,tempToTime,week,dow,wholeDaySwitch.isChecked(),dealWithTask.isChecked()).execute();
            }
        });


    }


    private void setFromTime(int hour,int minute){
        fromT.hour = hour;
        fromT.minute = minute;
        fromTimeShow.setText(fromT.tellTime());
        fromTimeShow.setTextColor(((BaseActivity) Objects.requireNonNull(FragmentAddEvent.this.getActivity())).getColorPrimary());
        fromTSet = true;
    }

    private void setToTime(int hour,int minute){
        toT.hour = hour;
        toT.minute = minute;
        toTimeShow.setText(toT.tellTime());
        toTimeShow.setTextColor(((BaseActivity) Objects.requireNonNull(FragmentAddEvent.this.getActivity())).getColorPrimary());
        toTSet = true;
    }
    private void sendRefreshMessages() {
        Intent tlr = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(tlr);
        Intent tlr2 = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(tlr2);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }

    @SuppressLint("StaticFieldLeak")
    class showSubjectsDialogTask extends AsyncTask {
        ArrayList<Subject> subjects;
        String[] res;

        @Override
        protected Object doInBackground(Object[] objects) {
            subjects = allCurriculum.get(thisCurriculumIndex).getSubjects();
            res = new String[subjects.size()];
            for (int i = 0; i < subjects.size(); i++) {
                res[i] = subjects.get(i).name;
            }
            return null;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            AlertDialog dialog = new AlertDialog.Builder(FragmentAddEvent.this.getContext()).
                    setTitle("选择考试科目")
                    .setItems(res, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            courseShow.setText(res[which]);
                            courseSet = true;
                            courseShow.setTextColor(((BaseActivity) FragmentAddEvent.this.getActivity()).getColorPrimary());
                            subjectCode = subjects.get(which).code;
                            subjectName = subjects.get(which).name;
                        }
                    }).create();
            dialog.show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class showTasksDialogTask extends AsyncTask {
        ArrayList<Task> tasks;
        String[] res;
        int[] dealtTime;

        @Override
        protected Object doInBackground(Object[] objects) {
            tasks = mainTimeTable.getUnfinishedTaskWithLength();
            res = new String[tasks.size()];
            dealtTime = new int[tasks.size()];
            for (int i = 0; i < tasks.size(); i++) {
                int dealt = tasks.get(i).getDealtTime_All();
                dealtTime[i] = dealt;
               // Log.e("task:",tasks.get(i).name+",all:"+tasks.get(i).getLength()+",dealt:"+delt);
                res[i] = tasks.get(i).name;
            }
            if(res.length==0) return false;
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if((Boolean)o){
                AlertDialog dialog = new AlertDialog.Builder(FragmentAddEvent.this.getContext()).
                        setTitle("选择任务")
                        .setItems(res, new DialogInterface.OnClickListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                taskShow.setText(res[which]);
                                taskSet = true;
                                taskShow.setTextColor(((BaseActivity) Objects.requireNonNull(FragmentAddEvent.this.getActivity())).getColorPrimary());
                                task = tasks.get(which);
                                name.setText("处理任务：" + task.name );
                                if (fromTSet && toTSet && fromT.getDuration(toT) > task.getLength()) {
                                    Toast.makeText(HContext, "时长超过任务时长！", Toast.LENGTH_SHORT).show();
                                    toTSet = false;
                                    toT = new HTime(now);
                                    toTimeShow.setText("设置结束时间");
                                    toTimeShow.setTextColor(ContextCompat.getColor(HContext, R.color.material_secondary_text));
                                }
                            }
                        }).create();
                dialog.show();
            }else{
               Toast.makeText(getContext(),"没有待处理的有时长任务！",Toast.LENGTH_SHORT).show();
            }

        }
    }

    class addEventTask extends AsyncTask {
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
        Task ddl_task = null;
        boolean dealWithTask;

        public addEventTask(String curriculumCode, int type, String eventName, String tag2, String tag3,  HTime start, HTime end, int week, int DOW, boolean isWholeDay
        ,boolean dealWithTask) {
            this.curriculumCode = curriculumCode;
            this.type = type;
            this.eventName = eventName;
            this.tag2 = tag2;
            this.tag3 = tag3;
            this.start = start;
            this.end = end;
            this.week = week;
            this.DOW = DOW;
            this.dealWithTask = dealWithTask;
            this.isWholeDay = isWholeDay;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (dealWithTask && taskSet && task != null) {
                tag4 = task.getUuid();
                tag3 = "任务处理事件";
            } else if (type == TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE) {
                ddl_task = new Task(mainTimeTable.core.curriculumCode, "处理DDL:" + name.getText().toString(), allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now), TimeTable.getDOW(now),
                        new HTime(now), week, dow, toT, "");
                tag4 = ddl_task.getUuid();
            } else if(type==TIMETABLE_EVENT_TYPE_EXAM){
                tag4 = TextUtils.isEmpty(subjectCode)?subjectName:subjectCode;
            }
            int[] types_length = new int[]{TimeTable.TIMETABLE_EVENT_TYPE_EXAM
                    ,TimeTable.TIMETABLE_EVENT_TYPE_COURSE,TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT,TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC};
            List<EventItem> overlapEvents = mainTimeTable.getEventFrom_typeLimit(week,dow,start,week,dow,end,types_length);
            if(!isWholeDay&&contains_integer(types_length,type)&&overlapEvents.size()>0){
                return overlapEvents;
            }else{
                EventItem toAdd = new EventItem(null, allCurriculum.get(thisCurriculumIndex).curriculumCode,type, eventName, tag2, tag3, tag4, start,end, week, dow, isWholeDay);
                uuid = mainTimeTable.addEvent(toAdd);
                if (dealWithTask && task != null && taskSet)
                    task.putEventMap(uuid+":::"+toAdd.week, false);
                if (type == TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE) {
                    ddl_task.setDdlName(uuid, week + "");
                    mainTimeTable.addTask(ddl_task);
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o!=null&&o instanceof List){
                List<EventItem> eis = (List<EventItem>) o;
                String[] dialogItems = new String[eis.size()];
                for(int i = 0;i<eis.size();i++){
                    dialogItems[i] = eis.get(i).mainName+" "+eis.get(i).startTime.tellTime()+"-"+eis.get(i).endTime.tellTime();
                }
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("事件时间与以下事件重叠：").setItems(dialogItems,null).setPositiveButton("修改时间",null).create();
                ad.show();
            }else{
                ActivityMain.saveData();
                sendRefreshMessages();
                if(!this.isCancelled())dismiss();
            }

        }
    }

}
