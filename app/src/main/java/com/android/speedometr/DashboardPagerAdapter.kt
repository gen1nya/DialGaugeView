package com.android.speedometr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter


class DashboardPagerAdapter : PagerAdapter() {

    companion object {
        const val ITEM_SPEEDOMETER_INDEX = 0
        const val ITEM_TACHOMETER_INDEX = 1
    }

    fun setSpeed(speed: Double) {
        views[ITEM_SPEEDOMETER_INDEX].setData(speed)
    }

    fun setRpm(rpm: Double) {
        views[ITEM_TACHOMETER_INDEX].setData(rpm)
    }

    private val views: MutableList<com.android.dialgaugeview.DialGaugeView> = mutableListOf()

    override fun getCount(): Int = 2

    override fun instantiateItem(container: ViewGroup, position: Int): Any =
        when (position) {
            ITEM_SPEEDOMETER_INDEX -> {
                LayoutInflater.from(container.context)
                    .inflate(R.layout.layout_speedometer, container, false)
                    .also {
                        views.add(position, it.findViewById(R.id.vSpeedometer))
                        container.addView(it)
                    }

            }
            ITEM_TACHOMETER_INDEX -> {
                LayoutInflater.from(container.context)
                    .inflate(R.layout.layout_tachometer, container, false)
                    .also {
                        views.add(position, it.findViewById(R.id.vTachometer))
                        container.addView(it)
                    }
            }
            else -> throw IndexOutOfBoundsException("index ($position) is out of bounds")
        }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`


}