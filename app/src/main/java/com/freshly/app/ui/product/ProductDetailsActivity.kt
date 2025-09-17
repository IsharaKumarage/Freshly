package com.freshly.app.ui.product

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.freshly.app.R
import com.freshly.app.data.model.Product
import com.freshly.app.data.repository.CartRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProductDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val json = intent.getStringExtra(EXTRA_PRODUCT_JSON)
        val product = fromJson(json)

        val iv = findViewById<ImageView>(R.id.ivMain)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvFarmer = findViewById<TextView>(R.id.tvFarmer)
        val btnAddToCart = findViewById<MaterialButton>(R.id.btnAddToCart)
        val btnBuyNow = findViewById<MaterialButton>(R.id.btnBuyNow)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        if (product != null) {
            tvName.text = product.name
            tvPrice.text = getString(R.string.price_per_unit_fmt, product.price, product.unit)
            tvFarmer.text = getString(R.string.from_farmer_fmt, product.farmerName)
            val url = product.imageUrls.firstOrNull()
            Glide.with(this).load(url).placeholder(R.drawable.freshly_logo).into(iv)
        }

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        val cartRepo = CartRepository()
        btnAddToCart.setOnClickListener {
            val p = product
            if (p != null) {
                lifecycleScope.launch {
                    cartRepo.addToCart(p, 1)
                        .onSuccess {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.added_to_cart), Snackbar.LENGTH_LONG).show()
                        }
                        .onFailure { e ->
                            Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                        }
                }
            }
        }
        btnBuyNow.setOnClickListener { /* TODO: checkout implementation */ }
    }

    companion object {
        const val EXTRA_PRODUCT_JSON = "extra_product_json"
        private val gson = Gson()
        fun toJson(product: Product): String = gson.toJson(product)
        fun fromJson(json: String?): Product? = try { gson.fromJson(json, Product::class.java) } catch (_: Exception) { null }
    }
}
