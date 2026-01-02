package com.example.glassfold

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppEntry(
  val label: String,
  val packageName: String,
  val activityName: String,
  val icon: Drawable,
  val launchIntent: Intent
)
