package com.example.glassfold

import android.app.Application
import android.util.Log

class GlassFoldApp : Application() {
  override fun onCreate() {
    super.onCreate()
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      Log.e(
        "GlassFoldCrash",
        "Uncaught exception on thread ${thread.name}",
        throwable
      )
      previousHandler?.uncaughtException(thread, throwable)
    }
  }
}
