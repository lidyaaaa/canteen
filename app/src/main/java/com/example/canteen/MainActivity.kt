package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.CanteenTheme
import kotlinx.coroutines.launch

// SCREEN
import com.example.canteen.ui.screen.*
import com.example.canteen.ui.screen.admin.AdminAddMenuScreen
import com.example.canteen.ui.screen.admin.AdminHomeScreen
import com.example.canteen.ui.screen.admin.AdminMenuRequestScreen
import com.example.canteen.ui.screen.admin.AdminMenuScreen
import com.example.canteen.ui.screen.admin.AdminSellerRequestsScreen
import com.example.canteen.ui.screen.buyer.CartScreen
import com.example.canteen.ui.screen.buyer.HomeScreen
import com.example.canteen.ui.screen.buyer.MenuDetailScreen
import com.example.canteen.ui.screen.buyer.OrdersScreen
import com.example.canteen.ui.screen.buyer.ProfileScreen
import com.example.canteen.ui.screen.seller.SellerHomeScreen
import com.example.canteen.ui.screen.seller.SellerMenuRequestStatusScreen
import com.example.canteen.ui.screen.seller.SellerMenuScreen
import com.example.canteen.ui.screen.seller.SellerOrderHistoryScreen
import com.example.canteen.ui.screen.seller.SellerOrdersScreen
import com.example.canteen.ui.screen.seller.SellerProfileScreen
import com.example.canteen.ui.screen.seller.SellerRequestScreen

class   MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CanteenTheme {

                val navController = rememberNavController()
                val repository = remember { FirebaseRepository() }
                val scope = rememberCoroutineScope()

                var startDestination by remember { mutableStateOf<String?>(null) }

                // 🔥 CHECK LOGIN STATUS
                LaunchedEffect(Unit) {
                    scope.launch {
                        if (repository.isUserLoggedIn()) {
                            val result = repository.getCurrentUser()
                            result.onSuccess { user ->
                                startDestination = when (user.role) {
                                    "admin" -> "admin_home"
                                    "seller" -> "seller_home"
                                    else -> "home"
                                }
                            }
                            result.onFailure {
                                startDestination = "login"
                            }
                        } else {
                            startDestination = "login"
                        }
                    }
                }

                if (startDestination != null) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->

                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            modifier = Modifier.padding(innerPadding)
                        ) {

                            // 🔐 AUTH
                            composable("login") {
                                LoginScreen(navController)
                            }

                            composable("register") {
                                RegisterScreen(navController)
                            }

                            // 🛒 BUYER SCREENS
                            composable("home") {
                                HomeScreen(navController)
                            }

                            composable(
                                route = "menu_detail/{menuId}",
                                arguments = listOf(
                                    navArgument("menuId") {
                                        type = NavType.StringType
                                    }
                                )
                            ) { backStackEntry ->
                                val menuId = backStackEntry.arguments?.getString("menuId") ?: ""
                                MenuDetailScreen(
                                    navController = navController,
                                    menuId = menuId
                                )
                            }

                            composable("cart") {
                                CartScreen(navController)
                            }

                            composable("orders") {
                                OrdersScreen(navController)
                            }

                            composable("profile") {
                                ProfileScreen(navController)
                            }

                            // 🏪 SELLER SCREENS
                            composable("seller_home") {
                                SellerHomeScreen(navController)
                            }

                            composable("seller_orders") {
                                SellerOrdersScreen(navController)
                            }

                            composable("seller_order_history") {
                                SellerOrderHistoryScreen(navController)
                            }

                            composable("seller_profile") {
                                SellerProfileScreen(navController)
                            }
                            composable("seller_menu") {
                                SellerMenuScreen(navController)
                            }

                            composable("seller_menu_requests") {
                                SellerMenuRequestStatusScreen(navController)
                            }

                            composable("add_menu") {
                                AddMenuScreen(navController)
                            }

                            composable(
                                route = "edit_menu/{id}",
                                arguments = listOf(
                                    navArgument("id") {
                                        type = NavType.StringType
                                    }
                                )
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id") ?: ""
                                EditMenuScreen(navController, id)
                            }

                            // 👑 ADMIN SCREENS
                            composable("admin_home") {
                                AdminHomeScreen(navController)
                            }

                            composable("admin_menu") {
                                AdminMenuScreen(navController)
                            }

                            composable("admin_add_menu") {
                                AdminAddMenuScreen(navController)
                            }

                            composable("admin_seller_requests") {
                                AdminSellerRequestsScreen(navController)
                            }

                            composable("admin_menu_requests") {
                                AdminMenuRequestScreen(navController = navController)
                            }

                            // 📝 SELLER REQUEST
                            composable("seller_request") {
                                SellerRequestScreen(navController)
                            }
                        }
                    }
                }
            }
        }
    }
}