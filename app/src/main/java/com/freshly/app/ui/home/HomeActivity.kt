package com.freshly.app.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.freshly.app.R
import com.freshly.app.data.model.Category
import com.freshly.app.data.model.Product
import com.freshly.app.databinding.ActivityHomeBinding
import com.freshly.app.ui.adapters.CategoryAdapter
import com.freshly.app.ui.adapters.ProductAdapter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategories()
        setupProducts()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        // Set up toolbar if needed
    }

    private fun setupCategories() {
        val categories = listOf(
            Category("Fruits", R.drawable.ic_fruits),
            Category("Vegetables", R.drawable.ic_vegetables),
            Category("Dairy", R.drawable.ic_dairy),
            Category("Eggs", R.drawable.ic_eggs)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            // Handle category click
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(
                this@HomeActivity,
                4,
                GridLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupProducts() {
        val products = listOf(
            Product("Fresh Organic Apple", "1kg", 2.99, R.drawable.placeholder_product),
            Product("Banana", "1kg", 1.49, R.drawable.placeholder_product),
            Product("Orange", "1kg", 1.99, R.drawable.placeholder_product),
            Product("Strawberry", "500g", 3.99, R.drawable.placeholder_product)
        )

        productAdapter = ProductAdapter(products) { product ->
            // Handle product click
        }

        binding.rvProducts.adapter = productAdapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on home
                    true
                }
                R.id.navigation_search -> {
                    // Navigate to search
                    true
                }
                R.id.navigation_cart -> {
                    // Navigate to cart
                    true
                }
                R.id.navigation_profile -> {
                    // Navigate to profile
                    true
                }
                else -> false
            }
        }
    }
}
