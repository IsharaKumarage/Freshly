package com.freshly.app.utils

import com.freshly.app.data.SampleDataProvider
import com.freshly.app.data.model.CartItem
import com.freshly.app.data.repository.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DemoDataManager {
    
    /**
     * Adds sample cart items for demo purposes
     * This creates the exact cart shown in your UI screenshots
     */
    fun addSampleCartForDemo(cartRepository: CartRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Clear existing cart first
                cartRepository.clearCart()
                
                // Add Organic Baby Spinach (3 kg) as shown in UI
                val spinach = SampleDataProvider.getSampleProducts().find { it.id == "1" }
                spinach?.let {
                    cartRepository.addToCart(it, 3)
                }
                
            } catch (e: Exception) {
                // Silently handle errors for demo
            }
        }
    }
    
    /**
     * Gets sample cart items that match the UI design
     */
    fun getSampleCartItems(): List<CartItem> {
        val spinach = SampleDataProvider.getSampleProducts().find { it.id == "1" }
        return if (spinach != null) {
            listOf(
                CartItem(
                    id = "cart_1",
                    productId = spinach.id,
                    name = spinach.name,
                    imageUrl = spinach.imageUrls.firstOrNull() ?: "",
                    unit = spinach.unit,
                    price = spinach.price,
                    quantity = 3,
                    farmerId = spinach.farmerId,
                    farmerName = spinach.farmerName
                )
            )
        } else {
            emptyList()
        }
    }
    
    /**
     * Calculates totals matching the UI design
     */
    fun calculateDemoTotals(): DemoTotals {
        val items = getSampleCartItems()
        val subtotal = items.sumOf { it.total } // 3 * $2.99 = $8.97
        val delivery = 2.99
        val discount = 3.00 // Rs. 3.00 OFF as shown in UI
        val total = subtotal + delivery - discount // $8.96
        
        return DemoTotals(
            subtotal = subtotal,
            delivery = delivery,
            discount = discount,
            total = total,
            itemCount = items.sumOf { it.quantity }
        )
    }
}

data class DemoTotals(
    val subtotal: Double,
    val delivery: Double,
    val discount: Double,
    val total: Double,
    val itemCount: Int
)
