    package com.example.canteen

    import android.content.Context
    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Scaffold
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalContext
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import com.example.canteen.ui.theme.CanteenTheme

    // SCREEN
    import com.example.canteen.ui.screen.LoginScreen
    import com.example.canteen.ui.screen.RegisterScreen
    import com.example.canteen.ui.screen.HomeScreen
    import com.example.canteen.ui.screen.AddMenuScreen       // 🔥 TAMBAH
    import com.example.canteen.ui.screen.EditMenuScreen     // 🔥 TAMBAH

    class MainActivity : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            setContent {
                CanteenTheme {

                    val context = LocalContext.current
                    val navController = rememberNavController()

                    // 🔥 CEK LOGIN
                    val sharedPref = context.getSharedPreferences(
                        "user_session",
                        Context.MODE_PRIVATE
                    )

                    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

                    val startDestination = if (isLoggedIn) "home" else "login"

                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {

                            // 🔐 LOGIN
                            composable("login") {
                                LoginScreen(navController)
                            }

                            // 📝 REGISTER
                            composable("register") {
                                RegisterScreen(navController)
                            }

                            // 🏠 HOME
                            composable("home") {
                                HomeScreen(navController)
                            }

                            // ➕ ADD MENU (🔥 INI YANG KURANG TADI)
                            composable("add_menu") {
                                AddMenuScreen(navController)
                            }

                            // ✏️ EDIT MENU (🔥 INI JUGA)
                            composable("edit_menu/{id}") { backStackEntry ->
                                val id = backStackEntry.arguments
                                    ?.getString("id")
                                    ?.toIntOrNull() ?: 0

                                EditMenuScreen(navController, id)
                            }
                        }
                    }
                }
            }
        }
    }