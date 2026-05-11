package com.example.canteen.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSellerRequestsScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showApproveDialog by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showRejectDialog by remember { mutableStateOf<Map<String, Any>?>(null) }

    fun loadRequests() {
        scope.launch {
            isLoading = true
            try {
                val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("seller_requests")
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

                val pendingRequests = snapshot.documents.map { doc ->
                    val data = doc.data ?: mapOf()
                    data + ("id" to doc.id)
                }

                requests = pendingRequests
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadRequests()
    }

    // Dialog Approve
    showApproveDialog?.let { request ->
        AlertDialog(
            onDismissRequest = { showApproveDialog = null },
            icon = {
                Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(48.dp))
            },
            title = { Text("Setujui Request?") },
            text = {
                Text("User akan menjadi seller dan kantin \"${request["canteenName"]}\" akan dibuat.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val requestId = request["id"].toString()
                                val userId = request["userId"].toString()
                                val canteenName = request["canteenName"].toString()
                                val userName = request["userName"].toString()

                                repository.updateSellerRequestStatus(requestId, "approved")
                                repository.updateUserRole(userId, "seller")
                                repository.createCanteen(canteenName, userId, userName)

                                Toast.makeText(context, "Request disetujui! 🎉", Toast.LENGTH_SHORT).show()
                                showApproveDialog = null
                                loadRequests()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                ) {
                    Text("Setujui")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Reject
    showRejectDialog?.let { request ->
        AlertDialog(
            onDismissRequest = { showRejectDialog = null },
            icon = {
                Icon(Icons.Default.Cancel, null, tint = RedError, modifier = Modifier.size(48.dp))
            },
            title = { Text("Tolak Request?") },
            text = {
                Text("Request dari \"${request["userName"]}\" akan ditolak.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val requestId = request["id"].toString()
                                repository.updateSellerRequestStatus(requestId, "rejected")
                                Toast.makeText(context, "Request ditolak", Toast.LENGTH_SHORT).show()
                                showRejectDialog = null
                                loadRequests()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Tolak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
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
            } else if (requests.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = GrayText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Tidak ada request masuk 😴",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Seller request akan muncul di sini",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info jumlah request
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = YellowPrimary.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Pending, null, tint = YellowPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${requests.size} request pending",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    items(requests, key = { it["id"].toString() }) { request ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // Header: avatar + nama
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(YellowPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (request["userName"].toString().firstOrNull()?.uppercase() ?: "?"),
                                            fontWeight = FontWeight.Bold,
                                            color = Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = request["userName"].toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Ingin jadi seller",
                                            fontSize = 12.sp,
                                            color = GrayText
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Info kantin
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Store,
                                        null,
                                        modifier = Modifier.size(18.dp),
                                        tint = YellowPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Kantin: ${request["canteenName"]}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = YellowPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Deskripsi
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = GrayBg
                                    )
                                ) {
                                    Text(
                                        text = request["description"].toString(),
                                        fontSize = 13.sp,
                                        color = GrayText,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Tombol aksi
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { showRejectDialog = request },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = RedError
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(RedError)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tolak")
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Button(
                                        onClick = { showApproveDialog = request },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GreenSuccess
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Setujui")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}