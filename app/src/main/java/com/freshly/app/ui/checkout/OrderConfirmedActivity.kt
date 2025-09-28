package com.freshly.app.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.ui.MainActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.freshly.app.utils.PriceUtil

class OrderConfirmedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmed)

        val total = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_TOTAL, 0.0)
        val orderId = intent.getStringExtra(PaymentSuccessActivity.EXTRA_ORDER_ID) ?: ""

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvSlot = findViewById<TextView>(R.id.tvSlot)
        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvDelivery = findViewById<TextView>(R.id.tvDelivery)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val btnContinueShopping = findViewById<MaterialButton>(R.id.btnContinueShopping)
        val tvOrderHistory = findViewById<TextView>(R.id.tvOrderHistory)

        tvTitle.text = getString(R.string.order_confirmed_title)
        tvDesc.text = getString(R.string.order_confirmed_desc, orderId)

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 2) // estimate in 2 days
        val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(cal.time)
        tvDate.text = dateStr
        tvSlot.text = getString(R.string.between_label, "9:00 AM", "12:00 PM")

        // Placeholder summary: show total only (others 0). Extend with real values if passed.
        tvSubtotal.text = PriceUtil.formatPrice(0.0)
        tvDelivery.text = PriceUtil.formatPrice(3.99)
        tvTotal.text = PriceUtil.formatPrice(total)

        btnContinueShopping.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
        tvOrderHistory.setOnClickListener {
            // TODO: navigate to an Orders screen when implemented
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
