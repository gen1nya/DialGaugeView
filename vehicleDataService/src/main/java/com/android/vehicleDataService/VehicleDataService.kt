package com.android.vehicleDataService

import android.app.Service
import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import com.android.aidl.VehicleDataCallback
import com.android.aidl.IVehicleData

class VehicleDataService: Service(), DataGenerator.DataGeneratorCallback {

    private var callback: VehicleDataCallback? = null
    private val generator = DataGenerator(1L)

    override fun onBind(intent: Intent?): IBinder = object: IVehicleData.Stub() {
        override fun registerCallback(callback: VehicleDataCallback?) {
            this@VehicleDataService.callback = callback
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        callback = null
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        generator.addSubscriber(this)
    }

    override fun onDestroy() {
        generator.removeSubscriber(this)
        super.onDestroy()
    }

    override fun onNext(speed: Double, rpm: Double) {
        try {
            callback?.onNext(speed, rpm)
        } catch (e: DeadObjectException) {
            e.printStackTrace()
            callback = null
            stopSelf()
        }
    }
}