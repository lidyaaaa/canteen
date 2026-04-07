package com.example.canteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.canteen.ui.theme.CanteenTheme

// SCREEN
import com.example.canteen.ui.screen.LoginScreen
import com.example.canteen.ui.screen.RegisterScreen
import com.example.canteen.ui.screen.HomeScreen // 🔥 tambahin ini

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CanteenTheme {

                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding) // 🔥 FIX padding
                    ) {

                        // LOGIN
                        composable("login") {
                            LoginScreen(navController)
                        }

                        // REGISTER
                        composable("register") {
                            RegisterScreen(navController)
                        }

                        // 🔥 HOME (PENTING BANGET)
                        composable("home") {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}