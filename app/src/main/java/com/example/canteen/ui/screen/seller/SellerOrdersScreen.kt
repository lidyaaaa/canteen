package com.example.canteen.ui.screen.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.CartItem
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.Order
import com.example.canteen.ui.component.SellerBottomNavBar
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(navController: NavController) {

    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var canteenId by remember { mutableStateOf("") }
    var processingId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackMsg by remember { mutableStateOf<String?>(null) }

    // Filter tab: semua / pending / diproses
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Semua", "Pending", "Diproses")

    fun loadOrders() {
        scope.launch {
            if (canteenId.isNotEmpty()) {
                repository.getOrdersForSeller(canteenId).onSuccess { result ->
                    // Tampilkan hanya yang belum selesai
                    orders = result.filter { it.status != "done" && it.status != "completed" && it.status != "selesai" }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getCurrentUser().onSuccess { user ->
            repository.getCanteenByOwnerId(user.id).onSuccess { canteen ->
                if (canteen != null) {
                    canteenId = canteen.id
                    repository.getOrdersForSeller(canteen.id).onSuccess { result ->
                        orders = result.filter { it.status != "done" && it.status != "completed" && it.status != "selesai" }
                    }
                }
            }
        }
        isLoading = false
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackbarHostState.showSnackbar(it); snackMsg = null }
    }

    val filteredOrders = when (selectedTab) {
        1 -> orders.filter { it.status == "pending" }
        2 -> orders.filter { it.status == "processing" || it.status == "diproses" }
        else -> orders
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesanan Masuk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { loadOrders() }) {
                        Icon(Icons.Default.Refresh, null, tint = Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                )
            )
        },
        bottomBar = {
            SellerBottomNavBar(
                navController = navController,
                currentRoute = "seller_orders",
                pendingOrderCount = orders.count { it.status == "pending" }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GrayBg
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab filter
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = White,
                contentColor = Black,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = YellowPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                label,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = YellowPrimary) }
                }

                filteredOrders.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt, null,
                                modifier = Modifier.size(64.dp), tint = GrayText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tidak ada pesanan", color = GrayText, fontSize = 16.sp)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
                            SellerOrderCard(
                                order = order,
                                isProcessing = processingId == order.id,
                                onProcess = {
                                    scope.launch {
                                        processingId = order.id
                                        repository.updateOrderStatus(order.id, "processing")
                                            .onSuccess {
                                                snackMsg = "✅ Pesanan sedang diproses"
                                                loadOrders()
                                            }
                                            .onFailure { snackMsg = "❌ Gagal: ${it.message}" }
                                        processingId = null
                                    }
                                },
                                onDone = {
                                    scope.launch {
                                        processingId = order.id
                                        repository.updateOrderStatus(order.id, "done")
                                            .onSuccess {
                                                snackMsg = "🎉 Pesanan selesai!"
                                                loadOrders()
                                            }
                                            .onFailure { snackMsg = "❌ Gagal: ${it.message}" }
                                        processingId = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    order: Order,
    isProcessing: Boolean,
    onProcess: () -> Unit,
    onDone: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("id")) }

    val statusConfig = when (order.status) {
        "pending" -> Triple("⏳ Menunggu", Color(0xFFFFA726), Color(0xFFFFF3E0))
        "processing", "diproses" -> Triple("🔄 Diproses", Color(0xFF2196F3), Color(0xFFE3F2FD))
        else -> Triple(order.status, GrayText, GrayBg)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person, null,
                        modifier = Modifier.size(16.dp), tint = GrayText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(order.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Black)
                }
                // Status badge
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = statusConfig.third)
                ) {
                    Text(
                        text = statusConfig.first,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusConfig.second
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                dateFormat.format(Date(order.createdAt)),
                fontSize = 11.sp, color = GrayText
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GrayBg)
            Spacer(modifier = Modifier.height(10.dp))

            // Items
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.quantity}x ${item.name}",
                        fontSize = 13.sp, color = Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(item.price * item.quantity)}",
                        fontSize = 13.sp, color = GrayText
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = GrayBg)
            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Black)
                Text(
                    "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(order.totalPrice)}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = YellowPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = YellowPrimary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                when (order.status) {
                    "pending" -> {
                        Button(
                            onClick = onProcess,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YellowPrimary,
                                contentColor = Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Proses Pesanan", fontWeight = FontWeight.Bold)
                        }
                    }
                    "processing", "diproses" -> {
                        Button(
                            onClick = onDone,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tandai Selesai", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Seller Orders Screen")
@Composable
fun SellerOrdersScreenPreview() {
    SellerOrdersScreen(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Order Card - Pending")
@Composable
fun SellerOrderCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SellerOrderCard(
            order = Order(
                id = "1",
                userId = "u1",
                userName = "Andi Pratama",
                items = listOf(
                    CartItem(menuId = "m1", name = "Nasi Goreng", price = 15000, quantity = 2),
                    CartItem(menuId = "m2", name = "Es Teh", price = 5000, quantity = 1)
                ),
                totalPrice = 35000,
                status = "pending",
                canteenId = "c1",
                canteenName = "Kantin A",
                createdAt = System.currentTimeMillis()
            ),
            isProcessing = false,
            onProcess = {},
            onDone = {}
        )
    }
}