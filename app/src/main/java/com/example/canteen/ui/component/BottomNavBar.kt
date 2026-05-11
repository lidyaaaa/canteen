package com.example.canteen.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String,
    cartCount: Int = 0
) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == "cart",
            onClick = {
                navController.navigate("cart")
            },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge {
                                Text(cartCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart"
                    )
                }
            },
            label = { Text("Cart") }
        )

        NavigationBarItem(
            selected = currentRoute == "orders",
            onClick = {
                navController.navigate("orders")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Orders"
                )
            },
            label = { Text("Orders") }
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}