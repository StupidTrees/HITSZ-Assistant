package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.List;


public class HorizontalSelectedView extends View {
    private Context context;
    private List<String> strings;
    private int seeSize;
    private int anInt;
    private TextPaint textPaint;
    private boolean firstVisible;
    private int width;
    private int height;
    private Paint selectedPaint;
    private int index;
    private float downX;
    private float anOffset;
    private float selectedTextSize;
    private int selectedColor;
    private float textSize;
    private int textColor;
    private Rect rect;
    private int textWidth;
    private int textHeight;
    private int centerTextHeight;

    public HorizontalSelectedView(Context context) {
        this(context, null);
    }

    public HorizontalSelectedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalSelectedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.strings = new ArrayList();
        this.seeSize = 5;
        this.firstVisible = true;
        this.rect = new Rect();
        this.textWidth = 0;
        this.textHeight = 0;
        this.centerTextHeight = 0;
        this.context = context;
        this.setWillNotDraw(false);
        this.setClickable(true);
        this.initAttrs(attrs);
        this.initPaint();
    }

    private void initPaint() {
        this.textPaint = new TextPaint(1);
        this.textPaint.setTextSize(this.textSize);
        this.textPaint.setColor(this.textColor);
        this.selectedPaint = new TextPaint(1);
        this.selectedPaint.setColor(this.selectedColor);
        this.selectedPaint.setTextSize(this.selectedTextSize);
    }

    @SuppressLint({"RestrictedApi", "ResourceType"})
    private void initAttrs(AttributeSet attrs) {
        TintTypedArray tta = TintTypedArray.obtainStyledAttributes(this.getContext(), attrs, R.styleable.HorizontalselectedView);
        this.seeSize = tta.getInteger(R.styleable.HorizontalselectedView_HorizontalselectedViewSeesize, 5);
        this.selectedTextSize = tta.getFloat(R.styleable.HorizontalselectedView_HorizontalselectedViewSelectedTextSize, 50.0F);
        this.selectedColor = tta.getColor(R.styleable.HorizontalselectedView_HorizontalselectedViewSelectedTextColor, this.context.getResources().getColor(17170444));
        this.textSize = tta.getFloat(R.styleable.HorizontalselectedView_HorizontalselectedViewTextSize, 40.0F);
        this.textColor = tta.getColor(R.styleable.HorizontalselectedView_HorizontalselectedViewTextColor, this.context.getResources().getColor(17170432));
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.e("action", "onTouchEvent: " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.downX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                this.anOffset = 0.0F;
                this.invalidate();
                if (listener != null) {
                    listener.onSelect(getSelectedString(), index);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float scrollX = event.getX();
                if (this.index != 0 && this.index != this.strings.size() - 1) {
                    this.anOffset = scrollX - this.downX;
                } else {
                    this.anOffset = (float) ((double) (scrollX - this.downX) / 1.5D);
                }

                if (scrollX > this.downX) {
                    if (scrollX - this.downX >= (float) this.anInt && this.index > 0) {
                        this.anOffset = 0.0F;
                        --this.index;
                        this.downX = scrollX;
                    }
                } else if (this.downX - scrollX >= (float) this.anInt && this.index < this.strings.size() - 1) {
                    this.anOffset = 0.0F;
                    ++this.index;
                    this.downX = scrollX;
                }
                this.invalidate();
        }

        return super.onTouchEvent(event);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.firstVisible) {
            this.width = this.getWidth();
            this.height = this.getHeight();
            this.anInt = this.width / this.seeSize;
            this.firstVisible = false;
        }

        if (this.index >= 0 && this.index <= this.strings.size() - 1) {
            String s = this.strings.get(this.index);
            this.selectedPaint.getTextBounds(s, 0, s.length(), this.rect);
            int centerTextWidth = this.rect.width();
            this.centerTextHeight = this.rect.height();
            canvas.drawText(this.strings.get(this.index), (float) (this.getWidth() / 2 - centerTextWidth / 2) + this.anOffset, (float) (this.getHeight() / 2 + this.centerTextHeight / 2), this.selectedPaint);

            for (int i = 0; i < this.strings.size(); ++i) {
                if (this.index > 0 && this.index < this.strings.size() - 1) {
                    this.textPaint.getTextBounds(this.strings.get(this.index - 1), 0, this.strings.get(this.index - 1).length(), this.rect);
                    int width1 = this.rect.width();
                    this.textPaint.getTextBounds(this.strings.get(this.index + 1), 0, this.strings.get(this.index + 1).length(), this.rect);
                    int width2 = this.rect.width();
                    this.textWidth = (width1 + width2) / 2;
                }

                if (i == 0) {
                    this.textPaint.getTextBounds(this.strings.get(0), 0, this.strings.get(0).length(), this.rect);
                    this.textHeight = this.rect.height();
                }

                if (i != this.index) {
                    canvas.drawText(this.strings.get(i), (float) ((i - this.index) * this.anInt + this.getWidth() / 2 - this.textWidth / 2) + this.anOffset, (float) (this.getHeight() / 2 + this.textHeight / 2), this.textPaint);
                }
            }
        }

    }

    public void setSeeSize(int seeSizes) {
        if (this.seeSize > 0) {
            this.seeSize = seeSizes;
            this.invalidate();
        }

    }

    public void setAnLeftOffset() {
        if (this.index < this.strings.size() - 1) {
            ++this.index;
            this.invalidate();
        }

    }

    public void setAnRightOffset() {
        if (this.index > 0) {
            --this.index;
            this.invalidate();
        }

    }

    public void setData(List<String> strings) {
        this.strings = strings;
        this.index = strings.size() / 2;
        this.invalidate();
    }

    public String getSelectedString() {
        return this.strings.size() != 0 ? this.strings.get(this.index) : null;
    }

    private OnSelectListener listener;

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    public interface OnSelectListener {
        void onSelect(String selectStr, int index);
    }
}