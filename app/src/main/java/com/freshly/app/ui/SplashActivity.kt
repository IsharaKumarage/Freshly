package com.freshly.app.ui

import android.annotation.SuppressLint
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
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // User is signed in, navigate to home activity
            startActivity(Intent(this, com.freshly.app.ui.home.HomeActivity::class.java))
        } else {
            // No user is signed in, navigate to login or onboarding
            val isFirstLaunch = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_first_launch", true)
                
            if (isFirstLaunch) {
                // First launch, show onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
                // Mark as not first launch anymore
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                    .putBoolean("is_first_launch", false)
                    .apply()
            } else {
                // Not first launch, go to login
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        
        finish()
    }
}
