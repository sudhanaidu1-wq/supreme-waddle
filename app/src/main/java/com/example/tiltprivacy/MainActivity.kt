package com.example.tiltprivacy

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val p = getSharedPreferences("p", MODE_PRIVATE)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val label = TextView(this).apply { textSize = 18f }
        val seek = SeekBar(this).apply { max = 55; progress = p.getInt("deg", 25) - 5 }
        fun upd() { label.text = "Blackout when tilted more than ${seek.progress + 5}°" }
        upd()
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, v: Int, u: Boolean) {
                upd(); p.edit().putInt("deg", v + 5).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        val toggle = Button(this)
        fun refresh() { toggle.text = if (p.getBoolean("enabled", false)) "Stop" else "Start" }
        refresh()
        toggle.setOnClickListener {
            if (p.getBoolean("enabled", false)) {
                stopService(Intent(this, TiltService::class.java))
                p.edit().putBoolean("enabled", false).apply()
            } else {
                if (!Settings.canDrawOverlays(this)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                    Toast.makeText(this, "Allow overlay, then press Start again", Toast.LENGTH_LONG).show()
                } else {
                    if (Build.VERSION.SDK_INT >= 33)
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                    startForegroundService(Intent(this, TiltService::class.java))
                    p.edit().putBoolean("enabled", true).apply()
                }
            }
            refresh()
        }
        root.addView(label); root.addView(seek); root.addView(toggle)
        setContentView(root)
    }
}
