package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.WheelView;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.util.EventsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.themeCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class PickSingleTimeDialog extends RoundedCornerDialog {
    private String plusTag;
    private int week,dow,hour,minute;
    BaseActivity context;
    private TextView dialogTitle,dialogTitleTime;
    private boolean timeSet = false;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private mWheel3DView picker_week;
    private mWheel3DView picker_dow;
    private mWheel3DView picker_hour, picker_minute;
    private CalendarView calendarView ;
    private mCalendarDateChangeListener calendarDateChangeListener ;
    private mWheelChangedListener weekWheelListener;
    private mWheelChangedListener dowWheelListener ;
    private boolean hasInit = false;
    private int init_week,init_dow;
    private HTime init_time;
    public interface onDialogConformListener{
        void onClick(int week,int dow,int hour,int minute,boolean timeSet);
    }
    public PickSingleTimeDialog(BaseActivity context, onDialogConformListener onDialogConformListener){
        super(context);
        mOnDialogConformListener = onDialogConformListener;
        this.context = context;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeCore.getCurrentThemeID());// your app theme here
        View view = getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_pick_single_time,null,false);
        setView(view);
        initViews(view);
    }
    public void setInitialValue(int week, int dow, HTime T){
        hasInit = true;
        init_dow = dow;
        init_week = week;
        init_time = T;
    }



    void initViews(View view){
        picker_week = view.findViewById(R.id.ade_weekpicker_week);
        picker_dow = view.findViewById(R.id.ade_weekpicker_dow);
        picker_hour = view.findViewById(R.id.hour);
        done = view.findViewById(R.id.done);
        picker_minute = view.findViewById(R.id.minute);
        calendarView = view.findViewById(R.id.ade_calendarview);
        dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitleTime = view.findViewById(R.id.dialog_title_time);
        calendarDateChangeListener = new mCalendarDateChangeListener();
        weekWheelListener = new mWheelChangedListener();
        dowWheelListener = new mWheelChangedListener();
        calendarView.setMinDate(timeTableCore.getCurrentCurriculum().getFirstDateAtWOT(1).getTimeInMillis());
        //calendarView.setMinDate(timeTableCore.getNow().getTimeInMillis());
        calendarView.setMaxDate(timeTableCore.getCurrentCurriculum().getFirstDateAtWOT(3000).getTimeInMillis());

        List weekTexts = new ArrayList();
        List<String> dowTexts = Arrays.asList(context.getResources().getStringArray(R.array.dow2));
        List<String> hourTexts = new ArrayList<>();
        List<String> minuteTexts = new ArrayList<>();
        for(int i=1;i<=timeTableCore.getCurrentCurriculum().getTotalWeeks();i++){
            if(i==timeTableCore.getThisWeekOfTerm()) weekTexts.add(i<10?"0"+i+"·":i+""+"·");
            else weekTexts.add(i<10?"0"+i:i+"");
        }
        for(int i=0;i<24;i++) if(i<10)hourTexts.add("0"+i+"");else hourTexts.add(i+"");
        for(int i=0;i<60;i++) if(i<10)minuteTexts.add("0"+i+"");else minuteTexts.add(i+"");
        picker_dow.setEntries(dowTexts);
        picker_week.setEntries(weekTexts);
        picker_hour.setEntries(hourTexts);
        picker_minute.setEntries(minuteTexts);
        calendarView.setOnDateChangeListener(calendarDateChangeListener);
        picker_dow.setOnWheelChangedListener(dowWheelListener);
        picker_week.setOnWheelChangedListener(weekWheelListener);
        picker_hour.setOnWheelChangedListener(new OnWheelChangedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                hour = newIndex;
                dialogTitleTime.setText(hour + ":" + (minute < 10 ? "0" + minute : minute));
            }
        });
        picker_minute.setOnWheelChangedListener(new OnWheelChangedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                minute = newIndex;
                dialogTitleTime.setText(hour + ":" + (minute < 10 ? "0" + minute : minute));
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSet = true;
                mOnDialogConformListener.onClick(week,dow,hour,minute, timeSet);
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(hasInit){
            picker_dow.setCurrentIndex(init_dow-1);
            picker_week.setCurrentIndex(init_week-1);
            picker_hour.setCurrentIndex(init_time.hour);
            picker_minute.setCurrentIndex(init_time.minute);
        }else{
            int tempDOW = timeTableCore.getNow().get(Calendar.DAY_OF_WEEK);
            picker_dow.setCurrentIndex(tempDOW==1?6:tempDOW-2);
            picker_week.setCurrentIndex(timeTableCore.getThisWeekOfTerm()-1);
            picker_hour.setCurrentIndex(timeTableCore.getNow().get(Calendar.HOUR_OF_DAY));
            picker_minute.setCurrentIndex(timeTableCore.getNow().get(Calendar.MINUTE));
            if (timeTableCore.isThisTerm())
                calendarView.setDate(timeTableCore.getNow().getTimeInMillis());
        }
        week = picker_week.getCurrentIndex()+1;
        dow = picker_dow.getCurrentIndex()+1;
        calendarDateChangeListener.fromWheel=false;
        weekWheelListener.fromCalender=false;
        dowWheelListener.fromCalender=false;
        timeSet = true;
    }

    class mCalendarDateChangeListener implements CalendarView.OnDateChangeListener {

        boolean fromWheel = false;

        @SuppressLint("SetTextI18n")
        @Override
        public void onSelectedDayChange( CalendarView view, int year, int month, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.set(year,month,dayOfMonth);
            dialogTitle.setText(EventsUtils.getDateString(c, true, EventsUtils.TTY_REPLACE));
            week = timeTableCore.getCurrentCurriculum().getWeekOfTerm(c);
            dow = TimetableCore.getDOW(c);
            if(!fromWheel){
                weekWheelListener.fromCalender = true;
                dowWheelListener.fromCalender = true;
                picker_dow.setCurrentIndex(dow - 1);
                picker_week.setCurrentIndex(timeTableCore.getCurrentCurriculum().getWeekOfTerm(c)-1);
            }else{
                fromWheel=false;
            }
        }
    }

    class mWheelChangedListener implements OnWheelChangedListener{

        boolean fromCalender = false;
        @SuppressLint("SetTextI18n")
        @Override
        public void onChanged(WheelView view, int oldIndex, int newIndex) {
            Calendar tempD = timeTableCore.getCurrentCurriculum().getDateAtWOT(picker_week.getCurrentIndex()+1,picker_dow.getCurrentIndex()+1);
            int tempDOW = tempD.get(Calendar.DAY_OF_WEEK);
            week = picker_week.getCurrentIndex()+1;
            dow = tempDOW==1?7:tempDOW-1;
            dialogTitle.setText(EventsUtils.getDateString(tempD, true, EventsUtils.TTY_REPLACE));
            if(!fromCalender){
               calendarDateChangeListener.fromWheel = true;
               calendarView.setDate(tempD.getTimeInMillis());
           }else{
               fromCalender = false;
           }

        }
    }
}
