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
fun EditMenuScreen(
    navController: NavController,
    id: Int
) {
    val context = LocalContext.current
    val db = DataHelper(context)

    var name by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // 🔥 IMAGE
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    // 🔥 LOAD DATA
    LaunchedEffect(Unit) {
        val menu = db.getMenuById(id)

        if (menu != null) {
            name = menu.name
            seller = menu.place
            price = menu.price

            imageUri =
                if (menu.imageUri.isNotEmpty()) Uri.parse(menu.imageUri)
                else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Text("Edit Menu", fontSize = 24.sp)

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

        // 🔥 GANTI GAMBAR
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ganti Gambar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔥 UPDATE
        Button(
            onClick = {

                if (name.isEmpty() || seller.isEmpty() || price.isEmpty()) {
                    Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                db.updateMenuById(
                    id,
                    name,
                    seller,
                    price,
                    imageUri?.toString() ?: ""
                )

                Toast.makeText(context, "Menu berhasil diupdate 🎉", Toast.LENGTH_SHORT).show()

                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Update")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Kembali")
        }
    }
}