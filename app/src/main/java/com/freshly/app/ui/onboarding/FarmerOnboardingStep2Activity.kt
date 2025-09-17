package com.freshly.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class FarmerOnboardingStep2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_onboarding_step2)

        val etFirst = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLast = findViewById<TextInputEditText>(R.id.etLastName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        btnContinue.setOnClickListener {
            val intent = Intent(this, FarmerOnboardingStep3Activity::class.java).apply {
                putExtra("firstName", etFirst.text?.toString()?.trim())
                putExtra("lastName", etLast.text?.toString()?.trim())
                putExtra("phone", etPhone.text?.toString()?.trim())
                putExtra("email", etEmail.text?.toString()?.trim())
            }
            startActivity(intent)
            finish()
        }
    }
}
