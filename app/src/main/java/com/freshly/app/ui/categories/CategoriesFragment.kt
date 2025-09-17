package com.freshly.app.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.marketplace.ProductAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import androidx.core.view.children

class CategoriesFragment : Fragment() {

    private val repository = FirebaseRepository()
    private lateinit var rv: RecyclerView
    private lateinit var chips: ChipGroup
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv = view.findViewById(R.id.rvProducts)
        chips = view.findViewById(R.id.chipGroup)

        adapter = ProductAdapter(onClick = { /* reuse Marketplace onClick in future */ })
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter

        setupChips()
        loadProducts()
    }

    private fun setupChips() {
        val categories = listOf("All", "Vegetables", "Fruits", "Dairy", "Eggs")
        categories.forEachIndexed { index, label ->
            val chip = LayoutInflater.from(requireContext()).inflate(R.layout.view_filter_chip, chips, false) as Chip
            chip.text = label
            chip.isCheckable = true
            chip.isChecked = index == 0
            chip.setOnClickListener { loadProducts() }
            chips.addView(chip)
        }
    }

    private fun loadProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { products ->
                    // If a specific chip is checked, simple filter by name containing label
                    val selected = chips.children.filterIsInstance<Chip>().firstOrNull { it.isChecked }?.text?.toString()
                    val filtered = if (selected == null || selected == "All") products else products.filter { p ->
                        p.category.name.contains(selected.uppercase()) || p.name.contains(selected, ignoreCase = true)
                    }
                    adapter.submitList(filtered)
                }
                .onFailure { e ->
                    Snackbar.make(requireView(), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
    }
}
