package com.example.canteen.ui.screen.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.CartItem
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.Order
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrderHistoryScreen(navController: NavController) {

    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Ringkasan statistik
    val totalRevenue by remember(orders) {
        derivedStateOf { orders.sumOf { it.totalPrice } }
    }
    val totalOrders by remember(orders) {
        derivedStateOf { orders.size }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                repository.getCanteenByOwnerId(user.id).onSuccess { canteen ->
                    if (canteen != null) {
                        repository.getCompletedOrdersForSeller(canteen.id).onSuccess { result ->
                            orders = result
                        }.onFailure {
                            errorMsg = it.message
                        }
                    } else {
                        errorMsg = "Kamu belum punya kantin."
                    }
                }
            }.onFailure {
                errorMsg = "Gagal load data user."
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                )
            )
        },
        containerColor = GrayBg
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YellowPrimary)
                }
            }

            errorMsg != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = RedError
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMsg ?: "Terjadi kesalahan", color = GrayText)
                    }
                }
            }

            orders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Belum ada pesanan selesai",
                            color = GrayText,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ringkasan di atas
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total pesanan
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Receipt,
                                label = "Total Pesanan",
                                value = "$totalOrders pesanan"
                            )
                            // Total pendapatan
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.AttachMoney,
                                label = "Total Pendapatan",
                                value = "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(totalRevenue)}"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // List order
                    items(orders, key = { it.id }) { order ->
                        OrderHistoryCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = YellowPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Black)
            Text(label, fontSize = 11.sp, color = GrayText)
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }
    val formattedDate = dateFormat.format(Date(order.createdAt))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: nama user + tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = GrayText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Black
                    )
                }
                // Badge selesai
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "✅ Selesai",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(formattedDate, fontSize = 11.sp, color = GrayText)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GrayInput)
            Spacer(modifier = Modifier.height(12.dp))

            // Item-item pesanan
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.name}",
                        fontSize = 13.sp,
                        color = Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(item.price * item.quantity)}",
                        fontSize = 13.sp,
                        color = GrayText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GrayInput)
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Black)
                Text(
                    text = "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(order.totalPrice)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = YellowPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Seller Order History Screen")
@Composable
fun SellerOrderHistoryScreenPreview() {
    SellerOrderHistoryScreen(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Order History Card")
@Composable
fun OrderHistoryCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        OrderHistoryCard(
            order = Order(
                id = "1",
                userId = "u1",
                userName = "Budi Santoso",
                items = listOf(
                    CartItem(menuId = "m1", name = "Nasi Goreng", price = 15000, quantity = 2),
                    CartItem(menuId = "m2", name = "Es Teh Manis", price = 5000, quantity = 1)
                ),
                totalPrice = 35000,
                status = "done",
                canteenId = "c1",
                canteenName = "Kantin A",
                createdAt = System.currentTimeMillis()
            )
        )
    }
}