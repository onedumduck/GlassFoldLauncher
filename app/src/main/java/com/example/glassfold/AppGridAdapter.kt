package com.example.glassfold

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.glassfold.databinding.ItemAppBinding

class AppGridAdapter(
  private val items: List<AppEntry>,
  private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ItemAppBinding.inflate(inflater, parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int = items.size

  inner class ViewHolder(private val binding: ItemAppBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: AppEntry) {
      binding.icon.setImageDrawable(item.icon)
      binding.label.text = item.label
      binding.root.setOnClickListener { onClick(item) }
    }
  }
}
