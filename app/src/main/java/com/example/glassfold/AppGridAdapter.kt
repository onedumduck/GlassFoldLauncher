package com.example.glassfold

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.glassfold.databinding.ItemAppBinding

class AppGridAdapter(
  private val items: MutableList<AppEntry>,
  private val onClick: (AppEntry) -> Unit,
  private val onLongPress: (() -> Unit)? = null,
  private val onRemove: ((AppEntry) -> Unit)? = null
) : RecyclerView.Adapter<AppGridAdapter.ViewHolder>() {

  var editMode: Boolean = false

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

      if (editMode) {
        val anim = AnimationUtils.loadAnimation(binding.root.context, R.anim.jiggle)
        binding.root.startAnimation(anim)
      } else {
        binding.root.clearAnimation()
      }

      binding.deleteBadge.visibility = if (editMode) View.VISIBLE else View.GONE
      binding.deleteBadge.setOnClickListener {
        if (editMode) onRemove?.invoke(item)
      }

      binding.root.setOnClickListener {
        if (!editMode) onClick(item)
      }
      binding.root.setOnLongClickListener {
        onLongPress?.invoke()
        true
      }
    }
  }

  fun swap(from: Int, to: Int) {
    val item = items.removeAt(from)
    items.add(to, item)
    notifyItemMoved(from, to)
  }
}
