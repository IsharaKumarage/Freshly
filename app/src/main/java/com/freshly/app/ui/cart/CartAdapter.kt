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
        val iv: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvFarmer: TextView = itemView.findViewById(R.id.tvFarmerName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvQty: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnMinus: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnDecrease)
        val btnPlus: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnIncrease)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnRemove)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        
        // Set product name
        holder.tvName.text = item.name
        
        // Set farmer name with "by" prefix
        holder.tvFarmer.text = "by ${item.farmerName}"
        
        // Set price with proper formatting
        holder.tvPrice.text = "₹${String.format("%.2f", item.price)} / ${item.unit}"
        
        // Set quantity
        holder.tvQty.text = item.quantity.toString()
        
        // Set total price
        holder.tvTotal.text = "₹${String.format("%.2f", item.total)}"
        
        // Load product image
        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder_product)
            .into(holder.iv)

        // Set up quantity change listeners
        holder.btnMinus.setOnClickListener { 
            val newQty = (item.quantity - 1).coerceAtLeast(1)
            onQtyChange(item.id, newQty)
        }
        
        holder.btnPlus.setOnClickListener { 
            onQtyChange(item.id, item.quantity + 1)
        }
        
        // Set up delete listener
        holder.btnDelete.setOnClickListener { 
            onRemove(item.id) 
        }
    }
}

