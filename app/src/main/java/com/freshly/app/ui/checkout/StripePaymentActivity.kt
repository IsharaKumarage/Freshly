package com.freshly.app.ui.checkout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.freshly.app.R
import com.freshly.app.data.remote.StripeApiService
import com.freshly.app.data.remote.StripePaymentInit
import com.freshly.app.data.repository.CartRepository
import com.freshly.app.data.repository.OrderRepository
import com.google.android.material.snackbar.Snackbar
import com.stripe.android.PaymentConfiguration
import com.stripe.android.payments.paymentlauncher.StripePaymentLauncher
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StripePaymentActivity : AppCompatActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null
    private var customerId: String? = null
    private var ephemeralKey: String? = null

    private var subtotal = 0.0
    private var delivery = 0.0
    private var discount = 0.0
    private var total = 0.0
    private var method: String = PaymentMethodActivity.METHOD_CARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No dedicated layout; we immediately fetch keys and show PaymentSheet
        subtotal = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_SUBTOTAL, 0.0)
        delivery = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_DELIVERY, 0.0)
        discount = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_DISCOUNT, 0.0)
        total = intent.getDoubleExtra(PaymentMethodActivity.EXTRA_TOTAL, 0.0)
        method = intent.getStringExtra(PaymentMethodActivity.EXTRA_METHOD) ?: method

        val publishableKey = getString(R.string.stripe_publishable_key)
        if (publishableKey.isBlank() || publishableKey.contains("your_key", true)) {
            Snackbar.make(findViewById(android.R.id.content), "Set stripe_publishable_key in strings.xml", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }
        PaymentConfiguration.init(this, publishableKey)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        lifecycleScope.launch {
            val init = fetchStripeInit() ?: return@launch
            paymentIntentClientSecret = init.paymentIntent
            customerId = init.customer
            ephemeralKey = init.ephemeralKey

            presentPaymentSheet()
        }
    }

    private suspend fun fetchStripeInit(): StripePaymentInit? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = getString(R.string.stripe_backend_base_url)
            if (baseUrl.isBlank() || baseUrl.contains("your_backend", true)) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), "Set stripe_backend_base_url in strings.xml", Snackbar.LENGTH_LONG).show()
                    finish()
                }
                return@withContext null
            }
            // Ensure Retrofit baseUrl ends with a trailing slash to avoid IllegalArgumentException
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val retrofit = Retrofit.Builder()
                .baseUrl(normalizedBaseUrl)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(StripeApiService::class.java)
            return@withContext api.preparePayment()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                finish()
            }
            null
        }
    }

    private fun presentPaymentSheet() {
        val clientSecret = paymentIntentClientSecret ?: return
        val customer = customerId ?: return
        val ekey = ephemeralKey ?: return
        val config = PaymentSheet.Configuration(
            merchantDisplayName = "Freshly",
            customer = PaymentSheet.CustomerConfiguration(customer, ekey)
        )
        paymentSheet.presentWithPaymentIntent(clientSecret, config)
    }

    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Canceled -> {
                Snackbar.make(findViewById(android.R.id.content), "Payment canceled", Snackbar.LENGTH_LONG).show()
                finish()
            }
            is PaymentSheetResult.Failed -> {
                Snackbar.make(findViewById(android.R.id.content), result.error.localizedMessage ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                finish()
            }
            is PaymentSheetResult.Completed -> {
                // Payment succeeded; place order and clear cart
                lifecycleScope.launch {
                    val cartRepo = CartRepository()
                    val orderRepo = OrderRepository()
                    val items = cartRepo.getItems().getOrElse {
                        Snackbar.make(findViewById(android.R.id.content), it.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                        finish(); return@launch
                    }
                    val orderId = orderRepo.placeOrder(items, subtotal, delivery, discount, total, method).getOrElse {
                        Snackbar.make(findViewById(android.R.id.content), it.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                        finish(); return@launch
                    }
                    cartRepo.clearCart()
                    startActivity(
                        Intent(this@StripePaymentActivity, PaymentSuccessActivity::class.java)
                            .putExtra(PaymentMethodActivity.EXTRA_TOTAL, total)
                            .putExtra(PaymentMethodActivity.EXTRA_METHOD, method)
                            .putExtra(PaymentSuccessActivity.EXTRA_ORDER_ID, orderId)
                            .putExtra(PaymentSuccessActivity.EXTRA_TRANSACTION_ID, "StripePaymentSheet")
                    )
                    finish()
                }
            }
        }
    }
}
