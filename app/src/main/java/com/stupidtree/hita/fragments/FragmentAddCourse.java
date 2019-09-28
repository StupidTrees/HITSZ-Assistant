package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.Wheel3DView;
import com.cncoderx.wheelview.WheelView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.hita.TextTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.core.TimeTable.contains_integer;

@SuppressLint("ValidFragment")
public class FragmentAddCourse extends BottomSheetDialogFragment {
    private boolean subjectSet = false, weekSet = false, timeSet = false;
    private TextInputLayout teacher_text, location_text;
    ImageView pick_teacher, pick_location;
    private TextView subjectText, weekText, timeText;
    Button done,cancel;
    List<Integer> weeks;
    Wheel3DView pickDow, pickFromT, pickToT;


    public FragmentAddCourse() {

    }

    public static FragmentAddCourse newInstance() {
        return new FragmentAddCourse();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_add_course, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);

        initViews(view);
        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    private void initViews(View v) {
        weeks = new ArrayList<>();
        pickDow = v.findViewById(R.id.pickdow);
        pickFromT = v.findViewById(R.id.pickfromt);
        pickToT = v.findViewById(R.id.picktot);
        teacher_text = v.findViewById(R.id.pick_teacher_text);
        weekText = v.findViewById(R.id.pick_week);
        location_text = v.findViewById(R.id.pick_location_text);
        timeText = v.findViewById(R.id.time_text);
        subjectText = v.findViewById(R.id.pick_subject);
        subjectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new showSubjectsDialogTask().execute();
            }
        });
        pickDow.setEntries(new String[]{"星期一","星期二","星期三","星期四","星期五","星期六","星期日"});
        pickDow.setCyclic(false);
        String[] times = new String[12];
        for (int i = 0; i < 12; i++) {
            times[i] = "第" + (i + 1) + "节";
        }
        pickFromT.setEntries(times);
        pickToT.setEntries(times);
        pickFromT.setCyclic(false);
        pickToT.setCyclic(false);
        weekText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] weeksStr = new CharSequence[mainTimeTable.core.totalWeeks - 1];
                boolean[] choices = new boolean[weeksStr.length];
                for (int i = 0; i < weeksStr.length; i++) {
                    weeksStr[i] = "第" + (i + 1) + "周";
                    if (weeks.contains(i + 1)) choices[i] = true;
                    else choices[i] = false;
                }
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("选择上课周数")
                        .setMultiChoiceItems(weeksStr, choices, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked && !weeks.contains(which + 1)) weeks.add(which + 1);
                                else if (!isChecked) weeks.remove((Integer) (which + 1));
                            }
                        }).
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (weeks.size() > 0) {
                                            StringBuilder sb = new StringBuilder("第");
                                            for (Integer i : weeks) {
                                                sb.append(i).append(",");
                                            }
                                            sb.append("周");
                                            weekSet = true;
                                            weekText.setTextColor(((BaseActivity) FragmentAddCourse.this.getActivity()).getColorPrimary());
                                            weekText.setText(sb.toString().replaceAll(",周", "周"));
                                        } else {
                                            weekSet = false;
                                            weekText.setText("设置周数");
                                            Toast.makeText(getContext(), "至少选择一周！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).create();
                ad.setCancelable(false);
                ad.show();
            }
        });


        done = v.findViewById(R.id.done);
        cancel = v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!subjectSet){
                    Toast.makeText(getContext(),"先设置科目啊！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(teacher_text.getEditText().getText())){
                    Toast.makeText(getContext(),"先选个老师给你上课啊！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(location_text.getEditText().getText())){
                    Toast.makeText(getContext(),"先选个地点上课啊！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!weekSet){
                    Toast.makeText(getContext(),"设置一下上课周啊！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pickFromT.getCurrentIndex()>pickToT.getCurrentIndex()){
                    Toast.makeText(getContext(),"你好像泳有使时间倒流的能力",Toast.LENGTH_SHORT).show();
                    return;
                }
                new addCourseTask(mainTimeTable.core.curriculumCode,weeks,teacher_text.getEditText().getText().toString(),location_text.getEditText().getText().toString() ,subjectText.getText().toString(),pickFromT.getCurrentIndex()+1,pickToT.getCurrentIndex()-pickFromT.getCurrentIndex()+1,pickDow.getCurrentIndex()+1).execute();
            }
        });


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
            try {
                AlertDialog dialog = new AlertDialog.Builder(FragmentAddCourse.this.getContext()).
                        setTitle("选择考试科目")
                        .setItems(res, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                subjectText.setText(res[which]);
                                subjectSet = true;
                                subjectText.setTextColor(((BaseActivity) FragmentAddCourse.this.getActivity()).getColorPrimary());
                            }
                        }).create();
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
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
                sb.append(i+from).append(",");
            }
            mainTimeTable.addEvents(weeks, dow, TIMETABLE_EVENT_TYPE_COURSE, name, location, teacher,  sb.subSequence(0,sb.toString().length()-1).toString(), from, last, false);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ActivityMain.saveData();
            sendRefreshMessages();
            if (!this.isCancelled()) dismiss();
        }
    }

}
