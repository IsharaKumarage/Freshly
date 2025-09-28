package com.freshly.app.data.model

import kotlin.collections.emptyList

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0, // For discount calculation
    val quantity: Int = 0,
    val availableQuantity: Int = 0, // Current stock
    val unit: String = "kg", // kg, pieces, liters, etc.
    val category: ProductCategory = ProductCategory.VEGETABLES,
    val farmerId: String = "",
    val farmerName: String = "",
    val imageUrls: List<String> = emptyList(),
    val isOrganic: Boolean = false,
    val hasFreeShipping: Boolean = false,
    val harvestDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis(),
    val location: String = "",
    val isAvailable: Boolean = true,
    val rating: Double = 0.0, // Average rating
    val reviewCount: Int = 0, // Number of reviews
    val reviews: List<Review> = emptyList(),
    val tags: List<String> = emptyList(), // Search tags
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ProductCategory {
    VEGETABLES,
    FRUITS,
    GRAINS,
    DAIRY,
    EGGS,
    MEAT,
    HERBS,
    SPICES,
    NUTS,
    OTHER
}

data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val products: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val deliveryAddress: String = "",
    val phoneNumber: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long = 0L,
    val paymentMethod: String = "",
    val notes: String = ""
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val pricePerUnit: Double = 0.0,
    val totalPrice: Double = 0.0
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

data class Review(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isVerifiedPurchase: Boolean = false
)

data class WishlistItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

data class Farmer(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val farmName: String = "",
    val farmAddress: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val description: String = "",
    val specialties: List<String> = emptyList(),
    val certifications: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val totalProducts: Int = 0,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
