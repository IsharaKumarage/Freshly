package com.freshly.app.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freshly.app.R
import com.freshly.app.data.CouponManager
import com.freshly.app.data.SampleDataProvider
import com.freshly.app.data.model.CartItem
import com.freshly.app.data.repository.CartRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import android.content.Intent
import com.freshly.app.ui.checkout.PaymentMethodActivity

class CartFragment : Fragment() {

    private val repo = CartRepository()

    private lateinit var rv: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDelivery: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: MaterialButton

    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv = view.findViewById(R.id.rvCart)
        tvSubtotal = view.findViewById(R.id.tvSubtotalVal)
        tvDelivery = view.findViewById(R.id.tvDeliveryVal)
        tvDiscount = view.findViewById(R.id.tvDiscountVal)
        tvTotal = view.findViewById(R.id.tvTotalVal)
        btnCheckout = view.findViewById(R.id.btnCheckout)

        adapter = CartAdapter(
            onQtyChange = { id, qty -> updateQuantity(id, qty) },
            onRemove = { id -> removeItem(id) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        btnCheckout.setOnClickListener {
            val subtotal = (tvSubtotal.text ?: "0").toString()
            val delivery = (tvDelivery.text ?: "0").toString()
            val discount = (tvDiscount.text ?: "0").toString()
            val total = (tvTotal.text ?: "0").toString()
            val intent = Intent(requireContext(), PaymentMethodActivity::class.java).apply {
                putExtra(PaymentMethodActivity.EXTRA_SUBTOTAL, extractAmount(subtotal))
                putExtra(PaymentMethodActivity.EXTRA_DELIVERY, extractAmount(delivery))
                putExtra(PaymentMethodActivity.EXTRA_DISCOUNT, extractAmount(discount) * -1)
                putExtra(PaymentMethodActivity.EXTRA_TOTAL, extractAmount(total))
            }
            startActivity(intent)
        }

        loadCart()
    }

    private fun loadCart() {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.getItems()
                .onSuccess { items ->
                    adapter.submitList(items)
                    updateTotals(items)
                }
                .onFailure { e ->
                    Snackbar.make(requireView(), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun updateQuantity(itemId: String, qty: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.updateQuantity(itemId, qty)
                .onSuccess { loadCart() }
                .onFailure { e -> Snackbar.make(requireView(), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun removeItem(itemId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.removeItem(itemId)
                .onSuccess { loadCart() }
                .onFailure { e -> Snackbar.make(requireView(), e.message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun updateTotals(items: List<CartItem>) {
        val subtotal = items.sumOf { it.total }
        val delivery = if (items.isNotEmpty()) 2.99 else 0.0
        
        // Apply automatic discount using CouponManager
        val automaticDiscount = CouponManager.getAutomaticDiscount(items)
        
        // Get best available coupon suggestion
        val bestCoupon = CouponManager.getBestAvailableCoupon(items)
        
        val total = subtotal + delivery - automaticDiscount

        tvSubtotal.text = "$${String.format("%.2f", subtotal)}"
        tvDelivery.text = "Rs. ${String.format("%.2f", delivery)}"
        tvDiscount.text = "-$${String.format("%.2f", automaticDiscount)}"
        tvTotal.text = "$${String.format("%.2f", total)}"
        
        // Update checkout button text with total and item count
        val itemCount = items.sumOf { it.quantity }
        btnCheckout.text = "Checkout"
        
        // Show coupon suggestion if available
        bestCoupon?.let { coupon ->
            if (automaticDiscount == 0.0) { // Only show if no automatic discount applied
                Snackbar.make(requireView(), 
                    "💡 Add $${String.format("%.2f", coupon.minimumOrderAmount - subtotal)} more to get ${coupon.title}!", 
                    Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun extractAmount(formatted: String): Double {
        // expects formats like $8.96 or -$3.00
        val cleaned = formatted.replace("$", "").replace(",", "").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}
