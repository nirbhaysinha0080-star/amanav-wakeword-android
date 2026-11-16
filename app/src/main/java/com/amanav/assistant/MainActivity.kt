

package com.amanav.assistant

import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {

    private val micCode = 101
    private val notificationCode = 102
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(30, 30, 30, 30)

        status = TextView(this)
        status.text = "Namaste Nirbhay!\n\nAmanav is ready."
        status.textSize = 22f
        status.gravity = Gravity.CENTER

        val button = Button(this)
        button.text = "START LISTENING"

        button.setOnClickListener {
            checkPermissionsAndStart()
        }

        layout.addView(status)
        layout.addView(button)
        setContentView(layout)
    }

    private fun checkPermissionsAndStart() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), micCode
            )
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                notificationCode
            )
            return
        }

        startAmanavService()
    }

    private fun startAmanavService() {
        val intent = Intent(this, WakeWordService::class.java)

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        status.text = "Amanav service started.\nWake-word setup pending."
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == micCode) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndStart()
            } else {
                status.text = "Microphone permission required."
            }
        } else if (requestCode == notificationCode) {
            startAmanavService()
        }
    }
}

