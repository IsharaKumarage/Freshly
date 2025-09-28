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
import com.google.firebase.firestore.FirebaseFirestore
import com.freshly.app.ui.farmer.FarmerDashboardActivity
import com.freshly.app.ui.admin.AdminDashboardActivity

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
        if (currentUser != null && rememberMe) {
            // Fetch role and route accordingly
            FirebaseFirestore.getInstance()
                .collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { snap ->
                    val role = (snap.getString("userType") ?: "CONSUMER").uppercase()
                    val target = when (role) {
                        "FARMER" -> FarmerDashboardActivity::class.java
                        "ADMIN" -> AdminDashboardActivity::class.java
                        else -> MainActivity::class.java
                    }
                    startActivity(Intent(this, target))
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevent delayed navigation from firing after Splash is finished
        handler.removeCallbacks(navigateRunnable)
    }
}
