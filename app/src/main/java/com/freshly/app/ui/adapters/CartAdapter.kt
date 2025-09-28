package com.freshly.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.freshly.app.R
import com.freshly.app.data.model.CartItem
import com.google.android.material.button.MaterialButton
import com.freshly.app.utils.PriceUtil
import com.freshly.app.utils.ImageUtil

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvFarmerName: TextView = itemView.findViewById(R.id.tvFarmerName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnDecrease: MaterialButton = itemView.findViewById(R.id.btnDecrease)
        private val btnIncrease: MaterialButton = itemView.findViewById(R.id.btnIncrease)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
        
        fun bind(cartItem: CartItem) {
            // Load product image
            Glide.with(itemView.context)
                .load(ImageUtil.asGlideModel(cartItem.imageUrl))
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivProductImage)

            // Product details
            tvProductName.text = cartItem.name
            tvFarmerName.text = "by ${cartItem.farmerName}"
            
            // Price formatting with Sri Lankan Rupee
            tvProductPrice.text = "${PriceUtil.formatPrice(cartItem.price)} / ${cartItem.unit}"
            tvTotalPrice.text = PriceUtil.formatPrice(cartItem.total)
            
            // Quantity
            tvQuantity.text = cartItem.quantity.toString()
            
            // Quantity controls
            btnDecrease.isEnabled = cartItem.quantity > 1
            btnIncrease.isEnabled = cartItem.quantity < 99
            
            // Click listeners
            btnDecrease.setOnClickListener {
                if (cartItem.quantity > 1) {
                    onQuantityChanged(cartItem, cartItem.quantity - 1)
                }
            }
            
            btnIncrease.setOnClickListener {
                if (cartItem.quantity < 99) {
                    onQuantityChanged(cartItem, cartItem.quantity + 1)
                }
            }
            
            btnRemove.setOnClickListener {
                onRemoveItem(cartItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}
