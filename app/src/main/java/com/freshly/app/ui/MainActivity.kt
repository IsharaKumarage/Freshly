package com.freshly.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.freshly.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.freshly.app.ui.marketplace.MarketplaceFragment
import com.freshly.app.ui.cart.CartFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_marketplace -> switchFragment(MarketplaceFragment())
                R.id.nav_my_products -> switchFragment(PlaceholderFragment.newInstance("My Products"))
                R.id.nav_cart -> switchFragment(CartFragment())
                R.id.nav_profile -> switchFragment(PlaceholderFragment.newInstance("Profile"))
            }
            true
        }

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_marketplace
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
