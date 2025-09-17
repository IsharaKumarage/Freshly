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
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    private val repo = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val btnSignOut = view.findViewById<MaterialButton>(R.id.btnSignOut)

        val uid = repo.getCurrentUserId()
        if (uid != null) {
            // In a real app, fetch the user profile from Firestore. Placeholder here.
            tvName.text = getString(R.string.full_name)
            tvEmail.text = getString(R.string.email)
        }

        btnSignOut.setOnClickListener {
            repo.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}
