package com.example.canteen.ui.screen.admin

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
import androidx.compose.ui.graphics.SolidColor
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuRequestScreen(navController: NavController) {

    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<MenuRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var processingId by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    fun loadRequests() {
        scope.launch {
            isLoading = true
            repository.getPendingMenuRequests().onSuccess {
                requests = it
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadRequests() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Request", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            Icons.Default.CheckCircle,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Tidak ada request pending",
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
                        MenuRequestCard(
                            request = request,
                            isProcessing = processingId == request.id,
                            onApprove = {
                                scope.launch {
                                    processingId = request.id
                                    repository.resolveMenuRequest(request, approved = true)
                                        .onSuccess {
                                            snackbarMessage = "✅ Request disetujui"
                                            loadRequests()
                                        }
                                        .onFailure {
                                            snackbarMessage = "❌ Gagal: ${it.message}"
                                        }
                                    processingId = null
                                }
                            },
                            onReject = {
                                scope.launch {
                                    processingId = request.id
                                    repository.resolveMenuRequest(request, approved = false)
                                        .onSuccess {
                                            snackbarMessage = "🚫 Request ditolak"
                                            loadRequests()
                                        }
                                        .onFailure {
                                            snackbarMessage = "❌ Gagal: ${it.message}"
                                        }
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

@Composable
fun MenuRequestCard(
    request: MenuRequest,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val typeLabel = when (request.type) {
        "add" -> "➕ Tambah Menu"
        "edit" -> "✏️ Edit Menu"
        "delete" -> "🗑️ Hapus Menu"
        else -> request.type
    }
    val typeColor = when (request.type) {
        "add" -> Color(0xFF4CAF50)
        "edit" -> Color(0xFF2196F3)
        "delete" -> RedError
        else -> GrayText
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Type badge + canteen name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = typeColor.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
                Text(
                    text = request.canteenName,
                    fontSize = 11.sp,
                    color = GrayText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detail menu
            Text(
                text = request.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Black
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (request.type != "delete") {
                    Text(
                        text = "Rp ${NumberFormat.getNumberInstance(Locale("id")).format(request.price)}",
                        fontSize = 14.sp,
                        color = YellowPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = request.category,
                        fontSize = 12.sp,
                        color = GrayText
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dari: ${request.sellerName}",
                fontSize = 12.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GrayInput)
            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Approve / Reject
            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = YellowPrimary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reject
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(RedError)
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tolak")
                    }

                    // Approve
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowPrimary,
                            contentColor = Black
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setujui", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Admin Menu Request Screen")
@Composable
fun AdminMenuRequestScreenPreview() {
    AdminMenuRequestScreen(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Menu Request Card - Add")
@Composable
fun MenuRequestCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        MenuRequestCard(
            request = MenuRequest(
                id = "1",
                sellerName = "Pak Budi",
                canteenName = "Kantin A",
                name = "Nasi Goreng Spesial",
                price = 15000,
                category = "Makanan",
                type = "add",
                status = "pending"
            ),
            isProcessing = false,
            onApprove = {},
            onReject = {}
        )
    }
}