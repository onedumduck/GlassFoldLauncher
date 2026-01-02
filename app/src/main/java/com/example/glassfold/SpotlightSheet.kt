package com.example.glassfold

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView

class SpotlightSheet : BottomSheetDialogFragment() {

  private var apps: List<AppEntry> = emptyList()

  fun setApps(apps: List<AppEntry>) {
    this.apps = apps
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (apps.isEmpty()) {
      apps = runCatching {
        AppFinder.queryLaunchableApps(
          requireContext(),
          excludePackage = requireContext().packageName
        )
      }.getOrDefault(emptyList())
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val v = inflater.inflate(R.layout.sheet_spotlight, container, false)

    val query = v.findViewById<EditText>(R.id.query)
    val results = v.findViewById<RecyclerView>(R.id.results)

    results.layoutManager = LinearLayoutManager(requireContext())

    fun show(list: List<AppEntry>) {
      results.adapter = SpotlightAdapter(list) { app ->
        runCatching { startActivity(app.launchIntent) }
          .onFailure {
            Toast.makeText(
              requireContext(),
              "Unable to open ${app.label}",
              Toast.LENGTH_SHORT
            ).show()
          }
        dismiss()
      }
    }

    show(apps.take(20))

    query.doAfterTextChanged { t ->
      val q = (t?.toString() ?: "").trim()
      if (q.isEmpty()) {
        show(apps.take(20))
      } else {
        val filtered = apps.filter { it.label.contains(q, ignoreCase = true) }.take(30)
        show(filtered)
      }
    }

    // Enter/Go => Google search
    query.setOnEditorActionListener { _, _, _ ->
      val q = query.text?.toString()?.trim().orEmpty()
      if (q.isNotEmpty()) {
        val url = "https://www.google.com/search?q=" + Uri.encode(q)
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
          .onFailure {
            Toast.makeText(
              requireContext(),
              "No browser available for search",
              Toast.LENGTH_SHORT
            ).show()
          }
        dismiss()
      }
      true
    }

    return v
  }
}
