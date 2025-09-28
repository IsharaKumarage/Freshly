package com.freshly.app.data.repository

import com.freshly.app.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Authentication
    suspend fun signUp(email: String, password: String, user: User): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            
            // Save user data to Firestore
            firestore.collection("users").document(userId).set(user).await()
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    // User operations
    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserData(userId: String, user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Product operations
    suspend fun addProduct(product: Product): Result<String> {
        return try {
            val documentRef = firestore.collection("products").add(product).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val querySnapshot = firestore.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val products = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(id = document.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByFarmer(farmerId: String): Result<List<Product>> {
        return try {
            val querySnapshot = firestore.collection("products")
                .whereEqualTo("farmerId", farmerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val products = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(id = document.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(productId: String, product: Product): Result<Unit> {
        return try {
            firestore.collection("products").document(productId).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            firestore.collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val querySnapshot = firestore.collection("products")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .await()
            
            val products = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(id = document.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProduct(productId: String): Result<Product?> {
        return try {
            val document = firestore.collection("products").document(productId).get().await()
            val product = document.toObject(Product::class.java)?.copy(id = document.id)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Review operations
    suspend fun addReview(review: Review): Result<String> {
        return try {
            val documentRef = firestore.collection("reviews").add(review).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductReviews(productId: String): Result<List<Review>> {
        return try {
            val querySnapshot = firestore.collection("reviews")
                .whereEqualTo("productId", productId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val reviews = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Review::class.java)?.copy(id = document.id)
            }
            
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Wishlist operations
    suspend fun addToWishlist(wishlistItem: WishlistItem): Result<String> {
        return try {
            val documentRef = firestore.collection("wishlist").add(wishlistItem).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeFromWishlist(userId: String, productId: String): Result<Unit> {
        return try {
            val querySnapshot = firestore.collection("wishlist")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", productId)
                .get()
                .await()
            
            querySnapshot.documents.forEach { document ->
                document.reference.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserWishlist(userId: String): Result<List<WishlistItem>> {
        return try {
            val querySnapshot = firestore.collection("wishlist")
                .whereEqualTo("userId", userId)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val wishlistItems = querySnapshot.documents.mapNotNull { document ->
                document.toObject(WishlistItem::class.java)?.copy(id = document.id)
            }
            
            Result.success(wishlistItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Farmer operations
    suspend fun addFarmer(farmer: Farmer): Result<String> {
        return try {
            val documentRef = firestore.collection("farmers").add(farmer).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFarmer(farmerId: String): Result<Farmer?> {
        return try {
            val document = firestore.collection("farmers").document(farmerId).get().await()
            val farmer = document.toObject(Farmer::class.java)?.copy(id = document.id)
            Result.success(farmer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateFarmer(farmerId: String, farmer: Farmer): Result<Unit> {
        return try {
            firestore.collection("farmers").document(farmerId).set(farmer).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFeaturedProducts(limit: Int = 10): Result<List<Product>> {
        return try {
            val querySnapshot = firestore.collection("products")
                .whereEqualTo("isAvailable", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val products = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(id = document.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByCategory(category: ProductCategory): Result<List<Product>> {
        return try {
            val querySnapshot = firestore.collection("products")
                .whereEqualTo("category", category.name)
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val products = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(id = document.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
