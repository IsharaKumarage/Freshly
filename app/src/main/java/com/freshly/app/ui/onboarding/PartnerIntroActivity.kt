package com.freshly.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.google.android.material.button.MaterialButton

class PartnerIntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_intro)

        findViewById<MaterialButton>(R.id.btnGetStartedPartner).setOnClickListener {
            startActivity(Intent(this, FarmerOnboardingStep1Activity::class.java))
            finish()
        }
    }
}
