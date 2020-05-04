package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.util.EventsUtils;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.themeCore;

public class PickSimpleDateDialog extends RoundedCornerDialog {
    BaseActivity context;
    private TextView dialogTitle;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private CalendarView calendarView ;
    private Calendar date;
    private mCalendarDateChangeListener calendarDateChangeListener ;

    public interface onDialogConformListener{
        void onConfirm(Calendar date);
    }
    public PickSimpleDateDialog(BaseActivity context, onDialogConformListener onDialogConformListener){
        super(context);
        mOnDialogConformListener = onDialogConformListener;
        this.context = context;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeCore.getCurrentThemeID());// your app theme here
        View view = getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_pick_simple_date,null,false);
        setView(view);
        initViews(view);
    }

    public PickSimpleDateDialog setInitialValue(int year,int month,int day){
        date.set(Calendar.YEAR,year);
        date.set(Calendar.MONTH,month-1);
        date.set(Calendar.DAY_OF_MONTH,day);
        return this;
    }

    public PickSimpleDateDialog setInitialValue(Calendar date){
        this.date.setTimeInMillis(date.getTimeInMillis());
        return this;
    }


    void initViews(View view){
        done = view.findViewById(R.id.done);
        calendarView = view.findViewById(R.id.ade_calendarview);
        date = Calendar.getInstance();
        dialogTitle = view.findViewById(R.id.dialog_title);
        calendarDateChangeListener = new mCalendarDateChangeListener();

        calendarView.setOnDateChangeListener(calendarDateChangeListener);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnDialogConformListener.onConfirm(date);
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        calendarView.setDate(date.getTimeInMillis());
        dialogTitle.setText(EventsUtils.getDateString(date, true, EventsUtils.TTY_REPLACE));
    }

    class mCalendarDateChangeListener implements CalendarView.OnDateChangeListener {


        @SuppressLint("SetTextI18n")
        @Override
        public void onSelectedDayChange( CalendarView view, int year, int month, int dayOfMonth) {
            date.set(year,month,dayOfMonth);
            dialogTitle.setText(context.getResources().getStringArray(R.array.months)[date.get(Calendar.MONTH)] +String.format(context.getString(R.string.date_day),date.get(Calendar.DAY_OF_MONTH)));
        }
    }

}
