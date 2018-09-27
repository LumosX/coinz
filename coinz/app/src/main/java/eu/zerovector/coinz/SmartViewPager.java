package eu.zerovector.coinz;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SmartViewPager extends ViewPager {

    public SmartViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // We want to disable swiping to drag pages whilst on the map (which is page zero)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCurrentItem() == 0) return false;
        else return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (getCurrentItem() == 0) return false;
        else return true;
    }
}
