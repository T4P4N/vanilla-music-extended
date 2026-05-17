package ch.blinkenlights.android.vanilla

import io.github.jeffshee.visualizer.utils.VisualizerHelper
import android.util.Log

object VisualizerConfig {
    @JvmStatic
    fun setToggles(helper: VisualizerHelper, exp: Boolean, spatial: Boolean, decay: Double) {
        setField(helper, "enableExponentialDecay", exp)
        setField(helper, "enableSpatialFilter", spatial)
        setDoubleField(helper, "decayFactor", decay)
    }

    private fun setField(helper: VisualizerHelper, fieldName: String, value: Boolean) {
        try {
            val field = helper.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(helper, value)
        } catch (e: Exception) {
            Log.w("VanillaMusic", "Failed to set visualizer field $fieldName: ${e.message}")
        }
    }

    private fun setDoubleField(helper: VisualizerHelper, fieldName: String, value: Double) {
        try {
            val field = helper.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(helper, value)
        } catch (e: Exception) {
            Log.w("VanillaMusic", "Failed to set visualizer field $fieldName: ${e.message}")
        }
    }
}
