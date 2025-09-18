package com.freshly.app.data

import com.freshly.app.data.model.CartItem

data class Coupon(
    val id: String,
    val title: String,
    val description: String,
    val discountAmount: Double,
    val minimumOrderAmount: Double,
    val isPercentage: Boolean = false,
    val isActive: Boolean = true,
    val expiryDate: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
)

object CouponManager {
    
    private val availableCoupons = listOf(
        Coupon(
            id = "FRESH5",
            title = "Rs. 5 OFF",
            description = "Min spend Rs. 15",
            discountAmount = 3.0, // $3.00 as shown in UI
            minimumOrderAmount = 15.0
        ),
        Coupon(
            id = "WELCOME10",
            title = "10% OFF",
            description = "New customer discount",
            discountAmount = 10.0,
            minimumOrderAmount = 25.0,
            isPercentage = true
        ),
        Coupon(
            id = "ORGANIC15",
            title = "15% OFF Organic",
            description = "On organic products only",
            discountAmount = 15.0,
            minimumOrderAmount = 20.0,
            isPercentage = true
        )
    )
    
    fun getAvailableCoupons(): List<Coupon> {
        return availableCoupons.filter { it.isActive }
    }
    
    fun applyCoupon(couponId: String, cartItems: List<CartItem>): CouponResult {
        val coupon = availableCoupons.find { it.id == couponId && it.isActive }
            ?: return CouponResult.Invalid("Coupon not found or expired")
        
        val subtotal = cartItems.sumOf { it.total }
        
        if (subtotal < coupon.minimumOrderAmount) {
            return CouponResult.Invalid("Minimum order amount is $${String.format("%.2f", coupon.minimumOrderAmount)}")
        }
        
        val discountAmount = if (coupon.isPercentage) {
            subtotal * (coupon.discountAmount / 100.0)
        } else {
            coupon.discountAmount
        }
        
        return CouponResult.Valid(coupon, discountAmount.coerceAtMost(subtotal))
    }
    
    fun getBestAvailableCoupon(cartItems: List<CartItem>): Coupon? {
        val subtotal = cartItems.sumOf { it.total }
        
        return availableCoupons
            .filter { it.isActive && subtotal >= it.minimumOrderAmount }
            .maxByOrNull { coupon ->
                if (coupon.isPercentage) {
                    subtotal * (coupon.discountAmount / 100.0)
                } else {
                    coupon.discountAmount
                }
            }
    }
    
    fun getAutomaticDiscount(cartItems: List<CartItem>): Double {
        // Automatic discount for orders over $20
        val subtotal = cartItems.sumOf { it.total }
        return if (subtotal >= 20.0) 3.0 else 0.0
    }
}

sealed class CouponResult {
    data class Valid(val coupon: Coupon, val discountAmount: Double) : CouponResult()
    data class Invalid(val message: String) : CouponResult()
}
