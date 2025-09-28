package com.freshly.app.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.freshly.app.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.freshly.app.ui.adapters.ProductAdapter
import com.freshly.app.data.model.Product
import com.freshly.app.data.repository.FirebaseRepository
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminDashboardActivity : AppCompatActivity() {
    private val repo = FirebaseRepository()
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        title = "Admin Dashboard"

        recycler = findViewById(R.id.rvAllListings)
        adapter = ProductAdapter(
            onProductClick = { showActions(it) },
            onAddToCart = { /* no-op for admin */ },
            onWishlistClick = { /* no-op for admin */ }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        loadAllListings()
    }

    private fun loadAllListings() {
        lifecycleScope.launch {
            repo.getProducts()
                .onSuccess { adapter.submitList(it) }
                .onFailure { e ->
                    Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun confirmDelete(product: Product) {
        if (product.id.isBlank()) return
        AlertDialog.Builder(this)
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete '${product.name}'?")
            .setPositiveButton("Delete") { d, _ ->
                d.dismiss()
                deleteProduct(product)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun approveProduct(product: Product) {
        if (product.id.isBlank()) return
        lifecycleScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("products")
                    .document(product.id)
                    .update("isApproved", true)
                    .await()
                Snackbar.make(findViewById(android.R.id.content), "Listing approved", Snackbar.LENGTH_SHORT).show()
                loadAllListings()
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showActions(product: Product) {
        val options = arrayOf(
            "Approve",
            "Delete",
            "Cancel"
        )
        AlertDialog.Builder(this)
            .setTitle(product.name)
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Approve" -> approveProduct(product)
                    "Delete" -> confirmDelete(product)
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            repo.deleteProduct(product.id)
                .onSuccess {
                    Snackbar.make(findViewById(android.R.id.content), "Listing deleted", Snackbar.LENGTH_SHORT).show()
                    loadAllListings()
                }
                .onFailure { e ->
                    Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
    }
}
