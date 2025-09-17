package com.freshly.app.data.repository

import com.freshly.app.data.model.CartItem
import com.freshly.app.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun itemsRef() = db.collection("carts")
        .document(requireUserId())
        .collection("items")

    private fun requireUserId(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    suspend fun addToCart(product: Product, qty: Int = 1): Result<Unit> {
        return try {
            // If item exists, increment; else create
            val query = itemsRef().whereEqualTo("productId", product.id).get().await()
            if (!query.isEmpty) {
                val doc = query.documents.first()
                val current = doc.getLong("quantity")?.toInt() ?: 0
                doc.reference.update("quantity", current + qty).await()
            } else {
                val data = hashMapOf(
                    "productId" to product.id,
                    "name" to product.name,
                    "imageUrl" to (product.imageUrls.firstOrNull() ?: ""),
                    "unit" to product.unit,
                    "price" to product.price,
                    "quantity" to qty,
                    "farmerId" to product.farmerId,
                    "farmerName" to product.farmerName,
                )
                itemsRef().add(data).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItems(): Result<List<CartItem>> {
        return try {
            val snapshot = itemsRef().get().await()
            val items = snapshot.documents.map { doc ->
                CartItem(
                    id = doc.id,
                    productId = doc.getString("productId") ?: "",
                    name = doc.getString("name") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    unit = doc.getString("unit") ?: "kg",
                    price = doc.getDouble("price") ?: 0.0,
                    quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                    farmerId = doc.getString("farmerId") ?: "",
                    farmerName = doc.getString("farmerName") ?: "",
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(itemId: String, qty: Int): Result<Unit> {
        return try {
            if (qty <= 0) {
                itemsRef().document(itemId).delete().await()
            } else {
                itemsRef().document(itemId).update("quantity", qty).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeItem(itemId: String): Result<Unit> {
        return try {
            itemsRef().document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(): Result<Unit> {
        return try {
            val snap = itemsRef().get().await()
            for (doc in snap.documents) doc.reference.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
