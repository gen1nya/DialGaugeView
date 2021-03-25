package com.android.vehicleDataService

import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread
import kotlin.math.sin

class DataGenerator (
    private val interval: Long
) {

    interface DataGeneratorCallback {
        fun onNext(speed: Double, rpm: Double)
    }

    init {
        val maxCounterValue = Math.PI * 2 * 10
        var counter = 0.0
        thread {
            while (true) {
                Thread.sleep(interval)
                counter += 0.001
                if (counter > maxCounterValue) counter = 0.0
                handler.sendMessage(handler.obtainMessage(0, getData(counter)))
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper()) {
        (it.obj as? List<Double>)?.let { data ->
            subscribers.forEach { subscriber ->
                subscriber.onNext(data[0], data[1])
            }
        }
        true
    }

    private val subscribers = mutableListOf<DataGeneratorCallback>()

    fun addSubscriber(subscriber: DataGeneratorCallback) = subscribers.add(subscriber)

    fun removeSubscriber(subscriber: DataGeneratorCallback) = subscribers.remove(subscriber)

    companion object {

        private const val RPM_MAX = 10000
        private const val RPM0_K = 0.1 * RPM_MAX
        private const val RPM1_K = 0.21 * RPM_MAX
        private const val RPM2_K = 0.19 * RPM_MAX

        private const val RPM0_OFFSET = 5000
        private const val RPM1_OFFSET = 0
        private const val RPM2_OFFSET = 0

        private const val RPM_PERIOD_K0 = 1
        private const val RPM_PERIOD_K1 = 0.4
        private const val RPM_PERIOD_K2 = 3

        private const val SPEED_MAX = 280
        private const val SPEED0_K = 0.46 * SPEED_MAX
        private const val SPEED1_K = 0.0357 * SPEED_MAX

        private const val SPEED0_OFFSET = 140
        private const val SPEED1_OFFSET = 0

        private const val SPEED_PERIOD_K0 = 1
        private const val SPEED_PERIOD_K1 = 10

        fun getData(counter: Double): List<Double> {
            val speedSource0 = sin(counter * SPEED_PERIOD_K0) * SPEED0_K + SPEED0_OFFSET
            val speedSource1 = sin(counter * SPEED_PERIOD_K1) * SPEED1_K + SPEED1_OFFSET
            val speedData = speedSource0 + speedSource1

            val rpmSource0 = sin(counter * RPM_PERIOD_K0) * RPM0_K + RPM0_OFFSET
            val rpmSource1 = sin(counter * RPM_PERIOD_K1) * RPM1_K + RPM1_OFFSET
            val rpmSource2 = sin(counter * RPM_PERIOD_K2) * RPM2_K + RPM2_OFFSET
            val rpmData = rpmSource0 + rpmSource1 + rpmSource2

            return listOf(speedData, rpmData)
        }
    }


}