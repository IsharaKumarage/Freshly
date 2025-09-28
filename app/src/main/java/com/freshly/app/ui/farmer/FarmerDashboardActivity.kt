package com.freshly.app.ui.farmer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.model.Product
import com.freshly.app.data.repository.FirebaseRepository
import com.freshly.app.ui.adapters.ProductAdapter
import com.freshly.app.ui.product.AddEditProductActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FarmerDashboardActivity : AppCompatActivity() {
    private val repo = FirebaseRepository()
    private lateinit var recycler: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_dashboard)
        title = getString(R.string.my_products)
        recycler = findViewById(R.id.rvMyListings)
        fab = findViewById(R.id.fabAddListing)

        adapter = ProductAdapter(
            onProductClick = { /* open edit */ openEditor(it) },
            onAddToCart = { /* no-op in dashboard */ },
            onWishlistClick = { /* no-op */ }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }

        loadMyListings()
    }

    private fun openEditor(product: Product) {
        val intent = Intent(this, AddEditProductActivity::class.java)
            .putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id)
            .putExtra(AddEditProductActivity.EXTRA_IS_EDIT, true)
        startActivity(intent)
    }

    private fun loadMyListings() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.login_required, Snackbar.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            repo.getProductsByFarmer(uid)
                .onSuccess { adapter.submitList(it) }
                .onFailure { e ->
                    Snackbar.make(findViewById(android.R.id.content), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
    }
}
