package com.freshly.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.freshly.app.R
import com.freshly.app.data.repository.FirebaseRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FarmerOnboardingStep4Activity : AppCompatActivity() {

    private val repo = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_onboarding_step4)

        val rbConventional = findViewById<RadioButton>(R.id.rbConventional)
        val rbOrganic = findViewById<RadioButton>(R.id.rbOrganic)
        val rbHydro = findViewById<RadioButton>(R.id.rbHydroponic)
        val chipGroup = findViewById<ChipGroup>(R.id.chipsCrops)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

        rbConventional.isChecked = true

        btnBack.setOnClickListener { finish() }

        btnContinue.setOnClickListener {
            val method = when {
                rbOrganic.isChecked -> "ORGANIC"
                rbHydro.isChecked -> "HYDROPONIC"
                else -> "CONVENTIONAL"
            }
            val crops = mutableListOf<String>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip ?: continue
                if (chip.isChecked) crops.add(chip.text.toString())
            }

            lifecycleScope.launch {
                val uid = repo.getCurrentUserId()
                if (uid == null) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                val update = hashMapOf(
                    "farmingMethod" to method,
                    "crops" to crops,
                    "updatedAt" to System.currentTimeMillis()
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .set(update, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        startActivity(Intent(this@FarmerOnboardingStep4Activity, FarmerOnboardingStep5Activity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                    }
            }
        }
    }
}
