package com.example.canteen.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.canteen.data.Canteen
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddMenuScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var canteens by remember { mutableStateOf<List<Canteen>>(emptyList()) }
    var selectedCanteenId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    LaunchedEffect(Unit) {
        scope.launch {
            repository.getAllCanteens().onSuccess {
                canteens = it
                if (it.isNotEmpty()) {
                    selectedCanteenId = it[0].id
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Menu", fontWeight = FontWeight.Bold) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBg)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 📸 PREVIEW GAMBAR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GrayInput),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = GrayText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Belum ada gambar", color = GrayText, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Gambar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📋 FORM MENU
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "Detail Menu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pilih Kantin
                    Text(
                        "Kantin",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val canteenName = canteens.find { it.id == selectedCanteenId }?.name ?: "Pilih Kantin"
                            Text(canteenName, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            canteens.forEach { canteen ->
                                DropdownMenuItem(
                                    text = { Text(canteen.name) },
                                    onClick = {
                                        selectedCanteenId = canteen.id
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Store, null)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nama Menu
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Menu") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Fastfood, null)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Harga & Kategori sejajar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Harga") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Text("Rp", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Kategori") },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Category, null, modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TOMBOL SIMPAN
            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank() || category.isBlank() || selectedCanteenId.isBlank()) {
                        Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val priceInt = price.toIntOrNull()
                    if (priceInt == null) {
                        Toast.makeText(context, "Harga harus angka", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        val canteen = canteens.find { it.id == selectedCanteenId }
                        val imageUrl = imageUri.toString()
                        val result = repository.addMenu(
                            canteenId = selectedCanteenId,
                            canteenName = canteen?.name ?: "",
                            name = name,
                            price = priceInt,
                            imageUrl = imageUrl,
                            category = category
                        )
                        result.onSuccess {
                            Toast.makeText(context, "Menu berhasil ditambahkan 🎉", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        result.onFailure { err ->
                            Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowPrimary,
                    contentColor = Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}