package com.freshly.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.freshly.app.R

class PlaceholderFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_placeholder, container, false)
        val label = view.findViewById<TextView>(R.id.tvLabel)
        label.text = arguments?.getString(ARG_TITLE) ?: "Screen"
        return view
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        fun newInstance(title: String): PlaceholderFragment {
            val f = PlaceholderFragment()
            f.arguments = Bundle().apply { putString(ARG_TITLE, title) }
            return f
        }
    }
}
