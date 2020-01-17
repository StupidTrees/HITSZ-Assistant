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

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.themeID;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class PickSimpleDateDialog extends AlertDialog{
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
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeID);// your app theme here
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
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
        dialogTitle.setText(context.getResources().getStringArray(R.array.months)[date.get(Calendar.MONTH)] +String.format(context.getString(R.string.date_day),date.get(Calendar.DAY_OF_MONTH)));
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
