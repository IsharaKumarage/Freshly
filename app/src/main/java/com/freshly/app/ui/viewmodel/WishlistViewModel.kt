package com.freshly.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.Product
import com.freshly.app.data.model.WishlistItem
import com.freshly.app.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class WishlistViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _wishlistItems = MutableLiveData<List<WishlistItem>>()
    val wishlistItems: LiveData<List<WishlistItem>> = _wishlistItems
    
    private val _wishlistProducts = MutableLiveData<List<Product>>()
    val wishlistProducts: LiveData<List<Product>> = _wishlistProducts
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadWishlist(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.getUserWishlist(userId).fold(
                onSuccess = { items ->
                    _wishlistItems.value = items
                    loadWishlistProducts(items)
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    private suspend fun loadWishlistProducts(wishlistItems: List<WishlistItem>) {
        val products = mutableListOf<Product>()
        wishlistItems.forEach { item ->
            repository.getProduct(item.productId).fold(
                onSuccess = { product ->
                    product?.let { products.add(it) }
                },
                onFailure = { /* Handle individual product load failure */ }
            )
        }
        _wishlistProducts.value = products
    }
    
    fun addToWishlist(userId: String, productId: String) {
        viewModelScope.launch {
            val wishlistItem = WishlistItem(
                userId = userId,
                productId = productId,
                addedAt = System.currentTimeMillis()
            )
            repository.addToWishlist(wishlistItem).fold(
                onSuccess = { 
                    loadWishlist(userId) // Refresh wishlist
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun removeFromWishlist(userId: String, productId: String) {
        viewModelScope.launch {
            repository.removeFromWishlist(userId, productId).fold(
                onSuccess = { 
                    loadWishlist(userId) // Refresh wishlist
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
