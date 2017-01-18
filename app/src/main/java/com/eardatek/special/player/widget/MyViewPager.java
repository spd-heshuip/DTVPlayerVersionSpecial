package com.eardatek.special.player.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 16-4-14.
 */
public class MyViewPager extends ViewPager{

    private float xDistance,xLast;
    private boolean noScroll = false;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (noScroll)
            return false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = 0f;
                xLast = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                //第一个Fragment禁止右滑
                if (xLast - curX < 0 && getCurrentItem() == 0) {
                    return false;
                }
                //最后一个Fragment禁止左滑
//                if (xLast - curX > 0 && getCurrentItem() == 1) {
//                    return false;
//                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setNoScroll(boolean noScroll){
        this.noScroll = noScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !noScroll && super.onTouchEvent(ev);
    }
}
