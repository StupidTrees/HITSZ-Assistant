package com.stupidtree.hita.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class TimeTableNowLine extends View {
    int color;
    public TimeTableNowLine(Context context,int color) {
        super(context);
        this.color = color;
        setBackgroundColor(color);
        setAlpha(0.65f);
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}
