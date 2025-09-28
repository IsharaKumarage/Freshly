package com.freshly.app.ui.notifications

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationsActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: NotificationAdapter
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        
        title = "Notifications"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        setupRecyclerView()
        loadNotifications()
        markAllAsRead()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.rvNotifications)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }
    
    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            try {
                val notifications = firestore.collection("notifications")
                    .document(userId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                
                val notificationList = notifications.documents.mapNotNull { doc ->
                    NotificationItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        type = doc.getString("type") ?: "general",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isRead = doc.getBoolean("isRead") ?: false,
                        data = doc.get("data") as? Map<String, Any>
                    )
                }
                
                if (notificationList.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(notificationList)
                }
                
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error loading notifications: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        
        lifecycleScope.launch {
            try {
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
                
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }
    
    private fun updateBadgeCount(count: Int) {
        // Update SharedPreferences or send broadcast to update badge
        val prefs = getSharedPreferences("notifications", MODE_PRIVATE)
        prefs.edit().putInt("badge_count", count).apply()
    }
    
    private fun handleNotificationClick(notification: NotificationItem) {
        when (notification.type) {
            "order" -> {
                // Navigate to order details
                val orderId = notification.data?.get("orderId") as? String
                // TODO: Open OrderDetailsActivity with orderId
            }
            "product" -> {
                // Navigate to product details
                val productId = notification.data?.get("productId") as? String
                // TODO: Open ProductDetailsActivity with productId
            }
            "promotion" -> {
                // Navigate to deals page
                // TODO: Open DealsActivity
            }
            else -> {
                // Just show the notification details
                Snackbar.make(
                    findViewById(android.R.id.content),
                    notification.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean,
    val data: Map<String, Any>?
)
