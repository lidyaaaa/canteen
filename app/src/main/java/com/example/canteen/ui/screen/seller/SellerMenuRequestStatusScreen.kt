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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.MenuRequest
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerMenuRequestStatusScreen(navController: NavController) {

    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<MenuRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            repository.getCurrentUser().onSuccess { user ->
                repository.getMenuRequestsBySeller(user.id).onSuccess {
                    requests = it
                }
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Request Menu", fontWeight = FontWeight.Bold) },
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

            requests.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PendingActions,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Belum ada request yang diajukan",
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
                    items(requests, key = { it.id }) { request ->
                        SellerMenuRequestCard(request = request)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerMenuRequestCard(request: MenuRequest) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }
    val formattedDate = dateFormat.format(Date(request.createdAt))

    val typeLabel = when (request.type) {
        "add" -> "➕ Tambah Menu"
        "edit" -> "✏️ Edit Menu"
        "delete" -> "🗑️ Hapus Menu"
        else -> request.type
    }

    val statusConfig = when (request.status) {
        "approved" -> Triple("✅ Disetujui", Color(0xFF4CAF50), Color(0xFF4CAF50).copy(alpha = 0.1f))
        "rejected" -> Triple("🚫 Ditolak", RedError, RedError.copy(alpha = 0.1f))
        else -> Triple("⏳ Menunggu", Color(0xFFFFA726), Color(0xFFFFA726).copy(alpha = 0.1f))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: type + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = GrayText
                )

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

            Spacer(modifier = Modifier.height(12.dp))

            // Detail
            Text(
                text = request.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black
            )

            if (request.type != "delete") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(request.price)}",
                        fontSize = 14.sp,
                        color = YellowPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(request.category, fontSize = 12.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(formattedDate, fontSize = 11.sp, color = GrayText)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Seller Menu Request Status Screen")
@Composable
fun SellerMenuRequestStatusScreenPreview() {
    SellerMenuRequestStatusScreen(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Request Card - Pending")
@Composable
fun SellerMenuRequestCardPendingPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SellerMenuRequestCard(
            request = MenuRequest(
                id = "1",
                name = "Nasi Goreng Spesial",
                price = 15000,
                category = "Makanan",
                type = "add",
                status = "pending",
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

@Preview(showBackground = true, name = "Request Card - Approved")
@Composable
fun SellerMenuRequestCardApprovedPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        SellerMenuRequestCard(
            request = MenuRequest(
                id = "2",
                name = "Es Teh Manis",
                price = 5000,
                category = "Minuman",
                type = "add",
                status = "approved",
                createdAt = System.currentTimeMillis()
            )
        )
    }
}