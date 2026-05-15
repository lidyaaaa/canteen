package com.example.canteen.ui.screen.seller

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canteen.data.FirebaseRepository
import com.example.canteen.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SellerRequestScreen(navController: NavController) {

    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var canteenName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Ajukan Jadi Seller 🏪",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Isi form di bawah untuk membuka kantin kamu sendiri",
            fontSize = 14.sp,
            color = GrayText
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = canteenName,
            onValueChange = { canteenName = it },
            label = { Text("Nama Kantin") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi (kenapa kamu mau jadi seller?)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (canteenName.isBlank() || description.isBlank()) {
                    Toast.makeText(context, "Isi semua field", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val userResult = repository.getCurrentUser()
                    userResult.onSuccess { user ->
                        val result = repository.submitSellerRequest(
                            userId = user.id,
                            userName = user.name,
                            canteenName = canteenName,
                            description = description
                        )
                        result.onSuccess {
                            Toast.makeText(
                                context,
                                "Request berhasil dikirim! Tunggu persetujuan admin 🎉",
                                Toast.LENGTH_LONG
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
                    userResult.onFailure {
                        Toast.makeText(context, "Silakan login ulang", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowPrimary,
                contentColor = Black
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Black)
            } else {
                Text("Kirim Request", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading
        ) {
            Text("Kembali")
        }
    }
}