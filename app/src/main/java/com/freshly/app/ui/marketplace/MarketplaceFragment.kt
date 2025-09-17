package com.freshly.app.ui.marketplace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.model.Product
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.product.ProductDetailsActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MarketplaceFragment : Fragment() {

    private val repository = FirebaseRepository()
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvDeals: RecyclerView
    private lateinit var rvNearby: RecyclerView

    private lateinit var dealsAdapter: ProductAdapter
    private lateinit var nearbyAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_marketplace, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvCategories = view.findViewById(R.id.rvCategories)
        rvDeals = view.findViewById(R.id.rvDeals)
        rvNearby = view.findViewById(R.id.rvNearby)

        categoryAdapter = CategoryAdapter(
            listOf(
                CategoryItem(R.drawable.ic_vegetables, "Vegetables"),
                CategoryItem(R.drawable.ic_fruits, "Fruits"),
                CategoryItem(R.drawable.ic_dairy, "Dairy"),
                CategoryItem(R.drawable.ic_eggs, "Eggs")
            )
        )
        rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        dealsAdapter = ProductAdapter(onClick = { product -> openDetails(product) })
        rvDeals.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvDeals.adapter = dealsAdapter

        nearbyAdapter = ProductAdapter(onClick = { product -> openDetails(product) })
        rvNearby.layoutManager = GridLayoutManager(requireContext(), 2)
        rvNearby.adapter = nearbyAdapter

        loadData()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { products ->
                    // simple split: first few as deals, rest as nearby
                    val deals = products.take(10)
                    val nearby = products
                    dealsAdapter.submitList(deals)
                    nearbyAdapter.submitList(nearby)
                }
                .onFailure { e ->
                    Snackbar.make(requireView(), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun openDetails(product: Product) {
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_JSON, ProductDetailsActivity.toJson(product))
        startActivity(intent)
    }
}
