package com.jarvis.ai

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import android.widget.*
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(40,70,40,40)

        val title = TextView(this)
        title.text = "🤖 Jarvis AI"
        title.textSize = 30f

        status = TextView(this)
        status.text = "Namaste! Mic dabao aur bolo."
        status.textSize = 18f
        status.setPadding(0,40,0,40)

        val mic = Button(this)
        mic.text = "🎙️ Bolo"
        mic.textSize = 20f

        box.addView(title)
        box.addView(status)
        box.addView(mic)
        setContentView(box)

        tts = TextToSpeech(this,this)

        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),100)
        }

        mic.setOnClickListener { listen() }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi","IN")
        }
    }

    private fun speak(text:String) {
        status.text = text
        tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"jarvis")
    }

    private fun listen() {
        val r = SpeechRecognizer.createSpeechRecognizer(this)
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi-IN")

        status.text = "🎧 Sun raha hoon..."

        r.setRecognitionListener(object: RecognitionListener {
            override fun onResults(b:Bundle) {
                val text = b.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )?.firstOrNull() ?: ""
                r.destroy()
                command(text.lowercase(Locale.getDefault()))
            }
            override fun onError(e:Int) {
                r.destroy()
                speak("Command samajh nahi aayi. Dobara bolo.")
            }
            override fun onReadyForSpeech(p:Bundle?){}
            override fun onBeginningOfSpeech(){}
            override fun onRmsChanged(v:Float){}
            override fun onBufferReceived(b:ByteArray?){}
            override fun onEndOfSpeech(){}
            override fun onPartialResults(b:Bundle?){}
            override fun onEvent(t:Int,b:Bundle?){}
        })
        r.startListening(i)
    }

    private fun command(c:String) {
        when {
            c.contains("youtube") -> {
                speak("YouTube khol raha hoon.")
                open("https://www.youtube.com")
            }
            c.contains("whatsapp") -> {
                speak("WhatsApp khol raha hoon.")
                packageManager.getLaunchIntentForPackage("com.whatsapp")?.let {
                    startActivity(it)
                } ?: speak("WhatsApp nahi mila.")
            }
            c.contains("google") || c.contains("search") -> {
                speak("Google khol raha hoon.")
                open("https://www.google.com")
            }
            c.contains("timer") -> {
                speak("5 minute ka timer laga raha hoon.")
                val i = Intent(AlarmClock.ACTION_SET_TIMER)
                i.putExtra(AlarmClock.EXTRA_LENGTH,300)
                i.putExtra(AlarmClock.EXTRA_SKIP_UI,false)
                startActivity(i)
            }
            c.contains("setting") -> {
                speak("Settings khol raha hoon.")
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            }
            c.contains("stop") || c.contains("band") -> speak("Theek hai.")
            else -> speak("Maine suna: $c")
        }
    }

    private fun open(url:String) {
        startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
