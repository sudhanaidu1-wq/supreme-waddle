package com.example.tiltprivacy

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.*
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import kotlin.math.*

class TiltService : Service(), SensorEventListener {
    private lateinit var wm: WindowManager
    private lateinit var overlay: View
    private var shown = false

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("tilt", "Tilt Privacy", NotificationManager.IMPORTANCE_MIN))
        val n = Notification.Builder(this, "tilt")
            .setContentTitle("Tilt Privacy active")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlay = View(this).apply { setBackgroundColor(Color.BLACK) }
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val s = sm.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sm.registerListener(this, s, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(e: SensorEvent) {
        val thr = getSharedPreferences("p", MODE_PRIVATE).getInt("deg", 25)
        val x = e.values[0].toDouble()
        val y = e.values[1].toDouble()
        val z = e.values[2].toDouble()
        // left/right tilt angle from upright, in degrees
        val angle = abs(Math.toDegrees(atan2(x, sqrt(y * y + z * z))))
        if (!shown && angle > thr) show()
        else if (shown && angle < thr - 4) hide()   // gap prevents flicker
    }

    private fun show() {
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT)
        try { wm.addView(overlay, lp); shown = true } catch (_: Exception) {}
    }

    private fun hide() {
        try { wm.removeView(overlay) } catch (_: Exception) {}
        shown = false
    }

    override fun onDestroy() {
        (getSystemService(Context.SENSOR_SERVICE) as SensorManager).unregisterListener(this)
        if (shown) hide()
        super.onDestroy()
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    override fun onBind(i: Intent?): IBinder? = null
}
