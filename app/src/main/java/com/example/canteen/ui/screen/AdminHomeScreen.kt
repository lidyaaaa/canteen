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
fun AdminHomeScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val adminName = sharedPref.getString("name", "Admin") ?: "Admin"
    val adminEmail = sharedPref.getString("email", "") ?: ""

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Dialog logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.Default.Logout, null, tint = RedError)
            },
            title = { Text("Logout") },
            text = { Text("Yakin ingin keluar dari akun admin?") },
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

            // 👑 Admin Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(YellowPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = adminName.firstOrNull()?.uppercase() ?: "A",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = adminName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )

            Text(
                text = adminEmail,
                fontSize = 14.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Admin badge
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = YellowPrimary)
            ) {
                Text(
                    text = "👑 Admin",
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
                // Kelola Menu
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    onClick = { navController.navigate("admin_menu") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = YellowPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Kelola Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tambah, edit, hapus",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                    }
                }

                // Seller Requests
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    onClick = { navController.navigate("admin_seller_requests") }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            null,
                            modifier = Modifier.size(40.dp),
                            tint = YellowPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Seller Request",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Approve / Reject",
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