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
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
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
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.TimeTableGenerator;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.diy.HDatePickerDialog;
import net.cachapa.expandablelayout.ExpandableLayout;
import java.util.ArrayList;
import java.util.Calendar;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
@SuppressLint("ValidFragment")
public class FragmentAddEvent extends BottomSheetDialogFragment {
    boolean dateSet=false,fromTSet=false,toTSet=false,courseSet = false;
    RadioGroup mRadioGroup;
    EditText name,tag2,tag3,tag4;
    TextView fromTimeShow,toTimeShow,dateShow,courseShow,examPlace;
    ImageView pickFromTime,pickToTime,pickDate,bt_expand,pickCourse;
    FloatingActionButton done;
    String subjectCode;
    LinearLayout pickToTimeLayout,pickFromTimeLayout,pickCourseLayout,nameLayout;
    ExpandableLayout mExpandableLayout;
    Switch wholeDaySwitch,autoAllocation;
    HTime fromT,toT;
    int week = 1;
    int dow = 1;
    public FragmentAddEvent() {
    }
    public static FragmentAddEvent newInstance(){
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
        initViews(view);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    void initViews(View v){
        mRadioGroup = v.findViewById(R.id.ade_radiogroup);
        mRadioGroup.check(R.id.ade_arrange);
        name = v.findViewById(R.id.ade_name);
        tag2 = v.findViewById(R.id.ade_tag2);
        tag3 = v.findViewById(R.id.ade_tag3);
        tag4 = v.findViewById(R.id.ade_tag4);
        nameLayout = v.findViewById(R.id.ade_namelayout);
        pickCourseLayout = v.findViewById(R.id.ade_courselayout);
        mExpandableLayout = v.findViewById(R.id.ade_expandlayout);
        bt_expand = v.findViewById(R.id.ade_expand_button);
        fromTimeShow = v.findViewById(R.id.ade_time_from_show);
        toTimeShow = v.findViewById(R.id.ade_time_to_show);
        dateShow = v.findViewById(R.id.ade_date_show);
        courseShow = v.findViewById(R.id.ade_text_course);
        examPlace = v.findViewById(R.id.ade_nexam_place);
        courseShow.setText("选择考试科目");
        pickFromTime = v.findViewById(R.id.ade_bt_picktime_from);
        pickToTime = v.findViewById(R.id.ade_bt_picktime_to);
        pickDate = v.findViewById(R.id.ade_bt_pickdate);
        pickCourse = v.findViewById(R.id.ade_pick_course_button);
        done = v.findViewById(R.id.ade_bt_done);
        pickToTimeLayout = v.findViewById(R.id.ade_picktotimelayout);
        pickFromTimeLayout = v.findViewById(R.id.ade_pickfromtimelayout);
        wholeDaySwitch = v.findViewById(R.id.ade_switch_wholeday);
        autoAllocation = v.findViewById(R.id.ade_switch_autoallocation);
        autoAllocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    wholeDaySwitch.setChecked(false);
                    wholeDaySwitch.setVisibility(View.GONE);
                    pickToTimeLayout.setVisibility(View.GONE);
                    pickFromTimeLayout.setVisibility(View.GONE);
                }else{
                    wholeDaySwitch.setChecked(false);
                    wholeDaySwitch.setVisibility(View.VISIBLE);
                    switch (mRadioGroup.getCheckedRadioButtonId()){
                        case R.id.ade_arrange:
                            pickFromTimeLayout.setVisibility(View.VISIBLE);
                            pickToTimeLayout.setVisibility(View.VISIBLE);
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
        wholeDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    autoAllocation.setChecked(false);
                    autoAllocation.setVisibility(View.GONE);
                    pickToTimeLayout.setVisibility(View.GONE);
                    pickFromTimeLayout.setVisibility(View.GONE);
                }else{
                    autoAllocation.setChecked(false);
                    autoAllocation.setVisibility(View.VISIBLE);
                    switch (mRadioGroup.getCheckedRadioButtonId()){
                        case R.id.ade_arrange:
                            pickFromTimeLayout.setVisibility(View.VISIBLE);
                            pickToTimeLayout.setVisibility(View.VISIBLE);
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
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                wholeDaySwitch.setChecked(false);
                autoAllocation.setChecked(false);
                switch (checkedId){
                    case R.id.ade_arrange:
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        autoAllocation.setVisibility(View.VISIBLE);
                        pickCourseLayout.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        pickFromTimeLayout.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_remind:
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        autoAllocation.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.GONE);
                        pickCourseLayout.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_ddl:
                        wholeDaySwitch.setVisibility(View.VISIBLE);
                        autoAllocation.setVisibility(View.VISIBLE);
                        pickToTimeLayout.setVisibility(View.GONE);
                        pickCourseLayout.setVisibility(View.GONE);
                        nameLayout.setVisibility(View.VISIBLE);
                        mExpandableLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.ade_exam:
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
                       fromT.hour = hourOfDay;
                       fromT.minute = minute;
                       fromTimeShow.setText(fromT.tellTime());
                       fromTimeShow.setTextColor(((BaseActivity)FragmentAddEvent.this.getActivity()).getColorPrimary());
                       fromTSet = true;
                   }
               }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
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
                        toT.hour = hourOfDay;
                        toT.minute = minute;
                        toTimeShow.setText(toT.tellTime());
                        toTimeShow.setTextColor(((BaseActivity)FragmentAddEvent.this.getActivity()).getColorPrimary());
                        toTSet = true;
                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
                TPD.create();
                TPD.show();
            }
        });

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HDatePickerDialog dlg =  new HDatePickerDialog((BaseActivity) FragmentAddEvent.this.getActivity(),dateShow);
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
                switch(mRadioGroup.getCheckedRadioButtonId()){
                    case R.id.ade_remind:type = TimeTable.TIMETABLE_EVENT_TYPE_REMIND;break;
                    case R.id.ade_arrange:type=TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT;break;
                    case R.id.ade_ddl:type=TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;break;
                    case R.id.ade_exam:type=TimeTable.TIMETABLE_EVENT_TYPE_EXAM;break;
                }
                if(type!=TimeTable.TIMETABLE_EVENT_TYPE_EXAM&&name.getText().toString().isEmpty()){
                    Toast.makeText(HContext,"请输入事件名称!",Toast.LENGTH_SHORT).show();
                    return;
                }else if(!dateSet){
                    Toast.makeText(HContext,"请设置事件日期！",Toast.LENGTH_SHORT).show();
                    return;
                }else if(!fromTSet&&pickFromTimeLayout.getVisibility()==View.VISIBLE){
                    Toast.makeText(HContext,"请设置开始时间！",Toast.LENGTH_SHORT).show();
                    return;
                }else if(!toTSet&&pickToTimeLayout.getVisibility()==View.VISIBLE){
                    Toast.makeText(HContext,"请设置结束时间！",Toast.LENGTH_SHORT).show();
                    return;
                }else if(type!=TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE&&type!=TimeTable.TIMETABLE_EVENT_TYPE_REMIND&&fromT.compareTo(toT)>0){
                    Toast.makeText(HContext,"请输入正确的事件时间！",Toast.LENGTH_SHORT).show();
                    return;
                }else if(type==TimeTable.TIMETABLE_EVENT_TYPE_EXAM&&!courseSet){
                    Toast.makeText(HContext,"请选择考试科目！",Toast.LENGTH_SHORT).show();
                    return;
                }
                String Tname,Ttag2,Ttag3,Ttag4;
                if(type == TimeTable.TIMETABLE_EVENT_TYPE_EXAM){
                    Ttag2 = examPlace.getText().toString().isEmpty()?"":examPlace.getText().toString();
                   Ttag3 ="科目代码："+subjectCode;
                   Tname = courseShow.getText()+"考试";
                   Ttag4 = fromT.tellTime()+"-"+toT.tellTime();
                }else{
                    Tname = name.getText().toString().isEmpty()?"":name.getText().toString();
                   Ttag2 = tag2.getText().toString().isEmpty()?"":tag2.getText().toString();
                   Ttag3 = tag3.getText().toString().isEmpty()?"":tag3.getText().toString();
                    Ttag4 = tag4.getText().toString().isEmpty()?"":tag4.getText().toString();
                }
                if(autoAllocation.isChecked()){
                    TimeTableGenerator.autoAdd(week,dow,Tname,Ttag2,Ttag3,Ttag4,type,120);
                }else{
                    HTime tempToTime = type ==TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT||type ==TimeTable.TIMETABLE_EVENT_TYPE_EXAM?toT:fromT;
                    EventItem toAdd = new EventItem(allCurriculum.get(thisCurriculumIndex).curriculumCode,type,Tname,Ttag2,Ttag3,Ttag4,fromT,tempToTime,week,dow,wholeDaySwitch.isChecked());
                    mainTimeTable.addEvent(toAdd);
                    if(type==TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE){
                        mainTimeTable.addTask("处理DDL:"+name.getText().toString(),allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now), TimeTable.getDOW(now),new HTime(now),toAdd);
                    }
                }

               // fragmentContext.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
                ActivityMain.saveData(getActivity());
                //FragmentTasks ftsk = fragmentContext.ftsk;
                //if(ftsk!=null&&ftsk.hasInit) ftsk.Refresh();
                sendRefreshMessages();
                dismiss();
            }
        });


    }



    private void sendRefreshMessages(){
        Intent tlr = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH_FROM_OTHER");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(tlr);
        Intent tlr2 = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(tlr2);
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
    }
    class showSubjectsDialogTask extends AsyncTask{
        ArrayList<Subject> subjects;
        String[] res;
        @Override
        protected Object doInBackground(Object[] objects) {
            subjects = allCurriculum.get(thisCurriculumIndex).getSubjects();
            res= new String[subjects.size()];
            for(int i = 0;i<subjects.size();i++ ){
                res[i] =subjects.get(i).name;
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
                            courseShow.setTextColor(((BaseActivity)FragmentAddEvent.this.getActivity()).getColorPrimary());
                            subjectCode = subjects.get(which).code;
                        }
                    }).create()
                    ;
            dialog.show();
        }
    }

}
