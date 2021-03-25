package com.android.speedometr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.aidl.IVehicleData
import com.android.aidl.VehicleDataCallback

class MainActivity : AppCompatActivity(), ServiceConnection {

    private var vehicleDataService: IVehicleData? = null

    private lateinit var dashboardPagerAdapter: DashboardPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dashboardPagerAdapter = DashboardPagerAdapter()
        val dashboardPager = findViewById<ViewPager>(R.id.vDashboardPager)
        dashboardPager?.adapter = dashboardPagerAdapter
    }

    override fun onStart() {
        super.onStart()
        bindService(getServiceIntent(), this, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        vehicleDataService = IVehicleData.Stub.asInterface(service)
        vehicleDataService?.registerCallback(object : VehicleDataCallback.Stub() {
            override fun onNext(speed: Double, rmp: Double) {
                dashboardPagerAdapter.setRpm(rmp)
                dashboardPagerAdapter.setSpeed(speed)
            }
        })
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        vehicleDataService = null
    }

    private fun getServiceIntent(): Intent {
        val intent = Intent("com.android.aidl.REMOTE_CONNECTION")
        val services = packageManager.queryIntentServices(intent, 0)
        if (services.isEmpty()) {
            Toast.makeText(this, "Service not found!", Toast.LENGTH_SHORT).show()
        }
        return Intent(intent).apply {
            val resolveInfo = services.first()
            val packageName = resolveInfo.serviceInfo.packageName
            val className = resolveInfo.serviceInfo.name
            component = ComponentName(packageName, className)
        }
    }
}