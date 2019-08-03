package com.stupidtree.hita.diy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.Wheel3DView;
import com.cncoderx.wheelview.WheelView;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.themeID;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;

public class HDatePickerDialog {
    String plusTag;
    int week;
    int dow;
    BaseActivity context;
    TextView showDate;
    TextView dialogTitle;
    boolean dateSet = false;
    onDialogConformListener mOnDialogConformListener;
    Wheel3DView picker_week ;
    Wheel3DView picker_dow;
    CalendarView calendarView ;
    mCalendarDateChangeListener calendarDateChangeListener ;
    mWheelChangedListener weekWheelListener;
    mWheelChangedListener dowWheelListener ;
    AlertDialog ad;
    public interface onDialogConformListener{
        void onClick(int week,int dow,boolean dateSet);
    }
    public HDatePickerDialog(final BaseActivity context, final TextView showDate){
        this.context = context;
        this.showDate = showDate;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeID);// your app theme here
        final View view = context.getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_addevent_pickdate,null);
        picker_week = view.findViewById(R.id.ade_weekpicker_week);
        picker_dow = view.findViewById(R.id.ade_weekpicker_dow);
        calendarView = view.findViewById(R.id.ade_calendarview);
        dialogTitle = view.findViewById(R.id.dialog_title);
        calendarView.setMinDate(allCurriculum.get(thisCurriculumIndex).getFirstDateAtWOT(1).getTimeInMillis());
        calendarView.setMaxDate(allCurriculum.get(thisCurriculumIndex).getFirstDateAtWOT(3000).getTimeInMillis());
        calendarDateChangeListener = new mCalendarDateChangeListener();
        weekWheelListener = new mWheelChangedListener();
        dowWheelListener = new mWheelChangedListener();
        ad = new AlertDialog.Builder(context).setView(view)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDate.setText("第"+week+"周 "+ TextTools.words_time_DOW[dow-1]+plusTag);
                        showDate.setTextColor(context.getColorPrimary());
                        dateSet = true;
                        mOnDialogConformListener.onClick(week,dow,dateSet);
                    }
                })
                .setNegativeButton("取消",null)
                .create();
        List weekTexts = new ArrayList();
        List<String> dowTexts = new ArrayList();
        for(int i=0;i<7;i++) dowTexts.add(TextTools.words_time_DOW[i]);
        for(int i=1;i<=allCurriculum.get(thisCurriculumIndex).totalWeeks;i++){
            if(i==thisWeekOfTerm) weekTexts.add("第"+i+"周(本周)");
            else weekTexts.add("第"+i+"周");
        }
        picker_dow.setEntries(dowTexts);
         picker_week.setEntries(weekTexts);
         calendarView.setOnDateChangeListener(calendarDateChangeListener);
        picker_dow.setOnWheelChangedListener(dowWheelListener);
        picker_week.setOnWheelChangedListener(weekWheelListener);

    }

    public void setOnDialogConformListener(onDialogConformListener m) {
        this.mOnDialogConformListener = m;
    }


    public void showDatePickerDialog(){
        int tempDOW = now.get(Calendar.DAY_OF_WEEK);
        picker_dow.setCurrentIndex(tempDOW==1?6:tempDOW-2);
        picker_week.setCurrentIndex(thisWeekOfTerm-1);
        week = picker_week.getCurrentIndex()+1;
        dow = picker_dow.getCurrentIndex()+1;
        calendarView.setDate(now.getTimeInMillis());
        calendarDateChangeListener.fromWheel=false;
        weekWheelListener.fromCalender=false;
        dowWheelListener.fromCalender=false;
        dateSet = true;
        ad.show();
    }

    class mCalendarDateChangeListener implements CalendarView.OnDateChangeListener {

        boolean fromWheel = false;

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
            ) plusTag="（今天）";
            else if(tempD2.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD2.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD2.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="（明天）";
            else if(tempD3.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD3.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD3.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="（后天）";
            else plusTag="";
            dialogTitle.setText(c.get(Calendar.MONTH)+1+"月"+c.get(Calendar.DAY_OF_MONTH)+"日"+plusTag);
            week = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(c);
            dow = tempDOW==1?7:tempDOW-1;
            if(!fromWheel){
                weekWheelListener.fromCalender = true;
                dowWheelListener.fromCalender = true;
                picker_dow.setCurrentIndex(tempDOW==1?6:tempDOW-2);
                picker_week.setCurrentIndex(allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(c)-1);
            }else{
                fromWheel=false;
            }
        }
    }

    class mWheelChangedListener implements OnWheelChangedListener{

        boolean fromCalender = false;
        @Override
        public void onChanged(WheelView view, int oldIndex, int newIndex) {
            Calendar tempD = allCurriculum.get(thisCurriculumIndex).getDateAtWOT(picker_week.getCurrentIndex()+1,picker_dow.getCurrentIndex()+1);
            Calendar tempD2 = (Calendar) tempD.clone();
            tempD2.add(Calendar.DATE,-1);
            Calendar tempD3 = (Calendar) tempD.clone();
            tempD3.add(Calendar.DATE,-2);
            if(tempD.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="（今天）";
            else if(tempD2.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD2.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD2.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="（明天）";
            else if(tempD3.get(Calendar.YEAR)==now.get(Calendar.YEAR)&&
                    tempD3.get(Calendar.MONTH)==now.get(Calendar.MONTH)
                    &&tempD3.get(Calendar.DATE)==now.get(Calendar.DATE)
            ) plusTag="（后天）";
            else plusTag="";
            int tempDOW = tempD.get(Calendar.DAY_OF_WEEK);
            week = picker_week.getCurrentIndex()+1;
            dow = tempDOW==1?7:tempDOW-1;
            dialogTitle.setText(tempD.get(Calendar.MONTH)+1+"月"+tempD.get(Calendar.DAY_OF_MONTH)+"日"+plusTag);
            if(!fromCalender){
               calendarDateChangeListener.fromWheel = true;
               calendarView.setDate(tempD.getTimeInMillis());
           }else{
               fromCalender = false;
           }

        }
    }
}
