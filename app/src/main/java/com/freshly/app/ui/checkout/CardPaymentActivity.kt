package com.freshly.app.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.freshly.app.R
import com.freshly.app.data.repository.CartRepository
import com.freshly.app.data.repository.OrderRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.UUID
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class CardPaymentActivity : AppCompatActivity(), PaymentResultListener {

    private var subtotal = 0.0
    private var delivery = 0.0
    private var discount = 0.0
    private var total = 0.0
    private var method: String = PaymentMethodActivity.METHOD_CARD

    private lateinit var etCardNumber: EditText
    private lateinit var etName: EditText
    private lateinit var etExpiry: EditText
    private lateinit var etCvv: EditText
    private lateinit var btnPay: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)

        // Preload payment SDK
        Checkout.preload(applicationContext)

        subtotal = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_SUBTOTAL, 0.0)
        delivery = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_DELIVERY, 0.0)
        discount = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_DISCOUNT, 0.0)
        total = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_TOTAL, 0.0)
        method = intent.getStringExtra(PaymentMethodActivity.EXTRA_METHOD) ?: method

        etCardNumber = findViewById(R.id.etCardNumber)
        etName = findViewById(R.id.etCardholderName)
        etExpiry = findViewById(R.id.etExpiry)
        etCvv = findViewById(R.id.etCvv)
        btnPay = findViewById(R.id.btnPay)
        val tvPayAmount = findViewById<TextView>(R.id.tvPayAmount)

        tvPayAmount.text = getString(R.string.pay_amount, getString(R.string.currency_amount, total))
        btnPay.text = tvPayAmount.text

        btnPay.setOnClickListener {
            // Optional basic validation (UI consistency)
            if (etName.text.isNullOrBlank()) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.cardholder_name), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startRazorpayCheckout()
        }
    }

    private fun startRazorpayCheckout() {
        val co = Checkout()
        // Set your Razorpay Key ID from strings.xml
        co.setKeyID(getString(R.string.razorpay_key_id))

        try {
            val options = JSONObject().apply {
                put("name", "Freshly")
                put("description", "Order Payment")
                put("currency", "INR")
                // Amount in subunits (paise). If total is in USD symbols, adapt as needed.
                val amountPaise = (total * 100).toInt()
                put("amount", amountPaise)
                put("prefill", JSONObject().apply {
                    put("name", etName.text?.toString())
                })
                put("theme", JSONObject().apply { put("color", "#1BA672") })
            }
            co.open(this, options)
        } catch (e: Exception) {
            Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        // Proceed to place order and clear cart
        lifecycleScope.launch {
            val cartRepo = CartRepository()
            val orderRepo = OrderRepository()

            val items = cartRepo.getItems().getOrElse {
                Snackbar.make(findViewById(android.R.id.content), it.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                return@launch
            }

            val orderId = orderRepo.placeOrder(items, subtotal, delivery, discount, total, method).getOrElse {
                Snackbar.make(findViewById(android.R.id.content), it.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                return@launch
            }

            cartRepo.clearCart()

            val txnId = razorpayPaymentID ?: ("TXN " + UUID.randomUUID().toString().substring(0, 6).uppercase())
            startActivity(
                Intent(this@CardPaymentActivity, PaymentSuccessActivity::class.java)
                    .putExtra(PaymentMethodActivity.EXTRA_TOTAL, total)
                    .putExtra(PaymentMethodActivity.EXTRA_METHOD, method)
                    .putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, orderId)
                    .putExtra(PaymentSuccessActivity.EXTRA_TRANSACTION_ID, txnId)
            )
            finish()
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        Snackbar.make(findViewById(android.R.id.content), response ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
    }
}
