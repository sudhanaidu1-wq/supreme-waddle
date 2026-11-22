package com.example.tiltprivacy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val p = c.getSharedPreferences("p", Context.MODE_PRIVATE)
        if (p.getBoolean("enabled", false) && Settings.canDrawOverlays(c)) {
            try { c.startForegroundService(Intent(c, TiltService::class.java)) } catch (_: Exception) {}
        }
    }
}
