package com.example.glassfold

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glassfold.databinding.ActivityHomeBinding
import com.example.glassfold.databinding.ItemAppBinding
import com.example.glassfold.databinding.ItemDockBinding

class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding
  private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    PreferenceManager.setDefaultValues(this, R.xml.prefs_launcher, false)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val gridColumns = prefs.getInt(KEY_GRID_COLS, 5).coerceAtLeast(3)
    val dockCount = prefs.getInt(KEY_DOCK_COUNT, 5).coerceAtLeast(0)

    val apps = loadLaunchableApps()

    val appAdapter = AppAdapter(apps) { launchApp(it) }
    binding.appGrid.layoutManager = GridLayoutManager(this, gridColumns)
    binding.appGrid.adapter = appAdapter

    val dockItems = apps.take(dockCount)
    val dockAdapter = DockAdapter(dockItems) { launchApp(it) }
    binding.dockRow.layoutManager =
      LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    binding.dockRow.adapter = dockAdapter

    applyDockStyle()
  }

  private fun loadLaunchableApps(): List<LaunchableApp> {
    val pm = packageManager
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
      .filter { it.activityInfo.packageName != packageName }
      .map {
        val label = it.loadLabel(pm)?.toString().orEmpty()
        val icon = it.activityInfo.loadIcon(pm)
        LaunchableApp(
          label = label,
          packageName = it.activityInfo.packageName,
          activityName = it.activityInfo.name,
          icon = icon
        )
      }
      .sortedBy { it.label.lowercase() }
  }

  private fun applyDockStyle() {
    val alpha = prefs.getInt(KEY_DOCK_ALPHA, 120).coerceIn(0, 255)
    val radiusDp = prefs.getInt(KEY_DOCK_RADIUS, 28).coerceAtLeast(0)
    val blurEnabled = prefs.getBoolean(KEY_BLUR_ENABLED, true)

    val shape = GradientDrawable().apply {
      val radiusPx = radiusDp * resources.displayMetrics.density
      cornerRadius = radiusPx
      setColor(Color.argb(alpha, 24, 24, 24))
    }

    binding.dockGlass.background = shape

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      binding.dockGlass.setRenderEffect(
        if (blurEnabled) {
          RenderEffect.createBlurEffect(30f, 30f, Shader.TileMode.CLAMP)
        } else {
          null
        }
      )
    }
  }

  private fun launchApp(app: LaunchableApp) {
    val intent = Intent().apply {
      setClassName(app.packageName, app.activityName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching { startActivity(intent) }
      .onFailure {
        Toast.makeText(
          this,
          "Unable to open ${app.label}",
          Toast.LENGTH_SHORT
        ).show()
      }
  }

  private companion object {
    const val KEY_BLUR_ENABLED = "blur_enabled"
    const val KEY_DOCK_ALPHA = "dock_alpha"
    const val KEY_DOCK_RADIUS = "dock_radius"
    const val KEY_DOCK_COUNT = "dock_count"
    const val KEY_GRID_COLS = "grid_cols"
  }
}

private data class LaunchableApp(
  val label: String,
  val packageName: String,
  val activityName: String,
  val icon: Drawable
)

private class AppAdapter(
  private val items: List<LaunchableApp>,
  private val onClick: (LaunchableApp) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
    val inflater = android.view.LayoutInflater.from(parent.context)
    val binding = ItemAppBinding.inflate(inflater, parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int = items.size

  inner class ViewHolder(private val binding: ItemAppBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LaunchableApp) {
      binding.icon.setImageDrawable(item.icon)
      binding.label.text = item.label
      binding.root.setOnClickListener { onClick(item) }
    }
  }
}

private class DockAdapter(
  private val items: List<LaunchableApp>,
  private val onClick: (LaunchableApp) -> Unit
) : RecyclerView.Adapter<DockAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
    val inflater = android.view.LayoutInflater.from(parent.context)
    val binding = ItemDockBinding.inflate(inflater, parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int = items.size

  inner class ViewHolder(private val binding: ItemDockBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LaunchableApp) {
      binding.icon.setImageDrawable(item.icon)
      binding.root.setOnClickListener { onClick(item) }
    }
  }
}
