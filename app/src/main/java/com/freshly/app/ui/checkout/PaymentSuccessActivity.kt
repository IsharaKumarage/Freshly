package com.freshly.app.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.data.repository.OrderRepository
import com.google.android.material.button.MaterialButton

class PaymentSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        val total = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_TOTAL, 0.0)
        val method = intent.getStringExtra(PaymentMethodActivity.EXTRA_METHOD) ?: PaymentMethodActivity.METHOD_CARD
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID) ?: ""

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)
        val tvMethodVal = findViewById<TextView>(R.id.tvMethodVal)
        val tvTxnVal = findViewById<TextView>(R.id.tvTxnVal)
        val tvDateVal = findViewById<TextView>(R.id.tvDateVal)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)

        tvTitle.text = getString(R.string.payment_successful_title)
        tvDesc.text = getString(R.string.payment_successful_desc, getString(R.string.currency_amount, total))
        tvMethodVal.text = method
        tvTxnVal.text = transactionId
        tvDateVal.text = OrderRepository().humanDate()

        btnContinue.setOnClickListener {
            startActivity(
                Intent(this, OrderConfirmedActivity::class.java)
                    .putExtra(EXTRA_ORDER_ID, orderId)
                    .putExtra(PaymentMethodActivity.EXTRA_TOTAL, total)
            )
            finish()
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_TRANSACTION_ID = "extra_txn_id"
    }
}
