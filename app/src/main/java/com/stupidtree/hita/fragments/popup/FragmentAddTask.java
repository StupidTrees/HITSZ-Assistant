package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.views.PickSingleTimeDialog;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

@SuppressLint("ValidFragment")
public class FragmentAddTask extends FragmentRadiusPopup {
    HTime fT = new HTime(timeTableCore.getNow()), tT = new HTime(timeTableCore.getNow());
    private int fW;
     int fDOW;
     int tDOW ;
     int tW;
     boolean fDset = false;
    private boolean tDset = false;
    private boolean fTset = false;
    private boolean tTset = false;

    private TextView fD_show, tD_show, fT_show, tT_show;
    private ImageView fD_pick, tD_pick, fT_pick, tT_pick;
    private FloatingActionButton bt_done;
    private Switch adt_switch, adt_switch2, adt_switch3;
    private NumberPicker adt_lengthpicker;
    private EditText name;
    private LinearLayout arrangetime, arrangelength;
    private AddTaskDoneListener addTaskDoneListener;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof AddTaskDoneListener){
            addTaskDoneListener = (AddTaskDoneListener) context;
        }

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_add_task, null);
        initViews(view);
        setViewFunctions();
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        name.requestFocus();
        name.setText("");
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
         super.onDismiss(dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fT = new HTime(timeTableCore.getNow());
        tT = new HTime(timeTableCore.getNow());
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

    private void setViewFunctions() {
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                decideWhetherShowDone();
            }
        });
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
                            fD_show.setTextColor(((BaseActivity) getActivity()).getColorAccent());
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
                            tD_show.setTextColor(((BaseActivity) getActivity()).getColorAccent());
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
                        fT_show.setTextColor(((BaseActivity) getActivity()).getColorAccent());
                        fTset = true;

                    }
                }, timeTableCore.getNow().get(Calendar.HOUR_OF_DAY), timeTableCore.getNow().get(Calendar.MINUTE), true);
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
                        tT_show.setTextColor(((BaseActivity) getActivity()).getColorAccent());
                        tTset = true;

                    }
                }, timeTableCore.getNow().get(Calendar.HOUR_OF_DAY), timeTableCore.getNow().get(Calendar.MINUTE), true);
                TPD.create();
                TPD.show();

            }
        });
        bt_done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adt_switch.isChecked()&&(!fDset||!tDset)){
                    Toast.makeText(getContext(),"请设置任务期限！",Toast.LENGTH_SHORT).show();
                }else if(name.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"请输入任务名称！",Toast.LENGTH_SHORT).show();
                }else{
                    Task t = new Task(timeTableCore.getCurrentCurriculum().getCurriculumCode(),name.getText().toString());
                    if(adt_switch.isChecked()){
                        String ddlUUID = timeTableCore.addEvent(tW, tDOW, TimetableCore.DDL,
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
                    Intent mes = new Intent(TIMETABLE_CHANGED);
                    //Intent mes = new Intent(TASK_REFRESH);
                    //Intent mes2 = new Intent(TIMELINE_REFRESH);
                    //Intent mes3 = new Intent(WATCHER_REFRESH);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mes);
//                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mes2);
//                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mes3);
                    ActivityMain.saveData();
                    if(addTaskDoneListener!=null)addTaskDoneListener.OnDone();
                }
            }
        });
    }


    void decideWhetherShowDone() {
        if (TextUtils.isEmpty(name.getText())) bt_done.hide();
        else bt_done.show();
    }


 
    public interface AddTaskDoneListener{
        void OnDone();
    }


}
