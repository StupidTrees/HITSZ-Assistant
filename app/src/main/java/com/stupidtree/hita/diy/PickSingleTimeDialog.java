package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.Wheel3DView;
import com.cncoderx.wheelview.WheelView;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.themeCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class PickSingleTimeDialog extends AlertDialog{
    private String plusTag;
    private int week,dow,hour,minute;
    BaseActivity context;
    private TextView dialogTitle,dialogTitleTime;
    private boolean timeSet = false;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private Wheel3DView picker_week ;
    private Wheel3DView picker_dow;
    private Wheel3DView picker_hour,picker_minute;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
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
        //calendarView.setMinDate(now.getTimeInMillis());
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
                dialogTitleTime.setText(hour+":"+minute);
            }
        });
        picker_minute.setOnWheelChangedListener(new OnWheelChangedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                minute = newIndex;
                dialogTitleTime.setText(hour+":"+minute);
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
            int tempDOW = now.get(Calendar.DAY_OF_WEEK);
            picker_dow.setCurrentIndex(tempDOW==1?6:tempDOW-2);
            picker_week.setCurrentIndex(timeTableCore.getThisWeekOfTerm()-1);
            picker_hour.setCurrentIndex(now.get(Calendar.HOUR_OF_DAY));
            picker_minute.setCurrentIndex(now.get(Calendar.MINUTE));
            if(timeTableCore.isThisTerm())calendarView.setDate(now.getTimeInMillis());
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
            int tempDOW = c.get(Calendar.DAY_OF_WEEK);
            Calendar tempD2 = (Calendar) c.clone();
            tempD2.add(Calendar.DATE,-1);
            Calendar tempD3 = (Calendar) c.clone();
            tempD3.add(Calendar.DATE,-2);
            if(c.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    c.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&c.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="("+context.getString(R.string.today)+")";
            else if(tempD2.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD2.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD2.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="("+context.getString(R.string.tomorrow)+")";
            else if(tempD3.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD3.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD3.get(Calendar.DATE)==now.get(Calendar.DATE)
            )  plusTag="("+context.getString(R.string.tda_tomorrow)+")";
            else plusTag="";
            dialogTitle.setText(context.getResources().getStringArray(R.array.months)[c.get(Calendar.MONTH)] +String.format(context.getString(R.string.date_day),c.get(Calendar.DAY_OF_MONTH))+plusTag);
            week = timeTableCore.getCurrentCurriculum().getWeekOfTerm(c);
            dow = tempDOW==1?7:tempDOW-1;
            if(!fromWheel){
                weekWheelListener.fromCalender = true;
                dowWheelListener.fromCalender = true;
                picker_dow.setCurrentIndex(tempDOW==1?6:tempDOW-2);
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
            Calendar tempD2 = (Calendar) tempD.clone();
            tempD2.add(Calendar.DATE,-1);
            Calendar tempD3 = (Calendar) tempD.clone();
            tempD3.add(Calendar.DATE,-2);
            if(tempD.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="("+context.getString(R.string.today)+")";
            else if(tempD2.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD2.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD2.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="("+context.getString(R.string.tomorrow)+")";
            else if(tempD3.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD3.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD3.get(Calendar.DATE)==now.get(Calendar.DATE)
            )  plusTag="("+context.getString(R.string.tda_tomorrow)+")";
            else plusTag="";
            int tempDOW = tempD.get(Calendar.DAY_OF_WEEK);
            week = picker_week.getCurrentIndex()+1;
            dow = tempDOW==1?7:tempDOW-1;
            dialogTitle.setText(context.getResources().getStringArray(R.array.months)[tempD.get(Calendar.MONTH)] +String.format(context.getString(R.string.date_day),tempD.get(Calendar.DAY_OF_MONTH))+plusTag);
            if(!fromCalender){
               calendarDateChangeListener.fromWheel = true;
               calendarView.setDate(tempD.getTimeInMillis());
           }else{
               fromCalender = false;
           }

        }
    }
}
