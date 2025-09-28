package com.freshly.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: NotificationManager? = null
        
        fun getInstance(context: Context): NotificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
    
    private val _badgeCount = MutableLiveData<Int>()
    val badgeCount: LiveData<Int> = _badgeCount
    
    init {
        // Load initial badge count
        _badgeCount.value = prefs.getInt("badge_count", 0)
    }
    
    /**
     * Initialize FCM and subscribe to topics
     */
    fun initializeFCM() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get FCM token
                val token = messaging.token.await()
                
                // Save token to Firestore
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .await()
                }
                
                // Subscribe to general topics
                subscribeToTopic("general_notifications")
                subscribeToTopic("deals_and_promotions")
                
                // Subscribe to user-specific topic
                userId?.let {
                    subscribeToTopic("user_$it")
                }
                
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Subscribe to FCM topic
     */
    fun subscribeToTopic(topic: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                messaging.subscribeToTopic(topic).await()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Unsubscribe from FCM topic
     */
    fun unsubscribeFromTopic(topic: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                messaging.unsubscribeFromTopic(topic).await()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Send a notification to a specific user
     */
    suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        message: String,
        type: String = "general",
        data: Map<String, Any> = emptyMap()
    ): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "title" to title,
                "message" to message,
                "type" to type,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "data" to data
            )
            
            firestore.collection("notifications")
                .document(userId)
                .collection("messages")
                .add(notification)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark all notifications as read for current user
     */
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            val batch = firestore.batch()
            val unreadNotifications = firestore.collection("notifications")
                .document(userId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            unreadNotifications.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            
            batch.commit().await()
            
            // Update badge count
            updateBadgeCount(0)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get unread notification count
     */
    suspend fun getUnreadCount(): Int {
        return try {
            val userId = auth.currentUser?.uid ?: return 0
            
            val unreadNotifications = firestore.collection("notifications")
                .document(userId)
                .collection("messages")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val count = unreadNotifications.size()
            updateBadgeCount(count)
            count
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Update badge count
     */
    fun updateBadgeCount(count: Int) {
        prefs.edit().putInt("badge_count", count).apply()
        _badgeCount.postValue(count)
    }
    
    /**
     * Clear all notifications for current user
     */
    suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            val notifications = firestore.collection("notifications")
                .document(userId)
                .collection("messages")
                .get()
                .await()
            
            val batch = firestore.batch()
            notifications.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            batch.commit().await()
            
            // Update badge count
            updateBadgeCount(0)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up when user logs out
     */
    fun onUserLogout() {
        val userId = auth.currentUser?.uid
        userId?.let {
            unsubscribeFromTopic("user_$it")
        }
        
        // Clear badge count
        updateBadgeCount(0)
    }
}
