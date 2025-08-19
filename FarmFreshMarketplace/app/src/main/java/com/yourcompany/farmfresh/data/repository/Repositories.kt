package com.yourcompany.farmfresh.data.repository

import com.yourcompany.farmfresh.data.model.CartItem
import com.yourcompany.farmfresh.data.model.Product
import com.yourcompany.farmfresh.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(name: String, email: String, password: String): Result<User>
    suspend fun logout()
}

interface ProductRepository {
    suspend fun getFeaturedProducts(): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    suspend fun getProductById(id: String): Product?
}

interface CartRepository {
    suspend fun getCart(): List<CartItem>
    suspend fun addToCart(product: Product, quantity: Int)
    suspend fun updateQuantity(productId: String, quantity: Int)
    suspend fun removeFromCart(productId: String)
    suspend fun clearCart()
}

