package com.freshly.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.ui.auth.LoginActivity
import com.freshly.app.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    private val splashTimeOut: Long = 3000 // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        auth = FirebaseAuth.getInstance()
        
        Handler(Looper.getMainLooper()).postDelayed({
            routeFromSplash()
        }, splashTimeOut)
    }
    
    private fun routeFromSplash() {
        val prefs = getSharedPreferences("freshly_prefs", MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        if (!onboardingCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        val currentUser = auth.currentUser
        val intent = if (currentUser != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
