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

class CardPaymentActivity : AppCompatActivity() {

    private var subtotal = 0.0
    private var delivery = 0.0
    private var discount = 0.0
    private var total = 0.0
    private var method: String = PaymentMethodActivity.METHOD_CARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)

        subtotal = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_SUBTOTAL, 0.0)
        delivery = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_DELIVERY, 0.0)
        discount = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_DISCOUNT, 0.0)
        total = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_TOTAL, 0.0)
        method = intent.getStringExtra(PaymentMethodActivity.EXTRA_METHOD) ?: method

        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etName = findViewById<EditText>(R.id.etCardholderName)
        val etExpiry = findViewById<EditText>(R.id.etExpiry)
        val etCvv = findViewById<EditText>(R.id.etCvv)
        val btnPay = findViewById<MaterialButton>(R.id.btnPay)
        val tvPayAmount = findViewById<TextView>(R.id.tvPayAmount)

        tvPayAmount.text = getString(R.string.pay_amount, getString(R.string.currency_amount, total))
        btnPay.text = tvPayAmount.text

        btnPay.setOnClickListener {
            // Basic validation
            if (etCardNumber.text.isNullOrBlank() || etName.text.isNullOrBlank() || etExpiry.text.isNullOrBlank() || etCvv.text.isNullOrBlank()) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

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

                // Clear cart after placing order
                cartRepo.clearCart()

                // Simulated transaction id
                val txnId = "TXN " + UUID.randomUUID().toString().substring(0, 6).uppercase()

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
    }
}
