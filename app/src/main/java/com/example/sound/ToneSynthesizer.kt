package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object ToneSynthesizer {
    private const val SAMPLE_RATE = 44100
    private var activeJob: Job? = null
    private var activeAudioTrack: AudioTrack? = null

    @Volatile
    var isPlaying: Boolean = false
        private set

    fun playPreset(
        scope: CoroutineScope,
        preset: String,
        volume: Float = 0.8f,
        loop: Boolean = false,
        onStopped: (() -> Unit)? = null
    ) {
        stop()

        isPlaying = true
        activeJob = scope.launch(Dispatchers.Default) {
            try {
                do {
                    playSingleBurst(preset, volume)
                    if (loop && isActive) {
                        delay(600)
                    }
                } while (loop && isActive)
            } finally {
                isPlaying = false
                onStopped?.invoke()
            }
        }
    }

    private fun playSingleBurst(preset: String, volume: Float) {
        val durationMs = when (preset) {
            "Lembut" -> 1400
            "Bel Klasik" -> 1200
            "Digital Beep" -> 700
            "Sirene" -> 1200
            else -> 1000
        }

        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val generatedSnd = ShortArray(numSamples)

        when (preset) {
            "Lembut" -> {
                // Harmonic relaxing chime chord (528Hz + 660Hz) with smooth decay
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val envelope = Math.exp(-2.2 * t)
                    val s1 = sin(2.0 * Math.PI * 528.0 * t)
                    val s2 = sin(2.0 * Math.PI * 660.0 * t) * 0.5
                    val s3 = sin(2.0 * Math.PI * 792.0 * t) * 0.3
                    val sampleVal = (s1 + s2 + s3) * envelope * volume * 0.5
                    generatedSnd[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            "Bel Klasik" -> {
                // Resonant bell chime (880Hz + 1760Hz overtone)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val envelope = Math.exp(-3.5 * t)
                    val s1 = sin(2.0 * Math.PI * 880.0 * t)
                    val s2 = sin(2.0 * Math.PI * 1760.0 * t) * 0.4
                    val sampleVal = (s1 + s2) * envelope * volume * 0.6
                    generatedSnd[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            "Digital Beep" -> {
                // Two distinct digital beeps (1050 Hz)
                val half = numSamples / 2
                val beepLength = (SAMPLE_RATE * 0.18).toInt()
                for (i in 0 until numSamples) {
                    val inFirstBeep = i < beepLength
                    val inSecondBeep = i >= half && i < half + beepLength
                    if (inFirstBeep || inSecondBeep) {
                        val t = i.toDouble() / SAMPLE_RATE
                        val sampleVal = sin(2.0 * Math.PI * 1050.0 * t) * volume * 0.7
                        generatedSnd[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    } else {
                        generatedSnd[i] = 0
                    }
                }
            }
            "Sirene" -> {
                // Two alternating frequencies: 750Hz and 1100Hz
                val step = numSamples / 4
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val freq = if ((i / step) % 2 == 0) 800.0 else 1150.0
                    val sampleVal = sin(2.0 * Math.PI * freq * t) * volume * 0.75
                    generatedSnd[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            else -> {
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val sampleVal = sin(2.0 * Math.PI * 800.0 * t) * volume * 0.5
                    generatedSnd[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, numSamples * 2)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        activeAudioTrack = audioTrack
        try {
            audioTrack.play()
            audioTrack.write(generatedSnd, 0, numSamples)
            // Wait for audio to drain
            Thread.sleep((durationMs).toLong())
        } catch (_: Exception) {
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
            if (activeAudioTrack == audioTrack) {
                activeAudioTrack = null
            }
        }
    }

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (_: Exception) {}
        activeAudioTrack = null
        isPlaying = false
    }
}
