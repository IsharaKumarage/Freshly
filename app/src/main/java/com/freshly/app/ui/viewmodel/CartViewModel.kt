package com.freshly.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.CartItem
import com.freshly.app.data.model.Product
import com.freshly.app.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    
    private val repository = CartRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var cartListener: ListenerRegistration? = null
    
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems
    
    private val _cartTotal = MutableLiveData<Double>()
    val cartTotal: LiveData<Double> = _cartTotal
    
    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        setupRealtimeCartListener()
        loadCartItems()
    }
    
    private fun setupRealtimeCartListener() {
        val userId = auth.currentUser?.uid ?: return
        
        cartListener = firestore.collection("cart")
            .document(userId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = error.message
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CartItem(
                            id = doc.id,
                            productId = doc.getString("productId") ?: "",
                            name = doc.getString("name") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            unit = doc.getString("unit") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            quantity = doc.getLong("quantity")?.toInt() ?: 1,
                            farmerId = doc.getString("farmerId") ?: "",
                            farmerName = doc.getString("farmerName") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                _cartItems.value = items
                _cartTotal.value = items.sumOf { it.total }
                _cartItemCount.value = items.sumOf { it.quantity }
            }
    }
    
    fun loadCartItems() {
        viewModelScope.launch {
            _loading.value = true
            repository.getItems().fold(
                onSuccess = { items ->
                    _cartItems.value = items
                    _cartTotal.value = items.sumOf { it.total }
                    _cartItemCount.value = items.sumOf { it.quantity }
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }
    
    fun addToCart(cartItem: CartItem) {
        viewModelScope.launch {
            repository.addItem(cartItem).fold(
                onSuccess = { 
                    loadCartItems() // Refresh cart
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun addProductToCart(product: Product, quantity: Int = 1) {
        val cartItem = CartItem(
            productId = product.id,
            name = product.name,
            imageUrl = product.imageUrls.firstOrNull() ?: "",
            unit = product.unit,
            price = product.price,
            quantity = quantity,
            farmerId = product.farmerId,
            farmerName = product.farmerName
        )
        addToCart(cartItem)
    }
    
    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateQuantity(productId, quantity).fold(
                onSuccess = { 
                    loadCartItems() // Refresh cart
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeItem(productId).fold(
                onSuccess = { 
                    loadCartItems() // Refresh cart
                    _error.value = null
                },
                onFailure = { 
                    _error.value = it.message
                }
            )
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart().fold(
                onSuccess = { 
                    loadCartItems() // Refresh cart
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
    
    override fun onCleared() {
        super.onCleared()
        cartListener?.remove()
    }
}
