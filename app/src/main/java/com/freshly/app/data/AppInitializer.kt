package com.freshly.app.data

import com.freshly.app.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppInitializer {
    
    fun initializeSampleData() {
        // Only initialize if user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val cartRepository = CartRepository()
                    
                    // Add sample cart item for demo (Organic Baby Spinach)
                    val sampleProduct = SampleDataProvider.getSampleProducts().first { it.id == "1" }
                    cartRepository.addToCart(sampleProduct, 3)
                    
                } catch (e: Exception) {
                    // Silently fail - this is just for demo purposes
                }
            }
        }
    }
    
    fun clearSampleData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val cartRepository = CartRepository()
                    cartRepository.clearCart()
                } catch (e: Exception) {
                    // Silently fail
                }
            }
        }
    }
}
