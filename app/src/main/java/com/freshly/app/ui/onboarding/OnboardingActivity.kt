package com.freshly.app.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import android.util.Log

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val btnGetStarted = findViewById<MaterialButton>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            markOnboardingCompleted()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun markOnboardingCompleted() {
        val editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, true)
        val success = editor.commit()
        Log.d("Onboarding", "Onboarding completion saved: $success")
    }

    companion object {
        const val PREFS_NAME = "freshly_prefs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
