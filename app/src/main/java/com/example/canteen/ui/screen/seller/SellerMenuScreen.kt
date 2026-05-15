package com.example.canteen.ui.screen.seller

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerMenuScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var canteenId by remember { mutableStateOf("") }
    var canteenName by remember { mutableStateOf("") }
    var sellerId by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }

    // Bottom sheet state
    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedMenu by remember { mutableStateOf<MenuItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackMsg by remember { mutableStateOf<String?>(null) }

    fun loadMenu() {
        scope.launch {
            isLoading = true
            if (canteenId.isNotEmpty()) {
                repository.getMenuByCanteen(canteenId).onSuccess { menuList = it }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        repository.getCurrentUser().onSuccess { user ->
            sellerId = user.id
            sellerName = user.name
            repository.getCanteenByOwnerId(user.id).onSuccess { canteen ->
                if (canteen != null) {
                    canteenId = canteen.id
                    canteenName = canteen.name
                    repository.getMenuByCanteen(canteen.id).onSuccess { menuList = it }
                }
            }
        }
        isLoading = false
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            snackMsg = null
        }
    }

    // ── Add Sheet ────────────────────────────────────────────
    if (showAddSheet) {
        MenuFormSheet(
            title = "Tambah Menu",
            onDismiss = { showAddSheet = false },
            onSubmit = { name, price, imageUrl, category ->
                scope.launch {
                    repository.submitMenuRequest(
                        sellerId = sellerId,
                        sellerName = sellerName,
                        canteenId = canteenId,
                        canteenName = canteenName,
                        name = name,
                        price = price,
                        imageUrl = imageUrl,
                        category = category
                    ).onSuccess {
                        snackMsg = "✅ Request tambah menu dikirim, menunggu persetujuan admin"
                        showAddSheet = false
                    }.onFailure {
                        snackMsg = "❌ Gagal: ${it.message}"
                    }
                }
            }
        )
    }

    // ── Edit Sheet ───────────────────────────────────────────
    if (showEditSheet && selectedMenu != null) {
        MenuFormSheet(
            title = "Edit Menu",
            initialName = selectedMenu!!.name,
            initialPrice = selectedMenu!!.price.toString(),
            initialImageUrl = selectedMenu!!.imageUrl,
            initialCategory = selectedMenu!!.category,
            onDismiss = { showEditSheet = false },
            onSubmit = { name, price, imageUrl, category ->
                scope.launch {
                    repository.submitMenuEditRequest(
                        sellerId = sellerId,
                        sellerName = sellerName,
                        canteenId = canteenId,
                        canteenName = canteenName,
                        menuId = selectedMenu!!.id,
                        name = name,
                        price = price,
                        imageUrl = imageUrl,
                        category = category
                    ).onSuccess {
                        snackMsg = "✅ Request edit menu dikirim, menunggu persetujuan admin"
                        showEditSheet = false
                    }.onFailure {
                        snackMsg = "❌ Gagal: ${it.message}"
                    }
                }
            }
        )
    }

    // ── Delete Dialog ────────────────────────────────────────
    if (showDeleteDialog && selectedMenu != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = RedError) },
            title = { Text("Hapus Menu?") },
            text = {
                Text("Request hapus \"${selectedMenu!!.name}\" akan dikirim ke admin untuk disetujui.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.submitMenuDeleteRequest(
                                sellerId = sellerId,
                                sellerName = sellerName,
                                canteenId = canteenId,
                                canteenName = canteenName,
                                menuId = selectedMenu!!.id,
                                menuName = selectedMenu!!.name
                            ).onSuccess {
                                snackMsg = "✅ Request hapus menu dikirim, menunggu persetujuan admin"
                                showDeleteDialog = false
                            }.onFailure {
                                snackMsg = "❌ Gagal: ${it.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) { Text("Kirim Request") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelola Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (canteenName.isNotEmpty()) {
                            Text(canteenName, fontSize = 12.sp, color = GrayText)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSheet = true }) {
                        Icon(Icons.Default.Add, null, tint = Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = YellowPrimary,
                contentColor = Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Menu")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GrayBg
    ) { padding ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = YellowPrimary) }
            }

            canteenId.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Store, null,
                            modifier = Modifier.size(64.dp), tint = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Kamu belum punya kantin", color = GrayText, fontSize = 16.sp)
                        Text("Hubungi admin untuk setup kantin", fontSize = 13.sp, color = GrayText)
                    }
                }
            }

            menuList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook, null,
                            modifier = Modifier.size(64.dp), tint = GrayText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Belum ada menu", color = GrayText, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAddSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YellowPrimary,
                                contentColor = Black
                            )
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Menu Pertama", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Info banner
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = YellowPrimary.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info, null,
                                    tint = YellowPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Perubahan menu memerlukan persetujuan admin",
                                    fontSize = 12.sp,
                                    color = Black
                                )
                            }
                        }
                    }

                    items(menuList, key = { it.id }) { menu ->
                        SellerMenuItemCard(
                            menu = menu,
                            onEdit = {
                                selectedMenu = menu
                                showEditSheet = true
                            },
                            onDelete = {
                                selectedMenu = menu
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerMenuItemCard(
    menu: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder image / category icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(GrayBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Fastfood, null,
                    tint = YellowPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(menu.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Rp ${String.format("%,d", menu.price).replace(',', '.')}",
                    fontSize = 13.sp,
                    color = YellowPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (menu.category.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(menu.category, fontSize = 11.sp, color = GrayText)
                }
            }

            // Edit & Delete
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = RedError, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFormSheet(
    title: String,
    initialName: String = "",
    initialPrice: String = "",
    initialImageUrl: String = "",
    initialCategory: String = "",
    onDismiss: () -> Unit,
    onSubmit: (name: String, price: Int, imageUrl: String, category: String) -> Unit
) {
    val categories = listOf("Makanan", "Minuman", "Snack", "Dessert", "Lainnya")

    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var category by remember { mutableStateOf(initialCategory.ifEmpty { categories[0] }) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Black)
            Spacer(modifier = Modifier.height(20.dp))

            // Nama menu
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Nama Menu") },
                isError = nameError,
                supportingText = { if (nameError) Text("Nama tidak boleh kosong") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowPrimary,
                    focusedLabelColor = YellowPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Harga
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() }; priceError = false },
                label = { Text("Harga (Rp)") },
                isError = priceError,
                supportingText = { if (priceError) Text("Harga tidak valid") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowPrimary,
                    focusedLabelColor = YellowPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kategori dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        focusedLabelColor = YellowPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    containerColor = White
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // URL Gambar (opsional)
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL Gambar (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowPrimary,
                    focusedLabelColor = YellowPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit
            Button(
                onClick = {
                    nameError = name.isBlank()
                    priceError = price.isBlank() || price.toIntOrNull() == null
                    if (!nameError && !priceError) {
                        onSubmit(name.trim(), price.toInt(), imageUrl.trim(), category)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowPrimary,
                    contentColor = Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Kirim Request", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Batal", color = GrayText) }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Seller Menu Screen")
@Composable
fun SellerMenuScreenPreview() {
    SellerMenuScreen(navController = rememberNavController())
}