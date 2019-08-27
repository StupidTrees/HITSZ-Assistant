//package com.stupidtree.hita.diy;
//
//import android.animation.Animator;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.animation.ValueAnimator;
//import android.view.animation.DecelerateInterpolator;
//import android.view.animation.LinearInterpolator;
//
//import com.gelitenight.waveview.library.WaveView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class WaveViewHelper {
//    private WaveView mWaveView;
//    private AnimatorSet mAnimatorSet;
//
//    public WaveViewHelper(WaveView waveView,float initlevel) {
//        mWaveView = waveView;
//        initAnimation(initlevel);
//    }
//
//
//    public void start() {
//        mWaveView.setShowWave(true);
//        if (mAnimatorSet != null) {
//            mAnimatorSet.start();
//        }
//    }
//
//    private void initAnimation(float initLevel) {
//        List<Animator> animators = new ArrayList<>();
//
//        // horizontal animation.
//        // wave waves infinitely.
//        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
//                mWaveView, "waveShiftRatio", 0f, 1f);
//        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
//        waveShiftAnim.setDuration(1000);
//        waveShiftAnim.setInterpolator(new LinearInterpolator());
//        animators.add(waveShiftAnim);
//
//
//        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
//                mWaveView, "waterLevelRatio",0, initLevel);
//        waterLevelAnim.setDuration(1200);
//        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
//        animators.add(waterLevelAnim);
//
//        // amplitude animation.
//        // wave grows big then grows small, repeatedly
//        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
//                mWaveView, "amplitudeRatio", 0.01f, 0.05f);
//        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
//        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
//        amplitudeAnim.setDuration(4000);
//        amplitudeAnim.setInterpolator(new LinearInterpolator());
//        animators.add(amplitudeAnim);
//
//        mAnimatorSet = new AnimatorSet();
//        mAnimatorSet.playTogether(animators);
//    }
//
//
//    public void cancel() {
//        if (mAnimatorSet != null) {
//             //mAnimatorSet.cancel();
//            mAnimatorSet.end();
//        }
//    }
//}