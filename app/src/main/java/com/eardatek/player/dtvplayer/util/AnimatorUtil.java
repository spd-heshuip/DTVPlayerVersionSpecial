package com.eardatek.player.dtvplayer.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by Administrator on 16-9-1.
 */
public class AnimatorUtil {

    public static void animateIn(final View view,float start,float end,int duration){

        view.setVisibility(View.VISIBLE);
        ObjectAnimator titleBarTranslateAnimator = ObjectAnimator.ofFloat(view,"translationY",
                start,end);

        titleBarTranslateAnimator.setDuration(duration).start();
    }

    public static void animateOut(final View view,float start,int duration){
        ObjectAnimator titleBarTranslateAnimator = ObjectAnimator.ofFloat(view,"translationY",start);
        titleBarTranslateAnimator.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        titleBarTranslateAnimator.start();
    }

    public static void alphaIn(final View view, float start, float end, int duration){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view,"alpha",start,end);
        animator.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
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
        animator.start();
    }
}
