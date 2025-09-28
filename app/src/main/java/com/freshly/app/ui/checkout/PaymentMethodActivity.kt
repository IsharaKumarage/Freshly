package com.freshly.app.ui.checkout

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.utils.PriceUtil

class PaymentMethodActivity : AppCompatActivity() {

    private var subtotal = 0.0
    private var delivery = 0.0
    private var discount = 0.0
    private var total = 0.0

    private lateinit var optionCard: LinearLayout
    private lateinit var optionBank: LinearLayout
    private lateinit var optionWallet: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDelivery: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnContinue: com.google.android.material.button.MaterialButton

    private var selectedMethod: String = METHOD_CARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        subtotal = intent.getDoubleExtra(EXTRA_SUBTOTAL, 0.0)
        delivery = intent.getDoubleExtra(EXTRA_DELIVERY, 0.0)
        discount = intent.getDoubleExtra(EXTRA_DISCOUNT, 0.0)
        total = intent.getDoubleExtra(EXTRA_TOTAL, 0.0)

        optionCard = findViewById(R.id.optionCard)
        optionBank = findViewById(R.id.optionBank)
        optionWallet = findViewById(R.id.optionWallet)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvDelivery = findViewById(R.id.tvDelivery)
        tvDiscount = findViewById(R.id.tvDiscount)
        tvTotal = findViewById(R.id.tvTotal)
        btnContinue = findViewById(R.id.btnContinue)

        tvSubtotal.text = PriceUtil.formatPrice(subtotal)
        tvDelivery.text = PriceUtil.formatPrice(delivery)
        tvDiscount.text = "-${PriceUtil.formatPrice(discount).removePrefix("Rs. ")}"
        tvTotal.text = PriceUtil.formatPrice(total)

        optionCard.setOnClickListener { selectedMethod = METHOD_CARD; highlightSelection() }
        optionBank.setOnClickListener { selectedMethod = METHOD_BANK; highlightSelection() }
        optionWallet.setOnClickListener { selectedMethod = METHOD_WALLET; highlightSelection() }

        btnContinue.setOnClickListener {
            val target = if (selectedMethod == METHOD_CARD) StripePaymentActivity::class.java else CardPaymentActivity::class.java
            val intent = Intent(this, target)
                .putExtra(EXTRA_SUBTOTAL, subtotal)
                .putExtra(EXTRA_DELIVERY, delivery)
                .putExtra(EXTRA_DISCOUNT, discount)
                .putExtra(EXTRA_TOTAL, total)
                .putExtra(EXTRA_METHOD, selectedMethod)
            startActivity(intent)
        }

        highlightSelection()
    }

    private fun highlightSelection() {
        optionCard.alpha = if (selectedMethod == METHOD_CARD) 1f else 0.6f
        optionBank.alpha = if (selectedMethod == METHOD_BANK) 1f else 0.6f
        optionWallet.alpha = if (selectedMethod == METHOD_WALLET) 1f else 0.6f
    }

    companion object {
        const val EXTRA_SUBTOTAL = "extra_subtotal"
        const val EXTRA_DELIVERY = "extra_delivery"
        const val EXTRA_DISCOUNT = "extra_discount"
        const val EXTRA_TOTAL = "extra_total"
        const val EXTRA_METHOD = "extra_method"

        const val METHOD_CARD = "Credit/Debit Card"
        const val METHOD_BANK = "Bank Transfer"
        const val METHOD_WALLET = "Freshly Wallet"
    }
}
