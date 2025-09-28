package com.freshly.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.Product
import com.freshly.app.data.model.ProductCategory
import com.freshly.app.data.model.Review
import com.freshly.app.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products
    
    private val _featuredProducts = MutableLiveData<List<Product>>()
    val featuredProducts: LiveData<List<Product>> = _featuredProducts
    
    private val _productDetails = MutableLiveData<Product?>()
    val productDetails: LiveData<Product?> = _productDetails
    
    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadProducts() {
        viewModelScope.launch {
            _loading.value = true
            repository.getProducts().fold(
                onSuccess = { 
                    _products.value = it
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun loadFeaturedProducts(limit: Int = 6) {
        viewModelScope.launch {
            _loading.value = true
            repository.getFeaturedProducts(limit).fold(
                onSuccess = { 
                    _featuredProducts.value = it
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.getProduct(productId).fold(
                onSuccess = { 
                    _productDetails.value = it
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun loadProductsByCategory(category: ProductCategory) {
        viewModelScope.launch {
            _loading.value = true
            repository.getProductsByCategory(category).fold(
                onSuccess = { 
                    _products.value = it
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.searchProducts(query).fold(
                onSuccess = { 
                    _products.value = it
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun loadProductReviews(productId: String) {
        viewModelScope.launch {
            repository.getProductReviews(productId).fold(
                onSuccess = { 
                    _reviews.value = it
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun addReview(review: Review) {
        viewModelScope.launch {
            repository.addReview(review).fold(
                onSuccess = { 
                    // Reload reviews after adding
                    loadProductReviews(review.productId)
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun addProduct(product: Product) {
        viewModelScope.launch {
            _loading.value = true
            repository.addProduct(product).fold(
                onSuccess = { 
                    loadProducts() // Reload products
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun getProduct(productId: String): Flow<Product?> = flow {
        repository.getProduct(productId).fold(
            onSuccess = { emit(it) },
            onFailure = { emit(null) }
        )
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _loading.value = true
            repository.updateProduct(product.id, product).fold(
                onSuccess = { 
                    loadProducts() // Reload products
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun updateProduct(productId: String, product: Product) {
        viewModelScope.launch {
            _loading.value = true
            repository.updateProduct(productId, product).fold(
                onSuccess = { 
                    loadProducts() // Reload products
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.deleteProduct(productId).fold(
                onSuccess = { 
                    loadProducts() // Reload products
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
