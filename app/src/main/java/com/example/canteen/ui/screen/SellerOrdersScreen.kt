package com.example.canteen.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.Order
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadOrders() {
        scope.launch {
            isLoading = true
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                val canteenResult = repository.getCanteenByOwnerId(user.id)
                canteenResult.onSuccess { canteen ->
                    if (canteen != null) {
                        repository.getOrdersForSeller(canteen.id).onSuccess {
                            orders = it
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesanan Masuk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary,
                    titleContentColor = Black
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBg)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = YellowPrimary
                )
            } else if (orders.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📭 Belum ada pesanan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pesanan dari buyer akan muncul di sini",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        SellerOrderCard(
                            order = order,
                            onUpdateStatus = { newStatus ->
                                scope.launch {
                                    repository.updateOrderStatus(order.id, newStatus).onSuccess {
                                        Toast.makeText(
                                            context,
                                            "Status diubah jadi: $newStatus ✅",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadOrders() // Refresh
                                    }.onFailure {
                                        Toast.makeText(
                                            context,
                                            "Gagal update status",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    order: Order,
    onUpdateStatus: (String) -> Unit
) {
    val statusColor = when (order.status) {
        "pending" -> Color(0xFFFF9800)    // Orange
        "diproses" -> Color(0xFF2196F3)   // Blue
        "selesai" -> GreenSuccess         // Green
        else -> GrayText
    }

    val statusLabel = when (order.status) {
        "pending" -> "⏳ Pending"
        "diproses" -> "🔄 Diproses"
        "selesai" -> "✅ Selesai"
        else -> order.status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: nama pemesan + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${order.userName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List item
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.name}",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "x${item.quantity}",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Rp ${item.price * item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = GrayInput
            )

            // Total + tombol aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: Rp ${order.totalPrice}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = YellowPrimary
                )

                when (order.status) {
                    "pending" -> {
                        Button(
                            onClick = { onUpdateStatus("diproses") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Proses", fontWeight = FontWeight.Bold)
                        }
                    }
                    "diproses" -> {
                        Button(
                            onClick = { onUpdateStatus("selesai") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSuccess
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Selesai", fontWeight = FontWeight.Bold)
                        }
                    }
                    "selesai" -> {
                        // Tidak ada tombol
                    }
                }
            }
        }
    }
}