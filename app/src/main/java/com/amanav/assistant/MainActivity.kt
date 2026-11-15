
package com.amanav.assistant

import android.app.Activity
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {

    private val micPermission = Manifest.permission.RECORD_AUDIO
    private val requestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(30, 30, 30, 30)

        val text = TextView(this)
        text.text = "Namaste Nirbhay!\\n\\nAmanav is ready."
        text.textSize = 24f
        text.gravity = Gravity.CENTER

        val button = Button(this)
        button.text = "START LISTENING"

        button.setOnClickListener {
            if (checkSelfPermission(micPermission) ==
                PackageManager.PERMISSION_GRANTED) {
                text.text = "Microphone permission granted!\\n\\nWake-word setup next."
            } else {
                requestPermissions(arrayOf(micPermission), requestCode)
            }
        }

        layout.addView(text)
        layout.addView(button)
        setContentView(layout)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == this.requestCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate()
        }
    }
}
