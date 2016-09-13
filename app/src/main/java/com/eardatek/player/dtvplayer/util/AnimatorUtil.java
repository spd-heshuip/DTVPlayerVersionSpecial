package com.eardatek.player.dtvplayer.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by Administrator on 16-9-1.
 */
public class AnimatorUtil {

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();


    public static void animateIn(final View view,float start,float end,int duration){
        LogUtil.i("EardatekVersion2Activity","animateIn start tranlationY" + view.getTranslationY());
        view.setVisibility(View.VISIBLE);
        ObjectAnimator titleBarTranslateAnimator = ObjectAnimator.ofFloat(view,"translationY",
                start,end);
        titleBarTranslateAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogUtil.i("EardatekVersion2Activity","animateIn after tranlationY" + view.getTranslationY());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        titleBarTranslateAnimator.setDuration(duration).start();
    }

    public static void animateOut(final View view,float end,int duration){
        LogUtil.i("EardatekVersion2Activity","animateOut start tranlationY" + view.getTranslationY());
        ObjectAnimator titleBarTranslateAnimator = ObjectAnimator.ofFloat(view,"translationY",0,end);
        titleBarTranslateAnimator.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.INVISIBLE);
                LogUtil.i("EardatekVersion2Activity","animateOut after tranlationY" + view.getTranslationY());
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

    public static void animateInCompat(View view){
        view.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(view).translationY(0)
                    .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                    .start();
        }
    }

    public static void animateOutCompat(View view,float end){
        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(view).translationY(end ).setInterpolator(INTERPOLATOR).withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {
                        public void onAnimationStart(View view) {
                        }

                        public void onAnimationCancel(View view) {
                        }

                        public void onAnimationEnd(View view) {
                            view.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }

    public static void alphaAnimatorIn(View view,float start,float end,int duration){
        view.setVisibility(View.VISIBLE);
        ObjectAnimator titleBarTranslateAnimator = ObjectAnimator.ofFloat(view,"alpha",
                start,end);
        titleBarTranslateAnimator.addListener(new Animator.AnimatorListener() {
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
        titleBarTranslateAnimator.setDuration(duration).start();
    }

    public static void alphaAnimatorIOut(final View view, float start, float end, int duration){
        ObjectAnimator animator = ObjectAnimator.ofFloat(view,"alpha",start,end);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(duration).start();
    }
}
