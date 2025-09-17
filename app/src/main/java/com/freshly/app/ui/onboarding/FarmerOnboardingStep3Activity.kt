package com.freshly.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.freshly.app.R
import com.freshly.app.data.repository.FirebaseRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class FarmerOnboardingStep3Activity : AppCompatActivity() {

    private val repo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_onboarding_step3)

        val etFarmName = findViewById<TextInputEditText>(R.id.etFarmName)
        val etAddress = findViewById<TextInputEditText>(R.id.etAddress)
        val etCity = findViewById<TextInputEditText>(R.id.etCity)
        val etDesc = findViewById<TextInputEditText>(R.id.etDescription)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        btnContinue.setOnClickListener {
            val farmName = etFarmName.text?.toString()?.trim().orEmpty()
            val address = etAddress.text?.toString()?.trim().orEmpty()
            val city = etCity.text?.toString()?.trim().orEmpty()
            val desc = etDesc.text?.toString()?.trim().orEmpty()

            val first = intent.getStringExtra("firstName").orEmpty()
            val last = intent.getStringExtra("lastName").orEmpty()
            val phone = intent.getStringExtra("phone").orEmpty()
            val email = intent.getStringExtra("email").orEmpty()

            lifecycleScope.launch {
                val uid = repo.getCurrentUserId()
                if (uid == null) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                    return@launch
                }

                val data = hashMapOf(
                    "firstName" to first,
                    "lastName" to last,
                    "phoneNumber" to phone,
                    "email" to email,
                    "farmName" to farmName,
                    "address" to address,
                    "city" to city,
                    "farmDescription" to desc,
                    "userType" to "FARMER",
                    "onboardingCompleted" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
                try {
                    // Merge farmer profile into user doc
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            startActivity(Intent(this@FarmerOnboardingStep3Activity, com.freshly.app.ui.MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                        }
                } catch (e: Exception) {
                    Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
