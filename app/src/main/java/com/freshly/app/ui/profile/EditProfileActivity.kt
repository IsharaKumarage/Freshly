package com.freshly.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.freshly.app.R
import com.freshly.app.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnChangePhoto: MaterialButton
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(ivProfileImage)
        }
    }

    private fun resetProfileFieldColors() {
        val black = androidx.core.content.ContextCompat.getColor(this, android.R.color.black)
        val grayHint = androidx.core.content.ContextCompat.getColor(this, android.R.color.darker_gray)
        // Clear potential TextInputLayout errors if present (ignore if not used)
        // findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilName)?.error = null
        // findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail)?.error = null
        // findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPhone)?.error = null
        // findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilAddress)?.error = null

        etName.setTextColor(black)
        etEmail.setTextColor(black)
        etPhone.setTextColor(black)
        etAddress.setTextColor(black)

        etName.setHintTextColor(grayHint)
        etEmail.setHintTextColor(grayHint)
        etPhone.setHintTextColor(grayHint)
        etAddress.setHintTextColor(grayHint)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        title = "Edit Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        setupListeners()
        loadUserProfile()
    }
    
    private fun initViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupListeners() {
        btnChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        btnSave.setOnClickListener {
            saveProfile()
        }
        
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }
    
    private fun loadUserProfile() {
        val user = auth.currentUser ?: return
        
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            try {
                // Load from Firebase Auth
                etName.setText(user.displayName)
                etEmail.setText(user.email)
                currentImageUrl = user.photoUrl?.toString()
                
                // Load additional data from Firestore
                val userDoc = firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    etPhone.setText(userDoc.getString("phone"))
                    etAddress.setText(userDoc.getString("address"))
                    
                    // Load profile image
                    val imageUrl = userDoc.getString("profileImage") ?: currentImageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@EditProfileActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(ivProfileImage)
                    }
                }
                
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error loading profile: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        
        if (name.isEmpty() || email.isEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Name and email are required",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
            
            try {
                val user = auth.currentUser ?: throw Exception("User not logged in")
                
                // Upload image if new one selected
                val imageUrl = if (selectedImageUri != null) {
                    uploadProfileImage(selectedImageUri!!)
                } else {
                    currentImageUrl
                }
                
                // Update Firebase Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .apply {
                        imageUrl?.let { setPhotoUri(Uri.parse(it)) }
                    }
                    .build()
                
                user.updateProfile(profileUpdates).await()
                
                // Update email if changed
                if (email != user.email) {
                    user.updateEmail(email).await()
                }
                
                // Update Firestore document
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "address" to address,
                    "profileImage" to imageUrl,
                    "updatedAt" to System.currentTimeMillis()
                )
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .await()
                
                Snackbar.make(
                    findViewById(android.R.id.content), 
                    "Profile updated successfully", 
                    Snackbar.LENGTH_SHORT
                ).show()
                resetProfileFieldColors()
                
                setResult(Activity.RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error updating profile: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
            }
        }
    }
    
    private suspend fun uploadProfileImage(uri: Uri): String {
        // If Firebase Storage is disabled (free plan), persist a local copy and return file:// URL
        val useStorage = resources.getBoolean(R.bool.use_firebase_storage)
        val useRealtimeDataUri = resources.getBoolean(R.bool.use_realtime_db_images)
        if (!useStorage) {
            if (useRealtimeDataUri) {
                // Return a data URI so it can be stored directly in Firestore/RTDB and rendered by Glide
                try {
                    val input = contentResolver.openInputStream(uri) ?: throw Exception("Cannot open image")
                    val bmp = BitmapFactory.decodeStream(input) ?: throw Exception("Decode failed")
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val encoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    return "data:image/jpeg;base64,$encoded"
                } catch (e: Exception) {
                    throw Exception("Failed to encode image: ${e.message}")
                }
            } else {
                try {
                    val input = contentResolver.openInputStream(uri) ?: throw Exception("Cannot open image")
                    val outFile = java.io.File(cacheDir, "profile_${UUID.randomUUID()}.jpg")
                    input.use { ins -> outFile.outputStream().use { outs -> ins.copyTo(outs) } }
                    return outFile.toURI().toString()
                } catch (e: Exception) {
                    throw Exception("Failed to save local image: ${e.message}")
                }
            }
        }
        return try {
            val storageRef = storage.reference
            val imageRef = storageRef.child("profiles/${auth.currentUser?.uid}/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteAccount() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            try {
                val user = auth.currentUser ?: throw Exception("User not logged in")
                
                // Delete user document from Firestore
                firestore.collection("users")
                    .document(user.uid)
                    .delete()
                    .await()
                
                // Delete user's cart (correct collection: "carts/{uid}/items")
                run {
                    val itemsSnap = firestore.collection("carts")
                        .document(user.uid)
                        .collection("items")
                        .get()
                        .await()
                    val batch = firestore.batch()
                    itemsSnap.documents.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().await()
                }

                // Delete user's wishlist (top-level collection filtered by userId)
                run {
                    val wishSnap = firestore.collection("wishlist")
                        .whereEqualTo("userId", user.uid)
                        .get()
                        .await()
                    val batch = firestore.batch()
                    wishSnap.documents.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().await()
                }
                
                // Delete profile image from storage
                currentImageUrl?.let { url ->
                    try {
                        storage.getReferenceFromUrl(url).delete().await()
                    } catch (e: Exception) {
                        // Ignore if image doesn't exist
                    }
                }
                
                // Delete Firebase Auth user
                user.delete().await()
                
                // Navigate to login screen
                val intent = Intent(this@EditProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error deleting account: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
