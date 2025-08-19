package com.yourcompany.farmfresh.data.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val farmerName: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val availableQuantity: Int = 0
)

