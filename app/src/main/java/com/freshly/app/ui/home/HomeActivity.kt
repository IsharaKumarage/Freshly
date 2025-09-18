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
        // Categories are provided via static includes in activity_home.xml's HorizontalScrollView.
        // If you want a dynamic list, add a RecyclerView with id rvCategories in the layout and
        // reintroduce the adapter + layoutManager setup here.
    }

    private fun setupProducts() {
        val products = listOf(
            Product(
                id = "1",
                name = "Fresh Organic Apple",
                description = "Crisp and sweet organic apples",
                price = 2.99,
                quantity = 1,
                unit = "kg",
                imageUrls = listOf()
            ),
            Product(
                id = "2",
                name = "Banana",
                description = "Ripe bananas",
                price = 1.49,
                quantity = 1,
                unit = "kg",
                imageUrls = listOf()
            ),
            Product(
                id = "3",
                name = "Orange",
                description = "Juicy oranges",
                price = 1.99,
                quantity = 1,
                unit = "kg",
                imageUrls = listOf()
            ),
            Product(
                id = "4",
                name = "Strawberry",
                description = "Fresh strawberries",
                price = 3.99,
                quantity = 500,
                unit = "g",
                imageUrls = listOf()
            )
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
