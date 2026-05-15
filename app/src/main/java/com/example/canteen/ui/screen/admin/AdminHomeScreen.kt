package com.example.canteen.ui.screen.admin

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
fun AdminHomeScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val adminName = sharedPref.getString("name", "Admin") ?: "Admin"
    val adminEmail = sharedPref.getString("email", "") ?: ""

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Hitung jumlah request pending biar admin tau ada berapa
    var pendingSellerCount by remember { mutableStateOf(0) }
    var pendingMenuCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        repository.getPendingSellerRequests().onSuccess { list ->
            pendingSellerCount = list.size
        }
        repository.getPendingMenuRequests().onSuccess { list ->
            pendingMenuCount = list.size
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = RedError) },
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
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 👑 Avatar
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

            Text(adminName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Black)
            Text(adminEmail, fontSize = 14.sp, color = GrayText)

            Spacer(modifier = Modifier.height(8.dp))

            // Badge
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

            // Section label
            Text(
                text = "Kelola Persetujuan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Row 1: Seller Request + Menu Request
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PersonAdd,
                    title = "Seller Request",
                    subtitle = "Approve / Reject",
                    badgeCount = pendingSellerCount,
                    onClick = { navController.navigate("admin_seller_requests") }
                )

                AdminActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Restaurant,
                    title = "Menu Request",
                    subtitle = "Review menu seller",
                    badgeCount = pendingMenuCount,
                    onClick = { navController.navigate("admin_menu_requests") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // Logout
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(RedError)
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

/**
 * Reusable card aksi untuk Admin (dengan badge counter notif pending)
 */
@Composable
fun AdminActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon, null,
                    modifier = Modifier.size(40.dp),
                    tint = YellowPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 11.sp, color = GrayText)
            }

            // Badge notif merah kalau ada pending
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(RedError),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        color = White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Admin Home Screen")
@Composable
fun AdminHomeScreenPreview() {
    // Preview pakai NavController dummy
    AdminHomeScreen(navController = rememberNavController())
}