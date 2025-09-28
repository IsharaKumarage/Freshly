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
import com.freshly.app.data.model.Product
import com.google.android.material.button.MaterialButton
import com.freshly.app.utils.PriceUtil
import com.freshly.app.utils.ImageUtil

class WishlistAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddToCart: (Product) -> Unit,
    private val onRemoveFromWishlist: (Product) -> Unit
) : ListAdapter<Product, WishlistAdapter.WishlistViewHolder>(ProductDiffCallback()) {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvFarmerName: TextView = itemView.findViewById(R.id.tvFarmerName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        private val tvDiscountBadge: TextView = itemView.findViewById(R.id.tvDiscountBadge)
        private val tvStockStatus: TextView = itemView.findViewById(R.id.tvStockStatus)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val btnAddToCart: MaterialButton = itemView.findViewById(R.id.btnAddToCart)
        private val btnRemoveWishlist: ImageButton = itemView.findViewById(R.id.btnWishlist)
        
        fun bind(product: Product) {
            // Load product image with Glide optimization
            Glide.with(itemView.context)
                .load(ImageUtil.asGlideModel(product.imageUrls.firstOrNull()))
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivProductImage)

            // Product details
            tvProductName.text = product.name
            tvFarmerName.text = "by ${product.farmerName}"
            
            // Price formatting with Sri Lankan Rupee
            tvProductPrice.text = PriceUtil.formatPrice(product.price)
            
            // Show original price and discount if applicable
            if (product.originalPrice > product.price && product.originalPrice > 0) {
                tvOriginalPrice.visibility = View.VISIBLE
                tvDiscountBadge.visibility = View.VISIBLE
                tvOriginalPrice.text = PriceUtil.formatPrice(product.originalPrice)
                val discount = ((product.originalPrice - product.price) / product.originalPrice * 100).toInt()
                tvDiscountBadge.text = "${discount}% OFF"
            } else {
                tvOriginalPrice.visibility = View.GONE
                tvDiscountBadge.visibility = View.GONE
            }
            
            // Rating
            if (product.rating > 0) {
                ratingBar.visibility = View.VISIBLE
                tvRating.visibility = View.VISIBLE
                ratingBar.rating = product.rating.toFloat()
                tvRating.text = String.format("%.1f ★", product.rating)
            } else {
                ratingBar.visibility = View.GONE
                tvRating.visibility = View.GONE
            }
            
            // Stock availability
            if (product.isAvailable && product.availableQuantity > 0) {
                btnAddToCart.isEnabled = true
                btnAddToCart.text = "Add to Cart"
                tvStockStatus.visibility = View.GONE
            } else {
                btnAddToCart.isEnabled = false
                btnAddToCart.text = "Out of Stock"
                tvStockStatus.visibility = View.VISIBLE
                tvStockStatus.text = "Out of Stock"
            }

            // Click listeners
            itemView.setOnClickListener { onProductClick(product) }
            btnAddToCart.setOnClickListener { 
                if (product.isAvailable && product.availableQuantity > 0) {
                    onAddToCart(product)
                }
            }
            btnRemoveWishlist.setOnClickListener { onRemoveFromWishlist(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
