package com.jarvis.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.service.voice.VoiceInteractionSession
import java.util.Locale

class JarvisVoiceSession(
    context: Context
) : VoiceInteractionSession(context), TextToSpeech.OnInitListener {

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null
    private var finishing = false

    override fun onCreate() {
        super.onCreate()

        tts = TextToSpeech(context, this)

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(context)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}

                override fun onError(error: Int) {
                    if (!finishing) {
                        speakAndFinish(
                            "Mujhe sunne mein problem hui."
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val command =
                        matches?.firstOrNull()?.trim() ?: ""

                    handleCommand(command)
                }
            }
        )
    }

    override fun onShow(
        args: Bundle?,
        showFlags: Int
    ) {
        super.onShow(args, showFlags)

        finishing = false
        startListening()
    }

    private fun startListening() {
        val speechRecognizer =
            recognizer ?: run {
                speakAndFinish(
                    "Speech recognition available nahi hai."
                )
                return
            }

        try {
            speechRecognizer.cancel()

            val intent =
                Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                ).apply {

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        "hi-IN"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "hi-IN"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_MAX_RESULTS,
                        3
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                        false
                    )
                }

            speechRecognizer.startListening(intent)

        } catch (e: Exception) {
            speakAndFinish(
                "Main abhi sun nahi paa rahi hoon."
            )
        }
    }

    private fun handleCommand(rawCommand: String) {
        var command = rawCommand.lowercase(Locale.ROOT).trim()
        listOf("hey jarvis", "ok jarvis", "jarvis", "हे जार्विस", "ए जार्विस")
            .firstOrNull { command.startsWith(it) }
            ?.let { command = command.removePrefix(it).trim() }

        if (command.isEmpty()) {
            speakAndFinish("हाँ, बोलिए।")
            return
        }

        val accessibility = JarvisAccessibilityService.instance
        val searchWords = listOf("search", "सर्च", "खोज", "ढूंढ", "ढूँढ", "गाना", "gaana", "gana", "song", "video", "play", "chalao", "bajao", "चलाओ", "बजाओ")
        val openWords = listOf("kholo", "khol", "open", "chalao", "खोलो", "खोल", "चलाओ", "चालू करो")

        // Search intent must run before the generic YouTube-app launch branch.
        if (
            (command.contains("youtube") || command.contains("यूट्यूब")) &&
            searchWords.any { command.contains(it) }
        ) {
            val fillerWords = setOf(
                "youtube", "यूट्यूब", "par", "pe", "पर", "पे", "search", "सर्च",
                "खोजो", "खोज", "ढूंढो", "ढूँढो", "gana", "gaana", "गाना", "song", "video",
                "play", "chalao", "chala", "bajao", "चलाओ", "बजाओ", "karo", "करो",
                "ka", "ki", "ke", "में", "को"
            )
            val query = command.split(Regex("\\s+")).filterNot { it in fillerWords }.joinToString(" ").trim()
            if (query.isBlank()) {
                openUrl("https://www.youtube.com")
            } else {
                openUrl("https://www.youtube.com/results?search_query=" + Uri.encode(query))
            }
            return
        }

        when {
            (command.contains("youtube") || command.contains("यूट्यूब")) &&
                (openWords.any { command.contains(it) } || command == "youtube" || command == "यूट्यूब") -> {
                openApp("com.google.android.youtube", "YouTube")
            }

            (command.contains("whatsapp") || command.contains("व्हाट्सएप")) &&
                (openWords.any { command.contains(it) } || command == "whatsapp" || command == "व्हाट्सएप") -> {
                openApp("com.whatsapp", "WhatsApp")
            }

            command.contains("whatsapp") || command.contains("व्हाट्सएप") -> {
                speakAndFinish("WhatsApp message bhejna abhi connect nahi hai. Main app khol sakti hoon; message khud se send nahi kiya.")
            }

            (command.contains("instagram") || command.contains("इंस्टाग्राम")) &&
                (openWords.any { command.contains(it) } || command == "instagram") -> {
                openApp("com.instagram.android", "Instagram")
            }

            (command.contains("facebook") || command.contains("फेसबुक")) &&
                (openWords.any { command.contains(it) } || command == "facebook") -> {
                openApp("com.facebook.katana", "Facebook")
            }

            command.contains("bgmi") || command.contains("battleground") -> {
                openApp("com.pubg.imobile", "BGMI")
            }

            command.contains("home") || command.contains("होम") || command.contains("घर") -> {
                if (accessibility?.pressHome() == true) speakAndFinish("Home खोल दिया।")
                else speakAndFinish("पहले Jarvis की Accessibility permission चालू करें।")
            }

            command.contains("back") || command.contains("पीछे") || command.contains("वापस") -> {
                if (accessibility?.pressBack() == true) speakAndFinish("वापस आ गई।")
                else speakAndFinish("पहले Jarvis की Accessibility permission चालू करें।")
            }

            command.contains("recent") || command.contains("recents") || command.contains("हाल के ऐप") -> {
                if (accessibility?.openRecents() == true) speakAndFinish("Recent apps खोल दिए।")
                else speakAndFinish("पहले Jarvis की Accessibility permission चालू करें।")
            }

            command.contains("scroll down") || command.contains("नीचे स्क्रॉल") || command == "नीचे करो" -> {
                if (accessibility?.scrollForward() == true) speakAndFinish("नीचे स्क्रॉल कर दिया।")
                else speakAndFinish("इस स्क्रीन पर स्क्रॉल नहीं कर पाई।")
            }

            command.contains("scroll up") || command.contains("ऊपर स्क्रॉल") || command == "ऊपर करो" -> {
                if (accessibility?.scrollBackward() == true) speakAndFinish("ऊपर स्क्रॉल कर दिया।")
                else speakAndFinish("इस स्क्रीन पर स्क्रॉल नहीं कर पाई।")
            }

            command.startsWith("click ") || command.startsWith("tap ") ||
                command.startsWith("क्लिक ") || command.startsWith("टैप ") -> {
                val target = command.replaceFirst(Regex("^(click|tap|क्लिक|टैप)\\s*"), "").trim()
                if (target.isNotEmpty() && accessibility?.clickText(target) == true) {
                    speakAndFinish("$target पर क्लिक कर दिया।")
                } else {
                    speakAndFinish("$target नहीं मिला या उस पर क्लिक नहीं हो पाया।")
                }
            }

            command.contains("screen") || command.contains("स्क्रीन") || command.contains("क्या दिख") -> {
                val screen = accessibility?.getScreenText()
                if (screen.isNullOrBlank()) {
                    speakAndFinish("स्क्रीन पढ़ने के लिए पहले Jarvis की Accessibility permission चालू करें।")
                } else {
                    speakAndFinish(screen.replace("\n", " ").take(500))
                }
            }

            command.contains("accessibility") || command.contains("एक्सेसिबिलिटी") -> {
                try {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    speakAndFinish("Accessibility settings खोल दी।")
                } catch (e: Exception) {
                    speakAndFinish("Accessibility settings नहीं खुल पाईं।")
                }
            }

            command.contains("timer") || command.contains("टाइमर") || command.contains("अलार्म") || command.contains("alarm") -> {
                speakAndFinish("Timer aur alarm ka real Android integration abhi baaki hai; maine timer set nahi kiya.")
            }

            command.contains("torch") || command.contains("flashlight") || command.contains("टॉर्च") || command.contains("brightness") || command.contains("ब्राइटनेस") -> {
                speakAndFinish("Torch aur brightness control abhi connect nahi hai; koi setting nahi badli.")
            }

            command.contains("call") || command.contains("कॉल") || command.contains("फोन करो") -> {
                speakAndFinish("Call karne ka feature abhi connect nahi hai. Maine koi call nahi ki.")
            }

            command.startsWith("google") || command.contains("search") || command.contains("सर्च") || command.contains("गूगल") -> {
                val query = command
                    .replace("google", "")
                    .replace("गूगल", "")
                    .replace("search", "")
                    .replace("सर्च", "")
                    .replace("karo", "")
                    .replace("करो", "")
                    .trim()
                if (query.isEmpty()) openUrl("https://www.google.com")
                else openUrl("https://www.google.com/search?q=" + Uri.encode(query))
            }

            else -> speakAndFinish("Maine suna: $command. Is command ka action abhi connect nahi hai.")
        }
    }

    private fun openApp(
        packageName: String,
        label: String
    ) {
        try {
            val launchIntent =
                context.packageManager
                    .getLaunchIntentForPackage(packageName)

            if (launchIntent == null) {
                speakAndFinish(
                    "$label installed नहीं है."
                )
                return
            }

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(launchIntent)

            speakAndFinish(
                "$label खोल दिया."
            )

        } catch (e: Exception) {
            speakAndFinish(
                "$label नहीं खुल पाया."
            )
        }
    }

    private fun openUrl(url: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            )

            speakAndFinish("खोल दिया.")

        } catch (e: Exception) {
            speakAndFinish(
                "मैं इसे खोल नहीं पाई."
            )
        }
    }

    private fun speakAndFinish(text: String) {
        finishing = true
        pendingSpeech = text
        if (ttsReady) speakPendingResponse()
    }

    private fun speakPendingResponse() {
        val message = pendingSpeech ?: run {
            finish()
            return
        }
        pendingSpeech = null
        val engine = tts ?: run {
            finish()
            return
        }

        engine.speak(message, TextToSpeech.QUEUE_FLUSH, Bundle(), "jarvis_response")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return
            val result = engine.setLanguage(Locale("hi", "IN"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        if (finishing) finish()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { finish() }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post { finish() }
                }
            })
            if (ttsReady && pendingSpeech != null) speakPendingResponse()
        } else if (finishing) {
            finish()
        }
    }

    override fun onDestroy() {
        finishing = true

        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }
}
