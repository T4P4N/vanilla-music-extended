package io.github.jeffshee.visualizer.utils

import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.*

class VisualizerHelper(sessionId: Int) {

    private val visualizer: Visualizer = Visualizer(sessionId)
    private val fftBuff: ByteArray
    private val waveBuff: ByteArray
    private val fftM: DoubleArray

    // Smoothing states
    private var smoothedFft: DoubleArray? = null

    // Smoothing parameters
    @JvmField var decayFactor = 0.85 
    @JvmField var spatialWeight = 0.2 
    @JvmField var numBins = 64 

    // Toggles
    @JvmField var enableExponentialDecay = true
    @JvmField var enableSpatialFilter = true

    // Verification logging
    private var lastLogTime = 0L

    init {
        Log.d("VisualizerHelper", "Custom VisualizerHelper initialized for session $sessionId")
        val range = Visualizer.getCaptureSizeRange()
        visualizer.captureSize = if (range.size >= 2) range[1] else 1024
        fftBuff = ByteArray(visualizer.captureSize)
        waveBuff = ByteArray(visualizer.captureSize)
        fftM = DoubleArray(visualizer.captureSize / 2 - 1)
        visualizer.enabled = true
    }

    fun getFft(): ByteArray {
        if (visualizer.enabled) {
            try {
                visualizer.getFft(fftBuff)
                applySmoothingToBuffer()
                
                val now = System.currentTimeMillis()
                if (now - lastLogTime > 2000) {
                    Log.d("VisualizerHelper", "Processing FFT: Decay=$enableExponentialDecay, Gravity=$decayFactor, Spatial=$enableSpatialFilter")
                    lastLogTime = now
                }
            } catch (e: Exception) {
                // Silently ignore
            }
        }
        return fftBuff
    }

    private fun applySmoothingToBuffer() {
        val originalSize = fftM.size
        var mag = DoubleArray(originalSize)
        for (k in 0 until originalSize) {
            val i = (k + 1) * 2
            mag[k] = hypot(fftBuff[i].toDouble(), fftBuff[i + 1].toDouble())
        }

        if (enableSpatialFilter) {
            mag = applySpatialFilter(mag)
        }

        if (enableExponentialDecay) {
            mag = applyExponentialDecay(mag)
        }

        // Encode back into fftBuff
        for (k in 0 until originalSize) {
            val i = (k + 1) * 2
            val sMag = mag[k]
            // Slight boost to compensate for smoothing dampening
            val scaledMag = if (enableExponentialDecay || enableSpatialFilter) sMag * 1.2 else sMag
            val clamped = max(-128.0, min(127.0, scaledMag)).toInt().toByte()
            fftBuff[i] = clamped
            fftBuff[i + 1] = 0
        }
    }

    private fun applySpatialFilter(data: DoubleArray): DoubleArray {
        val filtered = DoubleArray(data.size)
        for (i in data.indices) {
            val left = if (i > 0) data[i - 1] else data[i]
            val right = if (i < data.size - 1) data[i + 1] else data[i]
            filtered[i] = left * spatialWeight + data[i] * (1 - 2 * spatialWeight) + right * spatialWeight
        }
        return filtered
    }

    private fun applyExponentialDecay(data: DoubleArray): DoubleArray {
        if (smoothedFft == null || smoothedFft!!.size != data.size) {
            smoothedFft = data.copyOf()
            return data
        }
        val current = smoothedFft!!
        for (i in data.indices) {
            if (data[i] >= current[i]) {
                current[i] = data[i]
            } else {
                current[i] = current[i] * decayFactor + data[i] * (1 - decayFactor)
            }
        }
        return current
    }

    fun getWave(): ByteArray {
        if (visualizer.enabled) {
            try {
                visualizer.getWaveForm(waveBuff)
            } catch (e: Exception) {
                // Silently ignore
            }
        }
        return waveBuff
    }

    fun getFftMagnitude(): DoubleArray {
        getFft()
        for (k in 0 until fftM.size) {
            val i = (k + 1) * 2
            fftM[k] = hypot(fftBuff[i].toDouble(), fftBuff[i + 1].toDouble())
        }
        return fftM
    }

    fun getFftMagnitudeRange(startHz: Int, endHz: Int): DoubleArray {
        val sIndex = hzToFftIndex(startHz)
        val eIndex = hzToFftIndex(endHz)
        val mag = getFftMagnitude()
        val start = max(0, sIndex)
        val end = min(mag.size, eIndex)
        return if (start < end) mag.copyOfRange(start, end) else DoubleArray(0)
    }

    fun hzToFftIndex(Hz: Int): Int {
        val n = visualizer.captureSize
        val k = (Hz.toDouble() * n / 44100.0).toInt()
        return min(max(k - 1, 0), n / 2 - 2)
    }

    private var handler: Handler? = null
    private var runnable: Runnable? = null

    fun startDebug() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                Log.d("WfmAnalog", getWave().contentToString())
                Log.d("Fft", getFftMagnitude().contentToString())
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(runnable!!)
    }

    fun stopDebug() {
        runnable?.let { handler?.removeCallbacks(it) }
    }

    fun release() {
        try {
            visualizer.enabled = false
            visualizer.release()
        } catch (e: Exception) {
            // Silently ignore
        }
    }
}
