package com.freshly.app.ui.marketplace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.SampleDataProvider
import com.freshly.app.data.model.Product
import com.freshly.app.data.model.ProductCategory
import com.freshly.app.data.repository.CartRepository
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.product.ProductDetailsActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MarketplaceFragment : Fragment() {

    private val repository = FirebaseRepository()
    private val cartRepository = CartRepository()
    
    private lateinit var rvDeals: RecyclerView
    private lateinit var rvNearby: RecyclerView

    private lateinit var dealsAdapter: ProductAdapter
    private lateinit var nearbyAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_marketplace, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        rvDeals = view.findViewById(R.id.rvDeals)
        rvNearby = view.findViewById(R.id.rvNearby)

        // Setup adapters with add to cart functionality
        dealsAdapter = ProductAdapter(
            onClick = { product -> openDetails(product) },
            onAddToCart = { product -> addToCart(product) }
        )
        rvDeals.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDeals.adapter = dealsAdapter

        nearbyAdapter = ProductAdapter(
            onClick = { product -> openDetails(product) },
            onAddToCart = { product -> addToCart(product) }
        )
        rvNearby.layoutManager = GridLayoutManager(requireContext(), 2)
        rvNearby.adapter = nearbyAdapter

        loadData()
    }
    
    private fun filterByCategory(category: ProductCategory) {
        val filteredProducts = SampleDataProvider.getProductsByCategory(category)
        nearbyAdapter.submitList(filteredProducts)
        
        // Show snackbar with category name
        val categoryName = when(category) {
            ProductCategory.VEGETABLES -> "Vegetables"
            ProductCategory.FRUITS -> "Fruits"
            ProductCategory.DAIRY -> "Dairy & Eggs"
            else -> "Products"
        }
        Snackbar.make(requireView(), "Showing $categoryName", Snackbar.LENGTH_SHORT).show()
    }

    private fun loadData() {
        // Use sample data for now - replace with Firebase call when ready
        val allProducts = SampleDataProvider.getSampleProducts()
        val deals = SampleDataProvider.getDiscountedProducts()
        
        dealsAdapter.submitList(deals)
        nearbyAdapter.submitList(allProducts)
        
        // Uncomment below to use Firebase data
        /*
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { products ->
                    val deals = products.filter { SampleDataProvider.getProductDiscount(it.id) > 0 }
                    nearbyAdapter.submitList(products)
                    dealsAdapter.submitList(deals)
                }
                .onFailure { e ->
                    Snackbar.make(requireView(), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
        */
    }
    
    private fun addToCart(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            cartRepository.addItem(product)
                .onSuccess {
                    Snackbar.make(requireView(), "${product.name} added to cart", Snackbar.LENGTH_SHORT).show()
                }
                .onFailure { e ->
                    Snackbar.make(requireView(), e.message ?: "Failed to add to cart", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun openDetails(product: Product) {
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_JSON, ProductDetailsActivity.toJson(product))
        startActivity(intent)
    }
}
