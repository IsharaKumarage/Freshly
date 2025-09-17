package com.freshly.app.data.repository

import com.freshly.app.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun requireUserId(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    suspend fun placeOrder(
        items: List<CartItem>,
        subtotal: Double,
        deliveryFee: Double,
        discount: Double,
        total: Double,
        paymentMethod: String
    ): Result<String> {
        return try {
            val userId = requireUserId()
            val now = System.currentTimeMillis()
            val order = hashMapOf(
                "userId" to userId,
                "subtotal" to subtotal,
                "deliveryFee" to deliveryFee,
                "discount" to discount,
                "total" to total,
                "paymentMethod" to paymentMethod,
                "status" to "PAID",
                "createdAt" to now,
                "itemCount" to items.sumOf { it.quantity },
            )
            val docRef = db.collection("orders").add(order).await()

            val batch = db.batch()
            items.forEach { item ->
                val itemRef = docRef.collection("items").document()
                val data = hashMapOf(
                    "productId" to item.productId,
                    "name" to item.name,
                    "imageUrl" to item.imageUrl,
                    "unit" to item.unit,
                    "price" to item.price,
                    "quantity" to item.quantity,
                    "farmerId" to item.farmerId,
                    "farmerName" to item.farmerName,
                    "total" to item.total,
                )
                batch.set(itemRef, data)
            }
            batch.commit().await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun humanDate(millis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
