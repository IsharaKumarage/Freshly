package com.freshly.app.ui.marketplace

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freshly.app.R
import com.freshly.app.data.model.Product

class ProductAdapter(private val onClick: (Product) -> Unit) : ListAdapter<Product, ProductAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
        }
    }

    class VH(itemView: View, val onClick: (Product) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private var current: Product? = null
        init {
            itemView.setOnClickListener { current?.let { onClick(it) } }
        }
        fun bind(item: Product) {
            current = item
            tvName.text = item.name
            tvPrice.text = itemView.context.getString(R.string.price_per_unit_fmt, item.price, item.unit)
            val url = item.imageUrls.firstOrNull()
            Glide.with(itemView).load(url).placeholder(R.drawable.freshly_logo).into(ivImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_card, parent, false)
        return VH(view, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
