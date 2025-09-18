package com.freshly.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.ui.auth.LoginActivity
import com.freshly.app.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

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
            startActivity(Intent(this, com.freshly.app.ui.home.HomeActivity::class.java))
        } else {
            val prefs = getSharedPreferences("freshly_prefs", Context.MODE_PRIVATE)
            val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)
            Log.d("Splash", "Onboarding completed: $isOnboardingCompleted")
                
            if (isOnboardingCompleted) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }
        
        finish()
    }
}
