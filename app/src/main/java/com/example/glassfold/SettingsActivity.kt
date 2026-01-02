package com.example.glassfold

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    if (savedInstanceState == null) {
      supportFragmentManager
        .beginTransaction()
        .replace(android.R.id.content, LauncherSettingsFragment())
        .commit()
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return true
  }

  class LauncherSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.prefs_launcher, rootKey)
    }
  }
}