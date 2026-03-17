package com.example.newspulse.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun NewsPulseBottomBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("explore", "Explore", Icons.Default.Explore),
        BottomNavItem("saved", "Saved", Icons.Default.Bookmark),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF1C1B1F),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = when {
                currentRoute == item.route -> true
                currentRoute?.startsWith("articleDetail") == true -> item.route == "home"
                currentRoute == "filters" -> item.route == "explore"
                currentRoute == "interests" -> item.route == "profile"
                else -> false
            }
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    if (currentRoute == item.route) return@NavigationBarItem
                    when {
                        currentRoute == "interests" && item.route == "home" -> {
                            navController.popBackStack("profile", inclusive = true)
                            navController.navigate("home") { launchSingleTop = true }
                        }
                        currentRoute == "interests" && item.route != "home" -> {
                            navController.popBackStack("profile", inclusive = true)
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        item.route == "home" -> {
                            navController.popBackStack("home", inclusive = false)
                        }
                        else -> {
                            navController.navigate(item.route) {
                                popUpTo("home") { saveState = true; inclusive = false }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1C1B1F),
                    selectedTextColor = Color(0xFF1C1B1F),
                    unselectedIconColor = Color(0xFF79747E),
                    unselectedTextColor = Color(0xFF79747E),
                    indicatorColor = Color(0xFFE7E0EC)
                )
            )
        }
    }
}
