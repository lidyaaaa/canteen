package com.example.canteen.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.component.BottomNavBar
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val userEmail = sharedPref.getString("email", "") ?: ""
    val userName = sharedPref.getString("name", "User") ?: "User"
    val userRole = sharedPref.getString("role", "buyer") ?: "buyer"

    var cartCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                repository.getCartCount(userId).onSuccess { cartCount = it }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "profile",
                cartCount = cartCount
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBg)
                .padding(paddingValues)
        ) {
            // Profile header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YellowPrimary)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.toString()?.uppercase() ?: "?",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = YellowPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(userEmail, fontSize = 14.sp, color = White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                sharedPref.edit().clear().apply()
                                repository.logout()
                                navController.navigate("login") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(White)
                            )
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }

            // Seller request button (hanya untuk buyer)
            if (userRole == "buyer") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Ingin jadi seller? 🏪",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Buka kantin kamu sendiri dan jual makanan di aplikasi ini",
                                fontSize = 14.sp,
                                color = GrayText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    navController.navigate("seller_request")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YellowPrimary,
                                    contentColor = Black
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ajukan Jadi Seller", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Activity section placeholder
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "DATA BELANJA BULAN JANUARI",
                            fontSize = 12.sp,
                            color = GrayText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val data = listOf(60, 80, 40, 90, 70, 50, 85)
                            val days = listOf("S", "M", "T", "W", "T", "F", "S")
                            data.forEachIndexed { index, value ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height((value * 1.2f).dp)
                                            .background(YellowPrimary, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(days[index], fontSize = 10.sp, color = GrayText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}