package com.example.glassfold

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.glassfold.databinding.PageGridBinding

class HomePagerAdapter(
  private val pages: List<MutableList<AppEntry>>,
  private val cols: Int,
  private val onClick: (AppEntry) -> Unit,
  private val onRemove: (AppEntry) -> Unit,
  private val onEnterEditMode: (() -> Unit)
) : RecyclerView.Adapter<HomePagerAdapter.VH>() {

  var editMode: Boolean = false
  val adapters = MutableList<AppGridAdapter?>(pages.size) { null }

  class VH(val b: PageGridBinding) : RecyclerView.ViewHolder(b.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val b = PageGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return VH(b)
  }

  override fun getItemCount(): Int = pages.size

  override fun onBindViewHolder(holder: VH, position: Int) {
    val list = pages[position]
    holder.b.pageGrid.layoutManager = GridLayoutManager(holder.b.root.context, cols)
    val adapter = AppGridAdapter(list, onClick, { onEnterEditMode() }) { app ->
      onRemove(app)
    }
    adapter.editMode = editMode
    holder.b.pageGrid.adapter = adapter

    adapters[position] = adapter

    val helper = ItemTouchHelper(
      object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        0
      ) {
        override fun onMove(
          rv: RecyclerView,
          vh: RecyclerView.ViewHolder,
          target: RecyclerView.ViewHolder
        ): Boolean {
          if (!adapter.editMode) return false
          val from = vh.bindingAdapterPosition
          val to = target.bindingAdapterPosition
          if (
            from == RecyclerView.NO_POSITION ||
            to == RecyclerView.NO_POSITION ||
            from !in 0 until adapter.itemCount ||
            to !in 0 until adapter.itemCount
          ) return false
          adapter.swap(from, to)
          return true
        }

        override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) = Unit

        override fun isLongPressDragEnabled(): Boolean = adapter.editMode
      }
    )
    helper.attachToRecyclerView(holder.b.pageGrid)
  }
}
