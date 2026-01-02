package com.example.glassfold

import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.glassfold.databinding.ActivityHomeBinding
import com.example.glassfold.databinding.ItemDockBinding

class HomeActivity : AppCompatActivity() {

  private lateinit var binding: ActivityHomeBinding
  private lateinit var prefs: LauncherPrefs
  private var editMode = false
  private var pagerAdapter: HomePagerAdapter? = null
  private val wallpaperManager by lazy { WallpaperManager.getInstance(this) }
  private var pages: List<MutableList<AppEntry>> = emptyList()

  private val pickFoldedBg = registerForActivityResult(
    ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let {
      contentResolver.takePersistableUriPermission(
        it,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
      prefs.setFoldedBg(it.toString())
      applyBackground()
    }
  }

  private val pickUnfoldedBg = registerForActivityResult(
    ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let {
      contentResolver.takePersistableUriPermission(
        it,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
      )
      prefs.setUnfoldedBg(it.toString())
      applyBackground()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    PreferenceManager.setDefaultValues(this, R.xml.prefs_launcher, false)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)
    window.setBackgroundDrawable(null)
    window.setBackgroundDrawable(wallpaperManager.drawable)

    prefs = LauncherPrefs(this)

    val apps = queryLaunchableApps()

    val minCols = if (isUnfolded()) 6 else 4
    val cols = prefs.gridCols(coerceMin = minCols)

    val rows = if (isUnfolded()) 5 else 4
    val perPage = cols * rows

    pages = apps.chunked(perPage).map { it.toMutableList() }
    pagerAdapter = HomePagerAdapter(pages, cols, { app ->
      launchApp(app)
    }, { app ->
      removeFromPage(app)
    }) {
      setEditMode(true)
    }
    binding.pager.adapter = pagerAdapter
    binding.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    renderDots(pages.size, 0)

    binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        renderDots(pages.size, position)
      }
    })

    val dockItems = apps.take(prefs.dockCount())
    val dockAdapter = DockAdapter(dockItems) { launchApp(it) }
    binding.dockRow.layoutManager =
      LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    binding.dockRow.adapter = dockAdapter

    applyDockStyle()
    applyBackground()

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
        if (dy < -180 && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
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

    binding.root.setOnLongClickListener {
      setEditMode(!editMode)
      true
    }

    binding.editMenuBtn.setOnClickListener {
      binding.editPanel.visibility =
        if (binding.editPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
    binding.btnBgFolded.setOnClickListener { pickFoldedBg.launch(arrayOf("image/*")) }
    binding.btnBgUnfolded.setOnClickListener { pickUnfoldedBg.launch(arrayOf("image/*")) }
  }

  override fun onResume() {
    super.onResume()
    applyBackground()
  }

  private fun isUnfolded(): Boolean =
    resources.configuration.smallestScreenWidthDp >= 600

  private fun applyBackground() {
    val uriStr = if (isUnfolded()) prefs.unfoldedBgUri() else prefs.foldedBgUri()
    if (uriStr.isNullOrEmpty()) {
      binding.bgImage.setImageDrawable(wallpaperManager.drawable)
      return
    }
    try {
      binding.bgImage.setImageURI(Uri.parse(uriStr))
    } catch (_: Exception) {
      binding.bgImage.setImageDrawable(wallpaperManager.drawable)
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
    val alpha = prefs.dockAlpha()
    val radiusDp = prefs.dockRadius()

    val radiusPx = radiusDp * resources.displayMetrics.density
    binding.dockGlass.background = GlassBackgroundDrawable(alpha, radiusPx)
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

  private fun removeFromPage(app: AppEntry) {
    val pageIndex = pages.indexOfFirst { it.contains(app) }
    if (pageIndex >= 0) {
      val page = pages[pageIndex]
      val itemIndex = page.indexOf(app)
      if (itemIndex >= 0) {
        page.removeAt(itemIndex)
        pagerAdapter?.adapters?.getOrNull(pageIndex)?.notifyItemRemoved(itemIndex)
      }
    }
  }

  private fun setEditMode(enabled: Boolean) {
    editMode = enabled
    binding.editMenuBtn.visibility = if (enabled) View.VISIBLE else View.GONE
    if (!enabled) binding.editPanel.visibility = View.GONE
    pagerAdapter?.editMode = enabled
    pagerAdapter?.adapters?.forEach { adapter ->
      adapter?.let {
        it.editMode = enabled
        it.notifyDataSetChanged()
      }
    }
  }

  private fun renderDots(count: Int, active: Int) {
    binding.dots.removeAllViews()
    if (count <= 0) return

    val size = (6 * resources.displayMetrics.density).toInt()
    val pad = (6 * resources.displayMetrics.density).toInt()

    repeat(count) { index ->
      val view = View(this)
      val lp = LinearLayout.LayoutParams(size, size)
      lp.setMargins(pad, 0, pad, 0)
      view.layoutParams = lp
      view.background = getDrawable(
        if (index == active) R.drawable.dot_active else R.drawable.dot
      )
      binding.dots.addView(view)
    }
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
