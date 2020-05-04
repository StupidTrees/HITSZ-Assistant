package com.stupidtree.hita.views.pullextend;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.stupidtree.hita.R;

/**
 * Created by Renny on 2018/1/2.
 */

public class ExpendPoint extends View {

    float percent;
    float maxRadius = 15;
    float maxDist = 60;
    Paint mPaint;
    Paint outerPaint;

    public ExpendPoint(Context context) {
        this(context, null);
    }

    public ExpendPoint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpendPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        outerPaint = new Paint();
        mPaint.setAntiAlias(true);
        outerPaint.setAntiAlias(true);
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ExpendPoint, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i <= n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.ExpendPoint_pullDownAnimElementColor:
                    mPaint.setColor(a.getColor(attr, Color.GRAY));
                    outerPaint.setColor(a.getColor(attr, Color.GRAY));
                    break;
            }
        }
    }


    public void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    public void setMaxDist(float maxDist) {
        this.maxDist = maxDist;
    }

    public void setPercent(float percent) {
        if (percent != this.percent) {
            this.percent = percent;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        maxRadius = getHeight() / 2f;
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        if (percent <= 0.5f) {
            mPaint.setAlpha(255);
            outerPaint.setAlpha(64);
            float radius = percent * 2 * maxRadius;
            canvas.drawCircle(centerX, centerY, radius / 3, mPaint);
            canvas.drawCircle(centerX, centerY, radius, outerPaint);
        } else {
            float afterPercent = (percent - 0.5f) / 0.5f;
            float radius = maxRadius - maxRadius / 2 * afterPercent;
            canvas.drawCircle(centerX, centerY, radius / (3 - 1.5f * afterPercent), mPaint);
            canvas.drawCircle(centerX, centerY, radius, outerPaint);
//            canvas.drawCircle(centerX - afterPercent * maxDist, centerY, maxRadius / 2, mPaint);
//            canvas.drawCircle(centerX + afterPercent * maxDist, centerY, maxRadius / 2, mPaint);
        }
    }
}
