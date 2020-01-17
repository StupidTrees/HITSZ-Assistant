package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.timetable.timetable.Task;
import com.stupidtree.hita.diy.PickSingleTimeDialog;
import com.stupidtree.hita.hita.TextTools;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

@SuppressLint("ValidFragment")
public class FragmentAddTask extends BottomSheetDialogFragment {
     HTime fT = new HTime(now),tT = new HTime(now);
     int fW ;
     int fDOW;
     int tDOW ;
     int tW;
     boolean fDset = false;
     boolean tDset = false;
     boolean fTset = false;
     boolean tTset = false;

    TextView fD_show,tD_show,fT_show,tT_show;
    ImageView fD_pick,tD_pick,fT_pick,tT_pick;
    FloatingActionButton bt_done;
    Switch adt_switch,adt_switch2,adt_switch3;
    NumberPicker adt_lengthpicker;
    EditText name;
    LinearLayout arrangetime,arrangelength;
    AddTaskDoneListener addTaskDoneListener;
   // FragmentTasks attachedFragment;
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.getActivity(),themeID);// your app theme here
//        View v = inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.fragment_add_task,container,false);
//
//        return v;
//    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof AddTaskDoneListener){
            addTaskDoneListener = (AddTaskDoneListener) context;
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        fT = new HTime(now);
        tT = new HTime(now);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_add_task, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initViews(view);
        setViewFunctions();
        //name.requestFocus();
         return dialog;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
         super.onDismiss(dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void initViews(View v){
        fD_show = v.findViewById(R.id.adt_fdate_show);
        tD_show = v.findViewById(R.id.adt_tdate_show);
        fT_show = v.findViewById(R.id.adt_ftime_show);
        tT_show = v.findViewById(R.id.adt_ttime_show);
        fD_pick = v.findViewById(R.id.adt_pick_fdate);
        tD_pick = v.findViewById(R.id.adt_pick_tdate);
        fT_pick = v.findViewById(R.id.adt_pick_ftime);
        tT_pick = v.findViewById(R.id.adt_pick_ttime);
        adt_switch = v.findViewById(R.id.adt_switch);
        adt_switch2 = v.findViewById(R.id.adt_switch2);
        adt_switch3 = v.findViewById(R.id.adt_switch3);
        adt_lengthpicker = v.findViewById(R.id.adt_length_picker);
        arrangetime = v.findViewById(R.id.adt_arrangetime);
        arrangelength = v.findViewById(R.id.adt_arrangelength);
        name = v.findViewById(R.id.adt_name);
        bt_done = v.findViewById(R.id.adt_bt_done);
        adt_lengthpicker.setMaxValue(10000);
    }
    
    void setViewFunctions(){
        adt_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) arrangetime.setVisibility(View.VISIBLE);
                else arrangetime.setVisibility(View.GONE);
            }
        });
        adt_switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) arrangelength.setVisibility(View.VISIBLE);
                else arrangelength.setVisibility(View.GONE);
            }
        });
        fD_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PickSingleTimeDialog((BaseActivity) getActivity(),new PickSingleTimeDialog.onDialogConformListener() {
                    @Override
                    public void onClick(int week, int dow,int hour,int minute, boolean dateSet) {
                        fDset = dateSet;
                        fDOW = dow;
                        fW = week;
                        if(dateSet){
                            fD_show.setText(week+"周"+ TextTools.words_time_DOW[dow-1]+" "+hour+":"+minute);
                            fD_show.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
                        }
                    }
                }).show();
            }
        });

        tD_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PickSingleTimeDialog((BaseActivity) getActivity(),new PickSingleTimeDialog.onDialogConformListener() {
                    @Override
                    public void onClick(int week, int dow,int hour,int minute, boolean dateSet) {
                        tDset = dateSet;
                        tDOW = dow;
                        tW = week;
                        if(dateSet){
                            tD_show.setText(week+"周"+ TextTools.words_time_DOW[dow-1]+" "+hour+":"+minute);
                            tD_show.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
                        }
                    }
                }).show();
            }
        });

        fT_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog TPD = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        fT.setTime(hourOfDay,minute);
                        fT_show.setText(fT.tellTime());
                        fT_show.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
                        fTset = true;

                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                TPD.create();
                TPD.show();

            }
        });

        tT_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog TPD = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tT.setTime(hourOfDay,minute);
                        tT_show.setText(fT.tellTime());
                        tT_show.setTextColor(((BaseActivity)getActivity()).getColorPrimary());
                        tTset = true;

                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                TPD.create();
                TPD.show();

            }
        });
        bt_done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adt_switch.isChecked()&&(!fDset||!tDset)){
                    Toast.makeText(getContext(),"请设置任务期限！",Toast.LENGTH_SHORT).show();
                    return;
                }else if(name.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"请输入任务名称！",Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Task t = new Task(timeTableCore.getCurrentCurriculum().getCurriculumCode(),name.getText().toString());
                    if(adt_switch.isChecked()){
                        String ddlUUID = timeTableCore.addEvent(tW,tDOW, TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE,
                                "DDL:"+name.getText().toString(),"任务截至","Deadline",t.getUuid(),tT,tT,false);
                      //  timeTableCore.addTask(name.getText().toString(),fW,fDOW,tW,tDOW,fT,tT,"DDL:"+name.getText().toString());
                        t.arrangeTime(fW,fDOW,tW,tDOW,fT,tT,ddlUUID+":::"+tW);
                    }
                    //else  timeTableCore.addTask(name.getText().toString());
                    if(adt_switch2.isChecked()){
                        t.setLength(adt_lengthpicker.getValue());
                    }
                    t.setEvery_day(adt_switch3.isChecked());
                    timeTableCore.addTask(t);
                    dismiss();
                    Intent mes = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
                    Intent mes2 = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mes);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mes2);
                    ActivityMain.saveData();
                    if(addTaskDoneListener!=null)addTaskDoneListener.OnDone();
                }
            }
        });
    }

//    public void attachToFragment(FragmentTasks ft){
//        attachedFragment = ft;
//    }

 
    public interface AddTaskDoneListener{
        void OnDone();
    }


}
