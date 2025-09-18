package com.freshly.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freshly.app.R
import com.freshly.app.data.model.Product
import com.freshly.app.databinding.ItemProductBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                // Load product image using Glide or any other image loading library
                Glide.with(itemView)
                    .load(product.imageUrls.firstOrNull())
                    .placeholder(R.drawable.placeholder_product)
                    .into(ivProductImage)

                tvProductName.text = product.name
                tvProductWeight.text = "${product.quantity} ${product.unit}"
                tvProductPrice.text = "$${String.format("%.2f", product.price)}"

                // Set click listeners
                itemView.setOnClickListener { onProductClick(product) }
                btnAddToCart.setOnClickListener {
                    // Handle add to cart
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size
}
