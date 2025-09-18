package com.freshly.app.data.model

import kotlin.collections.emptyList

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val unit: String = "kg", // kg, pieces, liters, etc.
    val category: ProductCategory = ProductCategory.VEGETABLES,
    val farmerId: String = "",
    val farmerName: String = "",
    val imageUrls: List<String> = emptyList(),
    val isOrganic: Boolean = false,
    val harvestDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis(),
    val location: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ProductCategory {
    VEGETABLES,
    FRUITS,
    GRAINS,
    DAIRY,
    HERBS,
    SPICES,
    NUTS,
    OTHERS
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
