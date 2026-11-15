
package com.amanav.assistant

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "Namaste Nirbhay!\n\nAmanav is ready."
        text.textSize = 24f
        text.gravity = Gravity.CENTER

        setContentView(text)
    }
}
