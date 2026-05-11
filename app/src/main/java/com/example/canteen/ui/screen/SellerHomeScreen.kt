package com.example.canteen.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SellerHomeScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val sellerName = sharedPref.getString("name", "Seller") ?: "Seller"
    val sellerEmail = sharedPref.getString("email", "") ?: ""

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Dialog logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.Default.Logout, null, tint = RedError)
            },
            title = { Text("Logout") },
            text = { Text("Yakin ingin keluar dari akun seller?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        sharedPref.edit().clear().apply()
                        repository.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 🏪 Seller Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(YellowPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sellerName.firstOrNull()?.uppercase() ?: "S",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = sellerName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )

            Text(
                text = sellerEmail,
                fontSize = 14.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Seller badge
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = YellowPrimary)
            ) {
                Text(
                    text = "🏪 Seller",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 📋 Menu Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lihat Pesanan
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    onClick = { navController.navigate("seller_orders") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = YellowPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Pesanan Masuk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Lihat & proses pesanan",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                    }
                }

                // Riwayat Pesanan (placeholder)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    onClick = {
                        // Bisa ditambah fitur nanti
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = YellowPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Riwayat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pesanan selesai",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(RedError)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}