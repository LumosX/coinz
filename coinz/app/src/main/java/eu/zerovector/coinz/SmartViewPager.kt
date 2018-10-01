package eu.zerovector.coinz

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import eu.zerovector.coinz.Data.bool


class SmartViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var oldX: Float = 0.0f
    private var oldY: Float = 0.0f

    // Only allow movement if it's vertical; otherwise allow everything normally.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return CheckDirsForbidHor(event) and super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return CheckDirsForbidHor(event) and super.onInterceptTouchEvent(event)
    }

    fun CheckDirsForbidHor(event: MotionEvent): bool {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                oldX = event.x
                oldY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val newX = event.x
                val newY = event.y
                val deltaX = oldX - newX
                val deltaY = oldY - newY
                // Return "true" if the vertical delta is larger than the horizontal delta
                return Math.abs(deltaY) > Math.abs(deltaX)
            }
        }
        return true // just to be safe
    }
}