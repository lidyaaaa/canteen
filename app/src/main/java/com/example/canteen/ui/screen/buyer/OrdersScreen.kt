package com.example.canteen.ui.screen.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.canteen.ui.component.BottomNavBar
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var cartCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch {
            val userResult = repository.getCurrentUser()
            userResult.onSuccess { user ->
                // Ambil pesanan user
                repository.getOrdersByUser(user.id).onSuccess {
                    orders = it
                }
                // Ambil cart count untuk badge
                repository.getCartCount(user.id).onSuccess {
                    cartCount = it
                }
            }
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentRoute = "orders",
                cartCount = cartCount
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBg)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Pesanan Saya",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YellowPrimary)
                }
            } else if (orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pesanan 📭", color = GrayText)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(orders) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(order.canteenName, fontWeight = FontWeight.Bold)
                Text(
                    text = when (order.status) {
                        "pending" -> "Pending"
                        "diproses" -> "Diproses"
                        "selesai" -> "Selesai"
                        else -> order.status
                    },
                    color = when (order.status) {
                        "pending" -> YellowPrimary
                        "diproses" -> Color(0xFFFF9800)
                        "selesai" -> GreenSuccess
                        else -> GrayText
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Text("${item.name} x${item.quantity}", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Total: Rp ${order.totalPrice}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}