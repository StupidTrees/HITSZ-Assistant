package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.internal.ViewUtils;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.timetable.timetable.TimePeriod;
import com.stupidtree.hita.util.ColorBox;

import java.util.List;

import static com.stupidtree.hita.HITAApplication.defaultSP;

public class TimeTableBlockAddView extends FrameLayout {
    View card;
    View add;
    int dow;
    int week;
    TimePeriod timePeriod;

    public TimeTableBlockAddView(@NonNull final BaseActivity context, final int week, final int dow, final TimePeriod timePeriod) {
        super(context);
        this.dow = dow;
        this.week = week;
        this.timePeriod = timePeriod;
        inflate(context,R.layout.dynamic_timetable_block_add,this);
        add = findViewById(R.id.add);
        card = findViewById(R.id.card);
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new FragmentAddEvent().setInitialData(week,dow,timePeriod).show(context.getSupportFragmentManager(),"fae");
                ViewGroup parent = (ViewGroup) getParent();
                if(parent!=null) parent.removeView(TimeTableBlockAddView.this);
            }
        });
        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                add.callOnClick();
            }
        });
    }

    public int getDow() {
        return dow;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public int getDuration(){
        return timePeriod==null?0:timePeriod.getLength();
    }
}
