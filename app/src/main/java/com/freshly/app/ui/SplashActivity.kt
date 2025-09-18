package com.freshly.app.ui

import android.annotation.SuppressLint
import android.content.Context
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
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
    private val navigateRunnable = Runnable { routeFromSplash() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()

        handler.postDelayed(navigateRunnable, splashTimeOut)
    }
    
    private fun routeFromSplash() {
        val prefs = getSharedPreferences("freshly_prefs", Context.MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        if (!onboardingCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        val currentUser = auth.currentUser
        // Only auto-skip to Main if the user opted to be remembered and is still signed-in
        val rememberMe = prefs.getBoolean("remember_me", false)
        val intent = if (currentUser != null && rememberMe) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevent delayed navigation from firing after Splash is finished
        handler.removeCallbacks(navigateRunnable)
    }
}
