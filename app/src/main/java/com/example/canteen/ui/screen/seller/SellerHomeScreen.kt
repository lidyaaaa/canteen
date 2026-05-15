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
import androidx.compose.material.icons.outlined.Notifications
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
import java.text.NumberFormat
import java.util.*

@Composable
fun SellerHomeScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    val sellerName = sharedPref.getString("name", "Seller") ?: "Seller"
    val sellerEmail = sharedPref.getString("email", "") ?: ""

    var pendingOrderCount by remember { mutableStateOf(0) }
    var pendingMenuRequestCount by remember { mutableStateOf(0) }
    var canteenName by remember { mutableStateOf("") }
    var todayRevenue by remember { mutableStateOf(0) }
    var todayOrderCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            repository.getCurrentUser().onSuccess { user ->
                repository.getCanteenByOwnerId(user.id).onSuccess { canteen ->
                    if (canteen != null) {
                        canteenName = canteen.name

                        repository.getOrdersForSeller(canteen.id).onSuccess { orders ->
                            pendingOrderCount = orders.count { it.status == "pending" }
                        }

                        repository.getCompletedOrdersForSeller(canteen.id).onSuccess { completed ->
                            val since24h = System.currentTimeMillis() - 86_400_000L
                            val todayOrders = completed.filter { it.createdAt >= since24h }
                            todayOrderCount = todayOrders.size
                            todayRevenue = todayOrders.sumOf { it.totalPrice }
                        }
                    }
                }

                repository.getMenuRequestsBySeller(user.id).onSuccess { requests ->
                    pendingMenuRequestCount = requests.count { it.status == "pending" }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            SellerBottomNavBar(
                navController = navController,
                currentRoute = "seller_home",
                pendingOrderCount = pendingOrderCount
            )
        },
        containerColor = GrayBg
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Top Bar ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(YellowPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sellerName.firstOrNull()?.uppercase() ?: "S",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Black
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Halo, $sellerName 👋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Black
                        )
                        Text(
                            text = if (canteenName.isNotEmpty()) "🏪 $canteenName" else sellerEmail,
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                }

                // 🔔 Notif bell — badge kalau ada menu request pending
                BadgedBox(
                    badge = {
                        if (pendingMenuRequestCount > 0) {
                            Badge(containerColor = RedError) {
                                Text(
                                    text = pendingMenuRequestCount.toString(),
                                    fontSize = 9.sp,
                                    color = White
                                )
                            }
                        }
                    }
                ) {
                    IconButton(onClick = { navController.navigate("seller_menu_requests") }) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Status Request",
                            tint = Black
                        )
                    }
                }
            }

            // ── Stat Cards ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Receipt,
                    value = "$todayOrderCount",
                    label = "Pesanan Hari Ini"
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    value = "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(todayRevenue)}",
                    label = "Pendapatan"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Section: Menu ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "MENU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                )

                // Kelola Menu — prominent button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = YellowPrimary),
                    elevation = CardDefaults.cardElevation(0.dp),
                    onClick = { navController.navigate("seller_menu") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MenuBook, null,
                                tint = Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Kelola Menu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Black
                                )
                                Text(
                                    "Tambah, edit, atau hapus menu",
                                    fontSize = 12.sp,
                                    color = Black.copy(alpha = 0.55f)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight, null,
                            tint = Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status Request — secondary row
                SellerMenuRow(
                    icon = Icons.Default.PendingActions,
                    label = "Status Request Menu",
                    trailingBadge = pendingMenuRequestCount,
                    onClick = { navController.navigate("seller_menu_requests") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Section: Lainnya ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "LAINNYA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                SellerMenuRow(
                    icon = Icons.Default.Receipt,
                    label = "Pesanan Masuk",
                    trailingBadge = pendingOrderCount,
                    onClick = { navController.navigate("seller_orders") }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 36.dp),
                    color = GrayBg,
                    thickness = 1.dp
                )

                SellerMenuRow(
                    icon = Icons.Default.History,
                    label = "Riwayat Pesanan",
                    onClick = { navController.navigate("seller_order_history") }
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Sub-components ──────────────────────────────────────────

@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrayBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = YellowPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Black)
                Text(label, fontSize = 10.sp, color = GrayText)
            }
        }
    }
}

@Composable
fun SellerMenuRow(
    icon: ImageVector,
    label: String,
    trailingBadge: Int = 0,
    onClick: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingBadge > 0) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = RedError)
                    ) {
                        Text(
                            text = "$trailingBadge baru",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    Icons.Default.ChevronRight, null,
                    tint = GrayText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Preview ─────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Seller Home Screen")
@Composable
fun SellerHomeScreenPreview() {
    SellerHomeScreen(navController = rememberNavController())
}