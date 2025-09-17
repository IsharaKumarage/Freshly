package com.freshly.app.data.remote

import retrofit2.http.GET

// NOTE: You must implement a tiny backend that creates a Stripe Customer
// and returns an ephemeral key + PaymentIntent client secret for the total.
// This is a placeholder interface for that backend.
// Expected JSON:
// { "paymentIntent": "pi_..._secret_...", "ephemeralKey": "ek_test_...", "customer": "cus_..." }

interface StripeApiService {
    @GET("/payments/prepare")
    suspend fun preparePayment(): StripePaymentInit
}

data class StripePaymentInit(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String
)
