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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.canteen.data.FirebaseRepository
import com.example.canteen.data.*
import com.example.canteen.ui.theme.GrayBg
import com.example.canteen.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

@Composable
fun AddMenuScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Tambah Menu",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 NAMA MENU
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Menu") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 HARGA
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Harga") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 KATEGORI
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Kategori") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 PILIH GAMBAR
        Button(
            onClick = {
                launcher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Pilih Gambar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 PREVIEW IMAGE
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUri
                ),
                contentDescription = "preview",
                modifier = Modifier
                    .size(120.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 SIMPAN MENU
        Button(
            onClick = {

                if (name.isBlank() || price.isBlank() || category.isBlank()) {
                    Toast.makeText(
                        context,
                        "Isi semua field",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val priceInt = price.toIntOrNull()
                if (priceInt == null) {
                    Toast.makeText(
                        context,
                        "Harga harus angka",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                isLoading = true

                scope.launch {
                    val userResult = repository.getCurrentUser()

                    userResult.onSuccess { user ->

                        val canteenResult = repository.getCanteenByOwnerId(user.id)

                        canteenResult.onSuccess { canteen ->

                            if (canteen == null) {
                                Toast.makeText(
                                    context,
                                    "Anda belum memiliki kantin. Hubungi admin.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isLoading = false
                                return@launch
                            }

                            // TODO: Upload image to Firebase Storage
                            // For now, use empty string
                            val imageUrl = ""

                            val result = repository.addMenu(
                                canteenId = canteen.id,
                                canteenName = canteen.name,
                                name = name,
                                price = priceInt,
                                imageUrl = imageUrl,
                                category = category
                            )

                            result.onSuccess {
                                Toast.makeText(
                                    context,
                                    "Menu berhasil ditambahkan 🎉",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.popBackStack()
                            }

                            result.onFailure { error ->
                                Toast.makeText(
                                    context,
                                    "Gagal: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        canteenResult.onFailure { error ->
                            Toast.makeText(
                                context,
                                "Error: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    userResult.onFailure { error ->
                        Toast.makeText(
                            context,
                            "Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    isLoading = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = androidx.compose.ui.graphics.Color.Black
                )
            } else {
                Text("Simpan")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                navController.popBackStack()
            },
            enabled = !isLoading
        ) {
            Text("Kembali")
        }
    }
}