package com.example.glassfold

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glassfold.databinding.ItemAppBinding

class SpotlightAdapter(
  private val apps: List<AppEntry>,
  private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<SpotlightAdapter.VH>() {

  class VH(val b: ItemAppBinding) : RecyclerView.ViewHolder(b.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val b = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return VH(b)
  }

  override fun getItemCount() = apps.size

  override fun onBindViewHolder(holder: VH, position: Int) {
    val app = apps[position]
    holder.b.icon.setImageDrawable(app.icon)
    holder.b.label.text = app.label
    holder.b.root.setOnClickListener { onClick(app) }
  }
}
