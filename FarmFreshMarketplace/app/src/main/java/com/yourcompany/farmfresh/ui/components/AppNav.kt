package com.yourcompany.farmfresh.ui.components

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourcompany.farmfresh.ui.home.HomeScreen
import com.yourcompany.farmfresh.ui.marketplace.MarketplaceScreen
import com.yourcompany.farmfresh.ui.orders.OrdersScreen
import com.yourcompany.farmfresh.ui.profile.ProfileScreen

object Routes {
    const val Home = "home"
    const val Marketplace = "marketplace"
    const val Orders = "orders"
    const val Profile = "profile"
}

@Composable
fun FarmFreshApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home
        ) {
            composable(Routes.Home) { HomeScreen() }
            composable(Routes.Marketplace) { MarketplaceScreen() }
            composable(Routes.Orders) { OrdersScreen() }
            composable(Routes.Profile) { ProfileScreen() }
        }
    }
}

