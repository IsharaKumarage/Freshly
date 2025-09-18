package com.freshly.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.freshly.app.R
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import android.widget.CheckBox
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuthException

import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var repository: FirebaseRepository
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: MaterialTextView
    private lateinit var btnSignUp: MaterialButton
    private lateinit var cbRememberMe: CheckBox
    private lateinit var tvForgotPassword: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        repository = FirebaseRepository()
        initViews()
        prefillRemembered()
        setupClickListeners()
    }
    
    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        btnSignUp = findViewById(R.id.btnSignUp)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }
        
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text?.toString()?.trim()
            if (email.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.enter_email_to_reset), Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    repository.sendPasswordReset(email)
                        .onSuccess {
                            Toast.makeText(this@LoginActivity, getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show()
                        }
                        .onFailure { e ->
                            Toast.makeText(this@LoginActivity, e.message ?: getString(R.string.error_occurred), Toast.LENGTH_LONG).show()
                        }
                }
            }
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        
        return true
    }
    
    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = getString(R.string.loading)
        
        lifecycleScope.launch {
            repository.signIn(email, password)
                .onSuccess {
                    persistRememberMe(email, password)
                    Toast.makeText(this@LoginActivity, getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
                .onFailure { exception ->
                    val message = mapFirebaseAuthError(exception)
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                    btnLogin.text = getString(R.string.login)
                }
        }
    }

    private fun mapFirebaseAuthError(e: Throwable): String {
        if (e is FirebaseAuthException) {
            return when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email format"
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_USER_DISABLED" -> "This account has been disabled"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
                "ERROR_NETWORK_REQUEST_FAILED" -> getString(R.string.network_error)
                else -> e.localizedMessage ?: getString(R.string.error_occurred)
            }
        }
        return e.localizedMessage ?: getString(R.string.error_occurred)
    }

    private fun prefillRemembered() {
        val prefs = getSharedPreferences("freshly_prefs", MODE_PRIVATE)
        val remember = prefs.getBoolean("remember_me", false)
        if (remember) {
            etEmail.setText(prefs.getString("remember_email", "") ?: "")
            etPassword.setText(prefs.getString("remember_password", "") ?: "")
            cbRememberMe.isChecked = true
        }
    }

    private fun persistRememberMe(email: String, password: String) {
        val prefs = getSharedPreferences("freshly_prefs", MODE_PRIVATE)
        if (cbRememberMe.isChecked) {
            prefs.edit()
                .putBoolean("remember_me", true)
                .putString("remember_email", email)
                .putString("remember_password", password)
                .apply()
        } else {
            prefs.edit()
                .putBoolean("remember_me", false)
                .remove("remember_email")
                .remove("remember_password")
                .apply()
        }
    }
}
