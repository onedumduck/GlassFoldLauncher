package com.example.glassfold

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.glassfold.databinding.ActivityHomeBinding
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

    val apps = queryLaunchableApps()

    val minCols = if (resources.configuration.smallestScreenWidthDp >= 600) 6 else 4
    val cols = prefs.gridCols(coerceMin = minCols)

    val rows = if (resources.configuration.smallestScreenWidthDp >= 600) 5 else 4
    val perPage = cols * rows

    val pages = apps.chunked(perPage)
    binding.pager.adapter = HomePagerAdapter(pages, cols) { app ->
      launchApp(app)
    }
    binding.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

    val dockItems = apps.take(prefs.dockCount())
    val dockAdapter = DockAdapter(dockItems) { launchApp(it) }
    binding.dockRow.layoutManager =
      LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    binding.dockRow.adapter = dockAdapter

    applyDockStyle()

    val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
      override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
      ): Boolean {
        if (e1 == null) return false
        val dy = e2.y - e1.y
        val dx = e2.x - e1.x

        // Up swipe (mostly vertical)
        if (dy < -200 && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
          SpotlightSheet(apps).show(supportFragmentManager, "spotlight")
          return true
        }
        return false
      }
    })

    binding.root.setOnTouchListener { _, event ->
      detector.onTouchEvent(event)
      false
    }
  }

  private fun queryLaunchableApps(): List<AppEntry> {
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
        val launchIntent = Intent().apply {
          setClassName(it.activityInfo.packageName, it.activityInfo.name)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        AppEntry(
          label = label,
          packageName = it.activityInfo.packageName,
          activityName = it.activityInfo.name,
          icon = icon,
          launchIntent = launchIntent
        )
      }
      .sortedBy { it.label.lowercase() }
  }

  private fun applyDockStyle() {
    val alpha = prefs.getInt(KEY_DOCK_ALPHA, 120).coerceIn(0, 255)
    val radiusDp = prefs.getInt(KEY_DOCK_RADIUS, 28).coerceAtLeast(0)
    val blurEnabled = prefs.getBoolean(KEY_BLUR_ENABLED, true)

    val radiusPx = radiusDp * resources.displayMetrics.density
    binding.dockGlass.background = GlassBackgroundDrawable(alpha, radiusPx)

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

  private fun launchApp(app: AppEntry) {
    runCatching { startActivity(app.launchIntent) }
      .onFailure {
        Toast.makeText(
          this,
          "Unable to open ${app.label}",
          Toast.LENGTH_SHORT
        ).show()
      }
  }

  private fun SharedPreferences.gridCols(coerceMin: Int): Int =
    getInt(KEY_GRID_COLS, 5).coerceAtLeast(coerceMin)

  private fun SharedPreferences.dockCount(): Int =
    getInt(KEY_DOCK_COUNT, 5).coerceAtLeast(0)

  private companion object {
    const val KEY_BLUR_ENABLED = "blur_enabled"
    const val KEY_DOCK_ALPHA = "dock_alpha"
    const val KEY_DOCK_RADIUS = "dock_radius"
    const val KEY_DOCK_COUNT = "dock_count"
    const val KEY_GRID_COLS = "grid_cols"
  }
}

private class DockAdapter(
  private val items: List<AppEntry>,
  private val onClick: (AppEntry) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<DockAdapter.ViewHolder>() {

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
    androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

    fun bind(item: AppEntry) {
      binding.icon.setImageDrawable(item.icon)
      binding.root.setOnClickListener { onClick(item) }
    }
  }
}
