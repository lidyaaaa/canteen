package com.example.canteen.ui.screen.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.MenuItem
import com.example.canteen.ui.component.MenuCard
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var menuList by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var canteens by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCanteen by remember { mutableStateOf("Semua") }
    var isLoading by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<MenuItem?>(null) }

    fun loadMenu() {
        scope.launch {
            isLoading = true
            val result = repository.getAllMenu()
            result.onSuccess { menus ->
                menuList = menus
                canteens = listOf("Semua") + menus.map { it.canteenName }.distinct()
            }
            result.onFailure {
                Toast.makeText(context, "Gagal load menu", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadMenu()
    }

    val filteredMenu = if (selectedCanteen == "Semua") {
        menuList
    } else {
        menuList.filter { it.canteenName == selectedCanteen }
    }

    // Dialog konfirmasi hapus
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(Icons.Default.Warning, null, tint = RedError)
            },
            title = { Text("Hapus Menu?") },
            text = { Text("Yakin ingin menghapus \"${item.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteMenu(item.id).onSuccess {
                                Toast.makeText(context, "Menu dihapus", Toast.LENGTH_SHORT).show()
                                loadMenu()
                            }.onFailure {
                                Toast.makeText(context, "Gagal hapus", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Menu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    // Filter drop-down
                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { expanded = true },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Store,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(selectedCanteen)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = YellowPrimary,
                                labelColor = Black,
                                selectedContainerColor = YellowPrimary
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            canteens.forEach { canteen ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            canteen,
                                            fontWeight = if (canteen == selectedCanteen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedCanteen = canteen
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        if (canteen == selectedCanteen) {
                                            Icon(Icons.Default.Check, null, tint = GreenSuccess)
                                        }
                                    }
                                )
                            }
                        }
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
                onClick = { navController.navigate("admin_add_menu") },
                containerColor = YellowPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Tambah Menu")
            }
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
            } else if (filteredMenu.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = GrayText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Belum ada menu 😢",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tekan tombol + untuk menambah menu",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info jumlah menu
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = YellowPrimary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = YellowPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${filteredMenu.size} menu ditampilkan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Black
                                )
                            }
                        }
                    }

                    items(filteredMenu, key = { it.id }) { item ->
                        MenuCard(
                            item = item,
                            onEdit = {
                                navController.navigate("edit_menu/${item.id}")
                            },
                            onDelete = {
                                showDeleteDialog = item
                            }
                        )
                    }
                }
            }
        }
    }
}