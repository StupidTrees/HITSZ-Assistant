package com.stupidtree.hita.views.pullextend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class mRecyclerView extends RecyclerView {

    private float lastMotionY = -1;

    public mRecyclerView(@NonNull Context context) {
        super(context);
    }

    public mRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public mRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        ViewParent p = getParent();
        PullExtendLayout parent;
        if (p instanceof PullExtendLayout) {
            parent = (PullExtendLayout) p;
        } else return super.onTouchEvent(e);
        switch (e.getAction()) {
            //触摸开始、结束时，更新父布局状态
            case MotionEvent.ACTION_DOWN:
                lastMotionY = e.getY();
                parent.onTouchEvent(e);
                break;
//            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                lastMotionY = -1;
                parent.onTouchEvent(e);
                break;
            //触摸滑动时，若已达上下滑动边界，则只调用父布局的动作
            //若未达上下边界，还是要更新以下父布局的状态（上次触摸位置）防止效果错位
            case MotionEvent.ACTION_MOVE:
                float delta = e.getY() - lastMotionY;
                lastMotionY = e.getY();
                if (delta > 0 && !canScrollVertically(-1)) {
                    return parent.onTouchEvent(e);
                } else if (delta < 0 && !canScrollVertically(1)) {
                    parent.pullFooterLayout(delta);
                }
                parent.callWhenChildScrolled(e);
                break;
        }
        return super.onTouchEvent(e);

    }


}
