package com.yourcompany.farmfresh.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yourcompany.farmfresh.R

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.Home,
            onClick = { navController.navigate(Routes.Home) },
            icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.home)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Marketplace,
            onClick = { navController.navigate(Routes.Marketplace) },
            icon = { Icon(androidx.compose.material.icons.Icons.Default.Storefront, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.marketplace)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Orders,
            onClick = { navController.navigate(Routes.Orders) },
            icon = { Icon(androidx.compose.material.icons.Icons.Default.ShoppingBag, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.orders)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Profile,
            onClick = { navController.navigate(Routes.Profile) },
            icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.profile)) }
        )
    }
}

