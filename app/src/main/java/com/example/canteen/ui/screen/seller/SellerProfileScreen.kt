package com.example.canteen.ui.screen.seller

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.component.SellerBottomNavBar
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val sellerName = sharedPref.getString("name", "Seller") ?: "Seller"
    val sellerEmail = sharedPref.getString("email", "") ?: ""

    var canteenName by remember { mutableStateOf("") }
    var canteenId by remember { mutableStateOf("") }
    var totalMenu by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            repository.getCurrentUser().onSuccess { user ->
                repository.getCanteenByOwnerId(user.id).onSuccess { canteen ->
                    if (canteen != null) {
                        canteenName = canteen.name
                        canteenId = canteen.id
                        repository.getMenuByCanteen(canteen.id).onSuccess {
                            totalMenu = it.size
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = RedError) },
            title = { Text("Logout") },
            text = { Text("Yakin ingin keluar dari akun seller?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        sharedPref.edit().clear().apply()
                        repository.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            SellerBottomNavBar(
                navController = navController,
                currentRoute = "seller_profile"
            )
        },
        containerColor = GrayBg
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(YellowPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sellerName.firstOrNull()?.uppercase() ?: "S",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(sellerName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sellerEmail, fontSize = 13.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = YellowPrimary)
                    ) {
                        Text(
                            "🏪 Seller",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Stat kantin ───────────────────────────────────────
            if (canteenName.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Store,
                        label = "Kantin",
                        value = canteenName
                    )
                    ProfileStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MenuBook,
                        label = "Total Menu",
                        value = "$totalMenu item"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Info Akun ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "AKUN",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = GrayText, letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                ProfileInfoRow(icon = Icons.Default.Person, label = "Nama", value = sellerName)
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp), color = GrayBg)
                ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = sellerEmail)
                if (canteenName.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(start = 36.dp), color = GrayBg)
                    ProfileInfoRow(icon = Icons.Default.Store, label = "Kantin", value = canteenName)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Navigasi cepat ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "MENU CEPAT",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = GrayText, letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                ProfileNavRow(
                    icon = Icons.Default.MenuBook,
                    label = "Kelola Menu",
                    onClick = { navController.navigate("seller_menu") }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp), color = GrayBg)
                ProfileNavRow(
                    icon = Icons.Default.PendingActions,
                    label = "Status Request Menu",
                    onClick = { navController.navigate("seller_menu_requests") }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp), color = GrayBg)
                ProfileNavRow(
                    icon = Icons.Default.Receipt,
                    label = "Pesanan Masuk",
                    onClick = { navController.navigate("seller_orders") }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp), color = GrayBg)
                ProfileNavRow(
                    icon = Icons.Default.History,
                    label = "Riwayat Pesanan",
                    onClick = { navController.navigate("seller_order_history") }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Logout ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Logout, null,
                            tint = RedError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Logout",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = RedError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Sub-components ──────────────────────────────────────────

@Composable
fun ProfileStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrayBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = YellowPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Black)
            Text(label, fontSize = 11.sp, color = GrayText)
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = GrayText, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = GrayText)
            Text(value, fontSize = 14.sp, color = Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileNavRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = GrayText, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontSize = 14.sp, color = Black, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, null, tint = GrayText, modifier = Modifier.size(18.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Seller Profile Screen")
@Composable
fun SellerProfileScreenPreview() {
    SellerProfileScreen(navController = rememberNavController())
}