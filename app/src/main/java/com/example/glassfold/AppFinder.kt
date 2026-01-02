package com.example.glassfold

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object AppFinder {
  fun queryLaunchableApps(
    context: Context,
    excludePackage: String? = null
  ): List<AppEntry> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      pm.queryIntentActivities(
        mainIntent,
        PackageManager.ResolveInfoFlags.of(0)
      )
    } else {
      @Suppress("DEPRECATION")
      pm.queryIntentActivities(mainIntent, 0)
    }

    return resolved
      .filter { info -> info.activityInfo.packageName != excludePackage }
      .mapNotNull { info ->
        runCatching {
          val label = info.loadLabel(pm)?.toString().orEmpty()
          val launchIntent = Intent().apply {
            setClassName(info.activityInfo.packageName, info.activityInfo.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }

          AppEntry(
            label = label,
            packageName = info.activityInfo.packageName,
            activityName = info.activityInfo.name,
            icon = info.activityInfo.loadIcon(pm),
            launchIntent = launchIntent
          )
        }.getOrNull()
      }
      .sortedBy { it.label.lowercase() }
  }
}
