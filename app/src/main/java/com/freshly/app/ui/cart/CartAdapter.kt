package com.freshly.app.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freshly.app.R
import com.freshly.app.data.model.CartItem

class CartAdapter(
    private val onQtyChange: (itemId: String, qty: Int) -> Unit,
    private val onRemove: (itemId: String) -> Unit
) : ListAdapter<CartItem, CartAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem == newItem
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv: ImageView = itemView.findViewById(R.id.ivImage)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvFarmer: TextView = itemView.findViewById(R.id.tvFarmer)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvQty: TextView = itemView.findViewById(R.id.tvQty)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnDelete: ImageButton? = try { itemView.findViewById(R.id.btnDelete) } catch (e: Exception) { null }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        
        // Set product name
        holder.tvName.text = item.name
        
        // Set farmer name with "Farm:" prefix as shown in UI
        holder.tvFarmer.text = "Farm: ${item.farmerName}"
        
        // Set price with proper formatting
        holder.tvPrice.text = "$${String.format("%.2f", item.price)}/${item.unit}"
        
        // Set quantity
        holder.tvQty.text = item.quantity.toString()
        
        // Load product image
        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .placeholder(R.drawable.freshly_logo)
            .into(holder.iv)

        // Set up quantity change listeners
        holder.btnMinus.setOnClickListener { 
            val newQty = (item.quantity - 1).coerceAtLeast(0)
            onQtyChange(item.id, newQty)
        }
        
        holder.btnPlus.setOnClickListener { 
            onQtyChange(item.id, item.quantity + 1)
        }
        
        // Set up delete listener
        holder.btnDelete?.setOnClickListener { 
            onRemove(item.id) 
        }
    }
}

