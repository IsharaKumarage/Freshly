package com.freshly.app.ui.marketplace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R

data class CategoryItem(val iconEmoji: String, val title: String, val count: Int = 0)

class CategoryAdapter(private val items: List<CategoryItem>) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        val title: TextView = itemView.findViewById(R.id.tvCategoryName)
        val count: TextView = itemView.findViewById(R.id.tvProductCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.icon.text = item.iconEmoji
        holder.title.text = item.title
        holder.count.text = "${item.count} items"
    }

    override fun getItemCount(): Int = items.size
}
