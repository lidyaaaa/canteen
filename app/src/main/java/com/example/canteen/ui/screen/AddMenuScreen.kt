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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.ui.layout.ContentScale

import coil.compose.rememberAsyncImagePainter

import com.example.canteen.data.DataHelper
import com.example.canteen.ui.theme.GrayBg
import com.example.canteen.ui.theme.YellowPrimary

@Composable
fun AddMenuScreen(navController: NavController) {

    val context = LocalContext.current
    val db = remember { DataHelper(context) } // 🔥 FIX (biar ga recreate terus)

    var name by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

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

        Text("Tambah Menu", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Menu") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = seller,
            onValueChange = { seller = it },
            label = { Text("Nama Penjual") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Harga") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 PILIH GAMBAR
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pilih Gambar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 PREVIEW (AMAN)
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = "preview",
                modifier = Modifier
                    .size(120.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 SIMPAN
        Button(
            onClick = {

                if (name.isBlank() || seller.isBlank() || price.isBlank()) {
                    Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                db.insertMenu(
                    name,
                    seller,
                    price,
                    imageUri?.toString() ?: ""
                )

                Toast.makeText(context, "Menu berhasil ditambahkan 🎉", Toast.LENGTH_SHORT).show()

                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simpan")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Kembali")
        }
    }
}