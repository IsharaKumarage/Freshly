package com.freshly.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import com.freshly.app.ui.MainActivity
import com.freshly.app.ui.onboarding.PartnerIntroActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: MaterialTextView
    private lateinit var cardConsumer: LinearLayout
    private lateinit var cardFarmer: LinearLayout
    private lateinit var cbTerms: CheckBox

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var selectedRole: String = "CONSUMER" // or FARMER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews()
        setupClicks()
        highlightRole()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        cardConsumer = findViewById(R.id.cardConsumer)
        cardFarmer = findViewById(R.id.cardFarmer)
        cbTerms = findViewById(R.id.cbTerms)
    }

    private fun setupClicks() {
        btnRegister.setOnClickListener { attemptRegister() }
        tvLogin.setOnClickListener { finish() }

        cardConsumer.setOnClickListener { selectedRole = "CONSUMER"; highlightRole() }
        cardFarmer.setOnClickListener { selectedRole = "FARMER"; highlightRole() }
    }

    private fun attemptRegister() {
        val name = getText(etFullName)
        val email = getText(etEmail)
        val phone = getText(etPhone)
        val password = getText(etPassword)
        val confirm = getText(etConfirmPassword)

        if (TextUtils.isEmpty(name)) { etFullName.error = getString(R.string.full_name); return }
        if (TextUtils.isEmpty(email)) { etEmail.error = getString(R.string.email); return }
        if (TextUtils.isEmpty(password)) { etPassword.error = getString(R.string.password); return }
        if (password.length < 6) { etPassword.error = "Password must be at least 6 characters"; return }
        if (password != confirm) { etConfirmPassword.error = "Passwords do not match"; return }
        if (!cbTerms.isChecked) { Toast.makeText(this, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show(); return }

        btnRegister.isEnabled = false
        btnRegister.setText(R.string.loading)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onError("User ID is null")
                    return@addOnSuccessListener
                }

                val user = hashMapOf(
                    "id" to uid,
                    "fullName" to name,
                    "email" to email,
                    "phoneNumber" to phone,
                    "userType" to selectedRole,
                    "profileImageUrl" to "",
                    "address" to "",
                    "city" to "",
                    "state" to "",
                    "zipCode" to "",
                    "createdAt" to System.currentTimeMillis(),
                    "isActive" to true
                )

                firestore.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                        if (selectedRole == "FARMER") {
                            startActivity(Intent(this, PartnerIntroActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener { e -> onError(e.message) }
            }
            .addOnFailureListener { e -> onError(e.message) }
    }

    private fun getText(input: TextInputEditText): String = input.text?.toString()?.trim() ?: ""

    private fun onError(msg: String?) {
        Toast.makeText(this, msg ?: getString(R.string.error_occurred), Toast.LENGTH_LONG).show()
        btnRegister.isEnabled = true
        btnRegister.setText(R.string.register)
    }

    private fun highlightRole() {
        if (selectedRole == "CONSUMER") {
            cardConsumer.setBackgroundResource(R.drawable.splash_background)
            cardFarmer.setBackgroundColor(0xFFF7F7F7.toInt())
        } else {
            cardFarmer.setBackgroundResource(R.drawable.splash_background)
            cardConsumer.setBackgroundColor(0xFFF7F7F7.toInt())
        }
    }
}
