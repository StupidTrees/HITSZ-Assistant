package com.stupidtree.hita.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;

public class MaterialCircleAnimator {
   public static  void animShow(final View myView,int duration) {
       try {
           //duration*=1.7;
           // 从 View 的中心开始
           int cx = (myView.getLeft() + myView.getRight()) / 2;
           int cy = (myView.getTop() + myView.getBottom()) / 2;
           int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

           //为此视图创建动画设计(起始半径为零)
           Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
           // 使视图可见并启动动画
          // anim.setInterpolator(new DecelerateInterpolator());
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


    public static void rotateTo(boolean down, View view) {
        float fromD, toD;
        if (down) {
            fromD = 0f;
            toD = 180f;
        } else {
            fromD = 180f;
            toD = 0f;
        }
        RotateAnimation ra = new RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setInterpolator(new DecelerateInterpolator());
        ra.setDuration(200);//设置动画持续周期
        ra.setRepeatCount(0);//设置重复次数
        ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        view.setAnimation(ra);
        view.startAnimation(ra);
    }
}
