package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityTimeTable;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.TimeTableGenerator;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.diy.PickCourseTimeDialog;
import com.stupidtree.hita.diy.PickInfoDialog;
import com.stupidtree.hita.diy.PickSingleTimeDialog;
import com.stupidtree.hita.diy.PickTimePeriodDialog;
import com.stupidtree.hita.hita.TextTools;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.activities.ActivityMain.app_task_enabled;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.core.TimeTable.contains_integer;

@SuppressLint("ValidFragment")
public class FragmentAddEvent extends BottomSheetDialogFragment {
    private boolean timeSet = false, subjectSet = false, taskSet = false,locationSet = false;
    private boolean timeSet_course = false;
    private RadioGroup mRadioGroup;
    private EditText name, tag2, tag3,extra;
    //private TextView  examPlace;
    private String locationStr = "";
    FloatingActionButton done;
    private String subjectCode,subjectName;
    private Task task;
    private LinearLayout nameLayout;
  //  pickLocation,
    private ExpandableLayout mExpandableLayout;
    private Switch wholeDaySwitch;
    //autoAllocation
    private HTime fromT, toT;
    private int week = 1;
    private int dow = 1;
    private int initIndex = 0;
    List<Integer> weeks = new ArrayList<Integer>();
    private int begin,last;
    CardView pickTime;
    ImageView pickTimeIcon;
    TextView pickTimeText;
    CardView pickTask;
    ImageView pickTaskIcon;
    TextView pickTaskText;
    ImageView pickTaskCancel;
    CardView pickSubject;
    ImageView pickSubjectIcon;
    TextView pickSubjectText;
    CardView pickLocation;
    ImageView pickLocationIcon;
    TextView pickLocationText;
    ImageView pickLocationCancel;
    OnFragmentInteractionListener onFragmentInteractionListener;
    public FragmentAddEvent() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof  OnFragmentInteractionListener) onFragmentInteractionListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentInteractionListener = null;
    }

    public void showFor(FragmentManager fragmentManager, int index){
        this.initIndex = index;
        this.show(fragmentManager,"fae");
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
        timeSet = false;
        subjectSet = false;
        taskSet = false;
        initViews(view);
        int initCheckedId = R.id.ade_arrange;
        switch(initIndex){
            case 0:initCheckedId = R.id.ade_arrange;break;
            case 1:initCheckedId = R.id.ade_ddl;break;
            case 2:initCheckedId = R.id.ade_exam;break;
        }
       mRadioGroup.check(initCheckedId);
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
        pickLocationText = v.findViewById(R.id.pick_location_text);
        pickLocation = v.findViewById(R.id.pick_location);
        if(!app_task_enabled) pickTask.setVisibility(View.GONE);
        nameLayout = v.findViewById(R.id.ade_namelayout);
        mExpandableLayout = v.findViewById(R.id.ade_expandlayout);
        ImageView bt_expand = v.findViewById(R.id.ade_expand_button);
        //examPlace = v.findViewById(R.id.ade_nexam_place);
        done = v.findViewById(R.id.ade_bt_done);
        wholeDaySwitch = v.findViewById(R.id.ade_switch_wholeday);
       // autoAllocation = v.findViewById(R.id.ade_switch_autoallocation);
        pickTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!timeSet){
                    Toast.makeText(HContext,"请先设置日期！",Toast.LENGTH_SHORT).show();
                    //autoAllocation.setChecked(!isChecked);
                    return false;
                }
                    SparseArray<HTime> times = TimeTableGenerator.autoAdd_getTime(now,week,dow,25);
                    if(times!=null){
                        setFromTime(times.get(0).hour,times.get(0).minute);
                        setToTime(times.get(1).hour,times.get(1).minute);
                    }else{
                        Toast.makeText(HContext,"没有找到合适的分配时间！",Toast.LENGTH_SHORT).show();
                        //autoAllocation.setChecked(false);
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
                            if(app_task_enabled) pickTask.setVisibility(View.VISIBLE);
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
            }
        });
        pickSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new showSubjectsDialogTask().executeOnExecutor(HITAApplication.TPE);
            }
        });
        pickTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new showTasksDialogTask().executeOnExecutor(HITAApplication.TPE);;
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
        pickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickInfoDialog(getContext(), "选择地点", PickInfoDialog.LOCATION_ALL, new PickInfoDialog.OnPickListener() {
                    @Override
                    public void OnPick(String title, Object obj) {
                        locationSet = true;
                        locationStr = title;
                        refreshLocationBlock();
                    }
                }).show();
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                wholeDaySwitch.setChecked(false);
                //autoAllocation.setChecked(false);
                switch (checkedId) {
                    case R.id.ade_arrange:
                        if(false) pickTask.setVisibility(View.VISIBLE);
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                       // autoAllocation.setVisibility(View.VISIBLE);
                        pickLocation.setVisibility(View.GONE);
                        pickSubject.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        extra.setVisibility(View.GONE);
                        break;
                    case R.id.ade_ddl:
                        pickTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                       // autoAllocation.setVisibility(View.VISIBLE);
                        pickLocation.setVisibility(View.GONE);
                        pickSubject.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        extra.setVisibility(View.GONE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_exam:
                        pickTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.GONE);
                        //autoAllocation.setVisibility(View.GONE);
                        pickLocation.setVisibility(View.VISIBLE);
                        pickSubject.setVisibility(View.VISIBLE);
                        nameLayout.setVisibility(View.GONE);
                        extra.setVisibility(View.VISIBLE);
                        extra.setHint("输入考试名称...");
                        mExpandableLayout.setVisibility(View.GONE);
                        break;
                    case R.id.ade_course:
                        pickTask.setVisibility(View.GONE);
                        wholeDaySwitch.setVisibility(View.GONE);
                        //autoAllocation.setVisibility(View.GONE);
                        pickLocation.setVisibility(View.VISIBLE);
                        pickSubject.setVisibility(View.VISIBLE);
                        extra.setVisibility(View.VISIBLE);
                        extra.setHint("设置任课教师...");
                        nameLayout.setVisibility(View.GONE);
                        mExpandableLayout.setVisibility(View.GONE);
                        break;
                }
                refreshTimeBlock();
            }
        });

        pickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_ddl){
                    if(!wholeDaySwitch.isChecked()){
                        PickSingleTimeDialog pstd = new PickSingleTimeDialog((BaseActivity) FragmentAddEvent.this.getActivity(),new PickSingleTimeDialog.onDialogConformListener() {
                            @Override
                            public void onClick(int week, int dow, int hour,int minute,boolean dateSet) {
                                timeSet = dateSet;
                                if(dateSet){
                                    FragmentAddEvent.this.timeSet = dateSet;
                                    FragmentAddEvent.this.dow = dow;
                                    FragmentAddEvent.this.week = week;
                                    fromT.setTime(hour,minute);
                                    toT.setTime(hour,minute);
                                    refreshTimeBlock(); }
                                //Log.e("!!",week+"x"+dow);
                            }
                        });
                        if(timeSet) pstd.setInitialValue(week,dow,fromT);
                        pstd.show();
                    }else{
                        PickTimePeriodDialog ptpd = new PickTimePeriodDialog((BaseActivity) getActivity(), new PickTimePeriodDialog.onDialogConformListener() {
                            @Override
                            public void onClick(int week, int dow, int hour1, int minute1, int hour2, int minute2, boolean dateSet) {
                                timeSet = dateSet;
                                if(dateSet){
                                    FragmentAddEvent.this.timeSet = dateSet;
                                    FragmentAddEvent.this.dow = dow;
                                    FragmentAddEvent.this.week = week;
                                    refreshTimeBlock(); }
                            }
                        });
                        if(timeSet) ptpd.setInitialValue(week,dow,fromT,toT);
                        ptpd.dateOnly();;
                        ptpd.show();
                    }


                }else if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_course){
                    PickCourseTimeDialog pctd = new PickCourseTimeDialog((BaseActivity) getActivity(), new PickCourseTimeDialog.onDialogConformListener() {
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
                    if(timeSet_course) pctd.setInitialValue(weeks,dow,begin,last);
                    pctd.show();
                } else{
                    PickTimePeriodDialog ptpd = new PickTimePeriodDialog((BaseActivity) getActivity(), new PickTimePeriodDialog.onDialogConformListener() {
                        @Override
                        public void onClick(int week, int dow, int hour1, int minute1, int hour2, int minute2, boolean dateSet) {
                            timeSet = dateSet;
                            FragmentAddEvent.this.timeSet = FragmentAddEvent.this.timeSet;
                            FragmentAddEvent.this.dow = dow;
                            FragmentAddEvent.this.week = week;
                            fromT.setTime(hour1,minute1);
                            toT.setTime(hour2,minute2);
                            refreshTimeBlock();
                        }
                    });
                    if(timeSet) ptpd.setInitialValue(week,dow,fromT,toT);
                    if(wholeDaySwitch.isChecked()) ptpd.dateOnly();
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
                        type = TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT;
                        break;
                    case R.id.ade_ddl:
                        type = TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;
                        break;
                    case R.id.ade_exam:
                        type = TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
                        break;
                    case R.id.ade_course:
                        type = TIMETABLE_EVENT_TYPE_COURSE;
                        break;
                }
                if (type != TimeTable.TIMETABLE_EVENT_TYPE_EXAM && type!=TIMETABLE_EVENT_TYPE_COURSE&&TextUtils.isEmpty(name.getText())) {
                    Toast.makeText(HContext, "请输入事件名称!", Toast.LENGTH_SHORT).show();
                    return;
                } else if(type==TIMETABLE_EVENT_TYPE_EXAM&&TextUtils.isEmpty(extra.getText().toString())) {
                    Toast.makeText(HContext, "请输入考试名称!", Toast.LENGTH_SHORT).show();
                    return;
                }else if ((mRadioGroup.getCheckedRadioButtonId()!=R.id.ade_course&&!timeSet)
                ||(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_course&&!timeSet_course)
                ) {
                    Toast.makeText(HContext, "请设置事件时间", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type != TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE && type != TimeTable.TIMETABLE_EVENT_TYPE_REMIND && fromT.compareTo(toT) > 0) {
                    Toast.makeText(HContext, "请输入正确的事件时间！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type == TimeTable.TIMETABLE_EVENT_TYPE_EXAM && !subjectSet) {
                    Toast.makeText(HContext, "请选择考试科目！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (type == TIMETABLE_EVENT_TYPE_COURSE && !subjectSet) {
                    Toast.makeText(HContext, "请选择课程科目！", Toast.LENGTH_SHORT).show();
                    return;
                }else if (taskSet&&task!=null && fromT.getDuration(toT) > task.getLength()) {
                    Toast.makeText(HContext, "时长超过任务时长！", Toast.LENGTH_SHORT).show();
                    toT = fromT.getAdded(task.getLength());
                    // toTimeShow.setTextColor(ContextCompat.getColor(getActivity(),R.color.material_secondary_text));
                    return;
                }
                if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_course){
                    new  addCourseTask(mainTimeTable.core.curriculumCode,weeks,
                            extra.getText().toString(),locationStr ,subjectName,begin,last,dow).executeOnExecutor(HITAApplication.TPE);;
                    return;
                }

                String Tname, Ttag2, Ttag3;
                if (type == TimeTable.TIMETABLE_EVENT_TYPE_EXAM) {
                    Ttag2 = TextUtils.isEmpty(locationStr)? "" : locationStr;
                    if(TextUtils.isEmpty(subjectCode)){
                        Ttag3 = "科目名称：" + subjectName;
                    }else Ttag3 = "科目代码："+subjectCode;

                    Tname = extra.getText().toString();
                     //Ttag4 = fromT.tellTime()+"-"+toT.tellTime();
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
                        type,Tname,Ttag2,Ttag3,fromT,tempToTime,week,dow,wholeDaySwitch.isChecked(),taskSet).executeOnExecutor(HITAApplication.TPE);;
            }
        });


    }

    private void refreshTimeBlock(){
        if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_course){
            if(timeSet_course){
                pickTime.setCardBackgroundColor(((BaseActivity)getActivity()).getColorPrimary());
                pickTimeIcon.setColorFilter(((BaseActivity)getActivity()).getColorPrimary());
                pickTimeText.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
                pickTimeText.setText(TextTools.words_time_DOW[dow-1]+begin+"-"+(begin+last-1)+"节");
            }else{
                pickTimeText.setText("设置日期与时间段");
                pickTime.setCardBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
                pickTimeText.setTextColor(ContextCompat.getColor(getActivity(),R.color.text_color_secondary));
                pickTimeIcon.setColorFilter(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            }
            return;
        }
        if(timeSet){
            pickTime.setCardBackgroundColor(((BaseActivity)getActivity()).getColorPrimary());
            pickTimeIcon.setColorFilter(((BaseActivity)getActivity()).getColorPrimary());
            pickTimeText.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
           // pickTimeBG.setColorFilter(((BaseActivity)getActivity()).getColorPrimary());
            if(wholeDaySwitch.isChecked()){
                pickTimeText.setText(week+"周"+TextTools.words_time_DOW[dow-1]);
            }else if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_ddl){
                pickTimeText.setText(week+"周"+TextTools.words_time_DOW[dow-1]+" "+fromT.tellTime());
            }else{
                pickTimeText.setText(week+"周"+TextTools.words_time_DOW[dow-1]+" "+fromT.tellTime()+"-"+toT.tellTime());
            }
        }else{
            pickTime.setCardBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            pickTimeText.setTextColor(ContextCompat.getColor(getActivity(),R.color.text_color_secondary));
            pickTimeIcon.setColorFilter(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            if(wholeDaySwitch.isChecked()){
                pickTimeText.setText("设置日期");
            }else if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_ddl){
                pickTimeText.setText("设置日期与时间");
            }else{
                pickTimeText.setText("设置日期与时间段");
            }
        }
    }
    private void refreshTaskBlock(){
        if(taskSet&&task!=null){

            pickTaskIcon.setColorFilter(((BaseActivity)getActivity()).getColorPrimary());
            pickTaskText.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
            pickTask.setCardBackgroundColor(((BaseActivity)getActivity()).getColorPrimary());
            pickTaskText.setText(task.name);
            pickTaskCancel.setVisibility(View.VISIBLE);
        }else{
            pickTaskText.setText("处理任务");
            pickTask.setCardBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            pickTaskText.setTextColor(ContextCompat.getColor(getActivity(),R.color.text_color_secondary));
            //pickTaskText.invalidate();
            pickTaskIcon.setColorFilter(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            pickTaskCancel.setVisibility(View.GONE);
        }
    }
    private void refreshLocationBlock(){
        if(locationSet){
            pickLocationIcon.setColorFilter(((BaseActivity)getActivity()).getColorPrimary());
            pickLocationText.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
            pickLocation.setCardBackgroundColor(((BaseActivity)getActivity()).getColorPrimary());
            pickLocationText.setText(locationStr);
            pickLocationCancel.setVisibility(View.VISIBLE);
        }else{
            pickLocationText.setText("设置地点");
            pickLocationText.postInvalidate();
            pickLocation.setCardBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            pickLocationText.setTextColor(ContextCompat.getColor(getActivity(),R.color.text_color_secondary));
            //pickLocationText.invalidate();
            pickLocationIcon.setColorFilter(ContextCompat.getColor(getActivity(),R.color.color_control_normal));
            pickLocationCancel.setVisibility(View.GONE);
        }
    }
    private void refreshExamBlock(){
        if(subjectSet&&subjectName!=null){
            pickSubjectIcon.setColorFilter(((BaseActivity)getActivity()).getColorPrimary());
            pickSubjectText.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
            pickSubject.setCardBackgroundColor(((BaseActivity)getActivity()).getColorPrimary());
            pickSubjectText.setText(subjectName);
        }else{
            pickTaskText.setText("选择考试科目");
        }
    }
    private void setFromTime(int hour,int minute){
        fromT.hour = hour;
        fromT.minute = minute;
    }

    private void setToTime(int hour,int minute){
        toT.hour = hour;
        toT.minute = minute;
    }
    private void sendRefreshMessages() {
        Intent tlr = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(tlr);
        Intent tlr2 = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(tlr2);
        if(onFragmentInteractionListener!=null){
            onFragmentInteractionListener.onCalledRefresh();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onCalledRefresh();
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
                            subjectSet = true;
                            subjectCode = subjects.get(which).code;
                            subjectName = subjects.get(which).name;
                            if(mRadioGroup.getCheckedRadioButtonId()==R.id.ade_exam)extra.setText(subjectName+"考试");
                            refreshExamBlock();
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
            List<Task> toRemove = new ArrayList<>();
            for(Task t:tasks){
                int left = t.getLength() -   t.getDealtTime_All();
                if(left<= 0){
                    toRemove.add(t);}
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
                                taskSet = true;
                                task = tasks.get(which);
                                name.setText("处理任务：" + task.name );
                                if ( fromT.getDuration(toT) > task.getLength()) {
                                    Toast.makeText(HContext, "时长超过任务时长！", Toast.LENGTH_SHORT).show();
                                    toT = new HTime(now);
                                }
                                refreshTaskBlock();
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
      //  Task ddl_task = null;
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
            }
//            else if (type == TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE) {
//                ddl_task = new Task(mainTimeTable.core.curriculumCode, "处理DDL:" + eventName, allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now), TimeTable.getDOW(now),
//                        new HTime(now), week, dow, toT, "");
//                tag4 = ddl_task.getUuid();
//            }
            else if(type==TIMETABLE_EVENT_TYPE_EXAM){
                //tag4 = TextUtils.isEmpty(subjectCode)?subjectName:subjectCode;
                tag4 = fromT.tellTime()+"-"+toT.tellTime();
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
//                if (type == TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE) {
//                    ddl_task.setDdlName(uuid, week + "");
//                    mainTimeTable.addTask(ddl_task);
//                }
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
                if(this.getStatus()!=AsyncTask.Status.FINISHED)dismiss();
            }

        }


    }

    class addCourseTask extends AsyncTask {
        String curriculumCode;
        List<Integer> weeks;
        String teacher;
        String location;
        String name;
        int from;
        int last;
        int dow;

        public addCourseTask(String curriculumCode, List<Integer> weeks, String teacher, String location, String name, int from, int last, int dow) {
            this.curriculumCode = curriculumCode;
            this.weeks = weeks;
            this.teacher = teacher;
            this.location = location;
            this.name = name;
            this.from = from;
            this.last = last;
            this.dow = dow;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<last;i++){
                sb.append(i+from);
                if(i!=last-1)sb.append(",");
            }
            mainTimeTable.addEvents(weeks, dow, TIMETABLE_EVENT_TYPE_COURSE, name, location, teacher,  sb.subSequence(0,sb.toString().length()-1).toString(), from, last, false);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ActivityMain.saveData();
            sendRefreshMessages();
            if (this.getStatus()!=AsyncTask.Status.FINISHED) dismiss();
        }
    }
}
