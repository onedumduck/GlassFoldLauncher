package com.example.glassfold

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glassfold.databinding.PageGridBinding

class HomePagerAdapter(
  private val pages: List<List<AppEntry>>,
  private val cols: Int,
  private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<HomePagerAdapter.VH>() {

  class VH(val b: PageGridBinding) : RecyclerView.ViewHolder(b.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val b = PageGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return VH(b)
  }

  override fun getItemCount(): Int = pages.size

  override fun onBindViewHolder(holder: VH, position: Int) {
    val list = pages[position]
    holder.b.pageGrid.layoutManager = GridLayoutManager(holder.b.root.context, cols)
    holder.b.pageGrid.adapter = AppGridAdapter(list, onClick)
  }
}
