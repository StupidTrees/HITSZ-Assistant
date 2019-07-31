package com.stupidtree.hita.diy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;


public class RevealAnimation {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    private final View mView;
    private Activity mActivity;
    private int revealX;
    private int revealY;

    public RevealAnimation(View view, Intent intent, Activity activity) {
        mView = view;
        mActivity = activity;
        //when you're android version is at leat Lollipop it starts the reveal activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            view.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {

            //if you are below android 5 it jist shows the activity
            view.setVisibility(View.VISIBLE);
        }
    }
    public void revealActivity(int x, int y, Animator.AnimatorListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(mView.getWidth(), mView.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(mView, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            mView.setVisibility(View.VISIBLE);
            circularReveal.addListener(listener);
            circularReveal.start();

        } else {
            mActivity.finish();
        }
    }
    public void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(mView.getWidth(), mView.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(mView, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            mView.setVisibility(View.VISIBLE);
            circularReveal.start();
            circularReveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            mActivity.finish();
        }
    }
    public void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mActivity.finish();
        } else {
            float finalRadius = (float) (Math.max(mView.getWidth(), mView.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    mView, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(350);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mView.setVisibility(View.INVISIBLE);
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);
                }
            });

            circularReveal.start();
        }
    }

    public void unRevealActivity(Animator.AnimatorListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mActivity.finish();
        } else {
            float finalRadius = (float) (Math.max(mView.getWidth(), mView.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    mView, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(350);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mView.setVisibility(View.INVISIBLE);
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);
                }
            });

            circularReveal.addListener(listener);
            circularReveal.start();
        }
    }

}