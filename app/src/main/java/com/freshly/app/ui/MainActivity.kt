package com.freshly.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.freshly.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.freshly.app.data.AppInitializer
import com.freshly.app.ui.marketplace.MarketplaceFragment
import com.freshly.app.ui.cart.CartFragment
import com.freshly.app.ui.categories.CategoriesFragment
import com.freshly.app.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView

    private val fragments: Map<Int, Fragment> by lazy {
        mapOf(
            R.id.nav_marketplace to MarketplaceFragment(),
            R.id.nav_my_products to CategoriesFragment(),
            R.id.nav_cart to CartFragment(),
            R.id.nav_profile to ProfileFragment()
        )
    }

    private var currentItemId: Int = R.id.nav_marketplace
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        bottomNavigation.setOnItemSelectedListener { item ->
            val handled = fragments.containsKey(item.itemId)
            if (handled) switchTo(item.itemId)
            handled
        }
        bottomNavigation.setOnItemReselectedListener { /* no-op */ }

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_marketplace
            switchTo(R.id.nav_marketplace)
            
            // Initialize sample data for demo
            AppInitializer.initializeSampleData()
        } else {
            currentItemId = bottomNavigation.selectedItemId
            switchTo(currentItemId, restore = true)
        }
    }

    private fun switchTo(itemId: Int, restore: Boolean = false) {
        if (itemId == currentItemId && !restore) return
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.findFragmentByTag(currentItemId.toString())?.let { transaction.hide(it) }
        val tag = itemId.toString()
        var target = supportFragmentManager.findFragmentByTag(tag)
        if (target == null) {
            target = fragments[itemId] ?: return
            transaction.add(R.id.fragmentContainer, target, tag)
        } else {
            transaction.show(target)
        }
        transaction.commit()
        currentItemId = itemId
    }
}
