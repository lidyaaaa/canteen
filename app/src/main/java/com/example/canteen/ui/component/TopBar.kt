package com.example.canteen.ui.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.*

@Composable
fun TopBar(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val userName = sharedPref.getString("name", "Guest") ?: "Guest"
    val userRole = sharedPref.getString("role", "buyer") ?: "buyer"

    val initial = userName.firstOrNull()?.uppercase() ?: "?"

    var expanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Logout Confirmation Dialog ────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(Icons.Default.Logout, null, tint = RedError)
            },
            title = { Text("Logout") },
            text = { Text("Yakin ingin keluar dari akun?") },
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

    // ── Top Bar Layout ────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 👤 Avatar + Nama + Role badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(YellowPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Black
                )
                // Role label kecil di bawah nama
                val roleLabel = when (userRole) {
                    "admin"  -> "👑 Admin"
                    "seller" -> "🏪 Seller"
                    else     -> "🛒 Buyer"
                }
                Text(
                    text = roleLabel,
                    fontSize = 11.sp,
                    color = GrayText
                )
            }
        }

        // ⋮ Dropdown Menu
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Black
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = White
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Logout,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = RedError
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Logout",
                                color = RedError,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        showLogoutDialog = true
                    }
                )
            }
        }
    }
}

// ── Preview ────────────────────────────────────────────────

@Preview(showBackground = true, name = "TopBar - Buyer")
@Composable
fun TopBarBuyerPreview() {
    // Preview pakai NavController dummy, simulate user buyer
    TopBar(navController = rememberNavController())
}