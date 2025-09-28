package com.freshly.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.freshly.app.R
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.auth.LoginActivity
import com.freshly.app.ui.notifications.NotificationsActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.freshly.app.utils.ImageUtil

class ProfileFragment : Fragment() {

    private val repo = FirebaseRepository()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var tvHeaderName: TextView? = null
    private var tvHeaderEmail: TextView? = null
    private var tvNameVal: TextView? = null
    private var tvEmailVal: TextView? = null
    private var tvPhoneVal: TextView? = null
    private var tvAddressVal: TextView? = null
    private var ivProfileThumb: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvHeaderName = view.findViewById(R.id.tvName)
        tvHeaderEmail = view.findViewById(R.id.tvEmail)
        tvNameVal = view.findViewById(R.id.tvNameVal)
        tvEmailVal = view.findViewById(R.id.tvEmailVal)
        tvPhoneVal = view.findViewById(R.id.tvPhoneVal)
        tvAddressVal = view.findViewById(R.id.tvAddressVal)
        ivProfileThumb = view.findViewById(R.id.ivProfileThumb)
        val btnSignOut = view.findViewById<MaterialButton>(R.id.btnSignOut)
        val btnEditProfile = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnNotifications = view.findViewById<MaterialButton>(R.id.btnNotifications)

        // Initial load
        refreshUser()

        btnSignOut.setOnClickListener {
            repo.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload in case user updated profile
        refreshUser()
    }

    private fun refreshUser() {
        val user = auth.currentUser ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Header from FirebaseAuth
                tvHeaderName?.text = user.displayName ?: getString(R.string.full_name)
                tvHeaderEmail?.text = user.email ?: getString(R.string.email)

                // Additional from Firestore
                val doc = firestore.collection("users").document(user.uid).get().await()
                val name = doc.getString("name")
                val email = doc.getString("email")
                val phone = doc.getString("phone")
                val address = doc.getString("address")
                val profileImage = doc.getString("profileImage")
                tvNameVal?.text = name?.takeIf { it.isNotBlank() } ?: "-"
                tvEmailVal?.text = email?.takeIf { it.isNotBlank() } ?: user.email ?: "-"
                tvPhoneVal?.text = phone?.takeIf { it.isNotBlank() } ?: "-"
                tvAddressVal?.text = address?.takeIf { it.isNotBlank() } ?: "-"

                // Load profile thumbnail (supports data URI/file/http)
                profileImage?.let { url ->
                    Glide.with(requireContext())
                        .load(ImageUtil.asGlideModel(url))
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(ivProfileThumb!!)
                }
            } catch (_: Exception) {
                // Keep graceful defaults
            }
        }
    }
}
