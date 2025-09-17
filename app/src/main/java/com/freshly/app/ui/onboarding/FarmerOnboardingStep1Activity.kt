package com.freshly.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.google.android.material.button.MaterialButton

class FarmerOnboardingStep1Activity : AppCompatActivity() {
    private lateinit var cardFarmer: LinearLayout
    private lateinit var cardConsumer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_onboarding_step1)

        cardFarmer = findViewById(R.id.cardFarmer)
        cardConsumer = findViewById(R.id.cardConsumer)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

        // Default highlight farmer
        highlight(true)
        cardFarmer.setOnClickListener { highlight(true) }
        cardConsumer.setOnClickListener { highlight(false) }

        btnContinue.setOnClickListener {
            // Force farmer path for this flow
            startActivity(Intent(this, FarmerOnboardingStep2Activity::class.java))
            finish()
        }
        btnBack.setOnClickListener { finish() }
    }

    private fun highlight(isFarmer: Boolean) {
        if (isFarmer) {
            cardFarmer.setBackgroundResource(R.drawable.splash_background)
            cardConsumer.setBackgroundColor(0xFFF7F7F7.toInt())
        } else {
            cardConsumer.setBackgroundResource(R.drawable.splash_background)
            cardFarmer.setBackgroundColor(0xFFF7F7F7.toInt())
        }
    }
}
