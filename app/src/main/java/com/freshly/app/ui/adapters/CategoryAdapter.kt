package com.freshly.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.model.ProductCategory
import com.freshly.app.ui.categories.CategoryItem
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private val onCategoryClick: (ProductCategory) -> Unit
) : ListAdapter<CategoryItem, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardCategory)
        private val tvIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCount: TextView = itemView.findViewById(R.id.tvProductCount)

        fun bind(category: CategoryItem, position: Int) {
            tvIcon.text = category.icon
            tvName.text = category.name
            tvCount.text = "${category.productCount} items"

            // Update selection state
            card.isChecked = (position == selectedPosition)

            // Set click listener
            itemView.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category.category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
