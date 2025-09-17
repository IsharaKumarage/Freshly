package com.freshly.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.ui.MainActivity
import com.google.android.material.button.MaterialButton
import com.freshly.app.data.repository.FirebaseRepository
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FarmerOnboardingStep5Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_onboarding_step5)

        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val btnFinish = findViewById<MaterialButton>(R.id.btnFinish)

        btnBack.setOnClickListener { finish() }
        btnFinish.setOnClickListener {
            lifecycleScope.launch {
                val uid = FirebaseRepository().getCurrentUserId()
                if (uid == null) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .set(mapOf("onboardingCompleted" to true), com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        startActivity(Intent(this@FarmerOnboardingStep5Activity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                    }
            }
        }
    }
}
