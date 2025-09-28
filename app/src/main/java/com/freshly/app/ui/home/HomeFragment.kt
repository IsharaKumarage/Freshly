package com.freshly.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.model.CartItem
import com.freshly.app.data.model.Product
import com.freshly.app.data.model.WishlistItem
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.adapters.ProductAdapter
import com.freshly.app.ui.product.ProductDetailsActivity
import com.freshly.app.ui.viewmodel.AuthViewModel
import com.freshly.app.ui.viewmodel.CartViewModel
import com.freshly.app.ui.viewmodel.ProductViewModel
import com.freshly.app.ui.viewmodel.WishlistViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val wishlistViewModel: WishlistViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    private lateinit var featuredAdapter: ProductAdapter
    private lateinit var rvFeatured: RecyclerView
    private lateinit var repository: FirebaseRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = FirebaseRepository()
        setupViews(view)
        setupAdapters()
        setupObservers()
        loadData()
    }

    private fun setupViews(view: View) {
        rvFeatured = view.findViewById(R.id.rvFeaturedProducts)
        
        // Setup search bar click listener
        view.findViewById<View>(R.id.searchBar)?.setOnClickListener {
            // TODO: Navigate to marketplace/search screen
            showSuccess("Search functionality coming soon!")
        }
    }

    private fun setupAdapters() {
        featuredAdapter = ProductAdapter(
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                intent.putExtra("product_id", product.id)
                startActivity(intent)
            },
            onAddToCart = { product ->
                addToCart(product)
            },
            onWishlistClick = { product ->
                toggleWishlist(product)
            }
        )

        rvFeatured.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = featuredAdapter
        }
    }

    private fun setupObservers() {
        productViewModel.featuredProducts.observe(viewLifecycleOwner) { products ->
            featuredAdapter.submitList(products)
        }

        productViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
            view?.findViewById<View>(R.id.progressBar)?.visibility = 
                if (isLoading) View.VISIBLE else View.GONE
        }

        productViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                productViewModel.clearError()
            }
        }

        cartViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                cartViewModel.clearError()
            }
        }

        wishlistViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                wishlistViewModel.clearError()
            }
        }
    }

    private fun loadData() {
        productViewModel.loadFeaturedProducts(6) // Load 6 featured products for home
    }

    private fun addToCart(product: Product) {
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
        
        cartViewModel.addToCart(cartItem)
        showSuccess("${product.name} added to cart")
    }

    private fun toggleWishlist(product: Product) {
        val currentUser = authViewModel.currentUser.value
        if (currentUser == null) {
            showError("Please login to add items to wishlist")
            return
        }

        // For simplicity, always add to wishlist here
        // In a real app, you'd check if it's already in wishlist and toggle accordingly
        wishlistViewModel.addToWishlist(currentUser.id, product.id)
        showSuccess("${product.name} added to wishlist")
    }

    private fun showError(message: String) {
        view?.let { v ->
            Snackbar.make(v, message, Snackbar.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccess(message: String) {
        view?.let { v ->
            Snackbar.make(v, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(resources.getColor(R.color.success, null))
                .show()
        } ?: run {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
