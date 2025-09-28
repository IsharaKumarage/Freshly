package com.freshly.app.ui.categories

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.model.CartItem
import com.freshly.app.data.model.Product
import com.freshly.app.data.model.ProductCategory
import com.freshly.app.data.repository.CartRepository
import com.freshly.app.ui.adapters.CategoryAdapter
import com.freshly.app.ui.adapters.ProductAdapter
import com.freshly.app.ui.product.ProductDetailsActivity
import com.freshly.app.ui.viewmodel.ProductViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoriesActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ProductViewModel
    private lateinit var cartRepository: CartRepository
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var chipGroup: ChipGroup
    private lateinit var progressBar: ProgressBar
    
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    
    private val firestore = FirebaseFirestore.getInstance()
    private var selectedCategory: ProductCategory? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        
        title = "Categories"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        cartRepository = CartRepository()
        
        initViews()
        setupAdapters()
        loadCategories()
        observeViewModel()
    }
    
    private fun initViews() {
        rvCategories = findViewById(R.id.rvCategories)
        rvProducts = findViewById(R.id.rvProducts)
        chipGroup = findViewById(R.id.chipGroupCategories)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupAdapters() {
        // Setup category adapter
        categoryAdapter = CategoryAdapter { category ->
            selectedCategory = category
            loadProductsByCategory(category)
            updateChips(category)
        }
        rvCategories.layoutManager = GridLayoutManager(this, 3)
        rvCategories.adapter = categoryAdapter
        
        // Setup product adapter
        productAdapter = ProductAdapter(
            onProductClick = { product ->
                openProductDetails(product)
            },
            onAddToCart = { product ->
                addToCart(product)
            },
            onWishlistClick = { product ->
                addToWishlist(product)
            }
        )
        rvProducts.layoutManager = GridLayoutManager(this, 2)
        rvProducts.adapter = productAdapter
    }
    
    private fun loadCategories() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            try {
                // Load categories from Firestore
                val categoriesSnapshot = firestore.collection("categories")
                    .get()
                    .await()
                
                val categories = if (categoriesSnapshot.isEmpty) {
                    // If no categories in Firestore, create default ones
                    createDefaultCategories()
                    getDefaultCategories()
                } else {
                    categoriesSnapshot.documents.mapNotNull { doc ->
                        CategoryItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            icon = doc.getString("icon") ?: "",
                            category = ProductCategory.valueOf(
                                doc.getString("category") ?: "OTHER"
                            ),
                            productCount = doc.getLong("productCount")?.toInt() ?: 0
                        )
                    }
                }
                
                categoryAdapter.submitList(categories)
                
                // Load all products initially
                viewModel.loadProducts()
                
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error loading categories: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private suspend fun createDefaultCategories() {
        val defaultCategories = getDefaultCategories()
        
        defaultCategories.forEach { category ->
            firestore.collection("categories")
                .document(category.id)
                .set(mapOf(
                    "name" to category.name,
                    "icon" to category.icon,
                    "category" to category.category.name,
                    "productCount" to 0
                ))
                .await()
        }
    }
    
    private fun getDefaultCategories(): List<CategoryItem> {
        return listOf(
            CategoryItem("1", "Vegetables", "🥬", ProductCategory.VEGETABLES, 0),
            CategoryItem("2", "Fruits", "🍎", ProductCategory.FRUITS, 0),
            CategoryItem("3", "Dairy", "🥛", ProductCategory.DAIRY, 0),
            CategoryItem("4", "Eggs", "🥚", ProductCategory.EGGS, 0),
            CategoryItem("5", "Grains", "🌾", ProductCategory.GRAINS, 0),
            CategoryItem("6", "Meat", "🥩", ProductCategory.MEAT, 0),
            CategoryItem("7", "Herbs", "🌿", ProductCategory.HERBS, 0),
            CategoryItem("8", "Other", "📦", ProductCategory.OTHER, 0)
        )
    }
    
    private fun loadProductsByCategory(category: ProductCategory) {
        viewModel.loadProductsByCategory(category)
    }
    
    private fun updateChips(category: ProductCategory) {
        chipGroup.removeAllViews()
        
        val chip = Chip(this).apply {
            text = category.name.lowercase().capitalize()
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
                selectedCategory = null
                viewModel.loadProducts() // Load all products
            }
        }
        chipGroup.addView(chip)
    }
    
    private fun observeViewModel() {
        viewModel.products.observe(this) { products ->
            productAdapter.submitList(products)
            
            // Update product counts for categories
            updateCategoryProductCounts(products)
        }
        
        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.clearError()
            }
        }
    }
    
    private fun updateCategoryProductCounts(products: List<Product>) {
        val counts = products.groupBy { it.category }
            .mapValues { it.value.size }
        
        val updatedCategories = categoryAdapter.currentList.map { category ->
            category.copy(productCount = counts[category.category] ?: 0)
        }
        
        categoryAdapter.submitList(updatedCategories)
    }
    
    private fun openProductDetails(product: Product) {
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_JSON, 
            ProductDetailsActivity.toJson(product))
        startActivity(intent)
    }
    
    private fun addToCart(product: Product) {
        lifecycleScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                name = product.name,
                imageUrl = product.imageUrls.firstOrNull() ?: "",
                unit = product.unit,
                price = product.price,
                quantity = 1,
                farmerId = product.farmerId,
                farmerName = product.farmerName
            )
            
            cartRepository.addItem(cartItem)
                .onSuccess {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "${product.name} added to cart",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .onFailure { e ->
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Failed to add to cart: ${e.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        }
    }
    
    private fun addToWishlist(product: Product) {
        // TODO: Implement wishlist functionality
        Snackbar.make(
            findViewById(android.R.id.content),
            "${product.name} added to wishlist",
            Snackbar.LENGTH_SHORT
        ).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String,
    val category: ProductCategory,
    val productCount: Int
)
