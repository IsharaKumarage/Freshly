package com.freshly.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.freshly.app.R
import com.freshly.app.ui.MainActivity
import com.freshly.app.ui.notifications.NotificationsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FreshlyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val CHANNEL_ID = "freshly_notifications"
        private const val CHANNEL_NAME = "Freshly Notifications"
        private const val NOTIFICATION_ID = 1001
    }
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle FCM messages here
        val title = remoteMessage.notification?.title ?: "Freshly"
        val body = remoteMessage.notification?.body ?: ""
        val data = remoteMessage.data
        
        // Save notification to Firestore
        saveNotificationToFirestore(title, body, data)
        
        // Show local notification
        showNotification(title, body, data)
        
        // Update badge count
        updateBadgeCount()
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Send token to server or save to Firestore
        saveTokenToFirestore(token)
    }
    
    private fun saveTokenToFirestore(token: String) {
        val userId = auth.currentUser?.uid ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .await()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun saveNotificationToFirestore(title: String, body: String, data: Map<String, String>) {
        val userId = auth.currentUser?.uid ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notification = hashMapOf(
                    "title" to title,
                    "message" to body,
                    "type" to (data["type"] ?: "general"),
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false,
                    "data" to data
                )
                
                firestore.collection("notifications")
                    .document(userId)
                    .collection("messages")
                    .add(notification)
                    .await()
                    
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = when (data["type"]) {
            "order" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("navigate_to", "orders")
                    putExtra("order_id", data["orderId"])
                }
            }
            "product" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("navigate_to", "product_details")
                    putExtra("product_id", data["productId"])
                }
            }
            else -> {
                Intent(this, NotificationsActivity::class.java)
            }
        }
        
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getColor(R.color.freshly_green))
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Freshly app"
                enableLights(true)
                lightColor = getColor(R.color.freshly_green)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun updateBadgeCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                
                val unreadCount = firestore.collection("notifications")
                    .document(userId)
                    .collection("messages")
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()
                    .size()
                
                // Save badge count to SharedPreferences
                val prefs = getSharedPreferences("notifications", Context.MODE_PRIVATE)
                prefs.edit().putInt("badge_count", unreadCount).apply()
                
                // Send broadcast to update UI
                val intent = Intent("com.freshly.app.BADGE_UPDATE")
                intent.putExtra("count", unreadCount)
                sendBroadcast(intent)
                
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}
