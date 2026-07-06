package com.example.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object RetroSoundManager {
    private var toneGenerator: ToneGenerator? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted = false

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e("RetroSoundManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    fun getIsMuted() = isMuted

    fun playTapSound() {
        if (isMuted) return
        scope.launch {
            try {
                // Short retro high-pitch click sound
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            } catch (e: Exception) {
                Log.e("RetroSoundManager", "Error playing tap sound", e)
            }
        }
    }

    fun playUpgradeSound() {
        if (isMuted) return
        scope.launch {
            try {
                // Retro double-beep arpeggio
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 120)
            } catch (e: Exception) {
                Log.e("RetroSoundManager", "Error playing upgrade sound", e)
            }
        }
    }

    fun playMilestoneSound() {
        if (isMuted) return
        scope.launch {
            try {
                // Fanfare-like sequence of short beep-beeps
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 80)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 80)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 200)
            } catch (e: Exception) {
                Log.e("RetroSoundManager", "Error playing milestone sound", e)
            }
        }
    }
}
