package com.freshly.app.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationItem, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {
    
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardNotification)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val viewUnreadIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)
        
        fun bind(notification: NotificationItem) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            tvTime.text = formatTime(notification.timestamp)
            
            // Show unread indicator
            viewUnreadIndicator.visibility = if (!notification.isRead) View.VISIBLE else View.GONE
            
            // Set icon based on type
            val iconRes = when (notification.type) {
                "order" -> R.drawable.ic_order
                "product" -> R.drawable.ic_product
                "promotion" -> R.drawable.ic_promotion
                else -> R.drawable.ic_notification
            }
            ivIcon.setImageResource(iconRes)
            
            // Set background tint for unread
            if (!notification.isRead) {
                card.setCardBackgroundColor(itemView.context.getColor(R.color.notification_unread_bg))
            } else {
                card.setCardBackgroundColor(itemView.context.getColor(android.R.color.white))
            }
            
            itemView.setOnClickListener {
                onClick(notification)
            }
        }
        
        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} min ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                diff < 604800000 -> "${diff / 86400000} days ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}
