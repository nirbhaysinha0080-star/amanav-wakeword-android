
package com.amanav.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    private val voiceRequest = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 60, 40, 40)

        val title = TextView(this)
        title.text = "AMANAV AI"
        title.textSize = 28f

        val subtitle = TextView(this)
        subtitle.text = "Namaste Nirbhay ❤️"
        subtitle.textSize = 20f

        statusText = TextView(this)
        statusText.text = "Amanav ready hai."
        statusText.textSize = 18f
        statusText.setPadding(0, 30, 0, 30)

        val speakButton = Button(this)
        speakButton.text = "🎤 Bolo Amanav"
        speakButton.setOnClickListener {
            startVoiceInput()
        }

        val repeatButton = Button(this)
        repeatButton.text = "🔄 Dobara Bolo"
        repeatButton.setOnClickListener {
            startVoiceInput()
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(statusText)
        layout.addView(speakButton)
        layout.addView(repeatButton)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun startVoiceInput() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "hi-IN"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Boliye Nirbhay..."
        )

        try {
            startActivityForResult(intent, voiceRequest)
        } catch (e: Exception) {
            statusText.text = "Voice input available nahi hai."
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            results
        )

        if (requestCode == 100 &&
            results.isNotEmpty() &&
            results[0] == PackageManager.PERMISSION_GRANTED) {

            startVoiceInput()
        }
    }

    @Deprecated("Legacy speech input")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequest &&
            resultCode == RESULT_OK) {

            val text = data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                ?.firstOrNull()

            if (!text.isNullOrBlank()) {
                askAmanav(text)
            } else {
                statusText.text = "Kuch sunai nahi diya."
            }
        }
    }

    private fun askAmanav(question: String) {

        statusText.text =
            "Tum: $question\n\nAmanav soch raha hai..."

        thread {

            try {

                val url = URL(
                    "http://127.0.0.1:8080/v1/chat/completions"
                )

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 120000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val messages = JSONArray()

                messages.put(
                    JSONObject()
                        .put("role", "system")
                        .put(
                            "content",
                            "Tum Amanav ho, Nirbhay ke personal AI assistant. Hindi mein natural, friendly aur short jawab do."
                        )
                )

                messages.put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", question)
                )

                val body = JSONObject()
                    .put("messages", messages)
                    .put("temperature", 0.7)
                    .put("max_tokens", 512)

                connection.outputStream.use {
                    it.write(
                        body.toString().toByteArray(
                            Charsets.UTF_8
                        )
                    )
                }

                val code = connection.responseCode

                val stream =
                    if (code in 200..299)
                        connection.inputStream
                    else
                        connection.errorStream

                val response =
                    stream.bufferedReader().use {
                        it.readText()
                    }

                connection.disconnect()

                if (code !in 200..299) {
                    throw Exception("Server error: $code")
                }

                val answer = JSONObject(response)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                runOnUiThread {

                    statusText.text =
                        "Tum: $question\n\nAmanav: $answer"

                    if (ttsReady) {
                        tts.speak(
                            answer,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "amanav"
                        )
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    statusText.text =
                        "Amanav server se connect nahi hua.\n\n" +
                        "Termux mein llama-server chalu karo.\n\n" +
                        e.message
                }
            }
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts.setLanguage(
                Locale("hi", "IN")
            )

            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}

