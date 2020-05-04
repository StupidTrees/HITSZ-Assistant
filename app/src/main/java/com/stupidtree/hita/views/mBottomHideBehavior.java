package com.stupidtree.hita.views;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.animation.AnimationUtils;

public class mBottomHideBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    private int height = 0;
    private int currentState = 2;
    private ViewPropertyAnimator currentAnimator;
    private boolean fabSlideEnable = true;

    public mBottomHideBehavior() {
    }

    public mBottomHideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFabSlideEnable(boolean fabSlideEnable) {
        this.fabSlideEnable = fabSlideEnable;
    }

    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        this.height = child.getMeasuredHeight();
        return super.onLayoutChild(parent, child, layoutDirection);
    }

//    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
//        return nestedScrollAxes == 2;
//    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == 2;
        //  return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    //    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        if (this.currentState != 1 && dyConsumed > 0) {
//            this.slideDown(child);
//        } else if (this.currentState != 2 && dyConsumed < 0) {
//            this.slideUp(child);
//        }
//    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (!fabSlideEnable) return;
        if (this.currentState != 1 && dyConsumed > 0) {
            this.slideDown(child);
        } else if (this.currentState != 2 && dyConsumed < 0) {
            this.slideUp(child);
        }
        //super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
    }

    public void slideUp(V child) {
        if (this.currentAnimator != null) {
            this.currentAnimator.cancel();
            child.clearAnimation();
        }

        this.currentState = 2;

        this.animateChildTo(child, 0, 225L, AnimationUtils.DECELERATE_INTERPOLATOR);
    }

    public void slideDown(V child) {
        if (this.currentAnimator != null) {
            this.currentAnimator.cancel();
            child.clearAnimation();
        }
        this.currentState = 1;
        this.animateChildTo(child, this.height+100, 175L, AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
    }

    private void animateChildTo(V child, int targetY, long duration, TimeInterpolator interpolator) {
        this.currentAnimator = child.animate().translationY((float)targetY).setInterpolator(interpolator).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
              mBottomHideBehavior.this.currentAnimator = null;
            }
        });
    }
}
