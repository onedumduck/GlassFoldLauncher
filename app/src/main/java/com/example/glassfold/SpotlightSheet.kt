package com.example.glassfold

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView

class SpotlightSheet(
  private val apps: List<AppEntry>
) : BottomSheetDialogFragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val v = inflater.inflate(R.layout.sheet_spotlight, container, false)

    val query = v.findViewById<EditText>(R.id.query)
    val results = v.findViewById<RecyclerView>(R.id.results)

    results.layoutManager = LinearLayoutManager(requireContext())

    fun show(list: List<AppEntry>) {
      results.adapter = SpotlightAdapter(list) { app ->
        try { startActivity(app.launchIntent) } catch (_: Exception) {}
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
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        dismiss()
      }
      true
    }

    return v
  }
}
