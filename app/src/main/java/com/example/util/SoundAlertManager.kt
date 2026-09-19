package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generates pure tone alert (880Hz on price increase, 440Hz on price decrease)
 * using Android AudioTrack without any external audio assets.
 * Vibrates 80ms on mobile device.
 */
object SoundAlertManager {

    suspend fun playPriceChangeAlert(
        context: Context,
        isIncrease: Boolean,
        enableSound: Boolean = true,
        enableVibration: Boolean = true
    ) = withContext(Dispatchers.Default) {
        // 1. Mobile Vibration (80ms)
        if (enableVibration) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v?.vibrate(VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v?.vibrate(80L)
                    }
                }
            } catch (_: Throwable) {}
        }

        // 2. Synthesized Sound Tone (880Hz for increase / 440Hz for decrease)
        if (enableSound) {
            try {
                val freq = if (isIncrease) 880 else 440
                val durationMs = 150
                val sampleRate = 44100
                val numSamples = (durationMs * sampleRate) / 1000
                val samples = ShortArray(numSamples)
                val freqOfTone = freq.toDouble()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Smooth envelope to avoid audio clicks
                    val envelope = when {
                        i < 150 -> i / 150.0
                        i > numSamples - 150 -> (numSamples - i) / 150.0
                        else -> 1.0
                    }
                    val sinVal = Math.sin(2.0 * Math.PI * freqOfTone * t)
                    samples[i] = (sinVal * 0.75 * Short.MAX_VALUE * envelope).toInt().toShort()
                }

                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(numSamples * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                Thread.sleep(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Throwable) {}
        }
    }
}
