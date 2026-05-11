package com.example.canteen.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
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

@Composable
fun EditMenuScreen(
    navController: NavController,
    id: String
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Untuk admin
    var isAdmin by remember { mutableStateOf(false) }
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
            // Ambil data menu
            val result = repository.getMenuById(id)
            result.onSuccess { menu ->
                name = menu.name
                price = menu.price.toString()
                category = menu.category
                if (menu.imageUrl.isNotEmpty()) {
                    imageUri = Uri.parse(menu.imageUrl)
                }
                selectedCanteenId = menu.canteenId

                // Deteksi role
                val userResult = repository.getCurrentUser()
                userResult.onSuccess { user ->
                    if (user.role == "admin") {
                        isAdmin = true
                        repository.getAllCanteens().onSuccess {
                            canteens = it
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = YellowPrimary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrayBg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text("Edit Menu", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // Admin: pilih kantin
            if (isAdmin && canteens.isNotEmpty()) {
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowPrimary,
                            contentColor = Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val canteenName = canteens.find { it.id == selectedCanteenId }?.name ?: "Pilih Kantin"
                        Text(canteenName)
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
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Menu") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Harga") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ganti Gambar")
            }

            imageUri?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).background(White, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isEmpty() || price.isEmpty() || category.isEmpty()) {
                        Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val priceInt = price.toIntOrNull()
                    if (priceInt == null) {
                        Toast.makeText(context, "Harga harus angka", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    scope.launch {
                        val imageUrl = imageUri?.toString() ?: ""
                        val result = repository.updateMenu(
                            menuId = id,
                            name = name,
                            price = priceInt,
                            imageUrl = imageUrl,
                            category = category
                        )
                        result.onSuccess {
                            Toast.makeText(context, "Menu diupdate 🎉", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        result.onFailure { error ->
                            Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update")
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Kembali")
            }
        }
    }
}