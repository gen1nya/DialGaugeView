package com.android.speedometr

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class TwoFingerScrollViewPager(
    context: Context,
    attributeSet: AttributeSet
): ViewPager(context, attributeSet) {

    private var readyForSecondFinger = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        try {
            val action = ev?.action
            val counter = ev?.pointerCount
            if (counter == 1 && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) && !readyForSecondFinger) {
                if (action == MotionEvent.ACTION_MOVE) readyForSecondFinger = true
                return super.onTouchEvent(ev)
            }
            if (counter == 2 && (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_POINTER_UP) && readyForSecondFinger) {
                if (action == MotionEvent.ACTION_POINTER_UP) readyForSecondFinger = false
                return super.onTouchEvent(ev)
            }
            ev?.action = MotionEvent.ACTION_UP
            return super.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            // IllegalArgumentException: pointerIndex out of range
            // hello from 2011? android 2.1
            e.printStackTrace()
            return false
        }
    }

}