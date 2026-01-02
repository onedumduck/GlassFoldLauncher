package com.example.glassfold

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class LauncherPrefs(context: Context) {
  private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  fun foldedBgUri(): String? = prefs.getString(KEY_BG_FOLDED, null)
  fun unfoldedBgUri(): String? = prefs.getString(KEY_BG_UNFOLDED, null)

  fun setFoldedBg(uri: String?) {
    prefs.edit().putString(KEY_BG_FOLDED, uri).apply()
  }

  fun setUnfoldedBg(uri: String?) {
    prefs.edit().putString(KEY_BG_UNFOLDED, uri).apply()
  }

  fun gridCols(coerceMin: Int): Int = prefs.getInt(KEY_GRID_COLS, 5).coerceAtLeast(coerceMin)
  fun dockCount(): Int = prefs.getInt(KEY_DOCK_COUNT, 5).coerceAtLeast(0)
  fun dockAlpha(): Int = prefs.getInt(KEY_DOCK_ALPHA, 120).coerceIn(0, 255)
  fun dockRadius(): Int = prefs.getInt(KEY_DOCK_RADIUS, 32).coerceAtLeast(0)
  fun blurEnabled(): Boolean = prefs.getBoolean(KEY_BLUR_ENABLED, true)

  companion object {
    const val KEY_BLUR_ENABLED = "blur_enabled"
    const val KEY_DOCK_ALPHA = "dock_alpha"
    const val KEY_DOCK_RADIUS = "dock_radius"
    const val KEY_DOCK_COUNT = "dock_count"
    const val KEY_GRID_COLS = "grid_cols"
    private const val KEY_BG_FOLDED = "bg_folded"
    private const val KEY_BG_UNFOLDED = "bg_unfolded"
  }
}
