package com.stupidtree.hita.diy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.stupidtree.hita.R;

public class MaterialCircleAnimator {
   public static  void animShow(final View myView,int duration) {
       try {
           // 从 View 的中心开始
           int cx = (myView.getLeft() + myView.getRight()) / 2;
           int cy = (myView.getTop() + myView.getBottom()) / 2;
           int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

           //为此视图创建动画设计(起始半径为零)
           Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
           // 使视图可见并启动动画
           myView.setVisibility(View.VISIBLE);
           anim.setDuration(duration);
           anim.start();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
    public static  void animShow(final View myView,int duration,float x,float y) {

        try {
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

            //为此视图创建动画设计(起始半径为零)
            Animator anim = ViewAnimationUtils.createCircularReveal(myView, (int)x, (int)y, 0, finalRadius);
            // 使视图可见并启动动画
            myView.setVisibility(View.VISIBLE);
            anim.setDuration(duration);
            anim.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void animHide(final View myView) {
        try {
            int cx = (myView.getLeft() + myView.getRight()) / 2;
            int cy = (myView.getTop() + myView.getBottom()) / 2;

            int initialRadius = myView.getWidth();

            // 半径 从 viewWidth -> 0
            Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            });
            anim.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void animHide(final View myView,int duration,float x,float y) {
        try {
            int initialRadius = myView.getWidth();

            // 半径 从 viewWidth -> 0
            Animator anim = ViewAnimationUtils.createCircularReveal(myView, (int)x, (int)y, initialRadius, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            });
            anim.setDuration(duration);
            anim.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
