package com.freshly.app.data.model

data class CartItem(
    val id: String = "",           // document id
    val productId: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val unit: String = "kg",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val farmerId: String = "",
    val farmerName: String = ""
) {
    val total: Double get() = price * quantity
}
