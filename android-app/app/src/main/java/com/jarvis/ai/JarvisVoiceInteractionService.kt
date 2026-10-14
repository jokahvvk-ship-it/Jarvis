package com.jarvis.ai

import android.content.ComponentName
import android.service.voice.VoiceInteractionService
import android.util.Log

class JarvisVoiceInteractionService : VoiceInteractionService() {

    companion object {
        private const val TAG = "JarvisVIA"
    }

    override fun onReady() {
        super.onReady()

        val active = isActiveService(
            this,
            ComponentName(this, JarvisVoiceInteractionService::class.java)
        )

        Log.d(TAG, "Jarvis VoiceInteractionService READY")
        Log.d(TAG, "Jarvis active assistant = $active")
    }

    override fun onShutdown() {
        Log.d(TAG, "Jarvis VoiceInteractionService SHUTDOWN")
        super.onShutdown()
    }
}
