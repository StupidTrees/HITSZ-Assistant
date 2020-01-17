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
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.hita.TextTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.themeID;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class PickTimePeriodDialog extends AlertDialog{
    private String plusTag;
    private int week,dow,hour1,minute1,hour2,minute2;
    BaseActivity context;
    private TextView dialogTitle;
    private boolean timeSet = false;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private Wheel3DView picker_week ;
    private Wheel3DView picker_dow;
    private Wheel3DView picker_hour1,picker_minute1,picker_hour2,picker_minute2;
    private CalendarView calendarView ;
    private mCalendarDateChangeListener calendarDateChangeListener ;
    private mWheelChangedListener weekWheelListener;
    private mWheelChangedListener dowWheelListener ;

    private boolean hasInit = false;
    private int init_week,init_dow;
    private HTime init_fT,init_tT;
    private boolean dateOnly = false;
    private LinearLayout pickTimeLayout;
    public interface onDialogConformListener{
        void onClick(int week, int dow, int hour1, int minute1, int hour2, int minute2, boolean timeSet);
    }
    public PickTimePeriodDialog(BaseActivity context, onDialogConformListener onDialogConformListener){
        super(context);
        mOnDialogConformListener = onDialogConformListener;
        this.context = context;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeID);// your app theme here
        View view = getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_pick_time_period,null,false);
        setView(view);
        initViews(view);
    }

    public void setInitialValue(int week,int dow,HTime fT,HTime tT){
        hasInit = true;
        init_week = week;
        init_dow = dow;
        init_fT = fT;
        init_tT = tT;
    }
    public void dateOnly(){
        dateOnly = true;
    }

    public void setOnDialogConformListener(onDialogConformListener m) {
        this.mOnDialogConformListener = m;
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
        picker_hour1 = view.findViewById(R.id.hour1);
        picker_hour2 = view.findViewById(R.id.hour2);
        done = view.findViewById(R.id.done);
        pickTimeLayout = view.findViewById(R.id.pick_time_layout);
        picker_minute1 = view.findViewById(R.id.minute1);
        picker_minute2 = view.findViewById(R.id.minute2);
        calendarView = view.findViewById(R.id.ade_calendarview);
        dialogTitle = view.findViewById(R.id.dialog_title);
        calendarDateChangeListener = new mCalendarDateChangeListener();
        weekWheelListener = new mWheelChangedListener();
        dowWheelListener = new mWheelChangedListener();
        calendarView.setMinDate(timeTableCore.getCurrentCurriculum().getFirstDateAtWOT(1).getTimeInMillis());
        calendarView.setMaxDate(timeTableCore.getCurrentCurriculum().getFirstDateAtWOT(3000).getTimeInMillis());

        List weekTexts = new ArrayList();
        List<String> dowTexts = Arrays.asList(context.getResources().getStringArray(R.array.dow1));
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
        picker_hour1.setEntries(hourTexts);
        picker_hour2.setEntries(hourTexts);
        picker_minute1.setEntries(minuteTexts);
        picker_minute2.setEntries(minuteTexts);
        calendarView.setOnDateChangeListener(calendarDateChangeListener);
        picker_dow.setOnWheelChangedListener(dowWheelListener);
        picker_week.setOnWheelChangedListener(weekWheelListener);
        picker_hour1.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                hour1 = newIndex;
                HTime from = new HTime(hour1,minute1);
                HTime to = new HTime(hour2,minute2);
                if(to.before(from)){
                    if(hour2<newIndex) picker_hour2.setCurrentIndex(newIndex);
                    if(minute2<minute1) picker_minute2.setCurrentIndex(minute1);
                }
            }
        });
        picker_hour2.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                hour2 = newIndex;
                HTime from = new HTime(hour1,minute1);
                HTime to = new HTime(hour2,minute2);
                if(to.before(from)){
                    if(hour1>newIndex) picker_hour1.setCurrentIndex(newIndex);
                    if(minute2<minute1) picker_minute1.setCurrentIndex(minute2);
                }
            }
        });
        picker_minute1.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                minute1 = newIndex;
                HTime from = new HTime(hour1,minute1);
                HTime to = new HTime(hour2,minute2);
                if(to.before(from)){
                    if(hour1>hour2) picker_hour2.setCurrentIndex(hour1);
                    if(minute2<minute1) picker_minute2.setCurrentIndex(newIndex);
                }
            }
        });
        picker_minute2.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                minute2 = newIndex;
                HTime from = new HTime(hour1,minute1);
                HTime to = new HTime(hour2,minute2);
                if(to.before(from)){
                    if(hour1>hour2) picker_hour1.setCurrentIndex(hour2);
                    if(minute2<minute1) picker_minute1.setCurrentIndex(newIndex);
                }
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSet = true;
                mOnDialogConformListener.onClick(week,dow,hour1,minute1,hour2,minute2,timeSet);
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(dateOnly){
            pickTimeLayout.setVisibility(View.GONE);
        }else pickTimeLayout.setVisibility(View.VISIBLE);
        if(hasInit){
            picker_dow.setCurrentIndex(init_dow-1);
            picker_week.setCurrentIndex(init_week-1);
            picker_hour1.setCurrentIndex(init_fT.hour);
            picker_minute1.setCurrentIndex(init_fT.minute);
            picker_hour2.setCurrentIndex(init_tT.hour);
            picker_minute2.setCurrentIndex(init_tT.minute);
        }else{
            int tempDOW = now.get(Calendar.DAY_OF_WEEK);
            picker_dow.setCurrentIndex(tempDOW==1?6:tempDOW-2);
            picker_week.setCurrentIndex(timeTableCore.getThisWeekOfTerm()-1);
            picker_hour1.setCurrentIndex(now.get(Calendar.HOUR_OF_DAY));
            picker_minute1.setCurrentIndex(now.get(Calendar.MINUTE));
            picker_hour2.setCurrentIndex(now.get(Calendar.HOUR_OF_DAY));
            picker_minute2.setCurrentIndex(now.get(Calendar.MINUTE));
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
